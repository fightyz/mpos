package com.mcg.mpos.carddata;

import java.io.Serializable;
import java.util.ArrayList;

public class BankCardInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1706201591256525225L;
	private String mBankCardNumForShow;
	private String mBankCardNum;
	private String mCashBalance;
	private String mCashBalanceLimit;
	// ÕË»§Óà¶î
	private String mBalance;
	// pboc Êý¾Ý
	private String mDcData;

	private ArrayList<PbocTransRecords> mtransRecordsList;

	// private CBinInfo mCbinInfo;


	public String getBankCardNum() {
		return mBankCardNum;
	}

	public void setBankCardNum(String mBankCardNum) {
		this.mBankCardNum = mBankCardNum;
	}

	public String getCashBalance() {
		return mCashBalance;
	}
	
	public void setCashBalance(String mCashBalance) {
		this.mCashBalance = mCashBalance;
	}
	
	public String getCashBalanceLimit() {
		return mCashBalanceLimit;
	}
	
	public void setCashBalanceLimit(String mCashBalanceLimit) {
		this.mCashBalanceLimit = mCashBalanceLimit;
	}

	public String getBalance() {
		return mBalance;
	}

	public void setBalance(String mBalance) {
		this.mBalance = mBalance;
	}

	public String getBankCardNumForShow() {
		return mBankCardNumForShow;
	}

	public void setBankCardNumForShow(String mBankCardNumForShow) {
		this.mBankCardNumForShow = mBankCardNumForShow;
	}

	public String getDcData() {
		return mDcData;
	}

	public void setDcData(String mDcData) {
		this.mDcData = mDcData;
	}

	// public CBinInfo getmCbinInfo() {
	// return mCbinInfo;
	// }
	//
	// public void setmCbinInfo(CBinInfo mCbinInfo) {
	// this.mCbinInfo = mCbinInfo;
	// }
	
	public void setRecordList(ArrayList<PbocTransRecords> list) {
		mtransRecordsList = list;
	}
	
	public ArrayList<PbocTransRecords> getRecordList() {
		return mtransRecordsList;
	}

}
