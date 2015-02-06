package com.john.groupbuy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class CouponView
{
	private View container;
	private TextView title_cn;
	private TextView title_local;
	private TextView edaole_code;
	private TextView shop_name;
	private TextView shop_address;
	private TextView opening_hours;
	private TextView telephone;
	public CouponView(Context context)
	{
		container = (View) LayoutInflater.from(context).inflate(R.layout.coupon, null);
		title_cn = (TextView)container.findViewById(R.id.coupon_title_cn);
		title_local = (TextView)container.findViewById(R.id.coupon_title_local);
		edaole_code = (TextView)container.findViewById(R.id.coupon_edaole_code);
		shop_name = (TextView)container.findViewById(R.id.coupon_shop_name);
		shop_address = (TextView)container.findViewById(R.id.coupon_shop_address);
		opening_hours = (TextView)container.findViewById(R.id.coupon_opening_hours);
		telephone = (TextView)container.findViewById(R.id.coupon_telephone);
	}

	public View getView()
	{
		return container;
	}

	public View getContainer()
	{
		return container;
	}
	public TextView getTitleCN(){return title_cn;}
	public TextView getTitle_local(){return title_local;}
	public TextView getEdaole_code(){return edaole_code;}
	public TextView getShop_name(){return shop_name;}
	public TextView getShop_address(){return shop_address;}
	public TextView getOpening_hours(){return opening_hours;}
	public TextView getTelephone(){return telephone;}


}
