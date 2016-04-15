package com.risetek.UnionPayLib.sd;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.util.Log;

/**
 * 智能卡最低层协议类，提供了操作智能卡接口
 */
public class SmartCardProtocol {

	private static final int TIMER_OUT = 1000;// (6000 * 2);
	private static final int COMMAND_HEAD_LENGTH = 6;
	private static final byte SOLE_MODE = 0x00; // 独占模式
	private static final byte PPS_VALUE = (byte) 0x94;
	// 命令码
	private static final short CMD_SCIF_INFO = 0x0001;
	private static final short CMD_SCIF_DISCONNECT = 0x0101;
	private static final short CMD_SCIF_CONNECT = 0x0102;
	private static final short CMD_SCIF_ATR = 0x0103;
	private static final short CMD_SCIF_APDU = 0x0104;
	private static final short CMD_SCIF_PPS = 0x0105;
	private static final short CMD_SCIF_RF_R = 0x0201;// 请求卡片RF动态数据
	private static final short CMD_SCIF_RF_W = 0x0202;// 请求SD卡设置RF等级
	// 通信状态码
	private static final short COMM_SCIF_IO_OK = 0x0000;
	// private static final short COMM_SCIF_IO_ILLEAGAL_CMD = 0x0001;
	private static final short COMM_SCIF_IO_TIMEOUT = 0x0002;
	// private static final short COMM_SCIF_IO_ERROR = 0x0003;
	private static final short COMM_SCIF_IO_BUSY = 0x0004;
	// private static final short COMM_SCIF_ILLEAGAL_STATUS = 0x0005;
	// private static final short COMM_SCIF_SSC_ERROR = 0x0006;
	// private static final short COMM_SCIF_RF_W_ERROR = 0x0007;//设置RF自适应异常
	private static final int MAX_DEV_SSC = 4096;

	private int mDevSSC = 0;
	private int mInitDevSSC = 0;
	public SmartCardDevice mSmartCardDevice;
	private static final String TAG = "SmartCardProtocol";

	public SmartCardProtocol() {
		mSmartCardDevice = new SmartCardDevice();
	}

	/**
	 * 会话流水号加1
	 * 
	 * @return 加1后的会话流水号
	 */
	private int increaseDevSSC() {
		mDevSSC++;
		if (mDevSSC >= MAX_DEV_SSC) {
			mDevSSC = mInitDevSSC;
		}
		return mDevSSC;
	}

	private void setDevSSC(int s) {
		mInitDevSSC = s;
		mDevSSC = s;
	}

	/**
	 * 打开智能卡
	 * 
	 * @throws FileNotFoundException
	 *             非智能卡
	 * @throws UnionPayPaymentException
	 *             无SD卡等
	 */
	public void open() throws FileNotFoundException, UnionPayPaymentException {
		boolean failed = true;
		try {
			mSmartCardDevice.open();
			failed = false;
		} catch (UnionPayPaymentException e) {
//			e.printStackTrace();
//			Log.e(TAG, e.getMessage());
			throw e;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (failed) {
				Log.i(TAG, "test open failed");
			}
		}
	}

	/**
	 * 关闭智能卡
	 * 
	 * @throws IOException
	 *             关闭失败
	 */
	public void close() throws IOException {
		boolean failed = true;
		try {
			mSmartCardDevice.close();
			failed = false;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (failed) {
				Log.i(TAG, "test close failed");
			}
		}
	}

	/**
	 * 对智能卡上电通知
	 * 
	 * @throws IOException
	 *             发送命令失败
	 * @throws UnionPayPaymentException
	 *             接收超时等
	 */
	public void connect() throws IOException, UnionPayPaymentException {
		boolean failed = true;
		try {
			byte[] mode = new byte[1];
			mode[0] = SOLE_MODE;
			byte[] response = sendAndReceive(CMD_SCIF_CONNECT, mode, true);
			int ssc = StringEncode.convertTwoBytesToInt(response, 0);
			setDevSSC(ssc);
			failed = false;
		} catch (UnionPayPaymentException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (failed) {
				Log.i(TAG, "test connect failed");
			}
		}
	}

	/**
	 * 对智能卡PPS请求
	 * 
	 * @throws IOException
	 *             发送命令失败
	 * @throws UnionPayPaymentException
	 *             接收超时等
	 */
	public void pps() throws IOException, UnionPayPaymentException {
		boolean failed = true;
		try {
			byte[] data = new byte[1];
			data[0] = PPS_VALUE;
			sendAndReceive(CMD_SCIF_PPS, data, true);
			failed = false;
		} catch (UnionPayPaymentException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (failed) {
				Log.i(TAG, "test pps failed");
			}
		}
	}

	/**
	 * 对智能卡下电通知
	 * 
	 * @throws IOException
	 *             发送命令失败
	 * @throws UnionPayPaymentException
	 *             接收超时等
	 */
	public void disconnect() throws IOException, UnionPayPaymentException {
		boolean failed = true;
		try {
			sendAndReceive(CMD_SCIF_DISCONNECT, null, true);
			failed = false;
		} catch (UnionPayPaymentException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (failed) {
				Log.i(TAG, "test disconnect failed");
			}
		}
	}

	/**
	 * 对智能卡复位
	 * 
	 * @return 复位信息
	 * @throws IOException
	 *             发送命令失败
	 * @throws UnionPayPaymentException
	 *             接收超时等
	 */
	public byte[] getResetResponse() throws IOException,
			UnionPayPaymentException {
		boolean failed = true;
		try {
			byte[] ret = sendAndReceive(CMD_SCIF_ATR, null, true);
			failed = false;
			return ret;
		} catch (UnionPayPaymentException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (failed) {
				Log.i(TAG, "test getResetResponse failed");
			}
		}
	}

