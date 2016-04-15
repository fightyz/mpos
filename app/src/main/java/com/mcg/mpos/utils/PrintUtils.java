package com.mcg.mpos.utils;

public class PrintUtils {
	/**
	 * ���ֽ�������16���ƴ�ӡ������̨��������
	 * @param b Ҫ��ӡ���ֽ�����
	 * @param offset ƫ����
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
