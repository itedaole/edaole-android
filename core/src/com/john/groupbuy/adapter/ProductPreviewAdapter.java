package com.john.groupbuy.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.john.groupbuy.R;
import com.john.groupbuy.lib.http.GuideData;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class ProductPreviewAdapter extends PagerAdapter {
    private Context context;
    private List<String> imageUrls;
    private DisplayImageOptions options;

    public ProductPreviewAdapter(Context context) {
        this.context = context;
        imageUrls = new ArrayList<String>();
        options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisc(true).bitmapConfig(Bitmap.Config.ARGB_8888).build();
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
        notifyDataSetChanged();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        String url = imageUrls.get(position);
        ImageLoader.getInstance().displayImage(url,imageView,options);

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(0, 0);
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        container.addView(imageView, params);

        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (object instanceof View)
            container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return imageUrls.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
    }
}
