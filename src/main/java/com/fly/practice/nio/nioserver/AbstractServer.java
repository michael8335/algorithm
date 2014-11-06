package com.fly.practice.nio.nioserver;


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An abstract non-blocking server, designed to be run in its own thread. This class provides a set of callback 
 * methods for concrete implementations to know the state of the server and its connections and receive messages 
 * from clients. This server will automatically handle partially received messages or multiple message at once. 
 * A maximum message size is imposed by the server, as handled by the MessageLength parameter, which defaults to
 * the TwoByteMessageLength (and thus a max message of 65535 bytes).
 * 
 * Designed to work with concrete implementations of AbstractBlockingClient.
 * 
 * The default buffer size should be set such that it is as small as possible, but at the same time not so small that message
 * are regularly larger than the buffer. The smaller the buffer, the faster it can be processed. However, if a message is 
 * received larger than the buffer then the buffer must be resized to handle it. 
 * 
 * This server does not log, implementations should handle this.
 * 
 * This server does not support SSL or UDP connections.
 * 
 * @see AbstractBlockingClient
 * @see MessageLength
 */
public abstract class AbstractServer implements Runnable {

    private enum State {STOPPED, STOPPING, RUNNING}
    
    private static short DEFAULT_MESSAGE_SIZE = 512;
    
    private final AtomicReference<State> state = new AtomicReference<State>(State.STOPPED);
    private final int port;
    private final MessageLength messageLength;
    private final Map<SelectionKey, ByteBuffer> readBuffers = new HashMap<SelectionKey, ByteBuffer>(); 
    private final int defaultBufferSize;
    
    /**
     * Construct an unstarted server which will listen for connections on the given port. 
     * Will use the default message length and buffer.
     * @param port the port to start the server on. 
     */
    protected AbstractServer(int port) { 
        this(port, new TwoByteMessageLength(), DEFAULT_MESSAGE_SIZE);
    }
    
    /**
     * Construct an unstarted server which will listen for connections on the given port. 
     * @param port the port to start the server on. 
     * @param messageLength how to construct and parse message lengths.
     * @param defaultBufferSize the default buffer size for reads. This should as small as 
     * possible value that doesn't get exceeded often - see class documentation.
     */
    protected AbstractServer(int port, MessageLength messageLength, int defaultBufferSize) { 
        this.port = port; 
        this.messageLength = messageLength;
        this.defaultBufferSize = defaultBufferSize;
    }
    
    /**
     * Returns the port on which this server is accepting connections.
     * @return the port on which this server is accepting connections.
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Returns the host on which this server is accepting connections (usually localhost).
     * @return the host on which this server is accepting connections.
     */
    public InetAddress getServer() {
        try { 
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            return null;
        }
    }
    
    /**
     * Returns true if this server is currently accepting connections, reads & writes.
     * @return true if this server is currently accepting connections, reads & writes.
     */
    public boolean isRunning() {
        return state.get() == State.RUNNING;
    }
    
    /**
     * Returns true if this server is not currently accepting connections, reads & writes.
     * @return true if this server is not currently accepting connections, reads & writes.
     */
    public boolean isStopped() {
        return state.get() == State.STOPPED;
    }

    /**
     * Start the server running - accepting connections, receiving messages. If the server is
     * already running, it will not be started again. This method is designed to be called in
     * its own thread and will not return until the server is stopped.
     * 
     * @throws RuntimeException if the server fails
     */
    public void run() { 
        // ensure that the server is not started twice
        if (!state.compareAndSet(State.STOPPED, State.RUNNING)) {
            started(true);
            return;
        }

        Selector selector = null;
        ServerSocketChannel server = null;
        try { 
            selector = Selector.open(); 
            server = ServerSocketChannel.open(); 
            server.socket().bind(new InetSocketAddress(port)); 
            server.configureBlocking(false); 
            server.register(selector, SelectionKey.OP_ACCEPT); 
            started(false);
            while (state.get() == State.RUNNING) { 
                selector.select(100); // check every 100ms whether the server has been requested to stop
                for (Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext();) { 
                    SelectionKey key = i.next(); 
                    try { 
                        i.remove(); 
                        if (key.isConnectable()) { 
                            ((SocketChannel)key.channel()).finishConnect(); 
                        } 
                        if (key.isAcceptable()) { 
                            // accept connection 
                            SocketChannel client = server.accept(); 
                            client.configureBlocking(false); 
                            client.socket().setTcpNoDelay(true); 
                            connection(client.register(selector, SelectionKey.OP_READ));
                        } 
                        if (key.isReadable()) { 
                            for (ByteBuffer message: readIncomingMessage(key)) { 
                                messageReceived(message, key); 
                            } 
                        } 
                    } catch (IOException ioe) { 
                        resetKey(key);
                        disconnected(key); 
                    } 
                } 
            }           
        } catch (Throwable e) { 
            throw new RuntimeException("Server failure: "+e.getMessage());
        } finally {
            try {
                selector.close();
                server.socket().close();
                server.close();
                state.set(State.STOPPED);
                stopped();
            } catch (Exception e) {
                // do nothing - server failed
            }
        }
    } 
    
    /**
     * Stop the current server in a graceful manner. After this call the server may spend some time
     * in the process of stopping. A stopped callback will occur when the server actually stops. 
     * @return if the server was successfully set to stop.
     */
    public boolean stop() {
        return state.compareAndSet(State.RUNNING, State.STOPPING);
    }
    
    /**
     * Remove any internal state for the given key.
     * @param key the key to be cancelled.
     */
    protected void resetKey(SelectionKey key) { 
        key.cancel(); 
        readBuffers.remove(key); 
    }
   
