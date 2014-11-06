package com.fly.practice.nio.nioserver;

/**
 * Encodes a message length in 2 bytes, thus the maximum message length is 65535 bytes.
 */
public final class TwoByteMessageLength implements MessageLength {
	
	private final int NUM_BYTES = 2;
	private final long MAX_LENGTH = 65535;
	
	/**
	 * @see com.fly.practice.nio.nioserver.cordinc.faraway.server.network.MessageLength#byteLength()
	 */
	@Override public int byteLength() {
		return NUM_BYTES;
	}
	
	/**
	 * @see com.fly.practice.nio.nioserver.cordinc.faraway.server.network.MessageLength#maxLength()
	 */
	@Override public long maxLength() {
		return MAX_LENGTH;
	}
	
	/**
	 * @see com.fly.practice.nio.nioserver.cordinc.faraway.server.network.MessageLength#bytesToLength(byte[])
	 */
	@Override public long bytesToLength(byte[] bytes) {
		if (bytes.length!=NUM_BYTES) {
			throw new IllegalStateException("Wrong number of bytes, must be "+NUM_BYTES);
		}
		return ((long)(bytes[0] & 0xff) << 8) + (long)(bytes[1] & 0xff);
	}
	
	/**
	 * @see com.fly.practice.nio.nioserver.cordinc.faraway.server.network.MessageLength#lengthToBytes(long)
	 */
	@Override public byte[] lengthToBytes(long len) {
		if (len<0 || len>MAX_LENGTH) {
			throw new IllegalStateException("Illegal size: less than 0 or greater than "+MAX_LENGTH);
		}
		return new byte[] {(byte)((len >>> 8) & 0xff), (byte)(len & 0xff)};
	}
}
