package com.john.groupbuy.adapter;

import java.util.ArrayList;
import java.util.List;

import com.john.groupbuy.R;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class HotKeyAdapter extends BaseAdapter {

	private List<String> adapterData;
	private LayoutInflater inflater;
	
	public HotKeyAdapter(Context context) {
		inflater = LayoutInflater.from(context);
		adapterData = new ArrayList<String>();
	}
	
	@Override
	public int getCount() {
		return adapterData.size();
	}
	
	private boolean isAvailable(int index){
		return index >= 0 && index < adapterData.size();
	}

	@Override
	public Object getItem(int position) {
		if(isAvailable(position))
			return adapterData.get(position);
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView textView;
		if(convertView == null){
			textView = (TextView) inflater.inflate(R.layout.item_hot_key, null);
		}else{
			textView = (TextView) convertView;
		}
		String hotKey = (String) getItem(position);
		if(!TextUtils.isEmpty(hotKey))
			textView.setText(hotKey);
		return textView;
	}

	public List<String> getAdapterData() {
		return adapterData;
	}

	public void setAdapterData(List<String> adapterData) {
		if(adapterData == null){
			adapterData = new ArrayList<String>();
		}else{
			this.adapterData = adapterData;
		}
		notifyDataSetChanged();
	}

}
