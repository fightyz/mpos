package com.mcg.mpos.apdus;

import com.mcg.mpos.utils.ConvertUtils;

public class ReadBinaryBuilder extends APDU {

	public static final byte INS_VALUE = (byte)0xB1;
	
	public ReadBinaryBuilder() {
		super();
		setCLA(CLA_7816);
		setINS(INS_VALUE);
	}
	
	/**
	 * 设置要读取的二进制文件的SFI和读取的起始偏移量
	 * @param SFI	二进制文件的SFI
	 * @param offset	读取的数据的起始偏移量
	 */
	public ReadBinaryBuilder setTarget(byte SFI, short offset) {
		setParam((byte)0x00, (byte)(SFI & (byte) 0x1F));
		setCData(ConvertUtils.shortToByteArray(offset, 2));
		return this;
	}
}
