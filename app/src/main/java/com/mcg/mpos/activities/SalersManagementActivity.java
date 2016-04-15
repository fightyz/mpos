package com.mcg.mpos.activities;

import com.mcg.mpos.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SalersManagementActivity extends Activity implements OnClickListener {
	
	private static final String TAG = "SalersManagementActivity";
	
	private Button btnAdd;
	private Button btnDel;
	private Button btnQue;
	Context mContext = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_salers_management);
		
		btnAdd = (Button)findViewById(R.id.buttonAdd);
		btnDel = (Button)findViewById(R.id.buttonDelete);
		btnQue = (Button)findViewById(R.id.buttonQuery);
		
		btnAdd.setOnClickListener(this);
		btnDel.setOnClickListener(this);
		btnQue.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		switch(v.getId()) {
		case R.id.buttonAdd:
			intent.setClass(this, AddSalerActivity.class);
			break;
			
		case R.id.buttonDelete:
			intent.setClass(this, DeleteSalerActivity.class);
			break;
			
		case R.id.buttonQuery:
			intent.setClass(this, QueryActivity.class);
			break;
		}
		startActivity(intent);
	}
}
