package com.mcg.mpos.utils;

public class ConvertUtils {
	/**
	 * 将一个int数值转换成一个字节数组
	 * @param value 待转换的int数值
	 * @param n 转换成的字节数组大小，字节数组高字节补0
	 * @return
	 */
	public static byte[] intToByteArray(int value, int n) {
	    byte[] b = new byte[n];
	    for (int i = 0; i < n; i++) {
	        int offset = (b.length - 1 - i) * 8;
	        b[i] = (byte) ((value >>> offset) & 0xFF);
	    }
	    return b;
	}
	
	public static byte[] shortToByteArray(short value, int n) {
		byte[] b = new byte[n];
		for(int i = 0; i < n; i++) {
			int offset = (b.length - 1 - i) * 8;
			b[i] = (byte) ((value >>> offset) & 0xFF);
		}
		return b;
	}
	
	/**
	 * 
	 * @param bytes
	 * @return
	 */
	public static String bytesToString(byte[] bytes) {
		if(bytes == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		for (byte b : bytes) {
			sb.append(String.format("%02X ", b & 0xFF));
		}
		return sb.toString();
	}
	
	/**
	 * 将两个byte转换为整数
	 *
	 * @param data
	 *            待转换byte数组
	 * @param offset
	 *            数据偏移量
	 * @return 转换后整数值
	 */
	public static int convertTwoBytesToInt(byte[] data, int offset) {
		if (data.length - offset < 2) {
			return -1;
		}
		int value = data[offset];
		if (value < 0) {//byte to int 高位补全是有符号补全，所以+256能将高位的1全部消掉 
			value += 256;
		}
		value = value << 8;
		value += data[offset + 1];
		if (data[offset + 1] < 0) {
			value += 256;
		}
		return value;
	}
	
	/**
	 * 将一个字节数组转换成16进制字符串，不以空格隔开：0x01 0x02 -> 0102
	 * @param b 
	 * @param offset
	 * @return
	 */
	public static String bytesToHexString(byte[] b, int offset) {
		String hex = null;
		for (int i = offset; i < b.length; i++) { 
			hex = Integer.toHexString(b[i] & 0xFF); 
			if (hex.length() == 1) { 
				hex = '0' + hex; 
			} 
		}
		return hex.toUpperCase();
	}
}
