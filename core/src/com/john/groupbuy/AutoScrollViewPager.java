package com.john.groupbuy;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

//view pager can auto scroll
public class AutoScrollViewPager extends ViewPager implements Handler.Callback {
    private static final int MSG_AUTO_SCROLL = 1;
    private Handler handler;
    private int scrollDelay = 3000; //ms
    private boolean loop = false;

    public AutoScrollViewPager(Context context) {
        super(context);
        initView();
    }

    public AutoScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public void setScrollDelay(int scrollDelay) {
        this.scrollDelay = scrollDelay;
    }

    private void initView() {
        handler = new Handler(this);
    }

    public void startScroll() {
        handler.sendEmptyMessageDelayed(MSG_AUTO_SCROLL, scrollDelay);
    }

    @Override
    public boolean handleMessage(Message msg) {
        PagerAdapter adapter = getAdapter();
        if (adapter == null)
            return true;
        int pageCount = adapter.getCount();
        if (pageCount == 0)
            return true;
        int currentPage = getCurrentItem();
        if (currentPage == pageCount - 1){
            if (!loop)
                return true;
            currentPage = 0;
        }else{
            currentPage++;
        }
        setCurrentItem(currentPage, true);
        handler.sendEmptyMessageDelayed(MSG_AUTO_SCROLL, scrollDelay);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_MOVE)
            handler.removeMessages(MSG_AUTO_SCROLL);
        else if (ev.getAction() == MotionEvent.ACTION_UP)
            handler.sendEmptyMessageDelayed(MSG_AUTO_SCROLL, scrollDelay);
        return super.onTouchEvent(ev);
    }
}
