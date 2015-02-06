package com.john.groupbuy.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.john.groupbuy.R;
import com.john.groupbuy.lib.http.MyOrderInfoItem;
import com.john.groupbuy.lib.http.ProductInfo;
import com.john.util.GetTimeUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OrderListAdapter extends BaseAdapter {

	public List<MyOrderInfoItem> mList;
	protected final LayoutInflater mInflater;
	private Context context;

	public OrderListAdapter(Context context, List<MyOrderInfoItem> list) {
		this.context = context;
		this.mInflater = LayoutInflater.from(context);
		mList = new ArrayList<MyOrderInfoItem>();
		replaceWith(list);
	}

	public void replaceWith(List<MyOrderInfoItem> list) {
		if(list == null)
			return;
		removeBadData(list);
		mList.clear();
		mList = list;
		notifyDataSetChanged();
	}

	public void addData(List<MyOrderInfoItem> list) {
		if (list == null || list.isEmpty())
			return;
		if (mList == null) {
			mList = new ArrayList<MyOrderInfoItem>();
		}
		removeBadData(list);
		mList.addAll(list);
		notifyDataSetChanged();
	}
	
	private void removeBadData(List<MyOrderInfoItem> list){
		if(list == null)
			return;
		Iterator<MyOrderInfoItem> iter = list.iterator();
		while(iter.hasNext()){
			MyOrderInfoItem item = iter.next();
			if(item.team == null){
				iter.remove();
			}
		}
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
			convertView = mInflater.inflate(R.layout.item_product, null);
			holder.mTitle = (TextView) convertView
					.findViewById(R.id.title_text_view);
			holder.mHead = (ImageView) convertView
					.findViewById(R.id.product_icon);
			holder.mDetail = (TextView) convertView
					.findViewById(R.id.detail_text_view);
			holder.mPrice = (TextView) convertView
					.findViewById(R.id.price_text_view);
			holder.mMarkPrice = (TextView) convertView
					.findViewById(R.id.discount_text_view);
			holder.mCount = (TextView) convertView
					.findViewById(R.id.count_text_view);
			holder.mNew = (ImageView) convertView
					.findViewById(R.id.new_image_view_flag);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		ProductInfo item = mList.get(position).team;
		if(item == null)
			return convertView;
		
		if (item.partner != null) {
			holder.mTitle.setText(item.partner.getTitle());
		} else {
			holder.mTitle.setText(item.product);
		}
		holder.mDetail.setText(item.title);
		holder.mPrice.setText(context.getString(R.string.format_team_buy,
				item.getTeamPrice()));
		holder.mMarkPrice.setText(context.getString(R.string.format_sale_price,
				item.getMarketPrice()));
		holder.mCount.setText(item.now_number + "人");
		holder.mHead.setImageResource(R.drawable.default_pic_small);
		if (GetTimeUtil.isToday(item.begin_time)) {
			holder.mNew.setVisibility(View.VISIBLE);
		} else {
			holder.mNew.setVisibility(View.GONE);
		}

		String imageUrl = item.getSmallImageUrl();
		if (!TextUtils.isEmpty(imageUrl)) {
			ImageLoader.getInstance().displayImage(imageUrl, holder.mHead);
		} else {
			holder.mHead.setImageResource(R.drawable.default_pic_small);
		}
		return convertView;
	}

	private final class ViewHolder {
		public TextView mTitle;
		public ImageView mHead;
		public TextView mDetail;
		public TextView mPrice;
		public TextView mMarkPrice;
		public TextView mCount;
		public ImageView mNew;
	}

}
