package com.john.util;

import android.app.Activity;
import android.content.Context;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.john.groupbuy.R;

import java.util.HashMap;

public enum  GoogleAnalyticsManager {
    INSTANCE;

    private String TRACK_ID = "UA-56308970-1";
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }
    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();
    private Context context;

    public void init(Context context){
        this.context = context;
    }

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
            analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(TRACK_ID)
                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(
                    R.xml.global_tracker)
                    : analytics.newTracker(R.xml.ecommerce_tracker);
            t.enableAdvertisingIdCollection(true);
            mTrackers.put(trackerId, t);
        }
        return mTrackers.get(trackerId);
    }

    public void onActivityCreate(final Class<?> cls){
        Tracker tracker = getTracker(GoogleAnalyticsManager.TrackerName.APP_TRACKER);
        tracker.setScreenName(cls.getName());
        tracker.send(new HitBuilders.AppViewBuilder().build());
    }

    public void dispatchLocalHits(){
        GoogleAnalytics.getInstance(context).dispatchLocalHits();
    }
}
