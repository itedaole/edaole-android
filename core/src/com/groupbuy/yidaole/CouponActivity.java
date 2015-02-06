package com.groupbuy.yidaole;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.john.groupbuy.BaseActivity;
import com.john.groupbuy.CouponView;
import com.john.groupbuy.Dialog_share;
import com.john.groupbuy.R;
import com.john.groupbuy.Dialog_share.ShareDialogListener;
import com.john.groupbuy.lib.http.SingleCouponInfo;
import com.john.util.GoogleAnalyticsManager;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class CouponActivity extends BaseActivity{
	private IWXAPI api;
    //private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
    
    
    private CouponView couponview;
    
    private final static String WEIXINAPPID = "wx9d2279ea33cddad9";
    private SingleCouponInfo singleCouponInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleAnalyticsManager.INSTANCE.onActivityCreate(CouponActivity.class);
        
        singleCouponInfo = (SingleCouponInfo)getIntent().getSerializableExtra("SingleCouponInfo");
        System.out.println("singleCouponInfo="+(singleCouponInfo==null));
       
        couponview = new CouponView(this);
        setContentView(couponview.getView());
        setText(couponview.getTitleCN(), singleCouponInfo.result.title);
        setText(couponview.getTitle_local(), singleCouponInfo.result.title_local);
        setText(couponview.getShop_name(), singleCouponInfo.result.shop_name);
        setText(couponview.getShop_address(), singleCouponInfo.result.address);
        setText(couponview.getTelephone(), singleCouponInfo.result.phone);
        setText(couponview.getOpening_hours(), singleCouponInfo.result.yy_time);
        setText(couponview.getEdaole_code(), singleCouponInfo.result.couponcode);
        
        enableBackBehavior();
        setTitle("优惠劵");
        
        if(!getIntent().getBooleanExtra("Dismiss", false))
        {
            showShareDialog();
        }
        
    }
    
    private void setText(TextView textView, String str)
    {
    	if(str==null)
    	{
    		textView.setText("");
    	}
    	else
    	{
    		textView.setText(str);
    	}
    }

    private void showShareDialog()
    {
    	Dialog_share d = new Dialog_share(CouponActivity.this, singleCouponInfo.result.share_summary, singleCouponInfo.result.large_image, new ShareDialogListener() {		
			@Override
			public void onCommit(String content) {
				
				// TODO Auto-generated method stub
		            api = WXAPIFactory.createWXAPI(CouponActivity.this, WEIXINAPPID);
		        api.registerApp(WEIXINAPPID); 
		
		        
/*
				// 初始化一个WXTextObject对象
				WXTextObject textObj = new WXTextObject();
				textObj.text = singleCouponInfo.result.share_summary;

				// 用WXTextObject对象初始化一个WXMediaMessage对象
				WXMediaMessage msg = new WXMediaMessage();
				msg.mediaObject = textObj;
				// 发送文本类型的消息时，title字段不起作用
				// msg.title = "Will be ignored";
				msg.description = singleCouponInfo.result.share_summary;

				// 构造一个Req
				SendMessageToWX.Req req = new SendMessageToWX.Req();
				req.transaction = buildTransaction("text"); // transaction字段用于唯一标识一个请求
				req.message = msg;
				req.scene =  SendMessageToWX.Req.WXSceneTimeline;
				
				// 调用api接口发送数据到微信
				api.sendReq(req);
*/
		        WXWebpageObject webpage = new WXWebpageObject();
				webpage.webpageUrl = singleCouponInfo.result.share_url;
				WXMediaMessage msg = new WXMediaMessage(webpage);
				msg.title = singleCouponInfo.result.title;
				msg.description = singleCouponInfo.result.share_summary;
				//String imageUrl = Interface.DNS_NAME+"/"+singleCouponInfo.result.large_image;
				//Bitmap thumb = ImageLoader.getInstance().loadImageSync(imageUrl);
				//msg.thumbData = Util.bmpToByteArray(thumb, true);
				
				SendMessageToWX.Req req = new SendMessageToWX.Req();
				req.transaction = buildTransaction("webpage");
				req.message = msg;
				req.scene = SendMessageToWX.Req.WXSceneTimeline;
				api.sendReq(req);
			}
			@Override
			public void onCancel() {
				// TODO Auto-generated method stub
				closeOptionsMenu();
			}
		});
    	d.getTextView_title().setText(singleCouponInfo.result.title);
		d.show();
    }
    
    
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
        	showShareDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
	private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

}
