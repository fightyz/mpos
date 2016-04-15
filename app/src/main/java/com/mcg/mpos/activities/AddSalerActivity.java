package com.mcg.mpos.activities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.mcg.mpos.R;
import com.mcg.mpos.application.BaseApplication;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddSalerActivity extends Activity implements OnClickListener {
	
	private static final String TAG = "AddActivity";
	
	private Button btnSave;
	private Button btnCancel;
	private EditText number;
	private EditText name;
	private EditText password;
	public static final String FILE_NAME = "info.txt";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add);
		
		number = (EditText)findViewById(R.id.edtNum);
		name = (EditText)findViewById(R.id.edtName);
		password = (EditText)findViewById(R.id.edtPassword);
		
		btnSave = (Button)findViewById(R.id.buttonSave);
		btnCancel = (Button)findViewById(R.id.buttonCancel);
		
		btnSave.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.buttonSave:
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				if(number.getText().toString().equals("")
						|| name.getText().toString().equals("")
						|| password.getText().toString().equals("")){
					showInfo("Please input number & name & password");
				} else {
					// A record is like this: "001 yz 111111"
					String content = number.getText().toString() + " "
									+ name.getText().toString() + " "
									+ password.getText().toString() + "\n";
					File file = new File(this.getExternalFilesDir(null), BaseApplication.INFO_FILE);
					BufferedWriter bw;
					try {
						bw = new BufferedWriter(new FileWriter(file, true));
						bw.append(content);
						bw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					showInfo("Add saler success");
				}
			}
			break;
			
		case R.id.buttonCancel:
			onBackPressed();
			break;
		}
	}
	
	private void showInfo(String str)
	{
	    Toast.makeText(AddSalerActivity.this, str, Toast.LENGTH_LONG).show();
	}
}
