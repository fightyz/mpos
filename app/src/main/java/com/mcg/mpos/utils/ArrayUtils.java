package com.mcg.mpos.utils;

public class ArrayUtils {
	
	/**
	 * 将多个字节数组按从左到右顺序连接起来
	 * @param bs 要连接的字节数组
	 * @return 连接后的字节数组
	 */
	public static byte[] conbine(byte[]...bs) {
		int len = 0;
		int offset = 0;
		for(byte[] b : bs) {
			if(b != null) {
				len += b.length;
			}
		}
		byte[] rb = new byte[len];
		for(byte[] b : bs) {
			if(b != null) {
				System.arraycopy(b, 0, rb, offset, b.length);
				offset += b.length;
			}
		}
		return rb;
	}
	
	/**
	 * 截取一个字节数组的子数组
	 * @param b 被截取的字节数组
	 * @param first 截取的起始下标
	 * @param last 截取的终止下表
	 * @return 截取出来的子数组
	 */
	public static byte[] sub(byte[] b, int first, int last) {
		int length = last - first + 1;
		byte[] tmp = new byte[length];
		System.arraycopy(b, first, tmp, 0, length);
		return tmp;
	}
}
