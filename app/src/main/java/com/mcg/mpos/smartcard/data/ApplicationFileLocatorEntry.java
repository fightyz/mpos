package com.mcg.mpos.smartcard.data;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * 单条应用文件定位器入口
 */
public class ApplicationFileLocatorEntry {
	private byte mSFI;
	private byte mFistRecordNum;
	private byte mLastRecordNum;
	private byte mOfflineAuthNum;

	/**
	 * 单条应用文件定位器入口
	 *
	 * @param din
	 *            输入数据流
	 * @throws IOException
	 *             ֻ只有当内存不足时出现
	 */
	public ApplicationFileLocatorEntry(DataInputStream din) throws IOException {
		mSFI = (byte) (din.readByte() >> 3);
		mFistRecordNum = din.readByte();
		mLastRecordNum = din.readByte();
		mOfflineAuthNum = din.readByte();
	}

	/**
	 * 获取短文件标识符
	 */
	public byte getSFI() {
		return mSFI;
	}

	/**
	 * 获取第一条记录号
	 */
	public byte getFistRecordNum() {
		return mFistRecordNum;
	}

	/**
	 * 获取最后一条记录号
	 */
	public byte getLastRecordNum() {
		return mLastRecordNum;
	}

	/**
	 * 获取用于脱机认证记录数
	 */
	public byte getOfflineAuthNum() {
		return mOfflineAuthNum;
	}

}
