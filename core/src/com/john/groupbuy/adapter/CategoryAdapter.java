package com.john.groupbuy.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.john.groupbuy.R;
import com.john.groupbuy.lib.http.CategoryInfo;

public class CategoryAdapter extends BaseAdapter {

	private Context context;
	private List<CategoryInfo> adapterList;
	
	public CategoryAdapter(Context context) {
		setContext(context);
		adapterList = new ArrayList<CategoryInfo>();
    }
	
	@Override
	public int getCount() {
		return  adapterList.size();
	}

	@Override
	public Object getItem(int position) {
		if(position >= 0 && position < adapterList.size())
			return adapterList.get(position);
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CategoryInfo  info = (CategoryInfo) getItem(position);
		if(convertView == null){
			convertView = LayoutInflater.from(context).inflate(R.layout.item_hot_key, null);
		}
		convertView.setTag(info);
		TextView textView = (TextView) convertView;
		textView.setText(info.name);
		return textView;
	}
	
	public Context getContext() {
	    return context;
    }
	
	public void setContext(Context context) {
	    this.context = context;
    }
	
	public List<CategoryInfo> getAdapterList() {
	    return adapterList;
    }
	
	public void setAdapterList(List<CategoryInfo> adapterList) {
		if(adapterList == null)
			return;
	    this.adapterList = adapterList;
	    notifyDataSetChanged();
    }

}
