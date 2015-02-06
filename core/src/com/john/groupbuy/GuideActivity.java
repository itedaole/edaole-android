package com.john.groupbuy;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.VideoView;

import com.groupbuy.yidaole.wxapi.WXEntryActivity;
import com.john.groupbuy.adapter.GuideAdapter;
import com.john.groupbuy.lib.http.GlobalKey;
import com.john.groupbuy.lib.http.GuideData;

import java.util.ArrayList;
import java.util.List;

/**
 * Our guide activity which displaying at application launching at first time
 */
public class GuideActivity extends Activity implements View.OnClickListener {
    private static final int ID_SELECT_CITY = 1;
    private static final int ID_LOGIN = 2;
    private static final int ID_REGISTER = 3;

    private AutoScrollViewPager viewPager;
    private VideoView videoView;
    private PageIndicator pageIndicator;
    private GuideAdapter guideAdapter;
    private boolean playVideoError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        initViews();
    }

    private void initViews() {
        findViewById(R.id.guide_login).setOnClickListener(this);
        findViewById(R.id.guide_register).setOnClickListener(this);
        videoView = (VideoView) findViewById(R.id.guide_video_view);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
                playVideoError = true;
                videoView.setVisibility(View.GONE);
                return true;
            }
        });

        startPlay();

        viewPager = (AutoScrollViewPager) findViewById(R.id.guide_view_pager);
        pageIndicator = (PageIndicator) findViewById(R.id.guide_page_indicator);
        guideAdapter = new GuideAdapter(this);
        guideAdapter.setStartNowListener(this);
        viewPager.setOnPageChangeListener(pageIndicator);
        viewPager.setAdapter(guideAdapter);

        Resources resources = getResources();
        if (resources == null)
            return;
        String[] titles = resources.getStringArray(R.array.GuideTitles);
        String[] details = resources.getStringArray(R.array.GuideDetails);
        if (titles == null || details == null)
            return;

        List<GuideData> guideDatas = new ArrayList<GuideData>();
        for (int idx = 0; idx < titles.length; ++idx) {
            GuideData data = new GuideData();
            data.setTitle(titles[idx]);
            data.setDetail(details[idx]);
            guideDatas.add(data);
        }
        pageIndicator.setPageCount(guideDatas.size());
        guideAdapter.setGuideDatas(guideDatas);
        viewPager.startScroll();
    }

    @Override
    protected void onResume() {
        if (!playVideoError)
            startPlay();
        super.onResume();

    }

    private void startPlay() {
        String path = "android.resource://" + getPackageName() + "/" + R.raw.moments;
        videoView.setVideoURI(Uri.parse(path));
        videoView.start();
    }

    @Override
    protected void onPause() {
        if (!playVideoError)
            videoView.stopPlayback();
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.guide_login) {
            Intent intent = new Intent(GuideActivity.this,WXEntryActivity.class);
            startActivityForResult(intent,ID_LOGIN);
        } else if (id == R.id.guide_register) {
            Intent intent = new Intent(GuideActivity.this,RegisterActivity.class);
            startActivityForResult(intent,ID_REGISTER);
        } else if (id == R.id.guide_start_now) {
            Intent intent = new Intent(this,CityListActivity.class);
            intent.putExtra(CityListActivity.KEY_SELECT_CITY_MODE, true);
            startActivityForResult(intent, ID_SELECT_CITY);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ID_SELECT_CITY){
            if(data != null){
                SharedPreferences sharedPref = getSharedPreferences(GlobalKey.SHARE_PREFERS_NAME,MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(GlobalKey.SELECTED_CITY_ID,data.getStringExtra("id"));
                editor.putString(GlobalKey.SELECTED_CITY_NAME,data.getStringExtra("city"));
                editor.commit();
            }
            Intent intent = new Intent(GuideActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
