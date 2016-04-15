package com.mcg.mpos.activities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import com.mcg.mpos.R;
import com.mcg.mpos.application.BaseApplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity implements OnClickListener {
	
	private static final String TAG = "MainActivity";
	private Button loginBtn;
	private Button cancelBtn;
	private EditText usrEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		loginBtn = (Button)findViewById(R.id.login_btn);
		loginBtn.setOnClickListener(this);
		cancelBtn = (Button)findViewById(R.id.cancel_btn);
		cancelBtn.setOnClickListener(this);
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

	/**
	 * 1. confirm btn
	 * 2. cancel btn
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()) {
		case R.id.login_btn:
			EditText mTextView = (EditText)findViewById(R.id.user_name);
			String userName = mTextView.getText().toString();
			mTextView = (EditText)findViewById(R.id.password);
			String password = mTextView.getText().toString();
			//TODO yz: change to app internal storage
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				Log.i(TAG, "Data Directory: " + this.getExternalFilesDir(""));
				File file = new File(this.getExternalFilesDir(null), BaseApplication.INFO_FILE);
				BufferedReader br;
				String readline = "";
				Intent intent = null;
				boolean found = false;
				try {
					br = new BufferedReader(new FileReader(file));
					while((readline = br.readLine()) != null) {
						Log.d(TAG, "readline: " + readline);
						if(readline.substring(0,  3).equals(userName)) {
							found = true;
							if(userName.equals("000")
									&& password.equals(readline.substring(readline.length() - 6, readline.length()))) {//admin
								intent = new Intent(this, SalersManagementActivity.class);
							} else {//saler
								if(password.equals(readline.substring(readline.length() - 6, readline.length()))) {
									intent = new Intent(this, HomeActivity.class);
								}
							}
							break;
						}
					}
					br.close();
					if(!found) {
						if(userName.equals("000")) {
							intent = new Intent(this, SalersManagementActivity.class);
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(intent != null) {
					startActivity(intent);
				} else {
					Log.e(TAG, "username or password is wrong");
				}
			}
			break;
			
		case R.id.cancel_btn:
			super.onBackPressed();
			break;
		}
	}
}
