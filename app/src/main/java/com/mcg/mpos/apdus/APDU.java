package com.mcg.mpos.apdus;

public class APDU {
	
	public static final byte CLA_7816 = 0x00;
	public static final byte CLA_GP = (byte)0x80;

	/**
	 * apdu中CLA的偏移量
	 */
	public static final int OFFSET_CLA = 0;
	
	/**
	 * apdu中INS的偏移量
	 */
	public static final int OFFSET_INS = 1;
	
	/**
	 * APDU中P1的偏移量
	 */
	public static final int OFFSET_P1 = 2;
	
	/**
	 * APDU中P2的偏移量
	 */
	public static final int OFFSET_P2 = 3;
	
	/**
	 * APDU中Lc的偏移量
	 */
	public static final int OFFSET_LC = 4;
	
	/**
	 * APDU中CDATA的偏移量
	 */
	public static final int OFFSET_CDATA = 5;
	
	/**
	 * APDU的CLA字节
	 */
	private byte mCLA;
	
	/**
	 * APDU的INS字节
	 */
	private byte mINS;
	
	/**
	 * APDU的P1、P2参数
	 */
	private byte mP1, mP2;
	
	/**
	 * APDU的Le字节
	 */
	private byte mLe;
	
	/**
	 * APDU的数据域字节
	 */
	private byte mCData[];
	
	/**
	 * 构造函数
	 */
	public APDU()
	{
		mCLA = 0x00;
		mINS = 0x00;
		mP1 = 0x00;
		mP2 = 0x00;
		mLe = 0x00;
		mCData = null;
	}
	
	public APDU(byte cla, byte ins, byte p1, byte p2, byte data[], byte le)
	{
		setCLA(cla);
		setINS(ins);
		setParam(p1, p2);
		setCData(data);
		setLe(le);
	}

	public void setCLA(byte cla) {
		this.mCLA = cla;
	}

	public void setINS(byte ins) {
		this.mINS = ins;
	}

	public void setParam(byte p1, byte p2) {
		this.mP1 = p1;
		this.mP2 = p2;
	}

	public void setLe(byte le) {
		this.mLe = le;
	}

	public void setCData(byte[] data) {
		this.mCData = data;
	}
	
	public byte[] getAPDUBytes() throws Exception {
		int apduLength = 4;
		
		if(mCData != null) {	//若数据域不为空
			apduLength += mCData.length  + 1;
		}
		
		if(mLe != 0x00) {//若设置了LE字节
			apduLength += 1;
		}
		
		byte apduBytes[] = new byte[apduLength];
		apduBytes[OFFSET_CLA] = mCLA;
		apduBytes[OFFSET_INS] = mINS;
		apduBytes[OFFSET_P1] = mP1;
		apduBytes[OFFSET_P2] = mP2;
		
		if(mCData != null) {
			if(mCData.length > 255) {
				throw new Exception("Wrong Data Length");
			}
			
			apduBytes[OFFSET_LC] = (byte) mCData.length;
			System.arraycopy(mCData, 0, apduBytes, OFFSET_CDATA, mCData.length);
		}
		
		if(mLe != 0x00) {
			apduBytes[apduLength - 1] = mLe;
		}
		
		return apduBytes;
	}
}
