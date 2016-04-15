package com.mcg.mpos.tasks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Vector;

import com.mcg.mpos.application.BaseApplication;
import com.mcg.mpos.carddata.BankCardInfo;
import com.mcg.mpos.exceptions.SmartCardException;
import com.mcg.mpos.pbocKeyGenerator.ARPCGen;
import com.mcg.mpos.pbocKeyGenerator.MACGen;
import com.mcg.mpos.pbocKeyGenerator.SessionKeyGen;
import com.mcg.mpos.pbocKeyGenerator.UDKGen;
import com.mcg.mpos.smartcard.UPPCardAppInterface;
import com.mcg.mpos.smartcard.data.PBOCConstants;
import com.mcg.mpos.smartcard.data.PBOCContext;
import com.mcg.mpos.tlv.TLVElement;
import com.mcg.mpos.tlv.TLVParser;
import com.mcg.mpos.utils.StringEncode;
import com.mcg.mpos.utils.Utility;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;



/**
 * 执行圈存流程的类
 * @author liuy
 *
 */
public class ChargeTask {
	
	private static final String TAG = "ChargeTask";
	
	private ChargeListner mListner;
	
	private String MDKAC = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";
	private String MDKMAC = "77777777777777777777777777777777";
	private String MDKENC = "33333333333333333333333333333333";
	
	public ChargeTask(ChargeListner listner) {
		mListner = listner;
	}
	
	public void startExecute(String amount)
	{
		ReChargeTask task = new ReChargeTask(amount);
		task.execute();
	}
	
	private class ReChargeTask extends AsyncTask {
		private UPPCardAppInterface inter;
		private String amount;
		
		private byte[] UDKAC = null;
		private byte[] UDKMAC = null;
		
		private byte[] sessionKeyAC = null;
		private byte[] sessionKeyMAC = null;
		private byte[] generateArqc = null;
		private byte[] generateMac = null;	
		
		private String curCash;
		
		private final byte[] PUTDATA = { 0x04, (byte)0xDA, (byte)0x9F, (byte)0x79, (byte)0x0A };
		
