package com.john.groupbuy.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.john.groupbuy.R;
import com.john.groupbuy.lib.http.GuideData;

import java.util.ArrayList;
import java.util.List;

public class GuideAdapter extends PagerAdapter {
    private Context context;
    private List<GuideData> guideDatas;
    private View.OnClickListener startNowListener;

    public GuideAdapter(Context context) {
        this.context = context;
        guideDatas = new ArrayList<GuideData>();
    }

    public void setStartNowListener(View.OnClickListener startNowListener) {
        this.startNowListener = startNowListener;
    }

    public void setGuideDatas(List<GuideData> guideDatas) {
        this.guideDatas = guideDatas;
        notifyDataSetChanged();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.guide_page_view, null);
        TextView title = (TextView) view.findViewById(R.id.guide_page_title);
        TextView details = (TextView) view.findViewById(R.id.guide_page_details);
        GuideData data = guideDatas.get(position);
        title.setText(data.getTitle());
        details.setText(data.getDetail());

        if (position == guideDatas.size() - 1) {
            View startNow = view.findViewById(R.id.guide_start_now);
            startNow.setVisibility(View.VISIBLE);
            startNow.setOnClickListener(startNowListener);
        }

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(0, 0);
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        container.addView(view, params);

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (object instanceof View)
            container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return guideDatas.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
    }
}
