package com.john.groupbuy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.john.groupbuy.R;
import com.john.groupbuy.lib.http.ProductInfo;
import com.john.util.DensityUtil;
import com.john.util.GetTimeUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class ProductListAdapter extends BaseAdapter {

	private List<ProductInfo> adapterData;
	protected final LayoutInflater mInflater;
	private Context context;

	public ProductListAdapter(Context context, List<ProductInfo> list) {
		this.mInflater = LayoutInflater.from(context);
		setAdapterData(list);
		setContext(context);
	}

	@Override
	public int getCount() {
		return getAdapterData().size();
	}

	@Override
	public Object getItem(int position) {
		return getAdapterData().get(position);
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
			holder.mIcon = (ImageView) convertView
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
		ProductInfo item = getAdapterData().get(position);
        if (item.product != null){
            holder.mTitle.setText(item.product);
        }else if(item.partner != null){
            holder.mTitle.setText(item.partner.getTitle());
        }
		holder.mDetail.setText(item.title);
		holder.mPrice.setText(context.getString(R.string.format_team_buy,
				item.getTeamPrice()));
		holder.mMarkPrice.setText(context.getString(R.string.format_sale_price,
				item.getMarketPrice()));
		holder.mCount.setText(item.now_number + "人");
		holder.mIcon.setImageResource(R.drawable.default_pic_small);
		if (GetTimeUtil.isToday(item.begin_time)) {
			holder.mNew.setVisibility(View.VISIBLE);
		} else {
			holder.mNew.setVisibility(View.GONE);
		}

		//新改设置透明度
		holder.mTitle.getBackground().setAlpha(150);
		
		//设置控件高度
		int width = DensityUtil.getScreenWidth(context);
		int height = width*9/16;
		LayoutParams laParams=(LayoutParams)holder.mIcon.getLayoutParams();
		laParams.width= width;
		laParams.height= height;
				//holder.mIcon.getLayoutParams().width*9/16;
		holder.mIcon.setLayoutParams(laParams);
		
		
		
		
		String imageUrl = item.getLargeImageUrl();
		ImageLoader.getInstance().displayImage(imageUrl, holder.mIcon);
		return convertView;
	}

	protected String getAlbumImageUrl(String url) {
		return url.replace(".jpg", "_index.jpg");
	}

	private final class ViewHolder {
		public TextView mTitle;
		public ImageView mIcon;
		public TextView mDetail;
		public TextView mPrice;
		public TextView mMarkPrice;
		public TextView mCount;
		public ImageView mNew;
	}

	public void clearAdapterData() {
		if (getAdapterData() != null && !getAdapterData().isEmpty()) {
			getAdapterData().clear();
			notifyDataSetChanged();
		}
	}

	public List<ProductInfo> getAdapterData() {
		return adapterData;
	}

	public void setAdapterData(List<ProductInfo> adapterData) {
		if(adapterData == null){
			this.adapterData = new ArrayList<ProductInfo>();
			return;
		}
		this.adapterData = adapterData;
		notifyDataSetChanged();
	}

    public void addAdapterData(List<ProductInfo> data){
        if (adapterData == null || adapterData.isEmpty())
            return;
        this.adapterData.addAll(data);
        notifyDataSetChanged();
    }

	public Context getContext() {
		return context;
	}



	public void setContext(Context context) {
		this.context = context;
	}

}
