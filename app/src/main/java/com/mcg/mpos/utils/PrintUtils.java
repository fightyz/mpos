package com.mcg.mpos.utils;

public class PrintUtils {
	/**
	 * 将字节数组以16进制打印到控制台，并换行
	 * @param b 要打印的字节数组
	 * @param offset 偏移量
	 */
	public static void printHexString( byte[] b, int offset) {  
		for (int i = offset; i < b.length; i++) { 
			String hex = Integer.toHexString(b[i] & 0xFF); 
			if (hex.length() == 1) { 
				hex = '0' + hex; 
			} 
		    System.out.print(hex.toUpperCase() ); 
		}
		System.out.println();
	}
}