    /**
     * Send the given message to the given client. The message does not need to have the length
     * of the message prepended. It is not guaranteed the message will arrive, as it can't be determined
     * if writing on a closed connection (it could appear to work). This won't be known until later
     * when get a disconnected callback is made.  
     * 
     * @param channelKey the key of the client to which the message should be sent.
     * @param buffer the message to send.
     */
    public void write(SelectionKey channelKey, byte[] buffer)  {
        short len = (short)buffer.length;
        byte[] lengthBytes = messageLength.lengthToBytes(len);
        // copying into byte buffer is actually faster than writing to channel twice over many (>10000) runs
        ByteBuffer writeBuffer = ByteBuffer.allocate(len+lengthBytes.length);
        writeBuffer.put(lengthBytes);
        writeBuffer.put(buffer);
        writeBuffer.flip();
        if (buffer!=null && state.get()==State.RUNNING) {
            int bytesWritten;
            try {
                 // only 1 thread can write to a channel at a time 
                SocketChannel channel = (SocketChannel)channelKey.channel(); 
                synchronized (channel) { 
                    bytesWritten = channel.write(writeBuffer); 
                } 
                if (bytesWritten==-1) {
                    resetKey(channelKey);
                    disconnected(channelKey); 
                }
            } catch (Exception e) {
                resetKey(channelKey);
                disconnected(channelKey); 
            }
        }
    }

    /**
     * Read as many messages as available on the client connection.
     * @param key the client connection to read.
     * @return the messages read.
     * @throws IOException if the client connection is closed.
     */
    private List<ByteBuffer> readIncomingMessage(SelectionKey key) throws IOException { 
        ByteBuffer readBuffer = readBuffers.get(key); 
        if (readBuffer==null) {
            readBuffer = ByteBuffer.allocate(defaultBufferSize); 
            readBuffers.put(key, readBuffer); 
        }
        if (((ReadableByteChannel)key.channel()).read(readBuffer)==-1) {
            throw new IOException("Read on closed key");
        }
        
        readBuffer.flip(); 
        List<ByteBuffer> result = new ArrayList<ByteBuffer>();
            
        ByteBuffer msg = readMessage(key, readBuffer);
        while (msg!=null) {
            result.add(msg);
            msg = readMessage(key, readBuffer);
        }
        
        return result;
    }

    /**
     * Read a single message on the client connection and update the read buffer with
     * the current state of any communication with the client.
     * @param key the client connection to read.
     * @param readBuffer the buffer of data received from the client.
     * @return the message read from the client or null if there are no complete messages to read.
     */
    private ByteBuffer readMessage(SelectionKey key, ByteBuffer readBuffer) {
        int bytesToRead; 
        if (readBuffer.remaining()>messageLength.byteLength()) { // must have at least enough bytes to read the size of the message         
            byte[] lengthBytes = new byte[messageLength.byteLength()];
            readBuffer.get(lengthBytes);
            bytesToRead = (int)messageLength.bytesToLength(lengthBytes);
            if ((readBuffer.limit()-readBuffer.position())<bytesToRead) { 
                // Not enough data - prepare for writing again 
                if (readBuffer.limit()==readBuffer.capacity()) {
                    // message may be longer than buffer => resize buffer to message size
                    int oldCapacity = readBuffer.capacity();
                    ByteBuffer tmp = ByteBuffer.allocate(bytesToRead+messageLength.byteLength());
                    readBuffer.position(0);
                    tmp.put(readBuffer);
                    readBuffer = tmp;                   
                    readBuffer.position(oldCapacity); 
                    readBuffer.limit(readBuffer.capacity()); 
                    readBuffers.put(key, readBuffer); 
                    return null;
                } else {
                    // rest for writing
                    readBuffer.position(readBuffer.limit()); 
                    readBuffer.limit(readBuffer.capacity()); 
                    return null; 
                }
            } 
        } else { 
            // Not enough data - prepare for writing again 
            readBuffer.position(readBuffer.limit()); 
            readBuffer.limit(readBuffer.capacity()); 
            return null; 
        } 
        byte[] resultMessage = new byte[bytesToRead];
        readBuffer.get(resultMessage, 0, bytesToRead); 
        // remove read message from buffer
        int remaining = readBuffer.remaining();
        readBuffer.limit(readBuffer.capacity());
        readBuffer.compact();
        readBuffer.position(0);
        readBuffer.limit(remaining);
        return ByteBuffer.wrap(resultMessage);
    } 
    
    /**
     * Callback method for when the server receives a message from a connected client. The
     * message passed is a copy, so can be modified at will.
     * 
     * @param message the message received.
     * @param key the key for the client that send the message.
     */
    protected abstract void messageReceived(ByteBuffer message, SelectionKey key);
       
    /**
     * Callback method for when the server accepts a new client connection. Note that the
     * key provided is the key for reading/writing, not for the connection itself.
     * 
     * @param key the key for the connected client.
     */
    protected abstract void connection(SelectionKey key);

    /**
     * Callback method for when the server disconnects an client connection.
     * @param key the key for the disconnected client.
     */
    protected abstract void disconnected(SelectionKey key); 
    
    /**
     * Callback method for when the server has been started. If there are multiple attempts
     * to start the server then there will be multiple callbacks, but the server can not be 
     * started when it is already running. In this situation alreadyStarted will be true (false otherwise).
     * @param alreadyStarted whether it was attempted to start this server more than once.
     */
    protected abstract void started(boolean alreadyStarted);
    
    /**
     * Callback method for when the server has been stopped.
     */
    protected abstract void stopped();
}

