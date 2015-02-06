package com.john.groupbuy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.john.groupbuy.lib.FactoryCenter;
import com.john.groupbuy.lib.http.ErrorInfo;
import com.john.groupbuy.lib.http.GlobalKey;
import com.john.groupbuy.lib.http.Interface;
import com.john.groupbuy.lib.http.ProductInfo;
import com.john.groupbuy.lib.http.SubmitOrderInfo;
import com.john.util.HttpResponseException;
import com.john.util.LogUtil;

public class SubmitOrderActivity extends BaseActivity implements OnClickListener, TextWatcher {

    private EditText mEditor;
    private TextView mTotalPrice;
    private Button mBindingButton;
    private float mPrice;
    private ProgressDialog mDialog;
    private ProductInfo mInfo;

    private EditText mName;
    private EditText mAddress;
    private EditText mZipcode;
    private boolean mIsPostMode = false;
    private boolean mIsNeedSelectProductCategory = false;
    private AsyncTask<String, Void, SubmitOrderInfo> mTask;
    private LinearLayout mCategoryLayout;
    private List<RadioGroup> mRadioGroup;

    private boolean shouldBindingPhone = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        shouldBindingPhone = GroupBuyApplication.sUserInfo.shouldBindingPhone();

        setContentView(R.layout.activity_submit_order);
        enableBackBehavior();
        setTitle(R.string.title_SubmitOrderActivity);
        init();

