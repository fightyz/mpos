package com.mcg.mpos.tasks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import com.mcg.mpos.carddata.BankCardInfo;
import com.mcg.mpos.carddata.PbocTransRecords;
import com.mcg.mpos.exceptions.SmartCardException;
import com.mcg.mpos.smartcard.UPPCardAppInterface;
import com.mcg.mpos.smartcard.data.PBOCContext;
import com.mcg.mpos.utils.StringEncode;
import com.mcg.mpos.utils.Utility;

import android.os.AsyncTask;
import android.util.Log;




public class QueryTask {
	
	private QueryListner mListner;
		
	public QueryTask(QueryListner listner) {
		mListner = listner;
	}
	
	public void startExecute()
	{
		GetCardInfoTask task = new GetCardInfoTask();
		task.execute();
	}
	
	private class GetCardInfoTask extends AsyncTask {
		private UPPCardAppInterface inter;	
		private BankCardInfo mbankCardInfo;
		
		public GetCardInfoTask() {  
	        super();
	    } 
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
//			startLoadingDialog();
		}

		@Override
		protected Object doInBackground(Object... objects) {
			String errorMessage = null;
			try {	
				Log.v("GetCardInfoTask: ", "doInBackground");
				inter = UPPCardAppInterface.getInstance();
					
				byte[] selectResult = inter.selectPBOCApplicaticon();
				inter.doGpoAndReadRecords(selectResult);
				
				byte[] mPAN = PBOCContext.get(PBOCContext.TAG_PAN);
				byte[] mPANserialNumber = PBOCContext.get(PBOCContext.TAG_ICNUMBER);
				
				if(null == mPAN) {
					throw new SmartCardException("读取银行卡信息失败！");
				}
				if(null == mPANserialNumber) {
					throw new SmartCardException("读取银行卡信息失败！");
				}
				
				String accountNumber = getCardNumberNotWithF(mPAN);	
				String cashBalance = inter.getCashBalance(selectResult);
				cashBalance = Utility.formatMoney(cashBalance);
				String cashBalanceLimit = inter.getCashBalanceLimit(selectResult);
				cashBalanceLimit = Utility.formatMoney(cashBalanceLimit);
				
				mbankCardInfo = new BankCardInfo();
				mbankCardInfo.setBankCardNum(accountNumber);
				mbankCardInfo.setCashBalance(cashBalance);
				mbankCardInfo.setBankCardNumForShow(Utility.formatCard(Utility
						.protectBankCardNo(accountNumber)));
				mbankCardInfo.setCashBalanceLimit(cashBalanceLimit);
//				bankCardInfo.setDcData(inter.getDCData(null));
				getTransRecoards(selectResult);
				
				Log.v("GetCardInfoTask", accountNumber);
				Log.v("GetCardInfoTask", StringEncode.hexEncode(mPANserialNumber));
				Log.v("GetCardInfoTask", cashBalance);
				Log.v("GetCardInfoTask", cashBalanceLimit);			
				
			} catch (Exception e) {
				e.printStackTrace();
				errorMessage = e.getMessage();
			}
			return errorMessage;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(Object o) {
			super.onPostExecute(o);
			String errorMessage = (String) o;
			if (errorMessage == null) {
				mListner.onQuerySuccess(mbankCardInfo);
			} else {
				
				mListner.onQueryFail(errorMessage);
				
			}
		}

		@SuppressWarnings("unchecked")
		protected void onProgressUpdate(Object... values) {
			super.onProgressUpdate(values);
		}
		
		private String getCardNumberNotWithF(byte[] mPAN) throws SmartCardException {
			String accountNumber;
			if (null != mPAN) {
				accountNumber = StringEncode.hexEncode(mPAN);
				if (accountNumber.endsWith("F")) {
					accountNumber = accountNumber.substring(0,
							accountNumber.length() - 1);
				}
				return accountNumber;
			} else {
				throw new SmartCardException("读取银行卡信息失败！");
			}
		}
		
		private void getTransRecoards(byte[] selectResult) throws SmartCardException, IOException {
			byte[] entry = PBOCContext.get(PBOCContext.TAG_TRANSACTION_RECORD_ENTRY);
			if(!(entry == null || entry.length == 0)) {
				PBOCContext.del(PBOCContext.TAG_TRANSACTION_RECORD_ENTRY);
				ArrayList<byte[]> mTransRecords = new ArrayList<byte[]>();
				byte recordcount = entry[1];
				byte sfi = entry[0];
				
				String recordFormat = inter.getTransRecordsFormats(selectResult);
				
				for(byte i=1; i <= recordcount; i++) {
					try {
						byte[] transrecord = inter.getTransRecords(selectResult, sfi, i);
						mTransRecords.add(transrecord);
					} catch(Exception e) { }	
				}
				
				if(mTransRecords != null) {
					ArrayList<PbocTransRecords> list = new ArrayList<PbocTransRecords>();
					for(int i = 0; i < mTransRecords.size(); i++) {
						PbocTransRecords record = new PbocTransRecords();
						record.parseRecordsFormats(recordFormat);
						record.parseTransactionRecord(mTransRecords.get(i));
						list.add(record);
					}
					mbankCardInfo.setRecordList(list);
				}
				
			}
		}
	}
		
	/**
	 * 查询完成后的接口
	 * @author liuy
	 *
	 */
	public static interface QueryListner
	{
		/**
		 * 当读取卡片成功后所调用的方法
		 * @param cardInfo
		 */
		public abstract void onQuerySuccess(BankCardInfo bankcardInfo);
		
		/**
		 * 当卡片读取失败时调用的方法
		 * @param e
		 */
		public abstract void onQueryFail(String e);
	}
}
