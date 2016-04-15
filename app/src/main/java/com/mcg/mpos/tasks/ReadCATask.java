package com.mcg.mpos.tasks;

import com.mcg.mpos.apdus.ReadBinaryBuilder;
import com.mcg.mpos.application.BaseApplication;
import com.mcg.mpos.utils.ArrayUtils;
import com.mcg.mpos.utils.ConvertUtils;
import com.mobilesecuritycard.openmobileapi.Channel;
import com.mobilesecuritycard.openmobileapi.Reader;
import com.mobilesecuritycard.openmobileapi.SEService;
import com.mobilesecuritycard.openmobileapi.Session;

import android.os.AsyncTask;
import android.util.Log;

/**
 * 从SD卡中读取CA证书
 * @author yangzhou
 *
 */
public class ReadCATask {
	
	private SEService seService;
	
	private ReadBinaryBuilder rbBuilder;
	
	// Applet's AID which stored the CA Certificate
	private byte[] AID = {(byte)0x01, 0x02, 0x03, 0x04, 0x05, 0x01};
	
	private static final String TAG = "ReadCATask";
	
	public ReadCATask(SEService se) {
		this.seService = se;
	}
	
	public void startExecute() {
		GetCATask getCATask = new GetCATask();
		getCATask.execute();
	}
	
	private class GetCATask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			Reader[] readers = seService.getReaders();
			Reader sdReader = getSDSE(readers);
			if(sdReader != null) {
				if(sdReader.isSecureElementPresent()) {
					try {
						Session s = sdReader.openSession();
						Channel c = s.openBasicChannel(AID);
						Log.i(TAG, "select response: "
								+ ConvertUtils.bytesToString(c.getSelectResponse()));
						rbBuilder = new ReadBinaryBuilder();
						short offset = 0;
						byte[] rapdu;
						byte[] ca = null;
						do {
							rbBuilder.setTarget((byte)0x01, offset);
							Log.i(TAG, "READ_BINARY = " + ConvertUtils.bytesToString(rbBuilder.getAPDUBytes()));
							rapdu = c.transmit(rbBuilder.getAPDUBytes());
							offset += (short)rapdu[1];
							byte[] ca_tmp = new byte[rapdu.length - 2];
							System.arraycopy(rapdu, 2, ca_tmp, 0, rapdu.length - 2);
							ca = ArrayUtils.conbine(ca, ca_tmp);
						}while(rapdu[0] == 0x71);
						s.close();
						Log.i(TAG, "CA: " + ConvertUtils.bytesToString(ca));
						//TODO verify 
						Log.i(TAG, "Modulus: " + ConvertUtils.bytesToString(
								BaseApplication.getInstance().getmCerKeys().getCAModulus()));
						
					} catch(Exception e) {
						e.printStackTrace();
					}
				} else {
					Log.e(TAG, "SD SE is not present");
				}
			} else {
				Log.e(TAG, "Can't find SD SE");
			}
			return null;
		}
		
		private Reader getSDSE(Reader[] readers) {
			if(readers == null || readers.length < 1) {
				return null;
			}
			for(Reader r : readers) {
				if(r.getName().startsWith("SDCHINA")) {
					return r;
				}
			}
			return null;
		}
	}
}
