package com.john.groupbuy;
import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;


public class DiscountView extends TextView {

	public DiscountView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Paint paint = this.getPaint();
		paint.setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
		paint.setAntiAlias(true);
	}

}
