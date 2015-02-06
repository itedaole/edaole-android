package com.john.groupbuy;


import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.john.groupbuy.lib.http.Interface;
import com.john.groupbuy.lib.http.ProductInfo;
import com.john.util.DensityUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

public class Dialog_share extends Dialog
{
	private Button button_sure;
	private ImageButton button_miss;
	private TextView textView_title;
	

	private EditText editText_content;
	
    private ProductInfo productInfo = null;
    private ImageView product_image;

	public Dialog_share(Context context, String content, String imagePath, ShareDialogListener listener)
	{
		super(context,com.john.groupbuy.R.style.MMTheme_DataSheet);
		init(context, content, imagePath);
		setListener(listener);
	}
	
	private void init(Context context,String content, String imagePath)
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout layout=(RelativeLayout) inflater.inflate(R.layout.dialog_share, null);
		
		if (productInfo == null)
            productInfo = CacheManager.getInstance().getCurrentProduct();
		product_image = (ImageView)layout.findViewById(R.id.dialog_share_product_icon);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) product_image.getLayoutParams();
		params.height = (DensityUtil.getScreenWidth(context)-DensityUtil.dip2px(context, 100))*9/16;
		product_image.setLayoutParams(params);
		
		button_miss=(ImageButton) layout.findViewById(R.id.miss);
		button_sure=(Button) layout.findViewById(R.id.sure);
		textView_title=(TextView) layout.findViewById(R.id.dialog_share_product_title);
		textView_title.getBackground().setAlpha(150);
		editText_content=(EditText) layout.findViewById(R.id.content);
		editText_content.setText(content==null? "":content);
		
		//product_image.setImageResource(R.drawable.default_pic_small);
		
//		String imageUrl = productInfo.getLargeImageUrl();
		String imageUrl = Interface.DNS_NAME+"/"+imagePath;
		ImageLoader.getInstance().displayImage(imageUrl,product_image);

		
		Window w = getWindow();
		WindowManager.LayoutParams lp = w.getAttributes();
		lp.x = 0;
		lp.y = 0;
		lp.gravity = Gravity.CENTER;
		onWindowAttributesChanged(lp);
		setCanceledOnTouchOutside(true);
		setContentView(layout);
		
		System.out.println("lp.width="+lp.width);
		//Toast.makeText(context, "lp.width="+lp.width, Toast.LENGTH_LONG).show();
		
	}

	private void setListener(final ShareDialogListener listener)
	{
		button_sure.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				if (editText_content.getText()==null)
				{
					Toast.makeText(getContext(), "分享内容没有填写哦", Toast.LENGTH_SHORT).show();
					return;
				}
				listener.onCommit(editText_content.getText().toString());
				dismiss();
			}
		});
		
		button_miss.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				listener.onCancel();
				dismiss();
			}
		});
	}

	public interface ShareDialogListener{
		public void onCommit(String content);
		public void onCancel();
	}
	
	public TextView getTextView_title() {
		return textView_title;
	}

	public void setTextView_title(TextView textView_title) {
		this.textView_title = textView_title;
	}

}
