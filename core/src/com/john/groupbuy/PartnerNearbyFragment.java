package com.john.groupbuy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.john.groupbuy.DragListView.FooterState;
import com.john.groupbuy.DragListView.OnLoadingMoreListener;
import com.john.groupbuy.adapter.ProductListAdapter;
import com.john.groupbuy.lib.FactoryCenter;
import com.john.groupbuy.lib.http.ProductInfo;
import com.john.groupbuy.lib.http.ProductListInfo;
import com.john.groupbuy.map.GoogleMapActivity;
import com.john.util.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PartnerNearbyFragment extends Fragment implements
        OnLoadingMoreListener, OnClickListener {

    private static MyLocationListener sMapListener = null;
    private DragListView mListView;
    private ProductListAdapter mAdapter;
    private ProductListTask mTask;
    private int mCurrentPageNumber = 1;
    private int mPageCount;
    private double mLng;
    private double mLat;
    private List<ProductInfo> mProductList;
    private BDLocationListener mLocationListener;

    private LoadingView loadingView;
    private ImageView relocationBtn;
    private Animation rotateAnimation;
    private TextView addressView;
    private boolean isRelocation = false;
    private boolean requestLocation = false;
    private boolean useGoogleMap = false;

    public static void reLocation() {
        BaiduLocationManager.getInstance().requestLocation();
    }

    public static void setMapLocationListner(MyLocationListener listner) {
        sMapListener = listner;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        BaseActivity activity = (BaseActivity) getActivity();
        activity.getSupportActionBar().setDisplayShowCustomEnabled(false);
        showLoadingAnimation();

        useGoogleMap = CacheManager.getInstance().isUseGoogleMap();

        if (useGoogleMap){
            if(GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity()) != ConnectionResult.SUCCESS)
                useGoogleMap = false;
            BaiduLocationManager.getInstance().init(activity.getApplicationContext());
        }

        if (useGoogleMap) {
            GoogleLocationManager.INSTANCE.setLocationListener(new GoogleLocationManager.OnGoogleLocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    onGoogleLocationChanged(location);
                }

                @Override
                public void onAddressChanged(String address) {
                    onGoogleAddressChanged(address);
                }
            });
        } else {
            BaiduLocationManager.getInstance().registerLocationListener(mLocationListener);
        }
    }

    private void onGoogleAddressChanged(String address) {
        if (address == null || address.isEmpty()) {
            setAddress(getActivity().getString(R.string.address_fail_hint));
            return;
        }

        setAddress(address);
    }

    private void onGoogleLocationChanged(Location location) {
        isRelocation = false;
        relocationBtn.clearAnimation();
        if (location == null) {
            setAddress(getActivity().getString(
                    R.string.reloaction_fail_hint));
            loadingView.showMessage(R.string.reloaction_fail_hint,
                    false);
            return;
        }

        loadingView.setVisibility(View.GONE);
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        boolean dataChanged = lat != mLat || lng != mLng;
        mLat = lat;
        mLng = lng;

        if (mTask == null) {
            mTask = new ProductListTask();
            mTask.execute();
        } else if (dataChanged && requestLocation) {
            requestLocation = false;
            onRefresh();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.product_nearby);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            getActivity().setTitle(R.string.product_nearby);
            BaseActivity activity = (BaseActivity) getActivity();
            activity.getSupportActionBar().setDisplayShowCustomEnabled(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater
                .inflate(R.layout.partner_nearby_activity, null);
        initViewComponents(rootView);
        initData();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_map, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_map) {
            showMapActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void initViewComponents(View rootView) {
        mListView = (DragListView) rootView.findViewById(R.id.dragListView);
        // mMapBtn = (ImageView) rootView.findViewById(R.id.showMap);

        relocationBtn = (ImageView) rootView.findViewById(R.id.reloaction_btn);
        relocationBtn.setOnClickListener(this);

        addressView = (TextView) rootView.findViewById(R.id.address_label);

        loadingView = (LoadingView) rootView.findViewById(R.id.loading_view);
        loadingView.showMessage(R.string.loading_data_hint, true);

        getActivity().setTitle(R.string.product_nearby);
        mProductList = new ArrayList<ProductInfo>();
        mAdapter = new ProductListAdapter(getActivity(), mProductList);
        mListView.setAdapter(mAdapter);
        mListView.setOnRefreshListener(this);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                ProductInfo product = (ProductInfo) parent.getAdapter().getItem(
                        position);
                if (product == null)
                    return;
                CacheManager.getInstance().setCurrentProduct(product);
                Intent intent = new Intent(getActivity(), ProductActivity.class);
                startActivity(intent);
            }
        });

        Animation animation = new RotateAnimation(0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        animation.setDuration(800);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatMode(Animation.RESTART);
        rotateAnimation = animation;
    }

    protected void showMapActivity() {
        if (mProductList == null || mProductList.isEmpty()) {
            Toast.makeText(getActivity(), R.string.no_nearby_data_hint,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        //TODO google map handle
//        Intent intent = new Intent(getActivity(), MapActivity.class);
//        startActivity(intent);
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity()) != ConnectionResult.SUCCESS) {
            Toast.makeText(getActivity(), R.string.install_google_service, Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(getActivity(), GoogleMapActivity.class);
        startActivity(intent);
    }

    protected void initData() {
        mLocationListener = new BDLocationListener() {
            @Override
            public void onReceivePoi(BDLocation loaction) {
            }

            @Override
            public void onReceiveLocation(BDLocation location) {
                isRelocation = false;
                relocationBtn.clearAnimation();
                if (!dispalyErrorCode(location.getLocType())) {
                    setAddress(getActivity().getString(
                            R.string.reloaction_fail_hint));
                    loadingView.showMessage(R.string.reloaction_fail_hint,
                            false);
                    return;
                }

                loadingView.setVisibility(View.GONE);
                if (sMapListener != null) {
                    sMapListener.onReceiveLocation(location);
                    sMapListener.refreshMapWithLocationNearby(mProductList);
                }


                double lat = location.getLatitude();
                double lng = location.getLongitude();

                boolean dataChanged = lat != mLat || lng != mLng;
                mLat = lat;
                mLng = lng;
                setAddress(location.getAddrStr());

                if (mTask == null) {
                    mTask = new ProductListTask();
                    mTask.execute();
                } else if (dataChanged && requestLocation) {
                    requestLocation = false;
                    onRefresh();
                }
            }
        };
    }

    private void stopTask() {
        if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) {
            mTask.cancel(true);
            mTask = null;
        }
    }

    @Override
    public void onDestroy() {
        stopTask();
        if (useGoogleMap)
            GoogleLocationManager.INSTANCE.stopLocation();
        else
            BaiduLocationManager.getInstance().unRegisterLocationListener(mLocationListener);
        super.onDestroy();
    }

    protected boolean dispalyErrorCode(int errorCode) {
        if (errorCode == 61 || errorCode == 65) {
            // Toast.makeText(getActivity(), "GPS定位成功",
            // Toast.LENGTH_LONG).show();
            return true;
        } else if (errorCode == 161) {
            // Toast.makeText(getActivity(), "网络定位成功",
            // Toast.LENGTH_LONG).show();
            return true;
        } else {
//			Toast.makeText(getActivity(), String.format("定位失败，错误码为:%d",errorCode),
//                    Toast.LENGTH_LONG).show();
        }

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (useGoogleMap)
            GoogleLocationManager.INSTANCE.startLocation(getActivity());
        else
            BaiduLocationManager.getInstance().startService();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (useGoogleMap)
            GoogleLocationManager.INSTANCE.stopLocation();
        else
            BaiduLocationManager.getInstance().stopService();
    }

    private void reloadData() {
        stopTask();
        mTask = (ProductListTask) new ProductListTask().execute();
    }

    @Override
    public void onRefresh() {
        mCurrentPageNumber = 1;
        reloadData();
    }

    @Override
    public void onLoadMore() {
        ++mCurrentPageNumber;
        if (mCurrentPageNumber > mPageCount)
            return;
        reloadData();
        mListView.updateFooterState(FooterState.LOADING);
    }

    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @return true 表示开启
     */
    public boolean isOPenGPS() {
        LocationManager locationManager = (LocationManager) getActivity()
                .getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }

        return false;
    }

    @Override
    public boolean couldLoadMore() {
        return mCurrentPageNumber + 1 < mPageCount;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.reloaction_btn) {
            if (isRelocation) {
                return;
            }
            showLoadingAnimation();
            if (useGoogleMap)
                googleRelocation();
            else
                reLocation();
            requestLocation = true;
        }
    }

    private void googleRelocation() {
        GoogleLocationManager.INSTANCE.stopLocation();
        GoogleLocationManager.INSTANCE.startLocation(getActivity());
    }

    private void showLoadingAnimation() {
        // start aniamtion
        setAddress(getActivity().getString(R.string.loaction_tip));
        relocationBtn.startAnimation(rotateAnimation);
        isRelocation = true;
    }

    private void setAddress(String address) {
        if (TextUtils.isEmpty(address)) {
            return;
        }

        addressView.clearAnimation();
        final int startWidth = addressView.getWidth();
        addressView.setText(address);
        addressView.measure(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        final int endWidth = addressView.getMeasuredWidth();

        final int distance = endWidth - startWidth;
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime,
                                               Transformation t) {
                int dw = (int) (distance * interpolatedTime);
                int width = startWidth + dw;
                Log.d("Animation", "current width is : " + width);
                addressView.getLayoutParams().width = width;
                addressView.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        animation.setDuration(500);
        animation.setInterpolator(new LinearInterpolator());
        addressView.startAnimation(animation);
    }

    public interface MyLocationListener extends BDLocationListener {
        void refreshMapWithLocationNearby(List<ProductInfo> loactions);
    }

    private class ProductListTask extends
            AsyncTask<String, Void, ProductListInfo> {
        public ProductListTask() {
            super();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ProductListInfo doInBackground(String... params) {
            ProductListInfo res = null;
            try {
                res = FactoryCenter.getProcessCenter().getNearbyProductList(
                        mLng, mLat, mCurrentPageNumber);
            } catch (HttpResponseException e) {
                LogUtil.warn(e.getMessage(), e);
            } catch (IOException e) {
                LogUtil.warn(e.getMessage(), e);
            }
            return res;
        }

        @Override
        protected void onPostExecute(ProductListInfo result) {
            loadingView.setVisibility(View.GONE);
            if (result == null || result.getResult() == null) {
                // there must be some error,may be not connect network
                mListView.onRefreshComplete(true);
                mListView.updateFooterState(FooterState.ERROR);
                Toast.makeText(getActivity(), R.string.connecting_error,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            mPageCount = result.getResult().getPageEntity().getPageCount();
            if (mPageCount == 0) {
                mListView.onRefreshComplete(true);
                mListView.updateFooterState(FooterState.NO_DATA);
                return;
            }

            if (mCurrentPageNumber == 1) {
                mProductList.clear();
            }
            mProductList.addAll(result.getResult().getProductList());
            CacheManager.getInstance().setCachedProductList(mProductList);

            mAdapter.notifyDataSetChanged();
            if (mCurrentPageNumber == 1) {
                mListView.onRefreshComplete(false);
            } else {
                mListView.updateFooterState(FooterState.NORMAL);
            }
            if (mCurrentPageNumber >= mPageCount) {
                mListView.updateFooterState(FooterState.COMPLETED);
            }

        }

    }

}
