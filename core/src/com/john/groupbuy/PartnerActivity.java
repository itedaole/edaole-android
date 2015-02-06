package com.john.groupbuy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.john.groupbuy.lib.http.PartnerInfo;
import com.nostra13.universalimageloader.core.ImageLoader;

public class PartnerActivity extends BaseActivity implements OnClickListener {

	private ImageView partnerIcon;
	private TextView partnerName;
	private TextView partnerPhone;
	private TextView partnerAddress;
	
	private static PartnerInfo partnerInfo; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		enableBackBehavior();
		setTitle(R.string.partner_detail);
		setContentView(R.layout.partner_activity);
		initViews();
		updatePartnerUI();
	}

	private void updatePartnerUI() {
		if (getPartnerInfo() == null) {
			finish();
			return;
		}
		ImageLoader.getInstance().displayImage(getPartnerInfo().getImage(),
				partnerIcon);
		if (!TextUtils.isEmpty(getPartnerInfo().getTitle()))
			partnerName.setText(getPartnerInfo().getTitle());
		if (!TextUtils.isEmpty(getPartnerInfo().getPhone()))
			partnerPhone.setText(getPartnerInfo().getPhone());
		if (!TextUtils.isEmpty(getPartnerInfo().getAddress()))
			partnerAddress.setText(getPartnerInfo().getAddress());
	}


	private void initViews() {
		partnerIcon = (ImageView) findViewById(R.id.partner_icon);
		partnerName = (TextView) findViewById(R.id.partner_name);
		partnerPhone = (TextView) findViewById(R.id.partner_phone);
		partnerAddress = (TextView) findViewById(R.id.address_label);
		findViewById(R.id.phone_btn).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		 if (v.getId() == R.id.phone_btn) {
			if (getPartnerInfo() == null) 
				return;
			String phoneNum = getPartnerInfo().getPhone();
			if (TextUtils.isEmpty(phoneNum))
				return;
			String url = String.format("tel:%s", phoneNum);
			Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
			startActivity(intent);
		}
	}

	public static PartnerInfo getPartnerInfo() {
		return partnerInfo;
	}

	public static void setPartnerInfo(PartnerInfo partnerInfo) {
		PartnerActivity.partnerInfo = partnerInfo;
	}

}
