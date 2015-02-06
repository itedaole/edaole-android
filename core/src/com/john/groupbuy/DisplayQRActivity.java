package com.john.groupbuy;

import android.os.Bundle;

/**
 * Created by luckystar on 2014/5/7.
 */
public class DisplayQRActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_qrcode);
        enableBackBehavior();
        setTitle(R.string.scan_code);
    }
}
