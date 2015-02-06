package com.john.groupbuy.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.john.groupbuy.R;
import com.john.groupbuy.lib.http.CityItem;

public class CityListAdapter extends BaseAdapter implements SectionIndexer {
	private List<CityItem> mList;
	protected final LayoutInflater mInflater;

	public CityListAdapter(Context context, List<CityItem> list) {
		this.mInflater = LayoutInflater.from(context);
		mList = list;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return mList.get(arg0);

	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.city_item, null);
			holder.mTitle = (TextView) convertView.findViewById(R.id.text1);
			holder.mCharView = (TextView) convertView
					.findViewById(R.id.contactitem_catalog);
			holder.mCharView.setVisibility(View.GONE);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		CityItem item = mList.get(position);
		holder.mTitle.setText(item.getName());
		String category = item.getCategory();
		if (position == 0) {
			holder.mCharView.setVisibility(View.VISIBLE);
			holder.mCharView.setText(category);
		} else {
			String prevCategory = mList.get(position - 1).getCategory();
			if (category.equalsIgnoreCase(prevCategory)) {
				holder.mCharView.setVisibility(View.GONE);
			} else {
				holder.mCharView.setVisibility(View.VISIBLE);
				holder.mCharView.setText(category);
			}
		}

		return convertView;
	}

	private final class ViewHolder {
		public TextView mTitle;
		public TextView mCharView;
	}

	public String getCityNameByIndex(int pos) {
		return mList.get(pos).getName();

	}

	public String getCityIdByIndex(int pos) {
		return mList.get(pos).getId();
	}

	@Override
	public Object[] getSections() {
		return null;
	}

	@Override
	public int getPositionForSection(int section) {
		CityItem item = null;
		if (section == '!') {
			return 0;
		} else {
			for (int i = 0; i < getCount(); i++) {
				item = (CityItem) mList.get(i);
				int firstChar = item.getPingying().charAt(0);
				if (firstChar == section) {
					return i;
				}
			}
		}
		item = null;
		return -1;
	}

	@Override
	public int getSectionForPosition(int position) {
		return 0;
	}

}
