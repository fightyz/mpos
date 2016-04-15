package com.mcg.mpos.smartcard.data;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 应用文件定位器解析类
 */
public class ApplicationFileLocator {

	private ArrayList<ApplicationFileLocatorEntry> mAFLList;
	private static final int SINGLE_AFL_LENGTH = 4;

	/**
	 * 构建应用文件定位器
	 *
	 * @param afl
	 *            应用文件定位器byte数组
	 * @throws IOException
	 *             ֻ只有当内存不足时出现
	 */
	public ApplicationFileLocator(byte[] afl) throws IOException {
		ByteArrayInputStream bin = new ByteArrayInputStream(afl);
		DataInputStream din = new DataInputStream(bin);
		mAFLList = new ArrayList<ApplicationFileLocatorEntry>();
		while (din.available() >= SINGLE_AFL_LENGTH) {
			mAFLList.add(new ApplicationFileLocatorEntry(din));
		}
	}

	/**
	 * 获取所有应用文件定位器
	 */
	public ArrayList<ApplicationFileLocatorEntry> getApplicationFileLocatorEntry() {
		return mAFLList;
	}
}
