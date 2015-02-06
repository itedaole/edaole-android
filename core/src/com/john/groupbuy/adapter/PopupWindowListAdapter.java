package com.john.groupbuy.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import com.john.groupbuy.R;
import com.john.groupbuy.lib.http.CategoryInfo;

public class PopupWindowListAdapter extends BaseAdapter implements
		OnClickListener {

	public List<CategoryInfo> mList;
	protected final LayoutInflater mInflater;
	private int selectedIndex = -1;
	private boolean subMenu;
	private OnItemClickedListener listener;
	
	private int highlightColor ;
	private int grayColor;

	public PopupWindowListAdapter(Context context, List<CategoryInfo> list) {
		this.mInflater = LayoutInflater.from(context);
		mList = list;
		highlightColor = context.getResources().getColor(R.color.theme_color);
		grayColor = context.getResources().getColor(R.color.gray_bg);
	}

	public void setListener(OnItemClickedListener listener) {
		this.listener = listener;
	}

	public void setList(List<CategoryInfo> list) {
		if (list == null)
			return;
		mList = list;
		if(subMenu)
			selectedIndex = -1;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		if (position >= 0 && position < mList.size())
			return mList.get(position);
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.category_item, null);
			holder.categoryItem = (CheckedTextView) convertView;
			holder.categoryItem.setOnClickListener(this);
			holder.atIndex = position;
			holder.info = mList.get(position);
			convertView.setTag(holder);
			if(subMenu)
				holder.categoryItem.setBackgroundColor(Color.TRANSPARENT);
		} else {
			holder = (ViewHolder) convertView.getTag();
			holder.atIndex = position;
			holder.info = mList.get(position);
		}

		if(subMenu){
			if (position == selectedIndex) {
				holder.categoryItem.setTextColor(highlightColor);
			} else{
				holder.categoryItem.setTextColor(Color.BLACK);
			}
		}else{
			if (position == selectedIndex) {
				holder.categoryItem.setBackgroundColor(grayColor);
			} else{
				holder.categoryItem.setBackgroundResource(Color.TRANSPARENT);
			}
		}

		CategoryInfo item = mList.get(position);
		holder.categoryItem.setText(item.name);

		return convertView;
	}

	private final class ViewHolder {
		public CheckedTextView categoryItem;
		int atIndex;
		CategoryInfo info;
	}

	@Override
	public void onClick(View v) {
		ViewHolder holder = (ViewHolder) v.getTag();
		selectedIndex = holder.atIndex;
		if (listener != null)
			listener.onItemClicked(holder.info,holder.atIndex);
		notifyDataSetChanged();
	}

	public boolean isSubMenu() {
		return subMenu;
	}

	public void setSubMenu(boolean subMenu) {
		this.subMenu = subMenu;
	}

	public int getSelectedIndex() {
	    return selectedIndex;
    }

	public void setSelectedIndex(int selectedIndex) {
	    this.selectedIndex = selectedIndex;
    }

	public interface OnItemClickedListener {
		void onItemClicked(CategoryInfo info,int index);
	}

}
