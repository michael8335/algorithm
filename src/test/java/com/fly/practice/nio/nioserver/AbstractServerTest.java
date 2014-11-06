package com.fly.practice.nio.nioserver;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class AbstractServerTest
{

    private static final int PORT = 8000;

    private static AbstractServer server;

    private static CountDownLatch messageLatch = new CountDownLatch(1);

    private static CountDownLatch writeLatch = new CountDownLatch(1);

    private static CountDownLatch disconnected = new CountDownLatch(1);;

    private static List<String> messageHolder = new ArrayList<String>();

    private static AtomicReference<SelectionKey> channelKey = new AtomicReference<SelectionKey>();

    private static CountDownLatch serverStarted = new CountDownLatch(1);

    private static CountDownLatch serverStopped = new CountDownLatch(1);

    private static AtomicReference<SelectionKey> connectKey = new AtomicReference<SelectionKey>();

    @BeforeClass
    public static void setup()
        throws Exception
    {
        server = new AbstractServer(PORT, new TwoByteMessageLength(), 512)
        {
            @Override
            protected void disconnected(SelectionKey key)
            {
                disconnected.countDown();
            }

            @Override
            protected void messageReceived(ByteBuffer message, SelectionKey key)
            {
                messageHolder.add(new String(message.array()));
                channelKey.set(key);
                messageLatch.countDown();
                write(key, message.array());
                writeLatch.countDown();
            }

            @Override
            protected void started(boolean alreadyStarted)
            {
                serverStarted.countDown();
            }

            @Override
            protected void stopped()
            {
                serverStopped.countDown();
            }

            @Override
            protected void connection(SelectionKey key)
            {
                connectKey.set(key);
            }
        };
        new Thread(server).start();
        assertTrue(serverStarted.await(1000, TimeUnit.MILLISECONDS));
    }

    @AfterClass
    public static void tearDown()
        throws Exception
    {
        server.stop();
        assertTrue(serverStopped.await(1000, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testStop()
        throws Exception
    {
        final CountDownLatch localServerStarted = new CountDownLatch(1);
        final CountDownLatch localServerStopped = new CountDownLatch(1);
        AbstractServer localServer = new AbstractServer(PORT + 1)
        {
            @Override
            protected void disconnected(SelectionKey key)
            {}

            @Override
            protected void messageReceived(ByteBuffer message, SelectionKey key)
            {}

            @Override
            protected void started(boolean alreadyStarted)
            {
                localServerStarted.countDown();
            }

            @Override
            protected void stopped()
            {
                localServerStopped.countDown();
            }

            @Override
            protected void connection(SelectionKey key)
            {}
        };
        assertTrue(localServer.isStopped());
        new Thread(localServer).start();
        assertTrue(localServerStarted.await(1000, TimeUnit.MILLISECONDS));
        assertTrue(localServer.stop());
        assertTrue(localServerStopped.await(1000, TimeUnit.MILLISECONDS));
        assertTrue(localServer.isStopped());
    }

    @Test
    public void testStartTwice()
        throws Exception
    {
        final CountDownLatch localServerStarted = new CountDownLatch(1);
        final CountDownLatch localServerAlreadyStarted = new CountDownLatch(1);
        final CountDownLatch localServerStopped = new CountDownLatch(1);
        AbstractServer localServer = new AbstractServer(PORT + 1)
        {
            @Override
            protected void disconnected(SelectionKey key)
            {}

            @Override
            protected void messageReceived(ByteBuffer message, SelectionKey key)
            {}

            @Override
            protected void started(boolean alreadyStarted)
            {
                if (alreadyStarted)
                {
                    localServerAlreadyStarted.countDown();
                }
                else
                {
                    localServerStarted.countDown();
                }
            }

            @Override
            protected void stopped()
            {
                localServerStopped.countDown();
            }

            @Override
            protected void connection(SelectionKey key)
            {}
        };
        new Thread(localServer).start();
        assertTrue(localServerStarted.await(1000, TimeUnit.MILLISECONDS));
        new Thread(localServer).start();
        assertTrue(localServerAlreadyStarted.await(2000, TimeUnit.MILLISECONDS));
        localServer.stop();
        assertTrue(localServerStopped.await(1000, TimeUnit.MILLISECONDS));
        assertTrue(localServer.isStopped());
    }

    @Test
    public void testStopTwice()
        throws Exception
    {
        final CountDownLatch localServerStarted = new CountDownLatch(1);
        final CountDownLatch localServerStopped = new CountDownLatch(1);
        AbstractServer localServer = new AbstractServer(PORT + 1)
        {
            @Override
            protected void disconnected(SelectionKey key)
            {}

            @Override
            protected void messageReceived(ByteBuffer message, SelectionKey key)
            {}

            @Override
            protected void started(boolean alreadyStarted)
            {
                localServerStarted.countDown();
            }

            @Override
            protected void stopped()
            {
                localServerStopped.countDown();
            }

            @Override
            protected void connection(SelectionKey key)
            {}
        };
        new Thread(localServer).start();
        assertTrue(localServerStarted.await(1000, TimeUnit.MILLISECONDS));
        assertTrue(localServer.stop());
        assertFalse(localServer.stop());
        assertTrue(localServerStopped.await(1000, TimeUnit.MILLISECONDS));
        assertTrue(localServer.isStopped());
    }

    @Test
    public void testConnection()
        throws Exception
    {
        Socket sc = new Socket("localhost", PORT);
        assertTrue(sc.isConnected());
        sc.close();
    }

    @Test
    public void testSendMessage()
        throws Exception
    {
        messageLatch = new CountDownLatch(1);
        messageHolder.clear();
        Socket sc = new Socket("localhost", PORT);
        OutputStream os = sc.getOutputStream();
        os.write(0);
        os.write("Hello!".length());
        os.write("Hello!".getBytes());
        os.flush();
        assertTrue(messageLatch.await(20, TimeUnit.MILLISECONDS));
        assertEquals(messageHolder.get(0), "Hello!");
        assertTrue(server.isRunning());
        sc.close();
    }

    @Test
    public void testConnectionKey()
        throws Exception
    {
        messageLatch = new CountDownLatch(1);
        messageHolder.clear();
        Socket sc = new Socket("localhost", PORT);
        OutputStream os = sc.getOutputStream();
        os.write(0);
        os.write("Hello!".length());
        os.write("Hello!".getBytes());
        os.flush();
        assertTrue(messageLatch.await(20, TimeUnit.MILLISECONDS));
        assertEquals(connectKey.get(), channelKey.get());
        assertTrue(server.isRunning());
        sc.close();
    }

    @Test
    public void testSendSplitMessage()
        throws Exception
    {
        messageLatch = new CountDownLatch(1);
        messageHolder.clear();
        Socket sc = new Socket("localhost", PORT);
        OutputStream os = sc.getOutputStream();
        os.write(0);
        os.write("Hello!".length());
        os.write("Hel".getBytes());
        os.flush();
        os.write("lo!".getBytes());
        os.flush();
        assertTrue(messageLatch.await(20, TimeUnit.MILLISECONDS));
        assertEquals(messageHolder.get(0), "Hello!");
        assertTrue(server.isRunning());
        sc.close();
    }

    @Test
    public void testSendShortMessage()
        throws Exception
    {
        messageLatch = new CountDownLatch(1);
        messageHolder.clear();
        Socket sc = new Socket("localhost", PORT);
        OutputStream os = sc.getOutputStream();
        os.write(0);
        os.write("Hello!".length());
        os.write("Hel".getBytes());
        os.flush();
        assertFalse(messageLatch.await(20, TimeUnit.MILLISECONDS));
        assertEquals(messageHolder.size(), 0);
        assertTrue(server.isRunning());

        os.write("lo!c".getBytes());
        os.flush();
        assertTrue(messageLatch.await(20, TimeUnit.MILLISECONDS));
        assertEquals(messageHolder.get(0), "Hello!");
        assertTrue(server.isRunning());
        sc.close();
    }

    @Test
    public void testSendOverLengthMessage()
        throws Exception
    {
        messageLatch = new CountDownLatch(2);
        messageHolder.clear();
        Socket sc = new Socket("localhost", PORT);
        OutputStream os = sc.getOutputStream();
        os.write(0);
        os.write("Hello!".length());
        os.write("Hello!".getBytes());
        os.write(0);
        os.write("Again".length());
        os.write("Again".getBytes());
        os.flush();
        assertTrue(messageLatch.await(20, TimeUnit.MILLISECONDS));
        assertEquals(messageHolder.size(), 2);
        assertEquals(messageHolder.get(0), "Hello!");
        assertEquals(messageHolder.get(1), "Again");
        assertTrue(server.isRunning());
    }

    @Test
    public void testSendMultipleMessage()
        throws Exception
    {
        messageLatch = new CountDownLatch(2);
        messageHolder.clear();
        Socket sc = new Socket("localhost", PORT);
        OutputStream os = sc.getOutputStream();
        os.write(0);
        os.write("Hello!".length());
        os.write("Hello!".getBytes());
        os.flush();
        Thread.sleep(20);
        os.write(0);
        os.write("Again".length());
        os.write("Again".getBytes());
        os.flush();
        assertTrue(messageLatch.await(20, TimeUnit.MILLISECONDS));
        assertEquals(messageHolder.size(), 2);
        assertEquals(messageHolder.get(0), "Hello!");
        assertEquals(messageHolder.get(1), "Again");
        assertTrue(server.isRunning());
        sc.close();
    }

    @Test
    public void testSendOverDefaultLengthMessage()
        throws Exception
    {
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 0; i < 1000; i++ )
        {
            messageBuilder.append("x");
        }
        String message = messageBuilder.toString();
        messageLatch = new CountDownLatch(1);
        messageHolder.clear();
        Socket sc = new Socket("localhost", PORT);
        OutputStream os = sc.getOutputStream();
        os.write(toByte((short)message.length()));
        os.write(message.getBytes());
        os.flush();
        assertTrue(messageLatch.await(20, TimeUnit.MINUTES));
        assertEquals(messageHolder.get(0), message);
        assertTrue(server.isRunning());
        sc.close();
    }

    @Test
    public void testWriteOverDefaultLengthMessage()
        throws Exception
    {
        final StringBuilder messageBuilder = new StringBuilder();
        for (int i = 0; i < 1000; i++ )
        {
            messageBuilder.append("x");
        }
        messageBuilder.append("\n");
        writeLatch = new CountDownLatch(1);
        Socket sc = new Socket("localhost", PORT);
        OutputStream os = sc.getOutputStream();
        final InputStream is = sc.getInputStream();
        os.write(toByte((short)1000));
        os.write(messageBuilder.toString().getBytes());
        os.flush();
        assertTrue(writeLatch.await(20, TimeUnit.MILLISECONDS));
        long length = new TwoByteMessageLength().bytesToLength(new byte[] {(byte)is.read(),
            (byte)is.read()});
        assertEquals(length, 1000);
        for (int i = 0; i < 1000; i++ )
        {
            assertEquals(is.read(), (byte)120);
        }
        is.close();
        sc.close();
    }

    @Test
    public void testWrite()
        throws Exception
    {
        writeLatch = new CountDownLatch(1);
        Socket sc = new Socket("localhost", PORT);
        OutputStream os = sc.getOutputStream();
        final InputStream is = sc.getInputStream();
        os.write(0);
        os.write("Hello!\n".length());
        os.write("Hello!\n".getBytes());
        os.flush();
        assertTrue(writeLatch.await(2000000, TimeUnit.MILLISECONDS));
        long length = new TwoByteMessageLength().bytesToLength(new byte[] {(byte)is.read(),
            (byte)is.read()});
        assertEquals(length, 7);
        byte[] compare = new byte[] {(byte)72, (byte)101, (byte)108, (byte)108, (byte)111,
            (byte)33, (byte)10};
        for (int i = 0; i < length; i++ )
        {
            assertEquals(is.read(), compare[i]);
        }
        is.close();
        sc.close();
    }

    @Test(expected = IllegalStateException.class)
    public void testWriteOverMaxLengthMessage()
        throws Exception
    {
        messageLatch = new CountDownLatch(1);
        Socket sc = new Socket("localhost", PORT);
        OutputStream os = sc.getOutputStream();
        os.write(0);
        os.write("Hello!".length());
        os.write("Hello!".getBytes());
        os.flush();
        assertTrue(messageLatch.await(20, TimeUnit.MILLISECONDS));
        byte[] message = new byte[64000];
        for (int i = 0; i < 64000; i++ )
        {
            message[i] = 111;
        }
        server.write(channelKey.get(), message);
    }

    @Test
    public void testSendTwoMessages()
        throws Exception
    {
        messageLatch = new CountDownLatch(1);
        messageHolder.clear();
        Socket sc = new Socket("localhost", PORT);
        OutputStream os = sc.getOutputStream();
        os.write(0);
        os.write("Hello!".length());
        os.write("Hello!".getBytes());
        os.flush();
        assertTrue(messageLatch.await(20, TimeUnit.MILLISECONDS));
        assertEquals(messageHolder.size(), 1);
        assertEquals(messageHolder.get(0), "Hello!");
        messageLatch = new CountDownLatch(1);
        os.write(0);
        os.write("Again".length());
        os.write("Again".getBytes());
        os.flush();
        assertTrue(messageLatch.await(20, TimeUnit.MILLISECONDS));
        assertEquals(messageHolder.size(), 2);
        assertEquals(messageHolder.get(0), "Hello!");
        assertEquals(messageHolder.get(1), "Again");
        assertTrue(server.isRunning());
        sc.close();
    }

    @Test
    public void testWriteDisconnection()
        throws Exception
    {
        messageLatch = new CountDownLatch(1);
        disconnected = new CountDownLatch(1);
        messageHolder.clear();
        Socket sc = new Socket("localhost", PORT);
        OutputStream os = sc.getOutputStream();
        os.write(0);
        os.write("Hello!".length());
        os.write("Hello!".getBytes());
        os.flush();
        sc.close();
        messageLatch.countDown();
        assertTrue(disconnected.await(20, TimeUnit.MILLISECONDS));
    }

    public static byte[] toByte(short data)
    {
        return new byte[] {(byte)((data >>> 8) & 0xff), (byte)(data & 0xff)};
    }
}
