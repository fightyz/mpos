package com.mcg.mpos.activities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mcg.mpos.R;
import com.mcg.mpos.application.BaseApplication;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DeleteSalerActivity extends ListActivity {
    File file;
    private String[] mListStr;
    ListView mListView = null;
    MyListAdapter myAdapter = null;
    MyListAdapter myNewAdapter = null;
    DeleteSalerActivity arrayList = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    file = new File(this.getExternalFilesDir(null),BaseApplication.INFO_FILE);
    mListStr = File2StringArray(file);
	arrayList = this;
	mListView = getListView();
	myAdapter = new MyListAdapter(this,R.layout.activity_delete);
	setListAdapter(myAdapter);
	super.onCreate(savedInstanceState);
    }
   
    //------将SD卡中的txt文件存放到数组里---------
    private String[] File2StringArray(File file){
    	List<String>list = new ArrayList<String>();
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {			
			// 获取SDcard路径
			try{
				BufferedReader bufferedReader = new BufferedReader(new FileReader(file));				
				String readline = null;
				while ((readline = bufferedReader.readLine()) != null){
					System.out.println(readline);
					list.add(readline);
				}					
				bufferedReader.close();
				
			}catch(Exception e){
				e.printStackTrace();
			}			
		}
		return list.toArray(new String[0]);
	}

    //----定义一个arrayAdapter适配器--------
    public class MyListAdapter extends ArrayAdapter<Object> {
	int mTextViewResourceID = 0;
	private Context mContext;
	public MyListAdapter(Context context, int textViewResourceId) {
	    super(context, textViewResourceId);
	    mTextViewResourceID = textViewResourceId;
	    mContext = context;
	}

	public int getCount() {
	    return mListStr.length;
	}

	@Override
	public boolean areAllItemsEnabled() {
	    return false;
	}

	public Object getItem(int position) {
	    return position;
	}

	public long getItemId(int position) {
	    return position;
	}
	
	//－－－－－－显示dialog对话框确认删除-----------
	private void ShowDialog(String str, final int position) {
		AlertDialog.Builder builder = new AlertDialog.Builder(DeleteSalerActivity.this);
		builder.setIcon(R.drawable.ic_launcher);
		builder.setTitle(str);
		builder.setPositiveButton("确定",new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				try{
		    		ArrayList<String> list = new ArrayList<String>(Arrays.asList(mListStr));
		    		list.remove(position);
			    	BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			    	for(int i=0;i<list.size();i++){
			    		System.out.println(i+","+list.get(i));
			    		bw.write(list.get(i)+"\n"); //将arraylist元素写回数组
			    	}
			    	bw.close();//在这里不关闭bw的文件操作，会丢失文件里的所有内容
			    	Toast.makeText(DeleteSalerActivity.this,"删除营业员成功！", Toast.LENGTH_SHORT).show();			    	 
			    	myNewAdapter.notifyDataSetChanged();  
				 	mListView.invalidate();
		    	}catch(Exception e){
		    		e.printStackTrace();
		    	}	
				finish();
				
			}
		});
		
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {  
	           public void onClick(DialogInterface dialog, int id) {  
	                dialog.cancel();  
	           }  
	       });  
		builder.show();
		
	}

	
	public View getView(final int position, View convertView, ViewGroup parent) {
	    TextView text = null;
	    Button button = null;
	    if (convertView == null) {
		convertView = LayoutInflater.from(mContext).inflate(
			mTextViewResourceID, null);
		text = (TextView) convertView.findViewById(R.id.array_text);
		button = (Button)convertView.findViewById(R.id.array_button);
		//－－－－－－－－－－－删除按键触发事件－－－－－－－－－－
		button.setOnClickListener(new OnClickListener() {		    
		    @Override
		    public void onClick(View arg0) {
		    	ShowDialog("确定删除该营业员吗？",position);		    		    	
		    }
		});
	    }	
	    
	 	   	    
	    text.setText(mListStr[position]);
	    return convertView;
	}
	
    }
    
    
}