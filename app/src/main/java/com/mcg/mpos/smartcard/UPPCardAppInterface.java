package com.mcg.mpos.smartcard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.mcg.mpos.exceptions.SmartCardException;
import com.mcg.mpos.smartcard.data.ApplicationFileLocator;
import com.mcg.mpos.smartcard.data.ApplicationFileLocatorEntry;
import com.mcg.mpos.smartcard.data.PBOCConstants;
import com.mcg.mpos.smartcard.data.PBOCContext;
import com.mcg.mpos.tlv.TLVElement;
import com.mcg.mpos.tlv.TLVParser;
import com.mcg.mpos.tlv.TLVParserException;
import com.mcg.mpos.tlv.TLVUtils;
import com.mcg.mpos.utils.StringEncode;
import com.mcg.mpos.utils.Utility;

import android.util.Log;


/**
 * 智能卡操作接口类，为单例类
 */
public class UPPCardAppInterface {

	private static final byte[] AID0 = { (byte) 0xA0, 0x00, 0x00, 0x03, 0x33,
			0x01, 0x01, 0x01 };
	private static final byte[] AID1 = { (byte) 0xA0, 0x00, 0x00, 0x03, 0x33,
			0x01, 0x01, 0x02 };
	private static final byte[] AID2 = { (byte) 0xA0, 0x00, 0x00, 0x03, 0x33,
			0x01, 0x01, 0x06 };
	private static final byte[] PDOL = { (byte) 0x83, 0x09, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x01, 0x20, 0x01, 0x56 };
//	private static final byte[] PDOL = { (byte) 0x83, 0x22, 0x44, 0x00, 0x00,
//			0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00,
//			0x10, 0x00, 0x08, 0x26, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08, 0x26,
//			0x13, 0x05, 0x22, 0x00, 0x64, 0x00, (byte) 0xda, 0x03, 0x00 };
//
//	private static final byte[] PDOL_1 = { (byte) 0x83, 0x21, 0x44, 0x00, 0x00,
//			0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00,
//			0x10, 0x00, 0x08, 0x26, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08, 0x26,
//			0x13, 0x05, 0x22, 0x00, 0x64, 0x00, (byte) 0xda, 0x03 };
//	private static final byte[] PDOL_3 = { (byte) 0x83, 0x22, 0x00, 0x44, 0x00,
//			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x00,
//			0x00, 0x10, 0x00, 0x08, 0x26, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08,
//			0x26, 0x13, 0x05, 0x22, 0x00, 0x64, 0x00, (byte) 0xda, 0x03 };
	private static final byte[] TRANSACTION_DATA = { 0x00, 0x00, 0x00, 0x00,
			0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x56, 0x00,
			0x00, 0x00, 0x08, 0x00, 0x01, 0x56, 0x11, 0x09, 0x01, 0x02, 0x00,
			0x00, 0x00, 0x00, 0x16, 0x45, 0x30, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00 };
	private static final byte[] TRANSACTION_DATA2 = { 0x30, 0x30, 0x00, 0x00,
			0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01,
			0x56, 0x00, 0x00, 0x00, 0x08, 0x00, 0x01, 0x56, 0x11, 0x08, 0x30,
			0x02, 0x00, 0x00, 0x00, 0x00, 0x16, 0x45, 0x30 };
	private static final byte[] DC_DATA = { (byte) 0x9F, 0x26, 0x08, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x9F, 0x27, 0x01,
			0x00, (byte) 0x9F, 0x10, 0x13, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, (byte) 0x9F, 0x36, 0x02, 0x00, 0x00, (byte) 0x9F, 0x37,
			0x04, 0x00, 0x00, 0x00, 0x00, (byte) 0x95, 0x05, 0x00, 0x00, 0x00,
			0x08, 0x00, (byte) 0x9A, 0x03, 0x11, 0x09, 0x01, (byte) 0x9C, 0x01,
			0x31, (byte) 0x9F, 0x02, 0x06, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00,
			0x5F, 0x2A, 0x02, 0x01, 0x56, (byte) 0x9F, 0x1A, 0x02, 0x01, 0x56,
			(byte) 0x9F, 0x03, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			(byte) 0x82, 0x02, 0x7C, 0x00, (byte) 0x9F, 0x33, 0x03,
			(byte) 0xA0, 0x40, 0x00 };
	private static final int AIP_LENGTH = 2;// 应用交互特征长度
	private static SmartCardReader mSmartCardReader;
	private Lock mLock;
	private int mScriptState = 0;
	private static UPPCardAppInterface mSelf = new UPPCardAppInterface();
	private byte[] mPDOL = null;

