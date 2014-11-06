package com.fly.practice.nio.nioserver;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ByteMessageLengthTest {

	private MessageLength messageLength;
	
	@Before public void setup() {
		messageLength = new ByteMessageLength();
	}
	
	private void assertArrayEquals(byte[] arr1, byte[] arr2) {
		assertEquals(arr1.length, arr2.length);
		for (int i=0; i< arr1.length; i++) {
			assertEquals(arr1[i], arr2[i]);
		}
	}
	
	@Test public void testToBytesForNegative() {
		assertArrayEquals(messageLength.lengthToBytes(255), new byte[] {-1});
	}
	
	@Test public void testToLengthForNegative() {
		assertEquals(messageLength.bytesToLength(new byte[] {-1}), 255);
	}
	
	@Test public void testToBytes() {
		assertArrayEquals(messageLength.lengthToBytes(34), new byte[] {34});
	}
	
	@Test public void testToLength() {
		assertEquals(messageLength.bytesToLength(new byte[] {34}), 34);
	}
	
	@Test public void testToBytesZero() {
		assertArrayEquals(messageLength.lengthToBytes(0), new byte[] {0});
	}
	
	@Test public void testToLengthZero() {
		assertEquals(messageLength.bytesToLength(new byte[] {0}), 0);
	}
	
	@Test(expected=IllegalStateException.class) public void testToBytesNegative() {
		messageLength.lengthToBytes(-1);
	}
	
	@Test(expected=IllegalStateException.class) public void testToBytesToBig() {
		messageLength.lengthToBytes(300);
	}
	
	@Test(expected=IllegalStateException.class) public void testToLengthTooLong() {
		messageLength.bytesToLength(new byte[] {});
	}
	
	@Test(expected=IllegalStateException.class) public void testToLengthShort() {
		messageLength.bytesToLength(new byte[] {-56, 9, 1});
	}
	
	@Test(expected=NullPointerException.class) public void testToLengthNull() {
		messageLength.bytesToLength(null);
	}
	
	@Test public void testByteLength() {
		assertEquals(messageLength.byteLength(), 1);
	}
	
	@Test public void testMaxLength() {
		assertEquals(messageLength.maxLength(), 255);
	}
}
