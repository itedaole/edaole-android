package com.john.groupbuy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * our application about activity </br>
 * Created by qili on 2014/7/7.
 */
public class AboutActivity extends BaseActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableBackBehavior();
        setTitle(R.string.mu_support);
        setContentView(R.layout.about_activiy);
        initViews();
    }

    private void initViews() {
        findViewById(R.id.access_website).setOnClickListener(this);
        findViewById(R.id.connect_mu).setOnClickListener(this);

        TextView description = (TextView) findViewById(R.id.app_description);
        description.setText(getString(R.string.app_description,
                CacheManager.getInstance().getVersionCode()));
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.access_website) {
            String url = "http://www.moyooo.com/index.html";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        } else if (id == R.id.connect_mu) {
            String url = String.format("tel:%s", "4000680410");
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            startActivity(intent);
        }
    }
}
