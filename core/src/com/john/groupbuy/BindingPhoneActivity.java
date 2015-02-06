package com.john.groupbuy;

import java.io.IOException;

import org.apache.http.ParseException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.john.groupbuy.lib.http.Interface;
import com.john.groupbuy.lib.http.VerifyPhoneInfo;
import com.john.util.HttpResponseException;
import com.john.util.HttpUtil;
import com.umeng.analytics.MobclickAgent;

public class BindingPhoneActivity extends BaseActivity implements OnClickListener {

    protected final static int INITSTEP = 0;
    protected final static int VERIDYSTEP = 1;
    private EditText mPhoneEdit;
    private EditText mVerifyCodeEdt;
    private Button mBindingBtn;
    private Button mVerifyCodeBtn;
    private String mCurrentPhone;
    private ProgressDialog mProgressDlg;
    private CustomTask mTask;
    private int mStep = INITSTEP;

    protected class CustomTask extends AsyncTask<String, Void, VerifyPhoneInfo> {

        @Override
        protected VerifyPhoneInfo doInBackground(String... params) {
        	try {
        		String url = Interface.S_MOBILE_VERIFY;
        		if(mStep == INITSTEP){
        			url += "mobile=" + params[0];
        		}else{
        			url += "vcode="+params[0];
        		}
        		return HttpUtil.get(url, VerifyPhoneInfo.class);
        	} catch (ParseException e) {
        		e.printStackTrace();
        	} catch (IOException e) {
        		e.printStackTrace();
        	} catch (HttpResponseException e) {
        		e.printStackTrace();
        	}
        	return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDlg = new ProgressDialog(BindingPhoneActivity.this);
            mProgressDlg.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (mTask != null) {
                        mTask.cancel(true);
                        mTask = null;
                        mVerifyCodeBtn.setEnabled(true);
                        mBindingBtn.setEnabled(true);
                    }
                }
            });
            mProgressDlg.setIndeterminate(true);
            Activity activity = BindingPhoneActivity.this;
            if (mStep == INITSTEP) {
                mProgressDlg.setMessage(activity.getString(R.string.order_check_code_request));
            } else {
                mProgressDlg.setMessage(activity.getString(R.string.order_check_code_waiting));
            }
            mProgressDlg.setCancelable(true);
            mProgressDlg.show();
        }

        @Override
        protected void onPostExecute(VerifyPhoneInfo info) {
            super.onPostExecute(info);
            mProgressDlg.dismiss();
            if(info == null){
            	Toast.makeText(BindingPhoneActivity.this,R.string.connecting_error, Toast.LENGTH_SHORT).show();
            	return;
            }
            if ( info.error != null) {
                Toast.makeText(BindingPhoneActivity.this, info.error, Toast.LENGTH_SHORT).show();
                return;
            }

            Activity activity = BindingPhoneActivity.this;
            if (mStep == INITSTEP) {
            	mStep = VERIDYSTEP;
            	Toast.makeText(activity, activity.getString(R.string.order_check_code_succ), Toast.LENGTH_SHORT).show();
            } else if (mStep == VERIDYSTEP) {
            	if(info.status.equalsIgnoreCase("1")){
            		Toast.makeText(activity, R.string.bind_success, Toast.LENGTH_SHORT).show();
            		GroupBuyApplication.sBindingPhone = mCurrentPhone;
            		activity.setResult(BindingPhoneActivity.RESULT_OK);
            		activity.finish();
            	}
            	else{
            		Toast.makeText(activity,R.string.binding_failure_hint, Toast.LENGTH_SHORT).show();
            	}
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.binding_phone);
        enableBackBehavior();
        setTitle(R.string.title_binding_phone);
        initViewComponents();
    }

    protected void initViewComponents() {
        mPhoneEdit = (EditText) findViewById(R.id.phoneEdit);
        mVerifyCodeEdt = (EditText) findViewById(R.id.verifyCodeEdt);

        mBindingBtn = (Button) findViewById(R.id.bindingButton);
        mVerifyCodeBtn = (Button) findViewById(R.id.verifyCodeBtn);
        mBindingBtn.setOnClickListener(this);
        mVerifyCodeBtn.setOnClickListener(this);
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }
    
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
    
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bindingButton) {
            String phone = mPhoneEdit.getText().toString();
            String code = mVerifyCodeEdt.getText().toString();

            if (TextUtils.isEmpty(phone) || phone.length() != 11) {
                Toast.makeText(BindingPhoneActivity.this, "请输入手机号码", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(code) || code.length() != 5) {
                Toast.makeText(BindingPhoneActivity.this, getString(R.string.checkcode), Toast.LENGTH_SHORT).show();
                return;
            }
            mCurrentPhone = phone;
            mStep = VERIDYSTEP;
            mTask = (CustomTask) new CustomTask().execute(code);
        } else if (v.getId() == R.id.verifyCodeBtn) {
            String phone = mPhoneEdit.getText().toString();
            if (TextUtils.isEmpty(phone) || phone.length() != 11) {
                Toast.makeText(BindingPhoneActivity.this, "请输入手机号码", Toast.LENGTH_SHORT).show();
                return;
            }

            mCurrentPhone = phone;
            mStep = INITSTEP;
            mTask = (CustomTask) new CustomTask().execute(phone);
        }
    }
    
    

}
