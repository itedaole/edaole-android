package com.john.groupbuy.map;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.john.groupbuy.BaseActivity;
import com.john.groupbuy.CacheManager;
import com.john.groupbuy.ProductActivity;
import com.john.groupbuy.R;
import com.john.groupbuy.adapter.ProductListAdapter;
import com.john.groupbuy.lib.http.PartnerLocation;
import com.john.groupbuy.lib.http.ProductInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for display google map view
 */
public class GoogleMapActivity extends BaseActivity {
    private GoogleMap googleMap;
    private List<PartnerLocation> partnerLocations;
    private List<Marker> markerList;
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_map_activity);
        initViews();
        initMarkers();
    }

    private void initMarkers() {
        List<ProductInfo> productInfoList = CacheManager.getInstance().getCachedProductList();
        if (productInfoList == null)
            return;

        partnerLocations = new ArrayList<PartnerLocation>();
        handleProductInfo(productInfoList);

        markerList = new ArrayList<Marker>();

        CameraUpdate cameraUpdate = null;
        for (PartnerLocation location : partnerLocations) {
            if (cameraUpdate == null)
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(location.lat,location.lng),15);
            Marker marker = googleMap.addMarker(createMarker(location.lat, location.lng, location.title));
            markerList.add(marker);
        }

        googleMap.moveCamera(cameraUpdate);

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                int position = markerList.indexOf(marker);
                if (position == -1)
                    return false;
                showPopupListView(position);
                return false;
            }
        });
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                hidePopupWindow();
            }
        });
    }

    private void hidePopupWindow() {
        if (popupWindow != null) {
            popupWindow.dismiss();
            popupWindow = null;
        }
    }

    private void showPopupListView(int position) {

        PartnerLocation location = partnerLocations.get(position);
        if (location == null || location.products == null)
            return;


        hidePopupWindow();

        View parent = findViewById(R.id.google_map_layout);
        ListView listView = new ListView(this);
        ProductListAdapter adapter = new ProductListAdapter(this, location.products);
        listView.setAdapter(adapter);
        listView.setBackgroundColor(getResources().getColor(R.color.bg_color));
        listView.setDivider(null);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ProductInfo productInfo = (ProductInfo) parent.getAdapter().getItem(position);
                if (productInfo == null)
                    return;
                CacheManager.getInstance().setCurrentProduct(productInfo);
                startActivity(new Intent(GoogleMapActivity.this, ProductActivity.class));
            }
        });

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = (int) (metrics.heightPixels * 0.3f);
        listView.setLayoutParams(new ViewGroup.LayoutParams(width, height));
        popupWindow = new PopupWindow(listView, width, height);
        popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
    }

    protected void handleProductInfo(List<ProductInfo> list) {
        for (ProductInfo info : list) {
            PartnerLocation partner = null;
            int index = getPartnerIndex(info);
            if (index == -1) {
                partner = new PartnerLocation();
                partner.addProduct(info);
                partnerLocations.add(partner);
                continue;
            }
            partner = partnerLocations.get(index);
            partner.addProduct(info);
        }
    }

    protected int getPartnerIndex(ProductInfo info) {
        if (info._lat > 100.0f) {
            float temp = info._lat;
            info._lat = info._lng;
            info._lng = temp;
        }
        int index = -1;
        for (PartnerLocation partner : partnerLocations) {
            index++;
            if (partner.lat == info._lat && partner.lng == info._lng) {
                return index;
            }
        }
        return -1;
    }

    @Override
    protected void onDestroy() {
        hidePopupWindow();
        super.onDestroy();
    }

    private MarkerOptions createMarker(double lat, double lng, String title) {
        return new MarkerOptions()
                .draggable(false)
                .position(new LatLng(lat, lng))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_poi))
                .title(title);
    }

    private void initViews() {
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        googleMap = fragment.getMap();
        if (googleMap == null)
            return;
        googleMap.setMyLocationEnabled(true);
        initUiSetting();
    }

    private void initUiSetting() {
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setScrollGesturesEnabled(true);
        uiSettings.setZoomGesturesEnabled(true);
        uiSettings.setTiltGesturesEnabled(true);
        uiSettings.setRotateGesturesEnabled(true);
    }
}