	private UPPCardAppInterface() {
		mLock = new ReentrantLock();
	}

	/**
	 * 获取智能卡操作实例
	 */
	public static UPPCardAppInterface getInstance() {
		mSmartCardReader = SmartCardReader.getInstance();
		return mSelf;
	}

	public SmartCardReader getSmartCardReader() {
		return mSmartCardReader;
	}

	/**
	 * 获取发卡行脚本执行状态
	 * 
	 * @return 发卡行脚本执行状态 1：执行脚本失败 2；执行脚本成功 0:未执行脚本
	 */
	public int getScriptState() {
		return mScriptState;
	}

	/**
	 * 初始化发卡行脚本执行状态
	 */
	public void initScriptState() {
		mScriptState = 0;
	}

	/**
	 * 对智能卡执行复位操作
	 * 
	 * @return 复位返回数据
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws UnionPayPaymentException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 */
	public byte[] resetSmartCard() throws SmartCardException, IOException {
		try {
			mLock.lock();
			return mSmartCardReader.powerUp();
		} finally {
			mLock.unlock();
		}
	}

	/**
	 * 对智能卡上电
	 * 
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws UnionPayPaymentException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 */
	public void powerUp() throws SmartCardException, IOException {
		mSmartCardReader.powerUp();
	}

	/**
	 * 对智能卡下电
	 */
	public void powerDown() {
		mSmartCardReader.powerDown();
	}

	byte[] selectResult;

	/**
	 * 选择PBOC借贷记应用
	 * 
	 * @return true:选择成功 false:选择失败
	 * @throws IOException
	 */
	public byte[] selectPBOCApplicaticon() throws IOException {
		byte[] selectResult = null;
//		try {
//			selectResult = mSmartCardReader
//					.selectByNameWithResult("1PAY.SYS.DDF01".getBytes());
//			
//		} catch (SmartCardException e3) {
//			e3.printStackTrace();
//		}
		
		
			try {
				selectResult = mSmartCardReader.selectByNameWithResult(AID0);
			} catch (SmartCardException e) {
				try {
					selectResult = mSmartCardReader
							.selectByNameWithResult(AID1);
				} catch (SmartCardException e1) {
					try {
						selectResult = mSmartCardReader
								.selectByNameWithResult(AID2);
					} catch (SmartCardException e2) {
						e2.printStackTrace();
//						return false;
					}
				}
			}
//			this.selectResult = selectResult;
			return selectResult;
	}

	/**
	 * 获取借贷记应用数据
	 * 
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws UnionPayPaymentException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 */
	public String getDCData(String amount) throws SmartCardException,
			IOException {
		mSmartCardReader.powerDown();
		mSmartCardReader.powerUp();
		if (selectPBOCApplicaticon() != null) {
			// FIXME 很恶心的做法
			if(null == mPDOL){
				throw new SmartCardException("未获取到必要数据PDOL");
			}
			mSmartCardReader.getProcessingOptions(mPDOL);
			byte[] data;
			if (null != amount) {
				data = new byte[TRANSACTION_DATA.length];
				byte[] ret = StringEncode.hexDecode(amount);
				System.arraycopy(TRANSACTION_DATA, 0, data, 0, data.length);
				System.arraycopy(ret, 0, data, 0, ret.length);
			} else {
				data = TRANSACTION_DATA;
			}
			byte[] ac = mSmartCardReader.generateAC((byte) 0x80, data);
			PBOCContext.setLastApplicationCryptogram(data);
			byte[] dcData = new byte[DC_DATA.length];
			System.arraycopy(DC_DATA, 0, dcData, 0, dcData.length);
			System.arraycopy(ac, 2, dcData, 14, 1);
			System.arraycopy(ac, 5, dcData, 3, 8);
			System.arraycopy(ac, 13, dcData, 18, (ac[1] - 11));
			System.arraycopy(ac, 3, dcData, 40, 2);
			System.arraycopy(data, 0, dcData, 67, 6);
			return StringEncode.hexEncode(dcData);
		} else {
			throw new SmartCardException("不是PBOC卡！");
		}
	}

