package com.john.groupbuy;

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
import com.john.groupbuy.lib.FactoryCenter;
import com.john.groupbuy.lib.http.RegisterResult;
import com.john.groupbuy.lib.http.VerifyPhoneInfo;
import com.john.util.HttpResponseException;
import com.john.util.LogUtil;

import java.io.IOException;

public class RegisterActivity extends BaseActivity implements OnClickListener {

    private Button mRegistButton;
    public ProgressDialog myDialog;

    private RigisterTask mTask;

    private EditText mAccount;
    private EditText mPassword;
    private EditText mPassword2;
    private EditText mMobilePhone;
    private EditText mMobileCode;
    private EditText mEmail;
    private Button mBtnVCode;
    private VerifyTask mVerifyCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.regist);
        initView();
        enableBackBehavior();
        setTitle(R.string.title_activity_register);
    }

    private void initView() {
        mAccount = (EditText) findViewById(R.id.account);
        mPassword = (EditText) findViewById(R.id.password);
        mPassword2 = (EditText) findViewById(R.id.repassword);
        mMobilePhone = (EditText) findViewById(R.id.phone_num);
        mMobileCode = (EditText) findViewById(R.id.phone_code);
        mEmail = (EditText) findViewById(R.id.email_ad);

        mRegistButton = (Button) findViewById(R.id.reg_btn);
        mRegistButton.setOnClickListener(this);

        mBtnVCode = (Button) findViewById(R.id.btn_vcode);
        mBtnVCode.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_vcode) {
            String phone = mMobilePhone.getText().toString();
            if (TextUtils.isEmpty(phone) || phone.length() != 11) {
                Toast.makeText(RegisterActivity.this, "请输入手机号码", Toast.LENGTH_SHORT).show();
                return;
            }
            getVerifyCode(phone);
        }  else if (v.getId() == R.id.reg_btn) {
            String account = mAccount.getText().toString();
            if (TextUtils.isEmpty(account)) {
                
                Toast.makeText(RegisterActivity.this, "账号不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            String password = mPassword.getText().toString();
            if (TextUtils.isEmpty(account)) {
                Toast.makeText(RegisterActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            String password1 = mPassword2.getText().toString();
            if (!password.equals(password1)) {
                Toast.makeText(RegisterActivity.this, "两次密码不一致", Toast.LENGTH_SHORT).show();
                return;
            }
            String email = mEmail.getText().toString();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(RegisterActivity.this, "邮箱不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            String phone1 = mMobilePhone.getText().toString();
            String code = mMobileCode.getText().toString();
            if (!TextUtils.isEmpty(code) && TextUtils.isEmpty(phone1)) {
                Toast.makeText(RegisterActivity.this, "绑定手机号不能为空", Toast.LENGTH_SHORT).show();
            }
            request(account, password, code, email, phone1);
        }

    }

    private void request(String account, String password, String code, String email, String phone) {
        mTask = (RigisterTask) new RigisterTask().execute(account, password, code, email, phone);
    }

    @Override
    public void finish() {
        if (mTask != null)
            mTask.cancel(true);
        if (myDialog != null) {
            myDialog.dismiss();
            myDialog = null;
        }
        super.finish();
    }

    private class RigisterTask extends AsyncTask<String, Void, RegisterResult> {

        public RigisterTask() {
            super();
        }

        @Override
        protected void onPreExecute() {
            myDialog = new ProgressDialog(RegisterActivity.this);
            myDialog.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    if (mTask != null)
                        mTask.cancel(true);

                }
            });
            myDialog.setIndeterminate(true);
            myDialog.setMessage("注册中...");
            myDialog.setCancelable(true);
            myDialog.show();
        }

        @Override
        protected RegisterResult doInBackground(String... params) {
            final String username = params[0];
            final String password = params[1];
            final String phoneCode = params[2];
            final String email = params[3];
            final String phoneNum = params[4];
            RegisterResult result = null;
            try {

                result = FactoryCenter.getUserInfoCenter().userRegister(username, password, phoneCode, email,
                        phoneNum);

            } catch (HttpResponseException e) {
                LogUtil.warn(e.getMessage(), e);
            } catch (IOException e) {
                LogUtil.warn(e.getMessage(), e);
            }
            return result;
        }

        @Override
        protected void onPostExecute(RegisterResult result) {
            if (myDialog != null) {
                myDialog.dismiss();
                myDialog = null;
            }

            if (result != null) {
                if (result.status == 1) {
                    Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    if (result.error != null) {
                        Toast.makeText(RegisterActivity.this, result.error.mInfo, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(RegisterActivity.this,R.string.connecting_error, Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void getVerifyCode(String phone) {

        mVerifyCode = new VerifyTask();
        mVerifyCode.execute(phone);
    }

    protected class VerifyTask extends AsyncTask<String, Void, VerifyPhoneInfo> {

        @Override
        protected VerifyPhoneInfo doInBackground(String... params) {
            int length = params.length;
            VerifyPhoneInfo info = null;
            try {
                if (length == 1) {
                    info = FactoryCenter.getProcessCenter().getMobileVerifyInfo(params[0], "");
                } else if (length == 2) {
                    info = FactoryCenter.getProcessCenter().getMobileVerifyInfo(params[0], params[1]);
                }
            } catch (HttpResponseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return info;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            myDialog = new ProgressDialog(RegisterActivity.this);
            myDialog.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (mTask != null) {
                        mTask.cancel(true);
                        mTask = null;
                        mBtnVCode.setEnabled(true);
                    }
                }
            });
            myDialog.setIndeterminate(true);
            myDialog.setMessage(RegisterActivity.this.getString(R.string.order_check_code_request));
            myDialog.setCancelable(true);
            myDialog.show();
        }

        @Override
        protected void onPostExecute(VerifyPhoneInfo info) {
            super.onPostExecute(info);
            myDialog.dismiss();
            mBtnVCode.setEnabled(true);
            if(info == null){
            	Toast.makeText(RegisterActivity.this, R.string.connecting_error, Toast.LENGTH_SHORT).show();
            	return;
            }
            if (info.error != null) {
                Toast.makeText(RegisterActivity.this, info.error, Toast.LENGTH_SHORT).show();
                return;
            } else {
                Toast.makeText(RegisterActivity.this, "获取验证码成功，\n请在收到短信输入验证码", Toast.LENGTH_SHORT).show();
            }

        }

    }
}
