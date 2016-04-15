package com.mcg.mpos.fragments;

import com.mcg.mpos.R;
import com.mcg.mpos.activities.HomeActivity;
import com.mcg.mpos.tasks.DeductTask;
import com.mcg.mpos.widgets.ChooseDialog;
import com.mcg.mpos.widgets.LoadingDialog;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PayFragment extends Fragment {
	
	private static final String TAG = "PayFragment";

	private View mParent;
	
	private HomeActivity mActivity;
	
//	private TitleView mTitle;
	
	private TextView mText;
	private EditText edit;
	
	private String amount;
	private KeyboardView keyboardview;
	
	private LoadingDialog mLoadingDialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_pay, container, false);
		HomeActivity.currentFragment = 3;
		edit = (EditText) view.findViewById(R.id.edit);
		edit.setInputType(InputType.TYPE_NULL);
        
		
        Keyboard keyboard = new Keyboard(this.getActivity(), R.layout.keyboard);
        
        keyboardview = (KeyboardView) view.findViewById(R.id.keyboard_view);
        keyboardview.setKeyboard(keyboard);
        keyboardview.setEnabled(true); 
        
        keyboardview.setOnKeyboardActionListener(listner);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mActivity = (HomeActivity)getActivity();
		mParent = getView();
		mText = (TextView) mParent.findViewById(R.id.edit);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
	}
	
	private OnKeyboardActionListener listner = new OnKeyboardActionListener() {

		@Override
		public void onPress(int primaryCode) {}

		@Override
		public void onRelease(int primaryCode) {}

		@Override
		public void onText(CharSequence text) {}

		@Override
		public void swipeLeft() {}

		@Override
		public void swipeRight() {}

		@Override
		public void swipeDown() {}

		@Override
		public void swipeUp() {}
		
		@Override
		public void onKey(int primaryCode, int[] keyCodes) {
			Editable editable = edit.getText();
			int start = edit.getSelectionStart();
			
			if(primaryCode == Keyboard.KEYCODE_CANCEL) {
				String msg = "Bank Card Info";
				ChooseDialog chooseDialog = new ChooseDialog(mActivity, msg, new ListenerYes(), new ListenerNo());
				chooseDialog.showChosseDialog();
			} else if(primaryCode == Keyboard.KEYCODE_DELETE) {
				if (editable != null && editable.length() > 0) {  
                    if (start > 0) {  
                            editable.delete(start - 1, start);  
                    }  
				}
			} else if(primaryCode == Keyboard.KEYCODE_DONE) {
				mActivity.onBackPressed();
			} else {
				String num = editable.toString();
				if(num.equals("0") && !Character.toString((char) primaryCode).equals(".")) {
					
				} else if (num.contains(".") && Character.toString((char) primaryCode).equals(".")) {
					
				} else if(num.length() > 3 && num.charAt(editable.toString().length() - 3) == '.') {
					
				} else {
					editable.insert(start, Character.toString((char) primaryCode));
				}
			}
		}
	};
	
	class ListenerYes implements OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			//菊花进度条
			startLoadingDialog();
			DeductTask payTask = new DeductTask(new DeductTask.DeductListner() {
				
				@Override
				public void onDeductSuccess(String cash) {
					stopLoadingDialog();
					showToastMsg("pay success: " + cash);
				}
				
				@Override
				public void onDeductFail(String e) {
					stopLoadingDialog();
					showToastMsg("pay failed: " + e);
				}
			});
			
			amount = mText.getText().toString();
    	   	Float tmp =Float.valueOf(amount);
    	   	
   			tmp *= 100;
   			amount = "" + tmp.longValue();
   			Log.i(TAG, "amount: " + amount);
			payTask.startExecute(amount);
		}
	}
	
	class ListenerNo implements OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.cancel();
			showToastMsg("不消费");
		}
		
	}
	
	public void showToastMsg(String msg) {
		Toast.makeText(this.getActivity(), msg, Toast.LENGTH_LONG).show();
	}
	
	private void startLoadingDialog(){
        if (mLoadingDialog == null){
        	mLoadingDialog = LoadingDialog.getInstance(mActivity);
        }
        mLoadingDialog.setMessage("Paying...");
        mLoadingDialog.show();
    }
     
    private void stopLoadingDialog(){
        if (mLoadingDialog != null){
        	mLoadingDialog.dismiss();
        	mLoadingDialog = null;
        }
    }
}
