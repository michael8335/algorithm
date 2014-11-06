package com.fly.practice.nio.nioserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An abstract blocking client, designed to connect to implementations of AbstractServer in its own thread.
 * Since the client only has a single connection to a single server it can use blocking IO. This class provides
 * a set of callback methods for concrete implementations to know the state of the client and its connection 
 * and receive messages from the server. This client will automatically handle partially received messages or 
 * multiple message at once. A maximum message size is imposed by the server, as handled by the MessageLength 
 * parameter, which defaults to the TwoByteMessageLength (and thus a max message of 65535 bytes).
 * 
 * This client does not log, implementations should handle this.
 * 
 * This client does not support SSL or UDP connections.
 * 
 * @see AbstractServer
 * @see MessageLength
 */
public abstract class AbstractBlockingClient implements Runnable {
    
    private enum State {STOPPED, STOPPING, RUNNING}
    
    private static short DEFAULT_MESSAGE_SIZE = 512;
    
    private final AtomicReference<State> state = new AtomicReference<State>(State.STOPPED);
    private final InetAddress server;
    private final int port;
    private final int byteLength;
    private final MessageLength messageLength;
    private final int defaultBufferSize;
    private final AtomicReference<OutputStream> out = new AtomicReference<OutputStream>();
    private final AtomicReference<InputStream> in = new AtomicReference<InputStream>();
    
    /**
     * Construct an unstarted client which will attempt to connect to the given server on the given port. 
     * @param server the server address. 
     * @param port the port on which to connect to the server. 
     */
    public AbstractBlockingClient(InetAddress server, int port) {
        this(server, port, new TwoByteMessageLength(), DEFAULT_MESSAGE_SIZE);
    }
    
    /**
     * Construct an unstarted client which will attempt to connect to the given server on the given port. 
     * @param server the server address. 
     * @param port the port on which to connect to the server. 
     * @param messageLength how to construct and parse message lengths.
     * @param defaultBufferSize the default buffer size for reads. This should as small as 
     * possible value that doesn't get exceeded often - see class documentation.
     */
    public AbstractBlockingClient(InetAddress server, int port, MessageLength messageLength, int defaultBufferSize) {
        this.server = server;
        this.port = port;
        this.messageLength = messageLength;
        this.defaultBufferSize = defaultBufferSize;
        this.byteLength = messageLength.byteLength();
    }
    
    /**
     * Returns the port to which this client will connect.
     * @return the port to which this client will connect.
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Returns the host to which this client will connect.
     * @return the host to which this client will connect.
     */
    public InetAddress getServer() {
        return server;
    }
    
    /**
     * Returns true if this client is the running state (either connected or trying to connect).
     * @return true if this client is the running state (either connected or trying to connect).
     */
    public boolean isRunning() {
        return state.get() == State.RUNNING;
    }
    
    /**
     * Returns true if this client is the stopped state.
     * @return true if this client is the stopped state.
     */
    public boolean isStopped() {
        return state.get() == State.STOPPED;
    }

    /**
     * Attempt to connect to the server and receive messages. If the client is
     * already running, it will not be started again. This method is designed to be called in
     * its own thread and will not return until the client is stopped.
     * 
     * @throws RuntimeException if the client fails
     */
    public void run() {
        if (!state.compareAndSet(State.STOPPED, State.RUNNING)) {
            connected(true);
            return;
        }
        
        Socket socket = null;
        try {
            socket = new Socket(server, port); 
            socket.setKeepAlive(true); 
            out.set(socket.getOutputStream());
            in.set(socket.getInputStream()); 
            int limit = 0;
            byte[] inBuffer = new byte[defaultBufferSize];  
            connected(false);
            while (state.get() == State.RUNNING) { 
                limit += in.get().read(inBuffer, limit, inBuffer.length-limit);
                if (limit>=byteLength) {
                    int messageLen;
                    do {
                        byte[] lengthBytes = new byte[byteLength];
                        System.arraycopy(inBuffer, 0, lengthBytes, 0, byteLength);
                        messageLen = (int)messageLength.bytesToLength(lengthBytes);
                        if (limit>=messageLen) {
                            // enough data to extract the message
                            byte[] message = new byte[messageLen];
                            System.arraycopy(inBuffer, byteLength, message, 0, messageLen);
                            messageReceived(ByteBuffer.wrap(message));                      
                            // compact inBuffer
                            byte[] temp = new byte[inBuffer.length];
                            System.arraycopy(inBuffer, 0, temp, messageLen+byteLength, limit-messageLen-byteLength);
                            inBuffer = temp;
                            limit = limit-messageLen-byteLength;
                        } else if (messageLen>inBuffer.length) {
                            byte[] temp = new byte[messageLen+byteLength];
                            System.arraycopy(inBuffer, 0, temp, 0, inBuffer.length);
                            inBuffer = temp;
                        } 
                    } while (messageLen<limit);
                }
            }
        } catch (ClosedByInterruptException ie) {
            // do nothing
        } catch (ConnectException ce) {
            throw new RuntimeException(ce.getMessage());
        } catch (SocketException se) { 
            // do nothing
        } catch (IOException ioe) { 
            throw new RuntimeException("Client failure: "+ioe.getMessage());
        } finally {
            try {
                socket.close();
                state.set(State.STOPPED);
                disconnected();
            } catch (Exception e) {
                // do nothing - server failed
            }
        }
    }
    
    /**
     * Stop the client in a graceful manner. After this call the client may spend some time
     * in the process of stopping. A disconnected callback will occur when the client actually stops. 
     * @return if the client was successfully set to stop.
     */
    public boolean stop() {
        if (state.compareAndSet(State.RUNNING, State.STOPPING)) {
            try {in.get().close();} catch (IOException e) {return false;};
            return true;
        }
        return false;
    }
    
    /**
     * Send the given message to the server.
     * @param buffer the message to send.
     * @return true if the message was sent to the server.
     */
    public synchronized boolean write(byte[] buffer) {
        int len = buffer.length;
        byte[] lengthBytes = messageLength.lengthToBytes(len);
        try {           
            byte[] outBuffer = new byte[len+byteLength];        
            System.arraycopy(lengthBytes, 0, outBuffer, 0, byteLength);
            System.arraycopy(buffer, 0, outBuffer, byteLength, len);
            out.get().write(outBuffer);
            return true;
        } catch (Exception e) {
            // socket is closed, message not sent
            stop();
            return false;
        }
    }
    
    /**
     * Callback method for when the client receives a message from the server. 
     * @param message the message from the server.
     */
    protected abstract void messageReceived(ByteBuffer message);
    
    /**
     * Callback method for when the client connects to the server.
     * @param alreadyConnected whether the client was already connected to the server.
     */
    protected abstract void connected(boolean alreadyConnected);
    
    /**
     * Callback method for when the client disconnects from the server.
     */
    protected abstract void disconnected();
}

