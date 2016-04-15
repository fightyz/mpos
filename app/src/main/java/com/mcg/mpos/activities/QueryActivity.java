package com.mcg.mpos.activities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;

import com.mcg.mpos.R;
import com.mcg.mpos.application.BaseApplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class QueryActivity extends Activity {
	private TextView tvResult;
	private Button btnQue;
	private Button btnBack;
	private QueryActivity mContext;
	//private static final String FILE_NAME = "test.txt";
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_query);
		tvResult = (TextView)findViewById(R.id.tvResult);
		btnQue = (Button)findViewById(R.id.buttonQue);
		btnBack = (Button)findViewById(R.id.buttonBack);
		
		mContext = this;
		
		
		ButtonBackListener buttonBackLinstener = new ButtonBackListener();
		btnBack.setOnClickListener(buttonBackLinstener);
		
		ButtonQueListener buttonQueListner = new ButtonQueListener();
		btnQue.setOnClickListener(buttonQueListner);
		
	}
	
	//��������������������ҳ�����ת--------
	class ButtonBackListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			mContext.onBackPressed();			
		}
		
	}
	
	
	//��������������������ȡ�ı�����ݣ�д�뵽SD���ϵ�info.txt����������������������������
	class ButtonQueListener implements OnClickListener{

		@Override			
		public void onClick(View v) {

			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				// ��ȡSDcard·��
				try{
					File file = new File(mContext.getExternalFilesDir(null), BaseApplication.INFO_FILE);
					BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
					String readline = "";
					StringBuffer sb = new StringBuffer();
					while ((readline = bufferedReader.readLine()) != null){
						System.out.println(readline);
						sb.append(readline);
						sb.append("\n");
					}					
					tvResult.setText(sb.toString());
					//System.out.println("��ȡ���ݣ�"+sb.toString());//tostring֮��ԭ����/nʵ�ֵĻ��кϲ���
					showInfo("��ѯ�ɹ���");
					bufferedReader.close();
					
				}catch(Exception e){
					e.printStackTrace();
				}				
			}

		}										
	}
	
	//����������������toast��ʾ��ʾ��Ϣ������������������������
	private void showInfo(String str)
	{
	    // new AlertDialog.Builder(sdcard.this).setMessage(str).show();
	    Toast.makeText(QueryActivity.this, str, Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