	/**
	 * 向智能卡发送APDU命令
	 * 
	 * @param data
	 *            APDU命令
	 * @return APDU返回数据
	 * @throws IOException
	 *             发送命令失败
	 * @throws UnionPayPaymentException
	 *             接收超时等
	 */
	public byte[] sendApduCommand(byte[] data) throws IOException,
			UnionPayPaymentException {
		boolean failed = true;
		// TimeRecorder t = TimeRecorder.getTimeRecorder();
		try {
			byte[] ret = sendAndReceive(CMD_SCIF_APDU, data, true);
			failed = false;
			return ret;
		} catch (UnionPayPaymentException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			// t.endRecorder(TAG, "sendApduCommand " +
			// StringEncode.hexEncode(data).substring(0, 8));
			if (failed) {
				Log.i(TAG, "test sendApduCommand failed");
			}
		}
	}

	/**
	 * 取智能卡版本信息
	 * 
	 * @return 版本信息
	 * @throws IOException
	 *             发送命令失败
	 * @throws UnionPayPaymentException
	 *             接收超时等
	 */
	public byte[] getDevInfo() throws IOException, UnionPayPaymentException {
		boolean failed = true;
		try {
			byte[] ret = sendAndReceive(CMD_SCIF_INFO, null, true);
			failed = false;
			return ret;
		} catch (UnionPayPaymentException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (failed) {
				Log.i(TAG, "test getDevInfo failed");
			}
		}
	}

	/**
	 * 请求卡片RF动态数据
	 * 
	 * @return RF信号强度
	 * 
	 * @throws UnionPayPaymentException
	 * 
	 * @throws IOException
	 *             发送命令失败
	 */
	public byte RF_Read() throws UnionPayPaymentException, IOException {
		boolean failed = true;
		try {
			byte[] ret = sendAndReceive(CMD_SCIF_RF_R, null, false);
			failed = false;
			return ret[0];
		} catch (UnionPayPaymentException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (failed) {
				Log.i(TAG, "test RF_Read failed");
			}
		}
	}

	/**
	 * 请求SD 卡设置RF 等级
	 * 
	 * @param RFGrade
	 *            RF等级
	 * @return
	 * @throws UnionPayPaymentException
	 * 
	 * @throws IOException
	 * 
	 */
	public void RF_Write(byte RFGrade) throws UnionPayPaymentException,
			IOException {
		boolean failed = true;
		try {
			byte[] data = new byte[1];
			data[0] = RFGrade;
			sendAndReceive(CMD_SCIF_RF_W, data, false);
			failed = false;
		} catch (UnionPayPaymentException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (failed) {
				Log.i(TAG, "test RF_Write failed");
			}
		}
	}

	/**
	 * 向智能卡发送并接收返回数据
	 * 
	 * @param command
	 *            发送命令码
	 * @param data
	 *            命令参数
	 * @return 智能卡返回数据
	 * @throws IOException
	 *             发送命令失败
	 * @throws UnionPayPaymentException
	 *             接收超时等
	 */
	private byte[] sendAndReceive(short command, byte[] data, boolean isCheckSSC)
			throws IOException, UnionPayPaymentException {
		SendPackage send = new SendPackage();
		send.setCommand(command);
		send.setData(data);
		if (CMD_SCIF_CONNECT == command || CMD_SCIF_DISCONNECT == command
				|| CMD_SCIF_RF_R == command || CMD_SCIF_RF_W == command) {
			mDevSSC = 0;// SCIF_CONNECT、SCIF_DISCONNECT在新的规范中都要求SSC为0
			send.setSequnceCounter((short) mDevSSC);
		} else {
			send.setSequnceCounter((short) increaseDevSSC());
		}
		byte[] out = send.toByteArray();
		mSmartCardDevice.write(out);
		byte[] readBuff = new byte[SmartCardDevice.READ_WRITE_LENGTH];
		for (int i = 0; i < TIMER_OUT; i++) {
			int len = mSmartCardDevice.read(readBuff);
			if (len > 0) {
				if (equals(readBuff, out, COMMAND_HEAD_LENGTH)) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				}
				ResponsePackage response;
				try {
					response = ResponsePackage.buildReponsePackage(readBuff);
				} catch (IOException e) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					continue;
				}
				if (null == response
						|| (isCheckSSC && mDevSSC != response
								.getSendSequnceCounter())) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				}
				if (COMM_SCIF_IO_TIMEOUT == response.getStatusCode()
						|| COMM_SCIF_IO_BUSY == response.getStatusCode()) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				}
				// if (COMM_SCIF_IO_OK != response.getStatusCode()) {
				// throw new SmartCardCommunicationException(
				// response.getStatusCode());
				// }
				return response.getResponseData();
			}
		}
		throw new UnionPayPaymentException(
				UnionPayPaymentException.UPP_SMARTCARD_TIMEOUT);
	}

	/**
	 * 比较两个byte数组是否相等
	 * 
	 * @param b1
	 *            数组1
	 * @param b2
	 *            数组2
	 * @param length
	 *            比较长度
	 * @return true:相等 false:不相等
	 */
	private boolean equals(byte[] b1, byte[] b2, int length) {
		for (int i = 0; i < length; i++) {
			if (b1[i] != b2[i]) {
				return false;
			}
		}
		return true;
	}
}
