package com.mcg.mpos.widgets;

import com.mcg.mpos.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;


public class LoadingDialog extends ProgressDialog {
	
	private static final String TAG = "LoadingDialog";

	private static LoadingDialog mLoadingDialog = null;
	
	private Context mContext;
	private String msg;
	private TextView content;

	private LoadingDialog(Context context) {
		super(context, R.style.Theme_dialog);
		this.mContext = context;
		this.msg = "Processing...";
	}
	
	public synchronized static LoadingDialog getInstance(Context context) {
		if(mLoadingDialog == null) {
			mLoadingDialog = new LoadingDialog(context);
		}
		return mLoadingDialog;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View contentView = inflater.inflate(R.layout.progress_dialog_layout, null);
		content = (TextView) contentView.findViewById(R.id.content);
		content.setText(msg);
		setCanceledOnTouchOutside(false);
		setContentView(contentView);
	}

	public void setMessage(CharSequence c) {
		if(content == null) {
			msg = c.toString();
		} else {
			content.setText(c);
		}
	}

}