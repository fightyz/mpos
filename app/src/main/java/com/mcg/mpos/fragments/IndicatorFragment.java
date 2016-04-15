package com.mcg.mpos.fragments;

import com.mcg.mpos.R;
import com.mcg.mpos.activities.HomeActivity;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class IndicatorFragment extends Fragment implements OnClickListener {
	
	private View mParent;
	
	private HomeActivity mActivity;
	
//	private TitleView mTitle;
	
	private TextView mText;
	
	private Button queryBtn;
	private Button loadBtn;
	private Button payBtn;
	private Button logoutBtn;
	
//	private Fragment mFragments[];

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_indicator, container, false);
		HomeActivity.currentFragment = 0;
		queryBtn = (Button) view.findViewById(R.id.btn_indicator_query);
		loadBtn = (Button) view.findViewById(R.id.btn_indicator_load);
		payBtn = (Button) view.findViewById(R.id.btn_indicator_pay);
		logoutBtn = (Button) view.findViewById(R.id.btn_logout);
		queryBtn.setOnClickListener(this);
		loadBtn.setOnClickListener(this);
		payBtn.setOnClickListener(this);
		logoutBtn.setOnClickListener(this);
		return view;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mActivity = (HomeActivity)getActivity();
		mParent = getView();
		
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btn_indicator_query:
			mActivity.getFragmentManager().beginTransaction().replace(R.id.fragment_container, HomeActivity.mFragments[1]).addToBackStack(null).commit();
			break;
			
		case R.id.btn_indicator_load:
			mActivity.getFragmentManager().beginTransaction().replace(R.id.fragment_container, HomeActivity.mFragments[2]).addToBackStack(null).commit();
			break;
			
		case R.id.btn_indicator_pay:
			mActivity.getFragmentManager().beginTransaction().replace(R.id.fragment_container, HomeActivity.mFragments[3]).addToBackStack(null).commit();
			break;
			
		case R.id.btn_logout:
			mActivity.onBackPressed();
			break;
		}
	}
}
