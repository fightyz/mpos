package com.risetek.UnionPayLib.sd;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 发送给智能卡数据包类
 */
public class SendPackage {

	private short mSendSequnceCounter = 0;// 会话流水号
	private short mCommand; // 命令码
	private byte[] mData; // 参数或APDU命令

	/**
	 * 设置会话流水号
	 *
	 * @param ssc
	 *            会话流水号
	 */
	public void setSequnceCounter(short ssc) {
		mSendSequnceCounter = ssc;
	}

	/**
	 * 设置命令码
	 *
	 * @param command
	 *            命令码
	 */
	public void setCommand(short command) {
		mCommand = command;
	}

	/**
	 * 设置命令参数
	 *
	 * @param data
	 *            参数
	 */
	public void setData(byte[] data) {
		mData = data;
	}

	/**
	 * 转换为byte数组
	 *
	 * @return 发送包byte数组
	 * @throws IOException
	 *             转换错误，这里内存不足时才会出现
	 */
	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bout);
		out.writeShort(mSendSequnceCounter);
		out.writeShort(mCommand);
		if (mData != null && (mData.length > 0)) {
			out.writeShort(mData.length);
			out.write(mData);
		} else {
			out.writeShort(0);
		}
		out.writeByte(StringEncode.getXorCheck(bout.toByteArray(), 0,
				bout.toByteArray().length));
		return bout.toByteArray();
	}
}
