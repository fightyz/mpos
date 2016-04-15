package com.mcg.mpos.fragments;

import com.mcg.mpos.R;
import com.mcg.mpos.activities.HomeActivity;
import com.mcg.mpos.application.BaseApplication;
import com.mcg.mpos.carddata.BankCardInfo;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class QueryFragment extends Fragment {

	private View mParent;
	
	private HomeActivity mActivity;
	
	private TextView mText;
	
	private Button mButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		HomeActivity.currentFragment = 1;
		View view = inflater.inflate(R.layout.fragment_query, container, false);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mActivity = (HomeActivity)getActivity();
		mParent = getView();
		mText = (TextView) mParent.findViewById(R.id.query_area_img);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
	}

}