	/**
	 * 获取银行卡卡号
	 * 
	 * @return 银行卡卡号
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws UnionPayPaymentException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 */
	public String getBankCardNum() throws IOException, SmartCardException {
		try {
			mLock.lock();
			mSmartCardReader.powerDown();
			mSmartCardReader.powerUp();
			if (selectPBOCApplicaticon() != null) {
				try {
					byte[] ret = null;
					ByteArrayInputStream bin = new ByteArrayInputStream(this.selectResult);
					TLVParser parser = new TLVParser(bin);
					Vector<TLVElement> v = parser.parseAllTLVElement();
					Log.v("test: ", v.size() + "");
					for (int i = 0; i < v.size(); i++) {
						PBOCContext.add(v.elementAt(i));
					}
					byte[] pdol = PBOCContext.get(PBOCContext.TAG_PDOL);
					if(null != pdol){
						ByteArrayInputStream pdolIn = new ByteArrayInputStream(pdol);
						TLVParser pdolParser = new TLVParser(pdolIn);
						Vector<TLVElement> pdolv = pdolParser.parseDOL();
						ByteArrayOutputStream bout = new ByteArrayOutputStream();
						for (int i = 0; i < pdolv.size(); i++) {
							TLVElement one = pdolv.elementAt(i);
							bout.write(PBOCConstants.get(one.getTag(), one.getLength()));
						}
						byte[] data = bout.toByteArray();
						bout = new ByteArrayOutputStream();
						bout.write((byte) 0x83);
						bout.write(data.length);
						bout.write(data);
						mPDOL = bout.toByteArray();
					}else{
						mPDOL = PDOL;
					}
					ret = mSmartCardReader.getProcessingOptions(mPDOL);
					ByteArrayInputStream in = new ByteArrayInputStream(ret);
					parser = new TLVParser(in);
					TLVElement tree = parser.parseTLVElement();
					byte[] value = tree.getValue();
					// value组成为AIP(应用交互特征长度)+AFL(应用文件定位器);
					byte[] aflData = new byte[value.length - AIP_LENGTH];
					System.arraycopy(value, AIP_LENGTH, aflData, 0,
							aflData.length);
					ApplicationFileLocator afl = new ApplicationFileLocator(
							aflData);
					ArrayList<ApplicationFileLocatorEntry> aflList = afl
							.getApplicationFileLocatorEntry();
					ArrayList<String> tags = new ArrayList<String>();
					tags.add(PBOCContext.TAG_PAN);
					tags.add(PBOCContext.TAG_EXPIREDATE);
					tags.add(PBOCContext.TAG_ICNUMBER);
					tags.add(PBOCContext.TAG_TRACK2);
					tags.add(PBOCContext.TAG_CDOL2);
					for (int i = 0; i < aflList.size(); i++) {
						ApplicationFileLocatorEntry entry = aflList.get(i);
						for (int j = entry.getFistRecordNum(); j <= entry
								.getLastRecordNum(); j++) {
							byte[] data = mSmartCardReader.readRecord(
									entry.getSFI(), j);
							in = new ByteArrayInputStream(data);
							parser = new TLVParser(in);
							tree = parser.parseTLVElement();
							// 主账号
							if (TLVUtils.isContainsTag(tree, tags)) {
								PBOCContext.add(tree);
							}
						}
					}
				} catch (TLVParserException e) {
					e.printStackTrace();
					throw new SmartCardException("智能卡返回数据有误！");
				} finally {
					mSmartCardReader.powerDown();
				}
			} else {
				throw new SmartCardException("不是PCOC卡！");
			}
			byte[] value = PBOCContext.get(PBOCContext.TAG_PAN);
			if (null != value) {
				String accountNumber = StringEncode.hexEncode(value);
				if (accountNumber.endsWith("F")) {
					accountNumber = accountNumber.substring(0,
							accountNumber.length() - 1);
				}
				return accountNumber;
			} else {
				throw new SmartCardException("读取银行卡信息失败！");
			}
		} finally {
			mSmartCardReader.powerDown();
			mLock.unlock();
		}
	}

