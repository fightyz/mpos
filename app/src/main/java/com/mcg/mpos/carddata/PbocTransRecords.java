package com.mcg.mpos.carddata;

import java.util.ArrayList;

import com.mcg.mpos.smartcard.data.PBOCContext;
import com.mcg.mpos.utils.StringEncode;
import com.mcg.mpos.utils.Utility;

public class PbocTransRecords {
	/**
	 * 交易日期
	 */
	public String mTransactionDate;
	
	/**
	 * 交易时间
	 */
	public String mTransactionTime;
	
	/**
	 * 交易金额
	 */
	public String mTransactionCash;
	
	/**
	 * 交易其它金额
	 */
	public String mTransctionOtherCash;
	
	/**
	 * 终端国家代码
	 */
	protected String mTerminalNationCode;
	
	/**
	 * 交易货币类型
	 */
	protected String mCurrencyTypeCode;
	
	/**
	 * 商户名称
	 */
	public String mMerchantName;

	/**
	 * 交易类型代码
	 */
	protected String mTranactionTypeCode;
	
	/**
	 * 保存交易日志记录格式
	 */
	private ArrayList<TransactionFormats> mTransationRecordFormats;
	
	/**
	 * 对应一个交易记录格式的一项
	 * @author CaiYingjue
	 *
	 */
	protected static class TransactionFormats
	{
		/**
		 * 标签
		 */
		public String mTag;
		
		/**
		 * 长度
		 */
		public int mLength;
	}
	
	/**
	 * 设置交易记录格式
	 * @param recordFormat	通过Get-Data命令获取的交易记录格式
	 */
	public void parseRecordsFormats(String formatStrings)
	{
		mTransationRecordFormats = new ArrayList<TransactionFormats>();
		int index = 0;
		
		while (index < formatStrings.length()) {
			String tag = formatStrings.substring(index, index + 2);
			int value = 0;
			if(tag.charAt(1) == 'F') {	//若是xFxx类型的标签
				tag = formatStrings.substring(index, index + 4);
				value = Integer.valueOf(formatStrings.substring(index + 4, index + 6));
				index += 6;
			}
			else {
				value = Integer.valueOf(formatStrings.substring(index + 2, index + 4));
				index += 4;
			}
			
			TransactionFormats format = new TransactionFormats();
			format.mTag = tag;
			format.mLength = value;
			mTransationRecordFormats.add(format);
		}		
	}
	
	/**
	 * 根据交易记录的内容，将数据解析并填入对应域
	 * @param recordValue	读出的一条交易记录的数据
	 */
	public void parseTransactionRecord(byte recordValue[])
	{
		int index = 0;
		for(int i=0; i < mTransationRecordFormats.size(); i++) {
			String tag = mTransationRecordFormats.get(i).mTag;
			int length = mTransationRecordFormats.get(i).mLength;
			byte[] temp = new byte[length];
			if(tag.equals(PBOCContext.TAG_TRANSACTION_DATE)) {//获取交易日期
				System.arraycopy(recordValue, index, temp, 0, length);
				String tem = StringEncode.hexEncode(temp);
				mTransactionDate = tem.substring(0, 2) + "-" + tem.substring(2, 4) + "-" + tem.substring(4);
//				mTransactionDate = dataUtils.parseBytes(recordValue, index, length, DataFormat.DATA_FORMAT_DATE);
			}
			else if(tag.equals(PBOCContext.TAG_TRANSACTION_TIME)) {//获取交易时间
				System.arraycopy(recordValue, index, temp, 0, length);
				String tem = StringEncode.hexEncode(temp);
				mTransactionTime = tem.substring(0, 2) + ":" + tem.substring(2, 4) + ":" + tem.substring(4);
			}
			else if(tag.equals(PBOCContext.TAG_AUTHORIZED_CASH)) {//获取授权金额
				System.arraycopy(recordValue, index, temp, 0, length);
				mTransactionCash = StringEncode.hexEncode(temp);
			}
			else if(tag.equals(PBOCContext.TAG_OTHER_CASH)) {//获取其它金额
				System.arraycopy(recordValue, index, temp, 0, length);
				mTransctionOtherCash = StringEncode.hexEncode(temp);
			}
			else if(tag.equals(PBOCContext.TAG_TERMINAL_NATION_CODE)) {//获取终端国家代码
				System.arraycopy(recordValue, index, temp, 0, length);
				mTerminalNationCode = StringEncode.hexEncode(temp);
			}
			else if(tag.equals(PBOCContext.TAG_TRANSACTION_CURRENCY_CODE)) {//获取交易货币代码
				System.arraycopy(recordValue, index, temp, 0, length);
				mCurrencyTypeCode = StringEncode.hexEncode(temp);
			}
			else if(tag.equals(PBOCContext.TAG_MERCHANT_NAME)) {//获取商户名称
				System.arraycopy(recordValue, index, temp, 0, length);
				mMerchantName = StringEncode.hexEncode(temp);
			}
			else if(tag.equals(PBOCContext.TAG_TRANSACTION_TYPE)) {//获取交易类型
				System.arraycopy(recordValue, index, temp, 0, length);
				mTranactionTypeCode = StringEncode.hexEncode(temp);
			}
			
			index += length;
		}
	}
	
	/**
	 * 获取交易日期
	 * @return
	 */
	public String getTransactionDate() {
		return mTransactionDate + " " + mTransactionTime;
	}
	
	/**
	 * 获取交易金额
	 * @return
	 */
	public String getTransactionCash() {
		return Utility.formatMoney(mTransactionCash);
	}
	
	/**
	 * 获取交易类型
	 * @return
	 */
	public String getTransactionTypeCode() {
		return mTranactionTypeCode;
	}
}
