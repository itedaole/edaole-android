package com.john.groupbuy;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.john.groupbuy.lib.http.CouponItemInfo;
import com.john.groupbuy.lib.http.GlobalKey;
import com.john.groupbuy.zxing.EncodingHandler;

public class OrderDetailActivity extends BaseActivity {

	private ImageView mImgImageView;
	private CouponItemInfo currentItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_order_detail);
		initView(savedInstanceState);
		enableBackBehavior();
		setTitle(R.string.title_prder_detail);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    outState.putParcelable(GlobalKey.PARCELABLE_KEY, currentItem);
	}

	private void initView(Bundle savedInstanceState) {
		if(savedInstanceState == null){
			Intent intent = this.getIntent();
			if (intent != null) 
				currentItem = intent.getParcelableExtra(GlobalKey.PARCELABLE_KEY);
		}else{
			currentItem = savedInstanceState.getParcelable(GlobalKey.PARCELABLE_KEY);
		}
		
		if(currentItem == null){
			finish();
			return;
		}
		
		mImgImageView = (ImageView) findViewById(R.id.imageView1);
		String code = currentItem.id;
		String password = currentItem.secret;
		TextView codeView = (TextView) this.findViewById(R.id.codeView);
		TextView passwordView = (TextView) this
				.findViewById(R.id.passwordView);
		codeView.setText(code);
		passwordView.setText(password);

		String contentString = String.format("%s,%s", code, password);
		this.initImage(contentString);
	}

	private void initImage(String contentString) {
		try {
			Bitmap qrCodeBitmap = EncodingHandler.createQRCode(contentString,
					350);
			mImgImageView.setImageBitmap(qrCodeBitmap);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
