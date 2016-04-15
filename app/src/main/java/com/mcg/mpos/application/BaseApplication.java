package com.mcg.mpos.application;

import java.io.File;
import java.io.IOException;

import com.mcg.mpos.carddata.BankCardInfo;
import com.mcg.mpos.carddata.CerKeys;
import com.mobilesecuritycard.openmobileapi.SEService;

import android.app.Application;
import android.nfc.Tag;

public class BaseApplication extends Application {
	
	private static BaseApplication mSelf;
	
	private Tag mTag;
	
	private SEService seService;
	
	private BankCardInfo mCurrentBankCardInfo;
	
	private CerKeys mCerKeys;
	
	public static final String INFO_FILE = "info.txt";
	
	/**
	 * 获取单例对象
	 * @return 一个BaseApplication对象
	 */
	public static BaseApplication getInstance() {
		return mSelf;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		mCerKeys = new CerKeys();
		mSelf = this;
		File file = new File(this.getExternalFilesDir(null), INFO_FILE);
		if(!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public void exit() {
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	public SEService getSeService() {
		return seService;
	}

	public void setSeService(SEService seService) {
		this.seService = seService;
	}

	public BankCardInfo getmCurrentBankCardInfo() {
		return mCurrentBankCardInfo;
	}

	public void setCurrentBankCardInfo(BankCardInfo mBankCardInfo) {
		this.mCurrentBankCardInfo = mBankCardInfo;
	}

	public CerKeys getmCerKeys() {
		return mCerKeys;
	}

	public void setmCerKeys(CerKeys mCerKeys) {
		this.mCerKeys = mCerKeys;
	}
}