	/**
	 * 执行外部认证及终止交易
	 * 
	 * @param arpc
	 *            认证数据
	 * @throws TLVParserException
	 *             解析失败
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws UnionPayPaymentException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 */
	private void externalAuthAndGac2(String arpc) throws TLVParserException,
			IOException, SmartCardException {
		if (!StringEncode.isValid(arpc)) {
			throw new SmartCardException("外部认证参数错误！");
		}
		byte[] data = StringEncode.hexDecode(arpc);
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		TLVParser parser = new TLVParser(in);
		Vector<TLVElement> v = parser.parseAllTLVElement();
		for (int i = 0; i < v.size(); i++) {
			PBOCContext.add(v.elementAt(i));
		}
		byte[] authData = PBOCContext.get(PBOCContext.TAG_AUTHDATA);
		if (null == authData) {
			throw new SmartCardException("外部认证数据错误！");
		}
		mSmartCardReader.externalAuth(authData);
		byte[] lastAc = PBOCContext.getLastApplicationCryptogram();
		byte[] pdol2 = PBOCContext.get(PBOCContext.TAG_CDOL2);
		byte[] ac = null;
		if (null != pdol2) {
			String temp = StringEncode.hexEncode(pdol2);
			if (temp.endsWith("9F4E14")) {
				ac = new byte[TRANSACTION_DATA2.length + 20];
			}
		}
		if (null == ac) {
			ac = new byte[TRANSACTION_DATA2.length];
		}
		System.arraycopy(TRANSACTION_DATA2, 0, ac, 0, TRANSACTION_DATA2.length);
		System.arraycopy(lastAc, 0, ac, 2, ac.length - 2);
		mSmartCardReader.generateAC((byte) 0x40, ac);
	}

	/**
	 * 执行发卡行脚本
	 * 
	 * @param dcData
	 *            借贷记应用数据
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws UnionPayPaymentException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 * @throws TLVParserException
	 */
	public void runScript(String dcData) throws SmartCardException,
			IOException, TLVParserException {
		mSmartCardReader.setChannel(0);
		externalAuthAndGac2(dcData);
		byte[] script = PBOCContext.get(PBOCContext.TAG_SCRIPT);
		if (null == script) {
			throw new SmartCardException("脚本不存在！");
		}
		try {
			mScriptState = 1;
			mSmartCardReader.sendDirectAPDU(script);
			mScriptState = 2;
		} finally {
			mSmartCardReader.powerDown();
		}
	}

	/**
	 * 获取电子现金余额
	 * 
	 * @return 电子现金余额
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws UnionPayPaymentException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 */
	public String getCashBalance(byte[] selectResult) throws SmartCardException, IOException {
		try {
			mSmartCardReader.powerDown();
			mSmartCardReader.powerUp();
			if (selectResult != null) {
				return mSmartCardReader.getCashBanlance();
			} else {
				throw new SmartCardException("不是PBOC卡");
			}
		} finally {
			mSmartCardReader.powerDown();
		}
	}
	
	/**
	 * 获取电子现金余额上限
	 * 
	 * @return 电子现金余额上限
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws UnionPayPaymentException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 */
	public String getCashBalanceLimit(byte[] selectResult) throws SmartCardException, IOException {
		try {
			mSmartCardReader.powerDown();
			mSmartCardReader.powerUp();
			if (selectResult != null) {
				return mSmartCardReader.getCashBanlanceLimit();
			} else {
				throw new SmartCardException("不是PBOC卡");
			}
		} finally {
			mSmartCardReader.powerDown();
		}
	}
	
