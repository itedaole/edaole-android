package com.john.groupbuy;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LoadingView extends LinearLayout {

    private ProgressBar loadingCycle;
    private TextView statusView;

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_loading, this);
        loadingCycle = (ProgressBar) findViewById(R.id.loading_progress_bar);
        statusView = (TextView) findViewById(R.id.loading_status_label);
    }

    /**
     * set status label text
     *
     * @param msg       Message to display
     * @param isLoading If set to  true,then display loading progress bar, otherwise hide it
     */
    public void showMessage(String msg, boolean isLoading) {
        if (isLoading)
            loadingCycle.setVisibility(View.VISIBLE);
        else
            loadingCycle.setVisibility(View.GONE);

        statusView.setText(msg);
    }

    public void showMessage(int res, boolean isLoading) {
        if (isLoading)
            loadingCycle.setVisibility(View.VISIBLE);
        else
            loadingCycle.setVisibility(View.GONE);

        statusView.setText(res);
        setVisibility(View.VISIBLE);
    }

}
