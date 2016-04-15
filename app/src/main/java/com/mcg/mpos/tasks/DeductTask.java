package com.mcg.mpos.tasks;

import com.mcg.mpos.exceptions.SmartCardException;
import com.mcg.mpos.smartcard.UPPCardAppInterface;
import com.mcg.mpos.smartcard.data.PBOCConstants;
import com.mcg.mpos.smartcard.data.PBOCContext;
import com.mcg.mpos.utils.StringEncode;
import com.mcg.mpos.utils.Utility;

import android.os.AsyncTask;
import android.util.Log;

public class DeductTask {
	private DeductListner mListner;
	
	public DeductTask(DeductListner listner) {
		mListner = listner;
	}
	
	public void startExecute(String amount)
	{
		DeductMoneyTask task = new DeductMoneyTask(amount);
		task.execute();
	}
	
	private class DeductMoneyTask extends AsyncTask {
		private UPPCardAppInterface inter;	
		private String amount;
		private String curCash;
		private byte[] verSData;
		
		public DeductMoneyTask(String amount) {  
	        super();
	        this.amount = amount;
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
				Log.v("DeductMoneyTask: ", "doInBackground");
				inter = UPPCardAppInterface.getInstance();
					
				byte[] selectResult = inter.selectPBOCApplicaticon();
				
				String cashBalance = inter.getCashBalance(selectResult);
				String cashBalanceLimit = inter.getCashBalanceLimit(selectResult);
//				String mdcData = inter.getARQC(selectResult);
				String am = Utility.formateAccountBalance(amount);
				if(cashBalance.compareTo(am) >= 0) {
					
					Log.v("GetCardInfoTask", cashBalance + " " + am);
					
					verSData = inter.doQpbocGpoAndReadRecords(selectResult, am);
					
					byte[] mPAN = PBOCContext.get(PBOCContext.TAG_PAN);
					
					if(null == mPAN) {
						throw new SmartCardException("读取银行卡信息失败！");
					}
					
					String accountNumber = getCardNumberNotWithF(mPAN);	

					
					curCash = inter.getCashBalance(selectResult);
					curCash = Utility.formatMoney(curCash);
					
					try {
						doFdda(selectResult, verSData);
					} catch(Exception e) {
						e.printStackTrace();
						throw new Exception("FDDA验证失败,请重新刷卡!");
					}
					
					Log.v("GetCardInfoTask", accountNumber);
					Log.v("GetCardInfoTask", cashBalance);
					Log.v("GetCardInfoTask", cashBalanceLimit);	
					
				} else {
					throw new Exception("余额不足！请充值！");
				}
			} catch (Exception e) {
				e.printStackTrace();
				errorMessage = e.getMessage();
			}
			PBOCContext.delall();
			return errorMessage;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(Object o) {
			super.onPostExecute(o);
			String errorMessage = (String) o;
			if (errorMessage == null) {
				mListner.onDeductSuccess(curCash);
			} else {
				
				mListner.onDeductFail(errorMessage);
				
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
		
		private void doFdda(byte[] selectResult, byte[] verSDdata) throws Exception {
			if(verSData == null) {
				throw new Exception("verSData is null");
			}
			
			Dda myDda = new Dda();
			
			// 等待验证的静态应用数据
			myDda.setVerSData(StringEncode.hexEncode(verSData));
			
			myDda.setIssuePKCertificate(StringEncode.hexEncode(PBOCContext
					.get(PBOCContext.TAG_ISSUER_BANK_CERT)));
			myDda.setIssuePKRemainder(StringEncode.hexEncode(PBOCContext
					.get(PBOCContext.TAG_ISSUER_BANK_PUBKEY_REMAINS)));
			myDda.setIssuePKExponent(StringEncode.hexEncode(PBOCContext
					.get(PBOCContext.TAG_ISSUER_BANK_PUBKEY_EXPONENT)));
			myDda.setSignedSData(StringEncode.hexEncode(PBOCContext
					.get(PBOCContext.TAG_SDA)));
			
			myDda.setCaPKExponent("03");
			myDda.setCaPKRemainder("EB374DFC5A96B71D2863875EDA2EAFB96B1B"
					+ "439D3ECE0B1826A2672EEEFA7990286776F8BD989A15141A75C3"
					+ "84DFC14FEF9243AAB32707659BE9E4797A247C2F0B6D99372F384"
					+ "AF62FE23BC54BCDC57A9ACD1D5585C303F201EF4E8B806AFB809D"
					+ "B1A3DB1CD112AC884F164A67B99C7D6E5A8A6DF1D3CAE6D7ED3D5"
					+ "BE725B2DE4ADE23FA679BF4EB15A93D8A6E29C7FFA1A70DE2E54F"
					+ "593D908A3BF9EBBD760BBFDC8DB8B54497E6C5BE0E4A4DAC29E5");
			
			myDda.setEnIcPKCitificate(StringEncode.hexEncode(PBOCContext
					.get(PBOCContext.TAG_ICCARD_CERT)));
			myDda.setIcPKExponent(StringEncode.hexEncode(PBOCContext
					.get(PBOCContext.TAG_ICCARD_PUBKEY_EXPONENT)));
			
			// 加密的IC卡公钥证书：TAG_ICCARD_CERT
			if(PBOCContext.get(PBOCContext.TAG_ICCARD_PUBKEY_REMAINS) == null) {
				myDda.setIcPKRemainder(null);;
			} else {
				myDda.setIcPKRemainder(StringEncode.hexEncode(PBOCContext
					.get(PBOCContext.TAG_ICCARD_PUBKEY_REMAINS)));
			}
			
			byte[] unpredictableNum = PBOCConstants.get(PBOCContext.TAG_UNPREDICTABLE_NUM, 
					PBOCContext.TAG_UNPREDICTABLE_NUM_LEN);
			
			myDda.setUnpredictableNum(StringEncode.hexEncode(unpredictableNum));
			
			myDda.setSignedDData(StringEncode.hexEncode(PBOCContext
					.get(PBOCContext.TAG_SIGNED_DDATA)));
			
			myDda.doDda();		
		}
	}
		
	/**
	 * 圈存完成后的接口
	 * @author liuy
	 *
	 */
	public static interface DeductListner
	{
		/**
		 * 当读取卡片成功后所调用的方法
		 * @param cardInfo
		 */
		public abstract void onDeductSuccess(String cash);
		
		/**
		 * 当卡片读取失败时调用的方法
		 * @param e
		 */
		public abstract void onDeductFail(String e);
	}
}