	/**
	 * 获取交易记录格式
	 * 
	 * @return 交易记录格式
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws UnionPayPaymentException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 */
	public String getTransRecordsFormats(byte[] selectResult) throws SmartCardException, IOException {
		try {
			mSmartCardReader.powerDown();
			mSmartCardReader.powerUp();
			if (selectResult != null) {
				return mSmartCardReader.getTransRecordsFormats();
			} else {
				throw new SmartCardException("不是PBOC卡");
			}
		} finally {
			mSmartCardReader.powerDown();
		}
	}
	
	/**
	 * 获取交易记录
	 * 
	 * @return 交易记录
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws UnionPayPaymentException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 */
	public byte[] getTransRecords(byte[] selectResult, int sfi, int recordNum) throws SmartCardException, IOException {
		try {
			mSmartCardReader.powerDown();
			mSmartCardReader.powerUp();
			if (selectResult != null) {
				return mSmartCardReader.readRecord(sfi, recordNum);
			} else {
				throw new SmartCardException("不是PBOC卡");
			}
		} finally {
			mSmartCardReader.powerDown();
		}
	}

	/**
	 * 获取最后一次借贷记应用数据
	 * 
	 * @return 借贷记应用数据
	 * @throws UnionPayPaymentException
	 *             没有上次借贷记应用数据
	 */
	public String getLastDCData() throws SmartCardException {
		String lastDCData = PBOCContext.getLastDCData();
		if (!StringEncode.isValid(lastDCData)) {
			throw new SmartCardException("上下文书局错误");
		}
		String len = Integer.toString(PBOCContext.TAG_RUNSCRIPTRESULTLENGTH);
		if (len.length() < 2) {
			len = "0" + len;
		}
		String tagAndLength = PBOCContext.TAG_RUNSCRIPTRESULT + len;
		if (-1 == lastDCData.indexOf(tagAndLength)) {
			String state = "" + getScriptState();
			for (int i = state.length(); i < (PBOCContext.TAG_RUNSCRIPTRESULTLENGTH * 2); i++) {
				state += "0";
			}
			lastDCData += tagAndLength + state;
			PBOCContext.setLastDCData(lastDCData);
		}
		return lastDCData;
	}

	public String getScriptResultDCData() throws SmartCardException {
		String lastDcData = getLastDCData();
		String tmp = lastDcData.substring(22);
		PBOCContext.setLastDCData(tmp);
		return tmp;
	}
	
	/**
	 * 执行外部认证
	 * 
	 * @param arpc
	 *            认证数据
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws UnionPayPaymentException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 *  add by liuy           
	 */
	public void externalAuth(String arpc) throws IOException, SmartCardException {
		if (!StringEncode.isValid(arpc)) {
			throw new SmartCardException("外部认证参数错误！");
		}
		byte[] ARC = { 0x30 , 0x30 };
		
		byte[] data = StringEncode.hexDecode(arpc);
		
		byte[] authData = new byte[data.length + 2];
		
		System.arraycopy(data, 0, authData, 0, data.length);
		
		System.arraycopy(ARC, 0, authData, data.length, ARC.length);
		
		mSmartCardReader.externalAuth(authData);
	}
	
	/**
	 * 执行带有MAC的put data命令，修改电子现金金额
	 * 
	 * @param amount
	 *            电子现金金额
	 * @param mac
	 *            MAC值        
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws UnionPayPaymentException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 *  add by liuy           
	 */
	public void putData(String amount, String mac) throws IOException, SmartCardException {
		if (!StringEncode.isValid(amount)) {
			throw new SmartCardException("电子现金金额错误！");
		}
		if (!StringEncode.isValid(mac)) {
			throw new SmartCardException("MAC错误！");
		}
		
		byte[] cashAmount = StringEncode.hexDecode(Utility.formateAccountBalance(amount));
		byte[] currentMac = StringEncode.hexDecode(mac);
		
		byte[] data = new byte[cashAmount.length + currentMac.length];
		
		System.arraycopy(cashAmount, 0, data, 0, cashAmount.length);
		
		System.arraycopy(currentMac, 0, data, cashAmount.length, currentMac.length);
		
		mSmartCardReader.putDataMAC(data);
	}
	
