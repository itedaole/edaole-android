package com.john.groupbuy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


public class WXView
{
	public WXView(Context context)
	{
		container = (View) LayoutInflater.from(context).inflate(R.layout.wx_login, null);
		username = (TextView)container.findViewById(R.id.wx_user_name);
		usersex  = (TextView)container.findViewById(R.id.wx_user_sex);
		language = (TextView)container.findViewById(R.id.wx_user_language);
		residence = (TextView)container.findViewById(R.id.wx_user_residence);
		imageview = (ImageView)container.findViewById(R.id.wx_user_headImage);
	}
	public View getView()
	{
		return container;
	}
	
	private View container;
	private TextView username;
	private TextView usersex;
	private TextView language;
	private TextView residence;
	private ImageView imageview;
	public TextView getUsername() {
		return username;
	}
	public TextView getUsersex() {
		return usersex;
	}
	public TextView getLanguage() {
		return language;
	}
	public TextView getResidence() {
		return residence;
	}
	public ImageView getImageview() {
		return imageview;
	}
	
	public void setContainer(View container) {
		this.container = container;
	}
	public View getContainer()
	{
		return container;
	}


	
}
