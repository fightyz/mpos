package com.mcg.mpos.activities;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import com.mcg.mpos.R;
import com.mcg.mpos.apdus.ReadBinaryBuilder;
import com.mcg.mpos.application.BaseApplication;
import com.mcg.mpos.carddata.BankCardInfo;
import com.mcg.mpos.carddata.PbocTransRecords;
import com.mcg.mpos.fragments.IndicatorFragment;
import com.mcg.mpos.fragments.LoadFragment;
import com.mcg.mpos.fragments.PayFragment;
import com.mcg.mpos.fragments.QueryFragment;
import com.mcg.mpos.smartcard.MifareSmartCardReader;
import com.mcg.mpos.smartcard.UPPCardAppInterface;
import com.mcg.mpos.tasks.QueryTask;
import com.mcg.mpos.tasks.ReadCATask;
import com.mcg.mpos.utils.ArrayUtils;
import com.mcg.mpos.utils.ConvertUtils;
import com.mcg.mpos.utils.Utility;
import com.mcg.mpos.widgets.LoadingDialog;
import com.mobilesecuritycard.openmobileapi.Channel;
import com.mobilesecuritycard.openmobileapi.NfcContactLessBaseActivity;
import com.mobilesecuritycard.openmobileapi.Reader;
import com.mobilesecuritycard.openmobileapi.SEService;
import com.mobilesecuritycard.openmobileapi.Session;

public class HomeActivity extends NfcContactLessBaseActivity {

	private static final String TAG = "HomeActivity";
	
	// Applet's AID which stored the CA Certificate
	private byte[] AID = {(byte)0x01, 0x02, 0x03, 0x04, 0x05, 0x01};
	private ReadBinaryBuilder rbBuilder;
	
	private BankCardInfo mBankCardInfo;
	
	public static Fragment[] mFragments;
	
	public static int currentFragment;
	
	private LoadingDialog mLoading;
	
	private TextView txView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_home);
		
		mFragments = new Fragment[4];
		mFragments[0] = new IndicatorFragment();
		mFragments[1] = new QueryFragment();
		mFragments[2] = new LoadFragment();
		mFragments[3] = new PayFragment();
		getFragmentManager().beginTransaction().add(R.id.fragment_container, mFragments[0]).commit();
	}
	
	/**
	 * When service connected, read CA from SD
	 */
	@Override
	public void serviceConnected(SEService service) {
		if(service == null) {
			Log.e(TAG, "SEService is null");
			return;
		}
		
		if(!service.isConnected()) {
			Log.e(TAG, "SEService is not connected");
			return;
		}
		
		BaseApplication.getInstance().setSeService(service);
		
		ReadCATask readCATask = new ReadCATask(service);
		readCATask.startExecute();
		
	}
	
	@Override
	protected void onNfcErrors(Exception e) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onCardConnected() {
		
		try {
			// Get Contactless Reader & Open a session
			MifareSmartCardReader.getInstance().prepareSend(null);
		} catch (IOException e) {
			e.printStackTrace();
		}		
		// If current tab is Query, then read the card info
		if(currentFragment == 1) {
			txView = (TextView) mFragments[currentFragment].getView().findViewById(R.id.query_area_img);
			// 菊花进度条
			startLoadingDialog();
			
			QueryTask queryTask = new QueryTask(new QueryTask.QueryListner() {
				@Override
				public void onQuerySuccess(BankCardInfo bankcardInfo) {
					// TODO Auto-generated method stub
					stopLoadingDialog();
					txView.append("\n");
					txView.append("卡号: " + bankcardInfo.getBankCardNumForShow() + "\n");
					txView.append("余额: " + bankcardInfo.getCashBalance() + "\n");
					txView.append("余额上限: " + bankcardInfo.getCashBalanceLimit() + "\n");
					ArrayList<PbocTransRecords> list = bankcardInfo.getRecordList();
					if(list == null) {
						txView.append("不支持交易日志!\n");
					} else {
						txView.append("\n");
						for(int i = 0; i < list.size(); i++) {
							PbocTransRecords record = list.get(i);
							txView.append("日期: " + record.getTransactionDate() + "\n");
							txView.append("金额: " + record.getTransactionCash() + "\n");
							txView.append("类型: " + record.getTransactionTypeCode() + "\n");
						}
					}
				}
				@Override
				public void onQueryFail(String e) {
					// TODO Auto-generated method stub
					stopLoadingDialog();
					txView.append("\n");
					txView.append("error: " + e + "\n");
				}
				
			});
			queryTask.startExecute();
		} else {// If current tab is not Home, then let its Fragment to handle
			
		}
	}
	
	private void startLoadingDialog(){
		if(mLoading == null) {
			mLoading = LoadingDialog.getInstance(this);
			Log.d(TAG, "getInstance success");
		}
		mLoading.setMessage("Querying...");
		mLoading.show();
    }
     
    private void stopLoadingDialog(){
        if (mLoading != null){
        	mLoading.dismiss();
        	mLoading = null;
        }
    }
	
}
