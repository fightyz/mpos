package com.mobilesecuritycard.openmobileapi;

import com.mobilesecuritycard.openmobileapi.SEService.CallBack;
import com.mobilesecuritycard.openmobileapi.service.terminals.nfc.NFCContactlessCardReader;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;


public abstract class NfcContactLessBaseActivity extends Activity implements
	CallBack{	
	private static final String TAG = "NfcContactLessBaseActivity";
	private NfcAdapter mAdapter;//用于存储NfC适配器对象，用于开启NFC前台模式   
	private PendingIntent mPendingIntent;//用于设置Intent    
	private IntentFilter[] mFilters;//用于设置要捕获的NDEF_DISCOVERED的Intent的MIME类型    
	private String[][] mTechLists;//用于设置要捕获的Technology
	
	private Tag mNfcTag = null; //保存检测到的tag
	protected SEService seService = null;
	private Context mContext;
	private CallBack mCallBack;
	
	/* 如果在onCreate函数中调用initNfcAdapter()出错，
	 * 调用此函数，默认对出错信息不作任何处理，
	 * 如果需要处理，请重写改函数
	 */
	protected abstract void onNfcErrors(Exception e);
	
	/**
	 * After get NFC Tag
	 */
	protected abstract void onCardConnected();
	
	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg)
		{
			Log.i(TAG, "[yz] handler: " + Thread.currentThread().getId());
			seService = new SEService(mContext, mCallBack);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v(TAG,"oncreate");
		super.onCreate(savedInstanceState);
		mContext = this;
		mCallBack = this;
//		seService = new SEService(this, this);
		mHandler.sendEmptyMessageDelayed(0, 100);
		try {
			initNfcAdapter();
		} catch (Exception e) {
			onNfcErrors(e);
		}
	}
	
	private void initNfcAdapter() throws Exception
    {
		Log.v(TAG, "initNfcAdapter");
    	mAdapter=NfcAdapter.getDefaultAdapter(this);//获得NFC适配器对象
 		mPendingIntent=PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
 	    //创建PendingIntent用于捕获NFC标签发现的Intent
 		IntentFilter ndef=new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);//构建一个Intent，设置捕获NDEF_DICOVERED的Intent
 		try {            
 			ndef.addDataType("*/*");        
 		}catch (MalformedMimeTypeException e) 
 		{            
 			throw new Exception("MIME类型错误");     
 		}
 		mFilters=new IntentFilter[] {ndef};
 		mTechLists=new String[][]{ 	new String[]{"android.nfc.tech.IsoDep"}, 
									new String[]{NfcA.class.getName()}
 								  };//设置要捕获的NFC Technology
    }
	
	private void disableNfc()
	{
		if (mAdapter!=null) 
			mAdapter.disableForegroundDispatch(this);
	}
	
	private void enableNfc()
	{
		if (mAdapter!=null) 
			mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters,mTechLists); 
	}
	
	@Override    
	public void onNewIntent(Intent intent) 
	{ 
		resolveIntent(intent);
	}
	
	// 在onResume()之后这个actvity才成为前台程序
	// 因此要在onResume中调用enableForegroundDispatch
	@Override
    protected void onResume() 
    {
    	super.onResume();
    	enableNfc();
//    	resolveIntent(getIntent());
    }
   
    private void resolveIntent(Intent intent) 
    {
    	if(intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED) ||
    	   intent.getAction().equals(NfcAdapter.ACTION_TECH_DISCOVERED) ||
    	   intent.getAction().endsWith(NfcAdapter.ACTION_TAG_DISCOVERED))
		{
			mNfcTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			NFCContactlessCardReader.getInstance().setTag(mNfcTag);
			Log.v(TAG, "pboc:" + mNfcTag.toString());
			onCardConnected();
		}
	}
    
    // 如果在onResume中调用了enableForegroundDispatch，那么就要在onPause中调用disableForegroundDispatch
	@Override
    protected void onPause() 
    {
    	super.onPause();
    	disableNfc();
    }
	
	@Override
	protected void onDestroy() {
		if (seService != null && seService.isConnected()) {
			seService.shutdown();
		}
		super.onDestroy();
	}
	
	@Override
	public void serviceConnected(SEService service) {
		// TODO Auto-generated method stub
	}
	
}