		public ReChargeTask(String amount) {  
	        super();   
	        this.amount = amount;
	    } 
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Object doInBackground(Object... objects) {
			String errorMessage = null;
			try {	
				Log.v("ReChargeTask: ", "doInBackground");
				inter = UPPCardAppInterface.getInstance();
					
				byte[] selectResult = inter.selectPBOCApplicaticon();
				byte[] verSData = inter.doGpoAndReadRecords(selectResult);
				
				byte[] mPAN = PBOCContext.get(PBOCContext.TAG_PAN);
				byte[] mPANserialNumber = PBOCContext.get(PBOCContext.TAG_ICNUMBER);
				
				if(null == mPAN) {
					throw new SmartCardException("读取银行卡信息失败！");
				}
				if(null == mPANserialNumber) {
					throw new SmartCardException("读取银行卡信息失败！");
				}
				//DDA
				doDda(selectResult, verSData);
				
				String accountNumber = getCardNumberNotWithF(mPAN);	
				String cashBalance = inter.getCashBalance(selectResult);
				String cashBalanceLimit = inter.getCashBalanceLimit(selectResult);
				
				Integer tmp_amount = Integer.valueOf(amount);
				Integer tmp_curCash = Integer.valueOf(cashBalance);
				tmp_amount += tmp_curCash;		
				amount = tmp_amount.toString();
				
				String mdcData = inter.generateAC(amount, selectResult, (byte) 0x80);
				
				BankCardInfo mbankcardInfo = BaseApplication.getInstance().getmCurrentBankCardInfo();
				if(null != mbankcardInfo) { 
					 if(accountNumber.equalsIgnoreCase(mbankcardInfo.getBankCardNum())) {
						 String curcashBalance = inter.getCashBalance(selectResult);
						 mbankcardInfo.setCashBalance(curcashBalance);
					 } else {
						 throw new Exception("不是同一张卡!");
					 }
				}
				
				byte[] dcdata = StringEncode.hexDecode(mdcData);
				ByteArrayInputStream bin = new ByteArrayInputStream(dcdata);
				TLVParser parser = new TLVParser(bin);
				Vector<TLVElement> v = parser.parseAllTLVElement();
				for(int i = 0; i < v.size(); i++) {
					PBOCContext.add(v.elementAt(i));
				}
				
				byte[] mARQC = PBOCContext.get(PBOCContext.TAG_APPLICATIONCRYPTOGRAM);
				byte[] mATC = PBOCContext.get(PBOCContext.TAG_ATC);
				
				generateUDK(accountNumber, mPANserialNumber);
				generateSessionKey(mATC);
				generateARQC(mARQC);
				generateMAC(amount, mATC, mARQC);
				
				Log.v("chargeTask", StringEncode.hexEncode(mPAN));
				Log.v("chargeTask", StringEncode.hexEncode(mPANserialNumber));
				Log.v("chargeTask", cashBalance);
				Log.v("chargeTask", StringEncode.hexEncode(mARQC));
				Log.v("chargeTask", StringEncode.hexEncode(mATC));
				Log.v("chargeTask", StringEncode.hexEncode(UDKAC));
				Log.v("chargeTask", StringEncode.hexEncode(UDKMAC));
				Log.v("chargeTask", StringEncode.hexEncode(generateArqc));
				Log.v("chargeTask", StringEncode.hexEncode(generateMac));
				
				if(generateArqc != null) {
					inter.externalAuth(StringEncode.hexEncode(generateArqc));
					if(generateMac != null) {
						Log.d(TAG, "amount: " + amount);
						inter.putData(amount , StringEncode.hexEncode(generateMac));
						
						inter.generateAC2();
						
						curCash = inter.getCashBalance(selectResult);
						curCash = Utility.formatMoney(curCash);
						
					} else {
						Log.v("ChargeCash:", "generateMAC is null");
						throw new Exception("generateMAC is null");
					}
				} else {
					Log.v("ChargeCash:", "generateArqc is null");
					throw new Exception("generateArqc is null");
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
				mListner.onChargeSuccess(curCash);
			} else {
				
				mListner.onChargeFail(errorMessage);
				
			}
		}

		@SuppressWarnings("unchecked")
		@Override
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
		
		private void generateUDK(String PAN, byte[] mPANserialNumber) {
			UDKAC = UDKGen.generateUDK(PAN, StringEncode.hexEncode(mPANserialNumber), MDKAC);
			UDKMAC = UDKGen.generateUDK(PAN, StringEncode.hexEncode(mPANserialNumber), MDKMAC);
		}
		
		private void generateSessionKey(byte[] mATC) throws Exception {
			if(UDKAC != null && UDKMAC != null) {
				sessionKeyAC = SessionKeyGen.generateSessionKey(StringEncode.hexEncode(mATC), StringEncode.hexEncode(UDKAC));
				sessionKeyMAC = SessionKeyGen.generateSessionKey(StringEncode.hexEncode(mATC), StringEncode.hexEncode(UDKMAC));
			} else {
				throw new Exception("UDKAC or UDKMAC is null!");
			}
		}

		private void generateARQC(byte[] mARQC) throws Exception {
			if(sessionKeyAC != null && mARQC != null) {
				generateArqc = ARPCGen.generateARPC("3030",	StringEncode.hexEncode(mARQC), StringEncode.hexEncode(sessionKeyAC));	
			} else {
				throw new Exception("sessionKeyAC or mARQC is null!");
			}
		}
		private void generateMAC(String amount, byte[] mATC, byte[] mARQC) throws Exception {
			String am = Utility.formateAccountBalance(amount);
			byte[] mamount = StringEncode.hexDecode(am);
			
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			
			bout.write(PUTDATA);
			bout.write(mATC);
			bout.write(mARQC);
			bout.write(mamount);
			
			byte[] initData = bout.toByteArray();
			if(initData != null && sessionKeyMAC != null) {
				generateMac = MACGen.generateMAC(StringEncode.hexEncode(initData), StringEncode.hexEncode(sessionKeyMAC));
			} else {
				throw new Exception("initData or generateMac is null!");
			}
		}
		
		private void doDda(byte[] selectResult, byte[] verSData) throws Exception {
			
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
			// 捷德卡
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
			
			byte[] signedDData = inter.doInternalAuth(selectResult, unpredictableNum);
			
			myDda.setSignedDData(StringEncode.hexEncode(signedDData));
			
			myDda.doDda();
		}
	}
		
	/**
	 * 圈存完成后的接口
	 * @author liuy
	 *
	 */
	public static interface ChargeListner
	{
		/**
		 * 当读取卡片成功后所调用的方法
		 * @param cardInfo
		 */
		public abstract void onChargeSuccess(String cash);
		
		/**
		 * 当卡片读取失败时调用的方法
		 * @param e
		 */
		public abstract void onChargeFail(String e);
	}
}

