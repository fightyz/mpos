package com.mcg.mpos.smartcard;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.mcg.mpos.exceptions.SmartCardException;
import com.mcg.mpos.exceptions.SmartCardStatusWordException;
import com.mcg.mpos.utils.StringEncode;
import com.mobilesecuritycard.openmobileapi.Channel;
import com.mobilesecuritycard.openmobileapi.Reader;
import com.mobilesecuritycard.openmobileapi.Session;

/**
 * 智能卡操作类，提供智能卡操作接口，且为单例类
 */
public class SmartCardReader {
	private static final byte CLASS_00 = (byte) 0x00;
	private static final byte CLASS_80 = (byte) 0x80;
	// private static final byte CLASS_84 = (byte) 0x84;
	private static final byte COMMAND_SELECT = (byte) 0xA4;
	private static final byte COMMAND_READBINARY = (byte) 0xB0;
	private static final byte COMMAND_READRECORD = (byte) 0xB2;
	private static final byte COMMAND_UPDATEBINARY = (byte) 0xD6;
	private static final byte COMMAND_UPDATERECORD = (byte) 0xDC;
	private static final byte COMMAND_GETRESPONSE = (byte) 0xC0;
	private static final byte COMMAND_EXTERNALAUTH = (byte) 0x82;
	// 以下为PBOC特有命令
	private static final byte COMMAND_GPO = (byte) 0xA8;
	private static final byte COMMAND_CASHBALANCE = (byte) 0xCA;
	private static final byte COMMAND_GENERATEAC = (byte) 0xAE;

	private static final int APDU_STAUS_WORD_SUCCESS = 0x9000;
	private static final byte APDU_STAUS_WORD_NEED_RESPONSE = 0x61;
	private static final byte APDU_STAUS_WORD_NEED_RESEND = 0x6C;
	private static final int APDU_STATUS_WORD_LENGTH = 2;
	private static final int APDU_HEAD_LEN = 4;
	private boolean isIcPinValid = false;
	private static MifareSmartCardReader mSeInterface;
	private byte mChannel = 0;
	private static SmartCardReader mSmartCardReader;
	public Channel mBasicChannel;
	

	/**
	 * 获取SmartCardReader实例
	 * 
	 * @return SmartCardReader实例
	 */
	public static SmartCardReader getInstance() {
		if (null == mSmartCardReader) {
			mSmartCardReader = new SmartCardReader();
		}
		mSeInterface = MifareSmartCardReader.getInstance();
		return mSmartCardReader;
	}

	public MifareSmartCardReader getSeInterface() {
		return mSeInterface;
	}

	/**
	 * 是否有有效pin
	 * 
	 * @return true:有有效pin false:无有效pin
	 */
	public boolean isIcPinValid() {
		return isIcPinValid;
	}

	/**
	 * 设置逻辑通道 当smartcard为PBOC多应用卡时，第一次被选中应用通道号为0，第二次被选中应用通道号为1
	 * 
	 * @param channel
	 *            逻辑通道号
	 */
	public void setChannel(int channel) {
		mChannel = (byte) channel;
	}

	/**
	 * 智能卡上电
	 * 
	 * @return 智能卡复位信息
	 * @throws SmartCardException
	 *             无SD卡或非智能SD卡
	 * @throws IOException
	 *             打开SD卡文件失败
	 */
	public byte[] powerUp() throws SmartCardException, IOException {
		mSeInterface.open();
		return mSeInterface.resetDevice();
	}

	/**
	 * 智能卡下电，这里不抛出异常
	 */
	public void powerDown() {
		mChannel = 0;
		mSeInterface.close();
	}

