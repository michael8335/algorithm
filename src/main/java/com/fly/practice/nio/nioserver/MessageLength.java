package com.fly.practice.nio.nioserver;

/**
 * Provides methods to encode message lengths as byte arrays and convert them back again.
 */
public interface MessageLength {

	/**
	 * Returns the number of bytes used to encode the length of a message.
	 * @return the number of bytes used to encode the length of a message.
	 */
	int byteLength();

	/**
	 * Returns the maximum length of a message.
	 * @return the maximum length of a message.
	 */
	long maxLength();

	/**
	 * Convert the given byte array to a number. Assumes the most significant bytes will be at the front of the
	 * array.
	 * 
	 * @param bytes the byte array to convert to a number.
	 * @return the number encoded by the given byte array.
	 * @throws IllegalStateException if the size of the array is not equals to byteLength()
	 */
	long bytesToLength(byte[] bytes);

	/**
	 * Convert the given length into an array of unsigned bytes encoded the same length. The size 
	 * of the array will be byteLength() and the most significant bytes will be at the front of the
	 * array.
	 * 
	 * @param length the length to convert.
	 * @return the byte array conversion of the given length.
	 * @throws IllegalStateException if length is negative or greater than the larger allowable length.
	 */
	byte[] lengthToBytes(long length);

}