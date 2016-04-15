package com.mcg.mpos.smartcard.data;

import java.util.Hashtable;

/**
 * PBOC上下文信息类
 */
public class PBOCConstants {

	private static final byte[] TAG_9F66_VALUE = {0x44,0x00,0x00,0x00};
	private static final byte[] TAG_9F02_VALUE = {0x00,0x00,0x00,0x00,0x10, 0x00};
	private static final byte[] TAG_CHINA_VALUE = {0x01, 0x56};
	private static final byte[] TAG_95_VALUE = {0x00,0x00,0x00,0x00,0x00};
	private static final byte[] TAG_9A_VALUE = {0x13,0x07,0x22};
	private static final byte[] TAG_9C_VALUE = {0x00};
	private static final byte[] TAG_9F37_VALUE = {0x64,0x00,(byte)0xda,0x03};
	private static Hashtable<String, byte[]> mConstans = new Hashtable<String, byte[]>();

	static {
		mConstans.put("9F66", TAG_9F66_VALUE);
		mConstans.put("9F02", TAG_9F02_VALUE);
		mConstans.put("9F03", TAG_9F02_VALUE);
		mConstans.put("9F1A", TAG_CHINA_VALUE);
		mConstans.put("95", TAG_95_VALUE);
		mConstans.put("5F2A", TAG_CHINA_VALUE);
		mConstans.put("9A", TAG_9A_VALUE);
		mConstans.put("9C", TAG_9C_VALUE);
		mConstans.put("9F37", TAG_9F37_VALUE);
	}

	/**
	 * 获取特定标签数据
	 * 
	 * @param tag
	 *            标签
	 * @return 标签所对应的值，如果标签不存在则返回null
	 */
	public static byte[] get(String tag, long len) {
		byte[] data = mConstans.get(tag);
		if (null == data) {
			return new byte[(int) len];
		}
		if (len == data.length) {
			return data;
		} else if (len > data.length) {
			byte[] ret = new byte[(int) len];
			System.arraycopy(data, 0, ret, 0, data.length);
			return ret;
		} else {
			byte[] ret = new byte[(int) len];
			System.arraycopy(data, 0, ret, 0, (int) len);
			return ret;
		}
	}
}