	/**
	 * 执行第二次GenerateAC,终止交易
	 * 
	 * @throws TLVParserException
	 *             解析失败
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws UnionPayPaymentException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 */
	public void generateAC2() throws TLVParserException,
			IOException, SmartCardException {
		
		byte[] pdol2 = PBOCContext.get(PBOCContext.TAG_CDOL2);
		byte[] ac = null;
		if (null != pdol2) {
			String temp = StringEncode.hexEncode(pdol2);
			if (temp.endsWith("9F4E14")) {
				ac = new byte[TRANSACTION_DATA2.length + 20];
			}
		}
		if (null == ac) {
			ac = new byte[TRANSACTION_DATA2.length];
		}
		System.arraycopy(TRANSACTION_DATA2, 0, ac, 0, TRANSACTION_DATA2.length);
		mSmartCardReader.generateAC((byte) 0x40, ac);
	}
	
	/**
	 * 执行GPO并且读取卡内记录信息,并返回待验证的静态数据
	 * 
	 * @param selectResult
	 *            选择电子现金应用的返回apdu
	 * @return 待验证的静态数据
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws UnionPayPaymentException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 *  add by liuy
	 */
	public byte[] doGpoAndReadRecords(byte[] selectResult) throws IOException, SmartCardException {
		byte[] VerSData = null;
		try {
			mLock.lock();
			mSmartCardReader.powerDown();
			mSmartCardReader.powerUp();
			if (selectResult != null) {
				try {
					byte[] ret = null;
					ByteArrayInputStream bin = new ByteArrayInputStream(selectResult);
					TLVParser parser = new TLVParser(bin);
					Vector<TLVElement> v = parser.parseAllTLVElement();
					for (int i = 0; i < v.size(); i++) {
						PBOCContext.add(v.elementAt(i));
					}
					byte[] pdol = PBOCContext.get(PBOCContext.TAG_PDOL);
					if(null != pdol){
						ByteArrayInputStream pdolIn = new ByteArrayInputStream(pdol);
						TLVParser pdolParser = new TLVParser(pdolIn);
						Vector<TLVElement> pdolv = pdolParser.parseDOL();
						ByteArrayOutputStream bout = new ByteArrayOutputStream();
						for (int i = 0; i < pdolv.size(); i++) {
							TLVElement one = pdolv.elementAt(i);
							bout.write(PBOCConstants.get(one.getTag(), one.getLength()));
						}
						byte[] data = bout.toByteArray();
						bout = new ByteArrayOutputStream();
						bout.write((byte) 0x83);
						bout.write(data.length);
						bout.write(data);
						mPDOL = bout.toByteArray();
					}else{
						mPDOL = PDOL;
					}
					ret = mSmartCardReader.getProcessingOptions(mPDOL);
					ByteArrayInputStream in = new ByteArrayInputStream(ret);
					parser = new TLVParser(in);
					TLVElement tree = parser.parseTLVElement();
					byte[] value = tree.getValue();
					// value组成为AIP(应用交互特征长度)+AFL(应用文件定位器);
					byte[] aflData = new byte[value.length - AIP_LENGTH];
					System.arraycopy(value, AIP_LENGTH, aflData, 0,
							aflData.length);
					ApplicationFileLocator afl = new ApplicationFileLocator(
							aflData);
					ArrayList<ApplicationFileLocatorEntry> aflList = afl
							.getApplicationFileLocatorEntry();
//					ArrayList<String> tags = new ArrayList<String>();
//					tags.add(PBOCContext.TAG_PAN);
//					tags.add(PBOCContext.TAG_EXPIREDATE);
//					tags.add(PBOCContext.TAG_ICNUMBER);
//					tags.add(PBOCContext.TAG_TRACK2);
//					tags.add(PBOCContext.TAG_CDOL2);
				
					for (int i = 0; i < aflList.size(); i++) {
						ApplicationFileLocatorEntry entry = aflList.get(i);
						if(entry.getOfflineAuthNum() == (byte) 0x01) {	
							byte[] Data=mSmartCardReader.readRecord(entry.getSFI(),entry.getFistRecordNum());
							
							in = new ByteArrayInputStream(Data);
							parser = new TLVParser(in);
							TLVElement tlv =parser.parseParentTLVElement(); 
							VerSData = tlv.getValue();
						}
						for (int j = entry.getFistRecordNum(); j <= entry
								.getLastRecordNum(); j++) {
							byte[] data = mSmartCardReader.readRecord(
									entry.getSFI(), j);
							in = new ByteArrayInputStream(data);
							parser = new TLVParser(in);
							tree = parser.parseTLVElement();
							PBOCContext.add(tree);
						}
					}
				} catch (TLVParserException e) {
					e.printStackTrace();
					throw new SmartCardException("智能卡返回数据有误！");
				} finally {
					mSmartCardReader.powerDown();
				}
			} else {
				throw new SmartCardException("不是PCOC卡！");
			}
		} finally {
			mSmartCardReader.powerDown();
			mLock.unlock();
		}
		return VerSData;
	}
	