	/**
	 * 向智能卡发送getresponse APDU命令
	 * 
	 * @param number
	 *            APDU le值
	 * @return IC卡返回数据
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws SmartCardException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 */
	private byte[] getResponse(byte number) throws IOException,
			SmartCardException {
		byte[] command = new byte[APDU_HEAD_LEN + 1];
		command[0] = mChannel;
		command[1] = COMMAND_GETRESPONSE;
		command[2] = 0;
		command[3] = 0;
		command[4] = number;
		byte[] ret = mSeInterface.sendCommand(command);
		if (ret.length < APDU_STATUS_WORD_LENGTH) {
			throw new SmartCardException("IC卡返回数据长度不正确！");
		}
		int statusWord = StringEncode.convertTwoBytesToInt(ret, ret.length - 2);
		if (APDU_STAUS_WORD_SUCCESS == statusWord) {
			byte[] data = new byte[ret.length - 2];
			System.arraycopy(ret, 0, data, 0, data.length);
			return data;
		} else {
			throw new SmartCardException("IC卡执行命令错误，错误状态字："
					+ getStatusWord(statusWord));
		}
	}

	private String getStatusWord(int status) {
		byte[] s = new byte[2];
		s[0] = (byte) (status & 0xff);
		s[1] = (byte) (status >> 8 & 0xff);
		return "0x" + StringEncode.hexEncode(s);
	}

	/**
	 * 向智能卡发送APDU命令
	 * 
	 * @param command
	 *            命令数据，包括CLA INS等
	 * @param isVerify
	 *            是否需要对状态字节0x6C特殊处理
	 * @return 智能卡返回数据
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws SmartCardException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 */
	private byte[] sendDirectAPDU(byte[] command, boolean isVerify)
			throws IOException, SmartCardException {
		byte[] response = mSeInterface.sendCommand(command);
		if (APDU_STATUS_WORD_LENGTH == response.length) {
			if (APDU_STAUS_WORD_NEED_RESPONSE == response[0]) {
				return getResponse(response[1]);
			} else if (APDU_STAUS_WORD_NEED_RESEND == response[0]) {
				if (isVerify) {
					int statusWord = 0x63c0 | (response[1] & 0x03);
					throw new SmartCardStatusWordException("IC卡执行命令"
							+ StringEncode.hexEncode(command).substring(0, 8)
							+ "错误，错误状态字：" + getStatusWord(statusWord));
				} else {
					byte[] com;
					if (4 == command.length || 5 == command.length) {
						com = new byte[5];
						System.arraycopy(command, 0, com, 0, command.length);
						com[4] = response[1];
					} else {
						int le = command[4];
						com = new byte[5 + le + 1];
						System.arraycopy(command, 0, com, 0, 5 + le);
						com[5 + le] = response[1];
					}
					return sendDirectAPDU(com, isVerify);
				}
			}
		}
		if (response.length < APDU_STATUS_WORD_LENGTH) {
			throw new SmartCardException("IC卡返回数据长度不正确！");
		}
		int statusWord = StringEncode.convertTwoBytesToInt(response,
				response.length - 2);
		if (APDU_STAUS_WORD_SUCCESS == statusWord) {
			byte[] data = new byte[response.length - 2];
			System.arraycopy(response, 0, data, 0, data.length);
			return data;
		} else {
			throw new SmartCardStatusWordException("IC卡执行命令"
					+ StringEncode.hexEncode(command).substring(0, 8)
					+ "错误，错误状态字：" + getStatusWord(statusWord));
		}
	}

	/**
	 * 向智能卡发送APDU命令
	 * 
	 * @param command
	 *            命令数据，包括CLA INS等
	 * @return 智能卡返回数据
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws SmartCardException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 */
	public byte[] sendDirectAPDU(byte[] command) throws IOException,
			SmartCardException {
		return sendDirectAPDU(command, false);
	}

