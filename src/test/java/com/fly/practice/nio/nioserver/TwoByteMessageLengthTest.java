package com.fly.practice.nio.nioserver;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class TwoByteMessageLengthTest {

	private MessageLength messageLength;
	
	@Before public void setup() {
		messageLength = new TwoByteMessageLength();
	}
	
	private void assertArrayEquals(byte[] arr1, byte[] arr2) {
		assertEquals(arr1.length, arr2.length);
		for (int i=0; i< arr1.length; i++) {
			assertEquals(arr1[i], arr2[i]);
		}
	}
	
	@Test public void testToBytesForSingleNegative() {
		assertArrayEquals(messageLength.lengthToBytes(255), new byte[] {0, -1});
	}
	
	@Test public void testToLengthForSingleNegative() {
		assertEquals(messageLength.bytesToLength(new byte[] {0, -1}), 255);
	}
	
	@Test public void testToBytes() {
		assertArrayEquals(messageLength.lengthToBytes(34), new byte[] {0, 34});
	}
	
	@Test public void testToLength() {
		assertEquals(messageLength.bytesToLength(new byte[] {0, 34}), 34);
	}
	
	@Test public void testToBytesZero() {
		assertArrayEquals(messageLength.lengthToBytes(0), new byte[] {0, 0});
	}
	
	@Test public void testToLengthZero() {
		assertEquals(messageLength.bytesToLength(new byte[] {0, 0}), 0);
	}
	
	@Test public void testToBytesTwoBytesLastNegative() {
		assertArrayEquals(messageLength.lengthToBytes(1000), new byte[] {3, -24});
	}
	
	@Test public void testToLengthTwoBytesLastNegative() {
		assertEquals(messageLength.bytesToLength(new byte[] {3, -24}), 1000);
	}
	
	@Test public void testToBytesTwoBytes() {
		assertArrayEquals(messageLength.lengthToBytes(774), new byte[] {3, 6});
	}
	
	@Test public void testToLengthTwoBytes() {
		assertEquals(messageLength.bytesToLength(new byte[] {3, 6}), 774);
	}
	
	@Test public void testToBytesTwoBytesBothNegative() {
		assertArrayEquals(messageLength.lengthToBytes(44444), new byte[] {-83, -100});
	}
	
	@Test public void testToLengthTwoBytesBothNegative() {
		assertEquals(messageLength.bytesToLength(new byte[] {-83, -100}), 44444);
	}
	
	@Test public void testToBytesTwoBytesFirstNegative() {
		assertArrayEquals(messageLength.lengthToBytes(51209), new byte[] {-56, 9});
	}
	
	@Test public void testToLengthTwoBytesFirstNegative() {
		assertEquals(messageLength.bytesToLength(new byte[] {-56, 9}), 51209);
	}
	
	@Test(expected=IllegalStateException.class) public void testToBytesNegative() {
		messageLength.lengthToBytes(-1);
	}
	
	@Test(expected=IllegalStateException.class) public void testToBytesToBig() {
		messageLength.lengthToBytes(151209);
	}
	
	@Test(expected=IllegalStateException.class) public void testToLengthTooLong() {
		messageLength.bytesToLength(new byte[] {-56});
	}
	
	@Test(expected=IllegalStateException.class) public void testToLengthShort() {
		messageLength.bytesToLength(new byte[] {-56, 9, 1});
	}
	
	@Test(expected=NullPointerException.class) public void testToLengthNull() {
		messageLength.bytesToLength(null);
	}
	
	@Test public void testByteLength() {
		assertEquals(messageLength.byteLength(), 2);
	}
	
	@Test public void testMaxLength() {
		assertEquals(messageLength.maxLength(), 65535);
	}
}
