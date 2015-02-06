package com.john.groupbuy.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.john.groupbuy.R;
import com.john.groupbuy.lib.http.Interface;
import com.john.groupbuy.lib.http.NewCouponListResult_Result;
import com.nostra13.universalimageloader.core.ImageLoader;

public class NewCouponListAdapter extends BaseAdapter {

	public List<NewCouponListResult_Result> mList;
	protected final LayoutInflater mInflater;

	public NewCouponListAdapter(Context context, List<NewCouponListResult_Result> list) {
		this.mInflater = LayoutInflater.from(context);
		mList = list;
	}


	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
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
			convertView = mInflater.inflate(R.layout.my_order_item1, null);
			holder.mTitle = (TextView) convertView.findViewById(R.id.title);
			holder.mHead = (ImageView) convertView
					.findViewById(R.id.product_icon);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		NewCouponListResult_Result item = mList.get(position);
		if (item.title != null) {
			holder.mTitle.setText(item.title);
		}
		holder.mHead.setImageResource(R.drawable.default_pic_small);
		String url = Interface.IMAGE_APP_HOST+item.image;
		ImageLoader.getInstance().displayImage(url, holder.mHead);
		System.out.println(url);
		return convertView;
	}

	protected String getAlbumImageUrl(String url) {
		return url.replace(".jpg", "_index.jpg");
	}

	private final class ViewHolder {
		public TextView mTitle;
		public ImageView mHead;
	}

	public void addListData(List<NewCouponListResult_Result> result, boolean b) {
		if (b) {
			mList.clear();
		}
		mList.addAll(result);
		notifyDataSetChanged();
	}

}