	/**
	 * 向智能卡发送APDU命令
	 * 
	 * @param cla
	 *            APDU命令所属类
	 * @param ins
	 *            APDU命令码
	 * @param p1
	 *            参数1
	 * @param p2
	 *            参数2
	 * @param data
	 *            发送数据
	 * @param le
	 *            接收数据长度
	 * @param isVerify
	 *            是否需要对状态字节0x6C特殊处理
	 * @return 智能卡返回数据
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws SmartCardException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 */
	private byte[] sendCommand(byte cla, byte ins, byte p1, byte p2,
			byte[] data, int le, boolean isVerify) throws IOException,
			SmartCardException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		bout.write(cla | mChannel);
		bout.write(ins);
		bout.write(p1);
		bout.write(p2);
		if (null != data) {
			bout.write(data.length);
			bout.write(data);
		}
		if (le > 0) {
			bout.write(le);
		}
		return sendDirectAPDU(bout.toByteArray(), isVerify);
	}

	/**
	 * 向智能卡发送APDU命令，且不对状态字节0x6C特殊处理
	 * 
	 * @param cla
	 *            APDU命令所属类
	 * @param ins
	 *            APDU命令码
	 * @param p1
	 *            参数1
	 * @param p2
	 *            参数2
	 * @param data
	 *            发送数据
	 * @param le
	 *            接收数据长度
	 * @return 智能卡返回数据
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws SmartCardException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 */
	public byte[] sendCommand(byte cla, byte ins, byte p1, byte p2,
			byte[] data, int le) throws IOException, SmartCardException {
		return sendCommand(cla, ins, p1, p2, data, le, false);
	}

	/**
	 * 通过文件名选择
	 * 
	 * @param name
	 *            文件名
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws SmartCardException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 */
	public void selectByName(byte[] name) throws IOException,
			SmartCardException {
		sendCommand(CLASS_00, COMMAND_SELECT, (byte) 0x04, (byte) 0x00, name, 0);
	}

	/**
	 * 通过文件名选择
	 * 
	 * @param name
	 *            文件名
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws SmartCardException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 */
	public byte[] selectByNameWithResult(byte[] name) throws IOException,
			SmartCardException {
//		return sendCommand(CLASS_00, COMMAND_SELECT, (byte) 0x04, (byte) 0x00,
//				name, 0);
		byte response[] = null;
		if(mSeInterface.r!=null){
	    	if(mSeInterface.r.isSecureElementPresent()){
	    		
	    		if(mBasicChannel !=null && !mBasicChannel.isClosed())
	    			mBasicChannel.close();
	    		
	    		try {
					mBasicChannel = mSeInterface.mSession.openBasicChannel(name);
					response =  mBasicChannel.getSelectResponse();
					} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//	    		mBasicChannel.close();
	    	}
	    }
		return response;
	}
	
	
	
	/**
	 * 读取二进制文件
	 * 
	 * @param sfi
	 *            待读取文件的短文件标示符
	 * @param readLen
	 *            待读取长度
	 * @return 读出数据
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws SmartCardException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 */
	public byte[] readBinary(int sfi, int readLen) throws IOException,
			SmartCardException {
		byte p1 = (byte) (0x80 | (sfi & 0x1F));
		return sendCommand(CLASS_00, COMMAND_READBINARY, p1, (byte) 0, null,
				readLen);
	}

	/**
	 * 将数据写入到二进制文件
	 * 
	 * @param sfi
	 *            待写入文件的短文件标示符
	 * @param offset
	 *            文件中的偏移地址
	 * @param data
	 *            待写入数据
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws SmartCardException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 */
	public void updateBinary(int sfi, int offset, byte[] data)
			throws IOException, SmartCardException {
		byte p1 = (byte) (0x80 | (sfi & 0x1F));
		sendCommand(CLASS_00, COMMAND_UPDATEBINARY, p1, (byte) offset, data, 0);
	}

	/**
	 * 更新记录文件
	 * 
	 * @param sfi
	 *            待更新记录文件的短文件标示符
	 * @param recordNum
	 *            记录号
	 * @param data
	 *            记录数据
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws SmartCardException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 */
	public void updateRecord(int sfi, int recordNum, byte[] data)
			throws IOException, SmartCardException {
		byte p2 = (byte) ((sfi << 3) + 0x4);
		sendCommand(CLASS_00, COMMAND_UPDATERECORD, (byte) recordNum, p2, data,
				0);
	}

	/**
	 * 读取记录文件
	 * 
	 * @param sfi
	 *            待读取记录文件的短文件标示符
	 * @param recordNum
	 *            记录号
	 * @return 记录数据
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws SmartCardException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 */
	public byte[] readRecord(int sfi, int recordNum) throws IOException,
			SmartCardException {
		byte p2 = (byte) ((sfi << 3) + 0x4);
		return sendCommand(CLASS_00, COMMAND_READRECORD, (byte) recordNum, p2,
				null, 0);
	}

	/**
	 * 外部认证
	 * 
	 * @param data
	 *            外部认证数据
	 * @return 认证结果
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws SmartCardException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 */
	public String externalAuth(byte[] data) throws IOException,
			SmartCardException {
		byte[] retsult = sendCommand(CLASS_00, COMMAND_EXTERNALAUTH, (byte) 0,
				(byte) 0, data, 0);
		return StringEncode.hexEncode(retsult);
	}

	/** 以下为PBOC特有命令 **/

	/**
	 * 应用初始化
	 * 
	 * @param pdol
	 *            处理选项数据对象列表
	 * @return 结果数据
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws SmartCardException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 */
	public byte[] getProcessingOptions(byte[] pdol) throws IOException,
			SmartCardException {
		return sendCommand(CLASS_80, COMMAND_GPO, (byte) 0, (byte) 0, pdol, 0);
	}

	/**
	 * 获取电子现金余额
	 * 
	 * @return 电子现金余额
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws SmartCardException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 */
	public String getCashBanlance() throws IOException, SmartCardException {
		byte[] result = sendCommand(CLASS_80, COMMAND_CASHBALANCE, (byte) 0x9F,
				(byte) 0x79, null, 0);
		byte[] value = new byte[6];
		System.arraycopy(result, 3, value, 0, value.length);
		return StringEncode.hexEncode(value);
	}
	
	/**
	 * 获取电子现金余额上限
	 * 
	 * @return 电子现金余额上限
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws SmartCardException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 */
	public String getCashBanlanceLimit() throws IOException, SmartCardException {
		byte[] result = sendCommand(CLASS_80, COMMAND_CASHBALANCE, (byte) 0x9F,
				(byte) 0x77, null, 0);
		byte[] value = new byte[6];
		System.arraycopy(result, 3, value, 0, value.length);
		return StringEncode.hexEncode(value);
	}

	/**
	 * 获取交易记录格式
	 * 
	 * @return 交易记录格式
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws SmartCardException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 */
	public String getTransRecordsFormats() throws IOException, SmartCardException {
		byte[] result = sendCommand(CLASS_80, COMMAND_CASHBALANCE, (byte) 0x9F,
				(byte) 0x4F, null, 0);
		byte[] value = new byte[result.length - 3];
		System.arraycopy(result, 3, value, 0, value.length);
		return StringEncode.hexEncode(value);
	}
	
	/**
	 * 生成密文
	 * 
	 * @param p1
	 *            引用控制参数
	 * @param value
	 *            交易交易相关数据
	 * @return 密文
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws SmartCardException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 */
	public byte[] generateAC(byte p1, byte[] value) throws IOException,
			SmartCardException {
		return sendCommand(CLASS_80, COMMAND_GENERATEAC, p1, (byte) 0x00,
				value, 0);
	}
	
	/**
	 * put data命令 修改电子现金余额
	 * 
	 * @param data
	 *            带有MAC的put data命令
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws SmartCardException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 *    add by liuy
	 */
	public byte[] putDataMAC(byte[] data) throws IOException,
			SmartCardException {
		return sendCommand((byte) 0x04, (byte) 0xDA, (byte) 0x9F,
				(byte) 0x79, data, 0);
	}
	
	/**
	 * @param data
	 *内部认证数据("01020304").getBytes()
	 * @return 认证结果
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws SmartCardException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 *    add by YCY
	 */
	public byte[] internalAuth(byte[] data) throws IOException,	SmartCardException {			
		return sendCommand((byte) 0x00, (byte) 0x88, (byte) 0x00, (byte) 0x00, data, 0);
	}
	
}