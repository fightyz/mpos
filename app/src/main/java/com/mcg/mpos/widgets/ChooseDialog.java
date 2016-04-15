package com.mcg.mpos.widgets;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class ChooseDialog {

	private Context mContext;
	private String msg;
	private DialogInterface.OnClickListener listenerYes;
	private DialogInterface.OnClickListener listenerNo;
	
	public ChooseDialog(Context context,
						String message,
						DialogInterface.OnClickListener listenerYes,
						DialogInterface.OnClickListener listenerNo) {
		
		this.mContext = context;
		this.msg = message;
		this.listenerYes = listenerYes;
		this.listenerNo = listenerNo;
	}
	
	public void showChosseDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setMessage(msg);
		builder.setCancelable(false);
		builder.setPositiveButton("Yes", listenerYes);
		builder.setNegativeButton("No", listenerNo);
		AlertDialog alert = builder.create();
		alert.show();
	}
}
