package com.john.groupbuy.citylist;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class SideBar extends View {
    private char[] indexArray;
    private SectionIndexer sectionIndexter = null;
    private ListView list;
    private WeakReference<TextView> mDialogText;
    private int m_nItemHeight = 15;

    public SideBar(Context context) {
        super(context);
        init();
    }

    public SideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        indexArray = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
                'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
    }

    public SideBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setListView(ListView _list) {
        list = _list;
        sectionIndexter = (SectionIndexer) _list.getAdapter();
    }

    public void setTextView(TextView mDialogText) {
        this.mDialogText = new WeakReference<TextView>(mDialogText);
    }

    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (mDialogText == null || list == null)
            return false;

        int i = (int) event.getY();
        int idx = i / m_nItemHeight;
        if (idx >= indexArray.length) {
            idx = indexArray.length - 1;
        } else if (idx < 0) {
            idx = 0;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            ((View) mDialogText.get()).setVisibility(View.VISIBLE);
            ((TextView) mDialogText.get()).setText("" + indexArray[idx]);
            if (sectionIndexter == null) {
                sectionIndexter = (SectionIndexer) list.getAdapter();
            }
            int position = sectionIndexter.getPositionForSection(indexArray[idx]);
            if (position == -1) {
                return true;
            }
            list.setSelection(position);
        } else {
            mDialogText.get().setVisibility(View.INVISIBLE);
        }
        return true;
    }

    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(0x552B2B2B);
        paint.setTextSize(24);
        paint.setTextAlign(Paint.Align.CENTER);
        float widthCenter = getMeasuredWidth() / 2;
        m_nItemHeight = getMeasuredHeight() / 27;
        for (int i = 0; i < indexArray.length; i++) {
            canvas.drawText(String.valueOf(indexArray[i]), widthCenter, m_nItemHeight + (i * m_nItemHeight), paint);
        }
        super.onDraw(canvas);
    }
}
