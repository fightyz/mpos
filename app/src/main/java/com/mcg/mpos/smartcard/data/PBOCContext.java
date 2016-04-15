package com.mcg.mpos.smartcard.data;


import java.util.Hashtable;

import com.mcg.mpos.tlv.TLVElement;
import com.mcg.mpos.utils.StringEncode;

/**
 * PBOC上下文信息类
 */
public class PBOCContext {

	public static final String TAG_PAN = "5A";
	public static final String TAG_TRACK2 = "57";
	public static final String TAG_EXPIREDATE = "5F24";
	public static final String TAG_ICNUMBER = "5F34";
	public static final String TAG_AUTHDATA = "91";
	public static final String TAG_SCRIPT = "86";
	public static final String TAG_RUNSCRIPTRESULT = "DF31";
	public static final String TAG_CDOL2 = "8D";
	public static final String TAG_PDOL = "9F38";
	public static final int TAG_RUNSCRIPTRESULTLENGTH = 5;
	public static final String TAG_APPLICATIONCRYPTOGRAM = "9F26";
	public static final String TAG_ATC = "9F36";
	public static final String TAG_BANKAPPLICATIONDATA = "9F10";
	public static final String TAG_AIP = "82";
	public static final String TAG_AFL = "94";
	public static final int TAG_APPLICATIONCRYPTOGRAMTOTALLEN = 22;
	private static Hashtable<String, byte[]> mContex = new Hashtable<String, byte[]>();
	private static byte[] mLastApplicationCryptogram;
	private static String mLastDCData;
	
	//add
	public static final String TAG_ISSUER_BANK_CERT = "90"; //发卡行公钥证书
	public static final String TAG_ISSUER_BANK_PUBKEY_REMAINS = "92"; //发卡行公钥余项		
	public static final String TAG_ISSUER_BANK_PUBKEY_EXPONENT = "9F32"; //发卡行公钥指数
	public static final String TAG_SDA = "93"; //静态数据认证标签
	public static final String TAG_ICCARD_CERT = "9F46"; //IC卡公钥证书
	public static final String TAG_ICCARD_PUBKEY_EXPONENT = "9F47"; //IC卡公钥指数
	public static final String TAG_ICCARD_PUBKEY_REMAINS = "9F48"; //IC卡公钥余项
	public static final String TAG_SIGNED_DDATA = "9F4B"; //签名的动态应用数据标签
	public static final String TAG_UNPREDICTABLE_NUM = "9F37"; //不可预知数标签
	public static final int  TAG_UNPREDICTABLE_NUM_LEN = 4;  ////不可预知数标签长度
	
	public static final String TAG_TRANSACTION_DATE = "9A";
	public static final String TAG_TRANSACTION_TIME = "9F21";
	public static final String TAG_AUTHORIZED_CASH = "9F02";
	public static final String TAG_OTHER_CASH = "9F03";
	public static final String TAG_TERMINAL_NATION_CODE = "9F1A";
	public static final String TAG_TRANSACTION_CURRENCY_CODE = "5F2A";
	public static final String TAG_MERCHANT_NAME = "9F4E";
	public static final String TAG_TRANSACTION_TYPE = "9C";
	public static final String TAG_TRANSACTION_RECORD_ENTRY = "9F4D";
	
	/**
	 * 添加项,如果标签已经存在，会覆盖以前数据
	 * 
	 * @param tag
	 *            标签
	 * @param data
	 *            数据
	 */
	public static void add(String tag, byte[] data) {
		mContex.put(tag, data);
	}
	
	/**
	 * 删除项
	 * 
	 * @param tag
	 *            标签
	 * @param data
	 *            数据
	 */
	public static void del(String tag) {
		mContex.remove(tag);
	}
	/**
	 * 删除所有项
	 * 
	 */
	public static void delall() {
		mContex.clear();
	}

	/**
	 * 获取特定标签数据
	 * 
	 * @param tag
	 *            标签
	 * @return 标签所对应的值，如果标签不存在则返回null
	 */
	public static byte[] get(String tag) {
		return mContex.get(tag);
	}

	/**
	 * 添加数据
	 * 
	 * @param tlv
	 *            TLV节点
	 */
	public static void add(TLVElement tlv) {
		if (null == tlv) {
			return;
		}
		if (tlv.getChildren() != null && !tlv.getChildren().isEmpty()) {
			int childNumber = tlv.getChildren().size();
			for (int i = 0; i < childNumber; i++) {
				TLVElement child = (TLVElement) tlv.getChildren().get(i);
				add(child);
			}
		} else {
			add(tlv.getTag(), tlv.getValue());
		}
	}

	/**
	 * 获取上次基于PBOC交易密文
	 * 
	 * @return 上次基于PBOC交易密文
	 */
	public static byte[] getLastApplicationCryptogram() {
		return mLastApplicationCryptogram;
	}

	/**
	 * 设置上次基于PBOC交易密文
	 * 
	 * @param lastApplicationCryptogram
	 *            上次基于PBOC交易密文
	 */
	public static void setLastApplicationCryptogram(
			byte[] lastApplicationCryptogram) {
		mLastApplicationCryptogram = lastApplicationCryptogram;
	}

	public static String getPBOCTagValue(String tag) {
		byte[] value = get(tag);
		String temp = null;
		if (null != value) {
			temp = StringEncode.hexEncode(value);
			if (temp.endsWith("F")) {
				temp = temp.substring(0, temp.length() - 1);
			}
		}
		return temp;
	}

	/**
	 * 获取上次交易借贷记应用数据
	 *
	 * @return 上次交易借贷记应用数据
	 */
	public static String getLastDCData() {
		return mLastDCData == null ? "" : mLastDCData;
	}

	/**
	 * 设置上次交易借贷记应用数据
	 *
	 * @param dcData
	 *            上次交易借贷记应用数据
	 */
	public static void setLastDCData(String dcData) {
		mLastDCData = dcData;
	}
}