	/**
	 * 获取应用密文ARQC
	 * 
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws UnionPayPaymentException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 */
	public String generateAC(String amount, byte[] selectResult, byte p1) throws SmartCardException,
			IOException {
		mSmartCardReader.powerDown();
		mSmartCardReader.powerUp();
		if (selectResult != null) {
			byte[] data;
			if (null != amount) {
				data = new byte[TRANSACTION_DATA.length];
				String am = Utility.formateAccountBalance(amount);
				byte[] ret = StringEncode.hexDecode(am);
				System.arraycopy(TRANSACTION_DATA, 0, data, 0, data.length);
				System.arraycopy(ret, 0, data, 0, ret.length);
			} else {
				data = TRANSACTION_DATA;
			}
			byte[] ac = mSmartCardReader.generateAC(p1, data);
			PBOCContext.setLastApplicationCryptogram(data);
			byte[] dcData = new byte[DC_DATA.length];
			System.arraycopy(DC_DATA, 0, dcData, 0, dcData.length);
			System.arraycopy(ac, 2, dcData, 14, 1);
			System.arraycopy(ac, 5, dcData, 3, 8);
			System.arraycopy(ac, 13, dcData, 18, (ac[1] - 11));
			System.arraycopy(ac, 3, dcData, 40, 2);
			System.arraycopy(data, 0, dcData, 67, 6);
			return StringEncode.hexEncode(dcData);
		} else {
			throw new SmartCardException("不是PBOC卡！");
		}
	}
	
