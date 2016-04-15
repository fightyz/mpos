package com.mcg.mpos.utils;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 字符串编码转换类
 */
public class StringEncode {

	private static final String NULL_STRING = "null";
	/**
	 * HEX编码 将形如0x12 0x2A 0x01 转换为122A01
	 *
	 * @param data
	 *            待转换数据
	 * @return 转换后数据
	 */
	public static String hexEncode(byte[] data) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			String tmp = Integer.toHexString(data[i] & 0xff);
			if (tmp.length() < 2) {
				buffer.append('0');
			}
			buffer.append(tmp);
		}
		String retStr = buffer.toString().toUpperCase();
		return retStr;
	}

	/**
	 * HEX解码 将形如122A01 转换为0x12 0x2A 0x01
	 *
	 * @param data
	 *            待转换数据
	 * @return 转换后数据
	 */
	public static byte[] hexDecode(String data) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (int i = 0; i < data.length(); i += 2) {
			String onebyte = data.substring(i, i + 2);
			int b = Integer.parseInt(onebyte, 16) & 0xff;
			out.write(b);
		}
		return out.toByteArray();
	}

	/**
	 * 获取异或编码
	 *
	 * @param data
	 *            数据
	 * @param offset
	 *            开始偏移量
	 * @param length
	 *            长度
	 * @return 异或值
	 */
	public static byte getXorCheck(byte[] data, int offset, int length) {
		byte check = 0;
		for (int i = 0; i < length; i++) {
			check ^= data[offset + i];
		}
		return check;
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
	 * 获取当前日期及时间
	 *
	 * @return 当前日期及时间，格式为yyyyMMddhhmmss
	 */
	public static String getCurrentDateAndTime() {
		Date today = new Date();
		SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");
		return f.format(today);
	}

	/**
	 * 将字符串中的特殊字符以空格代替
	 *
	 * @param s
	 *            待转化字符串
	 * @return 转换后字符串
	 */
	public static String removeSpecialCharacters(String s) {
		return s.replaceAll("[\\n\\r\\t]", " ");
	}

	/**
	 * 判断字符串是否为有效字符串（不为空字符串）
	 * @param data 待判断字符串
	 * @return true:有效字符串  false:无效字符串
	 */
	public static boolean isValid(String data) {
		return !(null == data || "".equals(data) || NULL_STRING.equals(data));
	}
	
	/**
	 * 将一串数字字符串转换成两位小数的字符串，如000000100000 => 1000.00
	 * @param money
	 * @return
	 */
	public static String moneyFormat(String money) {
		Float tmp = Float.valueOf(money);
		tmp /= 100;
		return tmp.toString();
	}
}
