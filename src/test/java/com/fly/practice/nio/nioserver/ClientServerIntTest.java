package com.fly.practice.nio.nioserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

public class ClientServerIntTest {

	private static final int PORT = 8000;
	private static final InetAddress SERVER;
	static {
		InetAddress temp;
		try {temp=InetAddress.getByName("localhost");} catch (Exception e) {temp=null;}
		SERVER=temp;
	}
	
	@Test public void testIntegration() throws Exception {
		final CountDownLatch serverStarted = new CountDownLatch(1);
		final CountDownLatch serverStopped = new CountDownLatch(1);
		final CountDownLatch serverMessageRec = new CountDownLatch(1);
		final CountDownLatch serverDisconnected = new CountDownLatch(1);
		final AtomicReference<String> serverMessage = new AtomicReference<String>();
		AbstractServer server = new AbstractServer(PORT) {
			@Override protected void disconnected(SelectionKey key) {serverDisconnected.countDown();}
			@Override protected void messageReceived(ByteBuffer message, SelectionKey key) {
				serverMessage.set(new String(message.array()));
				serverMessageRec.countDown();
				write(key, "Who is there".getBytes());
			}
			@Override protected void started(boolean alreadyStarted) {serverStarted.countDown();}	    
			@Override protected void stopped() {serverStopped.countDown();}
			@Override protected void connection(SelectionKey key) {}
		};
		new Thread(server).start();	 
		assertTrue(serverStarted.await(1000, TimeUnit.SECONDS));
		
		final CountDownLatch clientMessageRec = new CountDownLatch(1);
		final AtomicReference<String> clientMessage = new AtomicReference<String>();
		final CountDownLatch connected = new CountDownLatch(1);
		final CountDownLatch clientDisconnected = new CountDownLatch(1);
		AbstractBlockingClient client = new AbstractBlockingClient(SERVER, PORT) {
			@Override protected void messageReceived(ByteBuffer message) {
				clientMessage.set(new String(message.array()));
				clientMessageRec.countDown();
			}
			@Override protected void connected(boolean alreadyConnected) {connected.countDown();}
			@Override protected void disconnected() {clientDisconnected.countDown();}
		};
		
		new Thread(client).start();	
		assertTrue(connected.await(1000, TimeUnit.SECONDS));
		client.write("Hello".getBytes());
		
		assertTrue(serverMessageRec.await(20, TimeUnit.MILLISECONDS));
		assertEquals(serverMessage.get(), "Hello");
		assertTrue(clientMessageRec.await(20, TimeUnit.MILLISECONDS));
		assertEquals(clientMessage.get(), "Who is there");
		
		client.stop();
		assertTrue(clientDisconnected.await(1000, TimeUnit.MILLISECONDS));
		
		assertTrue(serverDisconnected.await(1000, TimeUnit.MILLISECONDS));
		server.stop();
		assertTrue(serverStopped.await(1000, TimeUnit.SECONDS));
		assertTrue(server.isStopped());
	}
}
