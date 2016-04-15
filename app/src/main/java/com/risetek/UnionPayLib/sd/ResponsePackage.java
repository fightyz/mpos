package com.risetek.UnionPayLib.sd;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * 智能卡返回数据包类
 */
public class ResponsePackage {

	private short mSendSequnceCounter = 0;// 会话流水号
	private short mStatusCode; // 通讯状态码
	private byte[] mResponseData; // 响应数据
	private byte mCheckValue; // 字节校验和

	private ResponsePackage() {

	}

	/**
	 * 通过smartcard返回数据构建响应包
	 *
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public static ResponsePackage buildReponsePackage(byte[] data)
			throws IOException {
		ResponsePackage response = new ResponsePackage();
		ByteArrayInputStream bin = new ByteArrayInputStream(data);
		DataInputStream input = new DataInputStream(bin);

		response.mSendSequnceCounter = input.readShort();
		response.mStatusCode = input.readShort();
		int responseLengh = input.readShort();
		if (responseLengh < 0) {
			responseLengh += 65536;
		}
		if (responseLengh > 0) {
			response.mResponseData = new byte[responseLengh];
			input.read(response.mResponseData);
		}
		int num = data.length - bin.available();
		response.mCheckValue = input.readByte();
		byte result = StringEncode.getXorCheck(data, 0, num);
		if (result != response.mCheckValue) {
			return null;
		}
		return response;
	}

	/**
	 * 获取会话流水号
	 *
	 * @return 会话流水号
	 */
	public short getSendSequnceCounter() {
		return mSendSequnceCounter;
	}

	/**
	 * 获取通讯状态码
	 *
	 * @return 通讯状态码
	 */
	public short getStatusCode() {
		return mStatusCode;
	}

	/**
	 * 获取响应数据
	 *
	 * @return 响应数据
	 */
	public byte[] getResponseData() {
		return mResponseData;
	}
}
