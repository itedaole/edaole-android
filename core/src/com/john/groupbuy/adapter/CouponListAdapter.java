package com.john.groupbuy.adapter;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.john.groupbuy.R;
import com.john.groupbuy.lib.http.CouponItemInfo;
import com.john.util.TimeUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

public class CouponListAdapter extends BaseAdapter {

	public List<CouponItemInfo> mList;
	protected final LayoutInflater mInflater;

	public CouponListAdapter(Context context, List<CouponItemInfo> list) {
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
			convertView = mInflater.inflate(R.layout.my_order_item, null);
			holder.mTitle = (TextView) convertView.findViewById(R.id.title);
			holder.mSubTitle = (TextView) convertView
					.findViewById(R.id.subTitle);
			holder.mHead = (ImageView) convertView
					.findViewById(R.id.product_icon);
			holder.mEndTime = (TextView) convertView.findViewById(R.id.endtime);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		CouponItemInfo item = mList.get(position);
		String format = mInflater.getContext().getString(
				R.string.period_of_validity);
		holder.mEndTime.setText(String.format(format,
				TimeUtil.ConverTime(item.expire_time)));
		if (item.team.partner != null) {
			holder.mTitle.setText(item.team.partner.getTitle());
		}
		holder.mSubTitle.setText(item.team.product);

		if (item.team != null) {
			String imageUrl = item.team.getSmallImageUrl();
			if (!TextUtils.isEmpty(imageUrl)) {
			    ImageLoader.getInstance().displayImage(imageUrl, holder.mHead);
				return convertView;
			}
		}
		holder.mHead.setImageResource(R.drawable.default_pic_small);
		return convertView;
	}

	protected String getAlbumImageUrl(String url) {
		return url.replace(".jpg", "_index.jpg");
	}

	private final class ViewHolder {
		public TextView mTitle;
		public TextView mSubTitle;
		public ImageView mHead;
		public TextView mEndTime;
	}

	public void addListData(List<CouponItemInfo> result, boolean b) {
		if (b) {
			mList.clear();
		}
		mList.addAll(result);
		notifyDataSetChanged();
	}

}