	/**
	 * 执行qpboc流程的GPO，并读取卡内记录信息完成脱机消费
	 * 
	 * @param selectResult
	 *            选择电子现金应用的返回apdu
	 * @param amount
	 *            脱机消费的金额
	 * @return 待验证的静态数据
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws UnionPayPaymentException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 *  add by liuy
	 */
	public byte[] doQpbocGpoAndReadRecords(byte[] selectResult, String amount) throws IOException, SmartCardException {
		byte[] VerSData = null;
		try {
			mLock.lock();
			mSmartCardReader.powerDown();
			mSmartCardReader.powerUp();
			if (selectResult != null) {
				try {
					byte[] ret = null;
					ByteArrayInputStream bin = new ByteArrayInputStream(selectResult);
					TLVParser parser = new TLVParser(bin);
					Vector<TLVElement> v = parser.parseAllTLVElement();
					for (int i = 0; i < v.size(); i++) {
						PBOCContext.add(v.elementAt(i));
					}
					byte[] pdol = PBOCContext.get(PBOCContext.TAG_PDOL);
					if(null != pdol){
						ByteArrayInputStream pdolIn = new ByteArrayInputStream(pdol);
						TLVParser pdolParser = new TLVParser(pdolIn);
						Vector<TLVElement> pdolv = pdolParser.parseDOL();
						ByteArrayOutputStream bout = new ByteArrayOutputStream();
						for (int i = 0; i < pdolv.size(); i++) {
							TLVElement one = pdolv.elementAt(i);
							if(one.getTag().equals("9F66")) {
								bout.write(StringEncode.hexDecode("24000000"));
							} else {
								bout.write(PBOCConstants.get(one.getTag(), one.getLength()));
							}
						}
						byte[] data = bout.toByteArray();
						
						byte[] am = StringEncode.hexDecode(amount);
						System.arraycopy(am, 0, data, 4, am.length);
						
						bout = new ByteArrayOutputStream();
						bout.write((byte) 0x83);
						bout.write(data.length);
						bout.write(data);
						mPDOL = bout.toByteArray();
					}else{
						mPDOL = PDOL;
					}
					ret = mSmartCardReader.getProcessingOptions(mPDOL);
					ByteArrayInputStream in = new ByteArrayInputStream(ret);
					parser = new TLVParser(in);
					TLVElement tree = parser.parseTLVElement();
					PBOCContext.add(tree);
					byte[] appData = PBOCContext.get(PBOCContext.TAG_BANKAPPLICATIONDATA);
					byte[] mAIP = PBOCContext.get(PBOCContext.TAG_AIP);
					byte[] mAFL = PBOCContext.get(PBOCContext.TAG_AFL);
					byte[] mARQC = PBOCContext.get(PBOCContext.TAG_APPLICATIONCRYPTOGRAM);
					if(appData != null && mAIP != null && mAFL != null && mARQC != null) {
						if(appData[4] == (byte) 0x90) {
							ApplicationFileLocator afl = new ApplicationFileLocator(
									mAFL);
							ArrayList<ApplicationFileLocatorEntry> aflList = afl
									.getApplicationFileLocatorEntry();
							for(int i = 0; i < aflList.size(); i++) {
								ApplicationFileLocatorEntry entry = aflList.get(i);
								if(entry.getOfflineAuthNum() == (byte) 0x01) {
									byte[] Data=mSmartCardReader.readRecord(entry.getSFI(),entry.getFistRecordNum());
									
									in = new ByteArrayInputStream(Data);
									parser = new TLVParser(in);
									TLVElement tlv =parser.parseParentTLVElement(); 
									VerSData = tlv.getValue();
								}
								for(int j = entry.getFistRecordNum(); j <= entry.getLastRecordNum(); j++) {
									byte[] data = mSmartCardReader.readRecord(entry.getSFI(), j);
									in = new ByteArrayInputStream(data);
									parser = new TLVParser(in);
									tree = parser.parseTLVElement();
									PBOCContext.add(tree);
								}
							}
						} else {
							throw new SmartCardException("ic卡不支持电子现金脱机消费！");
						}
					} else {
						throw new SmartCardException("ic卡不支持电子现金脱机消费！");
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw new SmartCardException("IC卡返回数据有误！");
				} finally {
					mSmartCardReader.powerDown();
				}
			} else {
				throw new SmartCardException("不是PCOC卡！");
			}
		} finally {
			mSmartCardReader.powerDown();
			mLock.unlock();
		}
		return VerSData;
	}
	
	/**
	 * 执行内部认证
	 * 
	 * @throws IOException
	 *             打开SD卡文件失败
	 * @throws UnionPayPaymentException
	 *             无SD卡或非智能SD卡，IC卡返回错误等
	 *  add by YCY
	 */
	public byte[] doInternalAuth(byte[] selectResult, byte[] unpredictableNum) throws IOException, SmartCardException {
		
		Log.v("ycy","doInternalAuth");
		try {
			mLock.lock();
			mSmartCardReader.powerDown();
			mSmartCardReader.powerUp();
			if (selectResult != null) {
				byte[] DData = mSmartCardReader.internalAuth(unpredictableNum);
				Log.v("ycy",StringEncode.hexEncode(DData));
			
				ByteArrayInputStream in = new ByteArrayInputStream(DData);
				TLVParser parser = new TLVParser(in);
				TLVElement tlv;
	
				try {
					tlv = parser.parseParentTLVElement();
					return tlv.getValue();
				} catch (TLVParserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new SmartCardException("IC卡返回数据有误！");
				}
//				Dda.signedDData = StringEncode.hexEncode(tlv.getValue());
			} else {
				throw new SmartCardException("不是PCOC卡！");
			}	
		} finally {
			mSmartCardReader.powerDown();
			mLock.unlock();
		}
	}
}