        mEditor.setText("1");
    }

    protected void init() {
        mEditor = (EditText) findViewById(R.id.productCount);
        mEditor.addTextChangedListener(this);
        Button submitButton = (Button) findViewById(R.id.submitButton);
        submitButton.setOnClickListener(this);
        ImageButton addButton = (ImageButton) findViewById(R.id.plusBtn);
        addButton.setOnClickListener(this);
        ImageButton subButton = (ImageButton) findViewById(R.id.minusBtn);
        subButton.setOnClickListener(this);

        mBindingButton = (Button) findViewById(R.id.binding_phone);
        mBindingButton.setText(GroupBuyApplication.sBindingPhone);
        mBindingButton.setOnClickListener(this);

        mInfo = CacheManager.getInstance().getCurrentProduct();
        if (mInfo == null){
        	finish();
        	return;
        }

        mPrice = Float.parseFloat(mInfo.team_price);
        TextView view = (TextView) findViewById(R.id.productName);
        view.setText(mInfo.product);
        view = (TextView) findViewById(R.id.productPrice);
        view.setText(mInfo.team_price);
        mTotalPrice = (TextView) findViewById(R.id.productTotalPrice);
        updateTotalPrice();

        if (mInfo.express_relate == null) {
            // 隐藏与运费相关的东东
            LinearLayout layout = (LinearLayout) findViewById(R.id.postSection);
            layout.setVisibility(View.GONE);
        } else {
            mName = (EditText) findViewById(R.id.nameEdit);
            mAddress = (EditText) findViewById(R.id.addressEdit);
            mZipcode = (EditText) findViewById(R.id.zipcodeEdit);
            mIsPostMode = true;
        }

        if (TextUtils.isEmpty(mInfo.condbuy)) {
            // 隐藏商品类型相关内容
            LinearLayout layout = (LinearLayout) findViewById(R.id.condbuySection);
            layout.setVisibility(View.GONE);
        } else {
            mCategoryLayout = (LinearLayout) findViewById(R.id.condbuySection);
            inflationCategoryLayout(mInfo.condbuy);
            mIsNeedSelectProductCategory = true;
        }
    }

    protected void inflationCategoryLayout(String condbuy) {
        String[] sectionArray = condbuy.split("@");
        int indexStart, indexEnd, itemId;

        mRadioGroup = new ArrayList<RadioGroup>();
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        for (int i = 0; i < sectionArray.length; i++) {
            indexStart = 0;
            indexEnd = 0;
            itemId = 0;
            String section = sectionArray[i];
            HorizontalScrollView sv = new HorizontalScrollView(mCategoryLayout.getContext());
            sv.setHorizontalScrollBarEnabled(true);

            HorizontalScrollView.LayoutParams svParams = new HorizontalScrollView.LayoutParams(
                    HorizontalScrollView.LayoutParams.WRAP_CONTENT, 
                    HorizontalScrollView.LayoutParams.WRAP_CONTENT);
            svParams.setMargins(0, 5, 0, 5);
            sv.setLayoutParams(svParams);

            RadioGroup.LayoutParams groupParams = new RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.WRAP_CONTENT, 
                    RadioGroup.LayoutParams.WRAP_CONTENT);
            RadioGroup group = new RadioGroup(sv.getContext());
            group.setLayoutParams(groupParams);
            group.setOrientation(LinearLayout.HORIZONTAL);
            sv.addView(group);
            mRadioGroup.add(group);
            while ((indexStart = section.indexOf("{", indexStart)) != -1) {
                if ((indexEnd = section.indexOf("}", indexStart)) == -1)
                    break;
                String text = section.substring(indexStart + 1, indexEnd);
                indexStart = indexEnd;
                RadioButton button = (RadioButton) inflater.inflate(R.layout.gray_red_radio_btn, null);
                RadioGroup.LayoutParams rglp = new RadioGroup.LayoutParams(
                        RadioGroup.LayoutParams.WRAP_CONTENT,
                        RadioGroup.LayoutParams.MATCH_PARENT);
                rglp.setMargins(5, 5, 5, 5);
                button.setId(itemId);
                button.setText(text);
                button.setLayoutParams(rglp);
                group.addView(button);
                itemId++;
            }
            mCategoryLayout.addView(sv);
        }
    }

    protected void updateTotalPrice() {
        if (mEditor.getText().length() == 0)
            mTotalPrice.setText("");
        else {
            float totalPrice = mPrice * getEditorNumber();
            String text = String.format(Locale.US, "%.2f", totalPrice);
            mTotalPrice.setText(text);
        }
    }

    protected int getEditorNumber() {
        String text = mEditor.getText().toString();
        if (text == null || text.length() == 0)
            return 0;
        int number = Integer.valueOf(text).intValue();
        return number;
    }

    protected boolean verifyUserInput() {

        if (shouldBindingPhone && mBindingButton.getText().length() != 11) {
            Toast.makeText(this, getString(R.string.binding_number_first), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!mIsPostMode)
            return true;

        if (TextUtils.isEmpty(mName.getText().toString())) {
            Toast.makeText(SubmitOrderActivity.this, getString(R.string.empty_name), Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(mAddress.getText().toString())) {
            Toast.makeText(SubmitOrderActivity.this, getString(R.string.empty_address), Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(mZipcode.getText().toString())) {
            Toast.makeText(SubmitOrderActivity.this, getString(R.string.empty_zipCode), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (mIsNeedSelectProductCategory) {
            for (RadioGroup group : mRadioGroup) {
                if (group.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(SubmitOrderActivity.this, getString(R.string.select_product_category), Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.submitButton) {
            if (mEditor.getText().toString().length() == 0) {
                Toast.makeText(this, this.getString(R.string.input_goods_count), Toast.LENGTH_SHORT).show();
                return;
            }
            if (verifyUserInput()) {
                String key = Interface.S_PRODUCT_SUBMIT + generateArguments();
                mTask = new SubmitOrderTask().execute(key);
            }
        } else if (v.getId() == R.id.plusBtn) {
            int number = getEditorNumber() + 1;
            String text = String.valueOf(number);
            mEditor.setText(text);
            mEditor.setSelection(text.length());
        } else if (v.getId() == R.id.minusBtn) {
            int number = getEditorNumber() - 1;
            if (number <= 0)
                number = 1;
            String text = String.valueOf(number);
            mEditor.setText(text);
            mEditor.setSelection(text.length());
        } else if (v.getId() == R.id.binding_phone) {
            bindingPhone();
        }

    }

    protected void bindingPhone() {
        Intent intent = new Intent(SubmitOrderActivity.this, BindingPhoneActivity.class);
        this.startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == 1) {
            Toast.makeText(SubmitOrderActivity.this, getString(R.string.binding_succ), Toast.LENGTH_SHORT).show();
            mBindingButton.setText(GroupBuyApplication.sBindingPhone);
        } else if (requestCode == GlobalKey.REQUEST_PAYMENT) {
            // 支付完成时关闭当前页面
            this.finish();
        }
    }

    protected String generateArguments() {
        String arguments = null;
        if (mInfo != null) {
            arguments = String.format("id=%s&quantity=%s&plat=%s", mInfo.id, this.mEditor.getText(), "211");

            if (mIsPostMode && GroupBuyApplication.sUserInfo != null) {
                String expressid = (mInfo.express_relate == null) ? "00000" : mInfo.express_relate[0].id;
                String tell = GroupBuyApplication.sBindingPhone;
                String name = mName.getText().toString();
                String address = mAddress.getText().toString();
                String zipCode = mZipcode.getText().toString();
                arguments += String.format("&tell=%s&name=%s&address=%s&zipcode=%s&expressid=%s", tell, name, address, zipCode, expressid);
            }

            if (mIsNeedSelectProductCategory && mRadioGroup.size() != 0) {
                arguments += "&condbuy=";
                for (RadioGroup group : mRadioGroup) {
                    int id = group.getCheckedRadioButtonId();
                    if (id != -1) {
                        RadioButton button = (RadioButton) group.getChildAt(id);
                        arguments += "@" + button.getText();
                    }
                }
            }
        }
        return arguments;
    }

    private class SubmitOrderTask extends AsyncTask<String, Void, SubmitOrderInfo> {

        public SubmitOrderTask() {
            super();
        }

        @Override
        protected SubmitOrderInfo doInBackground(String... params) {
            final String key = params[0];
            SubmitOrderInfo res = null;
            try {
                res = FactoryCenter.getProcessCenter().getSubmitOrderInfo(key);
            } catch (HttpResponseException e) {
                LogUtil.warn(e.getMessage(), e);
            } catch (IOException e) {
                LogUtil.warn(e.getMessage(), e);
            }

            return res;
        }

        @Override
        protected void onPreExecute() {
            mDialog = new ProgressDialog(SubmitOrderActivity.this);
            mDialog.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    mTask.cancel(true);

                }
            });
            mDialog.setIndeterminate(true);
            mDialog.setMessage("提交订单中，请稍后!");
            mDialog.setCancelable(true);
            mDialog.show();
        }

        @Override
        protected void onPostExecute(SubmitOrderInfo result) {
            mDialog.dismiss();
            if (result == null) {
                Toast.makeText(getApplicationContext(), R.string.submit_error_hint, Toast.LENGTH_SHORT).show();
                return;
            }
            if (result.status.equalsIgnoreCase("1")) {
                // request success
                Intent intent = new Intent(SubmitOrderActivity.this, CheckoutOrderActivity.class);
                intent.putExtra(GlobalKey.PARCELABLE_KEY, result.result);
                intent.putExtra(GlobalKey.PRODUCT_NAME_KEY, mInfo.product);
                // startActivity(intent);
                startActivityForResult(intent, GlobalKey.REQUEST_PAYMENT);
            } else {
                if (result.error != null) {
                    ErrorInfo error = result.error;
                    Toast.makeText(getApplicationContext(), error.mText, Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        updateTotalPrice();
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

}
