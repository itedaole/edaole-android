package com.john.groupbuy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.john.groupbuy.R;
import com.john.groupbuy.lib.http.ProductInfo;

import java.util.List;

public class SimpleProductAdapter extends BaseAdapter {

    protected List<ProductInfo> mList;
    protected Context mContext;
    
    public SimpleProductAdapter(Context context , List<ProductInfo> list){
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
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView =  inflater.inflate(R.layout.simple_product_item, null);
            holder.mTitle = (TextView) convertView.findViewById(R.id.tile);
            holder.mDetail = (TextView)convertView.findViewById(R.id.subTitle);
            
            ProductInfo item = mList.get(position);
            if(item.partner != null){
                holder.mTitle.setText(item.partner.getTitle());
            }else{
                holder.mTitle.setText(item.product);
            }
            holder.mDetail.setText(item.title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        return convertView;
    }
    
    private final class ViewHolder {
        public TextView mTitle;
        public TextView mDetail;
    }

}
