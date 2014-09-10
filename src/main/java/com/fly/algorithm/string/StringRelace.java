package com.fly.algorithm.string;

/**
 * JDK自带的String的replace方法每次都会创建一个新的对象，导致内存浪费。 
 * 可以使用StringBuffer的replace方法替换
 * 
 * @author fly
 *
 */
public class StringRelace {
	/**
	 * 替换StringBuffer中的字符串，由于StringBuffer为不可变字符串，因此不需要返回值
	 * 
	 * @param buff
	 * @param oldStr
	 * @param newStr
	 */
	public static void stringBufferReplace(StringBuffer buff, String oldStr,
			String newStr) {
		int start;
		while ((start = buff.indexOf(oldStr)) >= 0)
			buff.replace(start, start + oldStr.length(), newStr);
	}

	public static void main(String[] args) {
		String s = "test|test";
		StringBuffer sb = new StringBuffer(s);
		stringBufferReplace(sb, "|", "#");
		System.out.println(sb);
	}

}
