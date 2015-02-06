package com.john.groupbuy;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * The page indicator of view pager
 */
public class PageIndicator extends View implements ViewPager.OnPageChangeListener {

    private int pageCount;
    private int currentPage = 0;
    private int indicatorSize;
    private int indicatorSpace;
    private int indicatorColor = Color.WHITE;
    private int indicatorColorSelected = Color.BLUE;
    private Paint painter;

    public PageIndicator(Context context) {
        super(context);
        initView();
    }

    public PageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
        resolveStyledAttrs(context, attrs, 0);
    }

    public PageIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
        resolveStyledAttrs(context, attrs, defStyle);
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
        requestLayout();
        invalidate();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
        invalidate();
    }

    private void initView() {
        Resources resources = getResources();
        if (resources == null)
            return;
        indicatorSize = resources.getDimensionPixelSize(R.dimen.page_indicator_size);
        indicatorSpace = resources.getDimensionPixelSize(R.dimen.page_indicator_space);
        painter = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    private void resolveStyledAttrs(Context context, AttributeSet attrs, int defStyle) {
        Resources.Theme theme = context.getTheme();
        if (theme == null)
            return;
        TypedArray typedArray = theme.obtainStyledAttributes(attrs, R.styleable.PageIndicator, defStyle, 0);
        if (typedArray == null)
            return;

        try {
            indicatorSize = typedArray.getDimensionPixelSize(R.styleable.PageIndicator_indicatorSize, indicatorSize);
            indicatorSpace = typedArray.getDimensionPixelSize(R.styleable.PageIndicator_indicatorSpace, indicatorSpace);
            indicatorColor = typedArray.getColor(R.styleable.PageIndicator_indicatorColor, indicatorColor);
            indicatorColorSelected = typedArray.getColor(R.styleable.PageIndicator_indicatorColorSelected, indicatorColorSelected);
        } finally {
            typedArray.recycle();
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float radius = indicatorSize / 2;
        float cy = (getHeight() - indicatorSize) / 2 + radius;
        for (int page = 0; page < pageCount; ++page) {
            if (page == currentPage) {
                painter.setColor(indicatorColorSelected);
            } else {
                painter.setColor(indicatorColor);
            }
            canvas.save();
            float cx = (indicatorSize + indicatorSpace) * page + radius;
            canvas.translate(cx, cy);
            canvas.drawCircle(0, 0, radius, painter);
            canvas.restore();
        }
    }

    private int measureWidth(int widthMeasureSpec) {
        int size = MeasureSpec.getSize(widthMeasureSpec);
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int measureSize = 0;
        switch (mode) {
            case MeasureSpec.EXACTLY:
                measureSize = size;
                break;
            case MeasureSpec.AT_MOST:
                int sizeHint = widthHint();
                measureSize = (sizeHint < size) ? sizeHint : size;
                break;
            case MeasureSpec.UNSPECIFIED:
                measureSize = widthHint();
                break;
        }
        return measureSize;
    }

    private int widthHint() {
        if (pageCount == 0)
            return 0;
        int size = pageCount * indicatorSize + (pageCount - 1) * indicatorSpace;
        size += getPaddingLeft() + getPaddingRight();
        return size;
    }


    private int heightHint() {
        return getPaddingTop() + getPaddingBottom() + indicatorSize;
    }

    private int measureHeight(int heightMeasureSpec) {
        int size = MeasureSpec.getSize(heightMeasureSpec);
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int measureSize = 0;
        switch (mode) {
            case MeasureSpec.EXACTLY:
                measureSize = size;
                break;
            case MeasureSpec.AT_MOST:
                int sizeHint = heightHint();
                measureSize = (sizeHint < size) ? sizeHint : size;
                break;
            case MeasureSpec.UNSPECIFIED:
                measureSize = heightHint();
                break;
        }
        return measureSize;
    }


    @Override
    public void onPageScrolled(int i, float v, int i2) {
    }

    @Override
    public void onPageSelected(int i) {
        currentPage = i;
        invalidate();
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }

    public void onPageCountChanged(int pageCount) {
        this.pageCount = pageCount;
        currentPage = 0;
        requestLayout();
    }
}
