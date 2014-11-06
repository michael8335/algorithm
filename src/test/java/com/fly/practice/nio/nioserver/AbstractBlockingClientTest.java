package com.fly.practice.nio.nioserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class AbstractBlockingClientTest {
	
	private static final int PORT = 8000;
	private static final InetAddress SERVER;
	static {
		InetAddress temp;
		try {temp=InetAddress.getByName("localhost");} catch (Exception e) {temp=null;}
		SERVER=temp;
	}
	private AbstractBlockingClient client;
	private static Thread serverThread;
	private static ReflectServer server;
	
	@BeforeClass 
	public static void setup() {
		server = new ReflectServer();
		serverThread = new Thread(server);
		serverThread.start();
	}
	
	@AfterClass 
	public static void tearDown() {
		serverThread.interrupt();
	}

	@Test(expected=RuntimeException.class) 
	public void testConnectNonExistant() throws Exception {
		new AbstractBlockingClient(SERVER, PORT+1) {
			@Override protected void messageReceived(ByteBuffer message) {}
			@Override protected void connected(boolean alreadyConnected) { }
			@Override protected void disconnected() { }
		}.run();
	}
	
	@Test 
	public void testStop() throws Exception {
		final CountDownLatch disconnected = new CountDownLatch(1);
		final CountDownLatch connected = new CountDownLatch(1);
		client = new AbstractBlockingClient(SERVER, PORT) {
			@Override protected void messageReceived(ByteBuffer message) {}
			@Override protected void connected(boolean alreadyConnected) {connected.countDown();}
			@Override protected void disconnected() {disconnected.countDown();}
		};
		assertTrue(client.isStopped());
		new Thread(client).start();	
		assertTrue(connected.await(1000, TimeUnit.MILLISECONDS));
		assertTrue(client.stop());		
		assertTrue(disconnected.await(1000, TimeUnit.MILLISECONDS));
	}
	
	@Test public void testStopTwice() throws Exception {
		final CountDownLatch disconnected = new CountDownLatch(1);
		final CountDownLatch connected = new CountDownLatch(1);
		client = new AbstractBlockingClient(SERVER, PORT) {
			@Override protected void messageReceived(ByteBuffer message) {}
			@Override protected void connected(boolean alreadyConnected) {connected.countDown();}
			@Override protected void disconnected() {disconnected.countDown();}
		};
		new Thread(client).start();	 
		assertTrue(connected.await(1000, TimeUnit.MILLISECONDS));
		assertTrue(client.stop());
		assertFalse(client.stop());
		assertTrue(disconnected.await(1000, TimeUnit.MILLISECONDS));
	}
	
	@Test public void testStartTwice() throws Exception {
		final CountDownLatch disconnected = new CountDownLatch(1);
		final CountDownLatch connected = new CountDownLatch(1);
		final CountDownLatch alreadyConnectedLatch = new CountDownLatch(1);
		client = new AbstractBlockingClient(SERVER, PORT) {
			@Override protected void messageReceived(ByteBuffer message) {}
			@Override protected void connected(boolean alreadyConnected) {
				if (alreadyConnected) {
					alreadyConnectedLatch.countDown();
				} else {
					connected.countDown();
				}
			}
			@Override protected void disconnected() {disconnected.countDown();}
		};
		new Thread(client).start();	 
		assertTrue(connected.await(1000, TimeUnit.MILLISECONDS));
		new Thread(client).start();	
		assertTrue(alreadyConnectedLatch.await(1000, TimeUnit.MILLISECONDS));
		client.stop();
		assertTrue(disconnected.await(1000, TimeUnit.MILLISECONDS));
	}
	
	@Test public void testConnection() throws Exception {
		final CountDownLatch disconnected = new CountDownLatch(1);
		final CountDownLatch connected = new CountDownLatch(1);
		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicReference<String> messageHolder = new AtomicReference<String>();
		client = new AbstractBlockingClient(SERVER, PORT) {
			@Override protected void messageReceived(ByteBuffer message) {
				messageHolder.set(new String(message.array()));	
				latch.countDown();
			}
			@Override protected void connected(boolean alreadyConnected) {connected.countDown();}
			@Override protected void disconnected() {disconnected.countDown();}
		};
		new Thread(client).start();
		assertTrue(connected.await(1000, TimeUnit.MILLISECONDS));
		client.write("Hello".getBytes());
		assertTrue(latch.await(1000, TimeUnit.MILLISECONDS));
		assertEquals(messageHolder.get(), "Hello");
		client.stop();
		assertTrue(disconnected.await(1000, TimeUnit.MILLISECONDS));
	}
	
	@Test public void testReadSplitMessage() throws Exception {
		final CountDownLatch disconnected = new CountDownLatch(1);
		final CountDownLatch connected = new CountDownLatch(1);
		final AtomicReference<String> messageHolder = new AtomicReference<String>();
		final AtomicReference<CountDownLatch> latch = new AtomicReference<CountDownLatch>(new CountDownLatch(1));
		client = new AbstractBlockingClient(SERVER, PORT) {
			@Override protected void connected(boolean alreadyConnected) {connected.countDown();}
			@Override protected void disconnected() {disconnected.countDown();}
			@Override protected void messageReceived(ByteBuffer message) {
				messageHolder.set(new String(message.array()));	
				latch.get().countDown();
			}
		};
		new Thread(client).start();
		assertTrue(connected.await(1000, TimeUnit.MILLISECONDS));
		client.write(new byte[] {111});
		assertTrue(latch.get().await(20, TimeUnit.MILLISECONDS));
		latch.set(new CountDownLatch(1));
		
		((SocketChannel) server.lastConnectedClient.get()).write(ByteBuffer.wrap(new byte[] {0, 6}));
		((SocketChannel) server.lastConnectedClient.get()).write(ByteBuffer.wrap("Hel".getBytes()));
		((SocketChannel) server.lastConnectedClient.get()).write(ByteBuffer.wrap("lo!".getBytes()));

		assertTrue(latch.get().await(20, TimeUnit.MILLISECONDS));
		assertEquals(messageHolder.get(), "Hello!");
		assertTrue(client.isRunning());
		client.stop();
		assertTrue(disconnected.await(1000, TimeUnit.MILLISECONDS));
		assertTrue(client.isStopped());
	}
	
	public static byte[] toByte(int data) {
		   return new byte[] {(byte)((data >>> 8) & 0xff), (byte)(data & 0xff)};
		}
	
	@Test public void testReadOverBufferLengthMessage() throws Exception {
		final CountDownLatch disconnected = new CountDownLatch(1);
		final CountDownLatch connected = new CountDownLatch(1);
		final AtomicReference<String> messageHolder = new AtomicReference<String>();
		final AtomicReference<CountDownLatch> latch = new AtomicReference<CountDownLatch>(new CountDownLatch(1));
		client = new AbstractBlockingClient(SERVER, PORT, new TwoByteMessageLength(), 512) {
			@Override protected void connected(boolean alreadyConnected) {connected.countDown();}
			@Override protected void disconnected() {disconnected.countDown();}
			@Override protected void messageReceived(ByteBuffer message) {
				messageHolder.set(new String(message.array()));	
				latch.get().countDown();
			}
		};
		new Thread(client).start();
		assertTrue(connected.await(1000, TimeUnit.MILLISECONDS));
		client.write(new byte[] {111});
		assertTrue(latch.get().await(20, TimeUnit.MILLISECONDS));
		
		latch.set(new CountDownLatch(1));		
		final StringBuilder messageBuilder = new StringBuilder();
		for (int i=0;i<999;i++) {
			messageBuilder.append("x");
		}
		String message = messageBuilder.toString();
		((SocketChannel) server.lastConnectedClient.get()).write(ByteBuffer.wrap(toByte(message.length())));
		((SocketChannel) server.lastConnectedClient.get()).write(ByteBuffer.wrap(message.getBytes()));
		assertTrue(latch.get().await(20, TimeUnit.MILLISECONDS));
		assertEquals(messageHolder.get(), message);
		
		assertTrue(client.isRunning());
		client.stop();
		assertTrue(disconnected.await(1000, TimeUnit.MILLISECONDS));
		assertTrue(client.isStopped());
	}
	
	@Test public void testReadMultipleMessage() throws Exception {
		final CountDownLatch disconnected = new CountDownLatch(1);
		final CountDownLatch connected = new CountDownLatch(1);
		final AtomicReference<String> messageHolder = new AtomicReference<String>();
		final AtomicReference<CountDownLatch> latch = new AtomicReference<CountDownLatch>(new CountDownLatch(1));
		client = new AbstractBlockingClient(SERVER, PORT) {
			@Override protected void connected(boolean alreadyConnected) {connected.countDown();}
			@Override protected void disconnected() {disconnected.countDown();}
			@Override protected void messageReceived(ByteBuffer message) {
				messageHolder.set(new String(message.array()));	
				latch.get().countDown();
			}
		};
		new Thread(client).start();
		assertTrue(connected.await(1000, TimeUnit.MILLISECONDS));
		client.write(new byte[] {111});
		assertTrue(latch.get().await(20, TimeUnit.MILLISECONDS));
		
		latch.set(new CountDownLatch(1));
		((SocketChannel) server.lastConnectedClient.get()).write(ByteBuffer.wrap(new byte[] {0, 6}));
		((SocketChannel) server.lastConnectedClient.get()).write(ByteBuffer.wrap("Hello!".getBytes()));
		assertTrue(latch.get().await(20, TimeUnit.MILLISECONDS));
		assertEquals(messageHolder.get(), "Hello!");
		
		latch.set(new CountDownLatch(1));
		((SocketChannel) server.lastConnectedClient.get()).write(ByteBuffer.wrap(new byte[] {0, 6}));
		((SocketChannel) server.lastConnectedClient.get()).write(ByteBuffer.wrap("Again!".getBytes()));
		assertTrue(latch.get().await(20, TimeUnit.MILLISECONDS));
		assertEquals(messageHolder.get(), "Again!");
		
		assertTrue(client.isRunning());
		client.stop();
		assertTrue(disconnected.await(1000, TimeUnit.MILLISECONDS));
		assertTrue(client.isStopped());
	}
	
	@Test(expected=IllegalStateException.class) public void testWriteOverMaxLengthMessage() throws Exception {
		final CountDownLatch disconnected = new CountDownLatch(1);
		final CountDownLatch connected = new CountDownLatch(1);
		client = new AbstractBlockingClient(SERVER, PORT) {
			@Override protected void connected(boolean alreadyConnected) {connected.countDown();}
			@Override protected void disconnected() {disconnected.countDown();}
			@Override protected void messageReceived(ByteBuffer message) {}
		};
		new Thread(client).start();
		assertTrue(connected.await(1000, TimeUnit.MILLISECONDS));
		try {	
			final StringBuilder messageBuilder = new StringBuilder();
			for (int i=0;i<99999;i++) {
				messageBuilder.append("x");
			}
			String message = messageBuilder.toString();
			client.write(message.getBytes());
		} finally {
			client.stop();
			assertTrue(disconnected.await(1000, TimeUnit.MILLISECONDS));
			assertTrue(client.isStopped());
		}
	}
	
	@Test public void testWriteAfterStop() throws Exception {
		final CountDownLatch disconnected = new CountDownLatch(1);
		final CountDownLatch connected = new CountDownLatch(1);
		client = new AbstractBlockingClient(SERVER, PORT) {
			@Override protected void messageReceived(ByteBuffer message) {}
			@Override protected void connected(boolean alreadyConnected) {connected.countDown();}
			@Override protected void disconnected() {disconnected.countDown();}
		};
		new Thread(client).start();
		assertTrue(connected.await(1000, TimeUnit.MILLISECONDS));
		client.write("Hello".getBytes());
		client.stop();
		assertTrue(disconnected.await(1000, TimeUnit.MILLISECONDS));
		assertFalse(client.write("Again".getBytes()));
	}
	
	@Test public void testWriteBeforeStart() throws Exception {
		final CountDownLatch disconnected = new CountDownLatch(1);
		final CountDownLatch connected = new CountDownLatch(1);
		client = new AbstractBlockingClient(SERVER, PORT) {
			@Override protected void messageReceived(ByteBuffer message) {}
			@Override protected void connected(boolean alreadyConnected) {connected.countDown();}
			@Override protected void disconnected() {disconnected.countDown();}
		};
		assertFalse(client.write("Hello".getBytes()));
	}

	private static class ReflectServer implements Runnable {
		AtomicReference<SocketChannel> lastConnectedClient = new AtomicReference<SocketChannel>();
		public void run() {
			try {
				ByteBuffer buffer = ByteBuffer.allocate(512);
			    Selector selector = Selector.open();
			    ServerSocketChannel server = ServerSocketChannel.open();
			    server.socket().bind(new java.net.InetSocketAddress(PORT));
			    server.configureBlocking(false);
			    SelectionKey serverkey = server.register(selector, SelectionKey.OP_ACCEPT);
	
			    for (;;) {
			    	selector.select();
			    	Set<SelectionKey> keys = selector.selectedKeys();
	
			    	for (Iterator<SelectionKey> i = keys.iterator(); i.hasNext();) {
			    		SelectionKey key = (SelectionKey) i.next();
			    		i.remove();
	
			    		if (key == serverkey) {
			    			if (key.isAcceptable()) {
			    				SocketChannel client = server.accept();
			    				client.configureBlocking(false);
			    				client.register(selector, SelectionKey.OP_READ);
			    			}
			    		} else {
			    			SocketChannel client = (SocketChannel) key.channel();
			    			lastConnectedClient.set(client);
			    			if (!key.isReadable())
			    				continue;
			    			int bytesread = client.read(buffer);
			    			if (bytesread == -1) {
			    				key.cancel();
			    				client.close();
			    				continue;
			    			}
			    			buffer.flip();
			    			client.write(buffer);
			    			buffer.clear();
			    		}
			    	}
			    }
			} catch (Exception e) {
			}
		}
	}
}
