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
import com.john.groupbuy.lib.http.MyExpressInfo;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ExpressListAdapter extends BaseAdapter {

	public List<MyExpressInfo> mList;
    protected final LayoutInflater mInflater;
    protected Context mContext;

    public ExpressListAdapter(Context context, List<MyExpressInfo> list) {
        this.mInflater = LayoutInflater.from(context);
        mList = list;
        mContext = context;
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
            convertView = mInflater.inflate(R.layout.express_item, null);
            holder.mTitle = (TextView) convertView.findViewById(R.id.titleName);
            holder.mHead = (ImageView) convertView.findViewById(R.id.product_icon);
            holder.mExpress = (TextView) convertView.findViewById(R.id.expressName);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        MyExpressInfo item = mList.get(position);

        String title = item.getExpressName();
        if(TextUtils.isEmpty(title))
        	title = mContext.getString(R.string.express_title);
        holder.mTitle.setText(title);
        String text = item.getExpressNo();
        if(text.equalsIgnoreCase("")){
        	text = "暂无快递信息";
        }
        holder.mExpress.setText(text);

        String imageUrl = item.getImageUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            ImageLoader.getInstance().displayImage(imageUrl, holder.mHead);
        } else {
            holder.mHead.setImageResource(R.drawable.default_pic_small);
        }
        return convertView;
    }
    
    protected String getAlbumImageUrl(String url){
    	return url.replace(".jpg", "_index.jpg");
    }

    private final class ViewHolder {
        public TextView mTitle;
        public ImageView mHead;
        public TextView mExpress;
    }

    public void addListData(List<MyExpressInfo> result, boolean b) {
        if (b) {
            mList.clear();
        }
        mList.addAll(result);
        notifyDataSetChanged();
    }

}
