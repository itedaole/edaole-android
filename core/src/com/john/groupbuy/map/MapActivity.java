package com.john.groupbuy.map;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.utils.CoordinateConvert;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.john.groupbuy.BaseActivity;
import com.john.groupbuy.CacheManager;
import com.john.groupbuy.PartnerNearbyFragment;
import com.john.groupbuy.PartnerNearbyFragment.MyLocationListener;
import com.john.groupbuy.ProductActivity;
import com.john.groupbuy.R;
import com.john.groupbuy.adapter.SimpleProductAdapter;
import com.john.groupbuy.lib.http.PartnerLocation;
import com.john.groupbuy.lib.http.ProductInfo;
import com.john.groupbuy.map.CustomOverlay.CustomOverlayObserver;
import com.john.util.BaiduLocationManager;

public class MapActivity extends BaseActivity implements MyLocationListener,
		CustomOverlayObserver {

	protected BMapManager mBMapMan = null;
	protected MapView mMapView = null;
	private LocationData locData = new LocationData();
	MyLocationOverlay myLocationOverlay = null;
	private MKMapViewListener mMapListener;
	private MapController mMapController = null;
	private boolean mRelocationg = false;

	private ListView mPopupListView;

	private List<PartnerLocation> mPartners;
	private ProgressDialog mWaitingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		enableBackBehavior();
		setTitle(R.string.dd_map);
		mBMapMan = new BMapManager(getApplication());
		mBMapMan.init("AA3330B01C442D2CE49EB64C04E320D8163A0115",
				new MKGeneralListener() {
					@Override
					public void onGetNetworkState(int iError) {
						if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
							Toast.makeText(getApplicationContext(), "您的网络出错啦！",
									Toast.LENGTH_LONG).show();
						} else if (iError == MKEvent.ERROR_NETWORK_DATA) {
							Toast.makeText(getApplicationContext(),
									"输入正确的检索条件！", Toast.LENGTH_LONG).show();
						}
					}

					@Override
					public void onGetPermissionState(int iError) {
					}
				});
		// 注意：请在试用setContentView前初始化BMapManager对象，否则会报错

		setContentView(R.layout.map_activity);
		mMapView = (MapView) findViewById(R.id.mapView);

		mMapView.setBuiltInZoomControls(false); // 去掉缩放按钮
		mMapView.setLongClickable(true);

		mMapController = mMapView.getController();
		mMapController.setZoom(14); // [3-19]
		mMapController.enableClick(true);

		mMapListener = new MKMapViewListener() {

			@Override
			public void onMapMoveFinish() {
			}

			@Override
			public void onClickMapPoi(MapPoi mapPoiInfo) {
				String title = "";
				if (mapPoiInfo != null) {
					title = mapPoiInfo.strText;
					Toast.makeText(MapActivity.this, title, Toast.LENGTH_SHORT)
							.show();
				}
			}

			@Override
			public void onGetCurrentMap(Bitmap b) {
			}

			@Override
			public void onMapAnimationFinish() {
			}
		};

		enableBackBehavior();
		ImageButton location = (ImageButton) findViewById(R.id.locationBtn);
		location.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MapActivity.this.requestGetLocation();
			}
		});
		ImageButton zoomIn = (ImageButton) findViewById(R.id.btn_zoom_in);
		zoomIn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MapActivity.this.zoomIn();
			}
		});
		ImageButton zoomOut = (ImageButton) findViewById(R.id.btn_zoom_out);
		zoomOut.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MapActivity.this.zoomOut();
			}
		});

		mMapView.regMapViewListener(mBMapMan, mMapListener);
		PartnerNearbyFragment.setMapLocationListner(this);

		mPartners = new ArrayList<PartnerLocation>();
		myLocationOverlay = new MyLocationOverlay(mMapView);

		requestGetLocation();
	}

	protected void zoomIn() {
		if (mMapView.getMaxZoomLevel() == mMapView.getZoomLevel()) {
			Toast.makeText(this, "已经缩放到最大了~", Toast.LENGTH_SHORT).show();
			return;
		}
		mMapController.zoomIn();
	}

	protected void zoomOut() {
		if (mMapView.getMinZoomLevel() == mMapView.getZoomLevel()) {
			Toast.makeText(this, "已经缩放到最小了~", Toast.LENGTH_SHORT).show();
			return;
		}
		mMapController.zoomOut();
	}

	protected void requestGetLocation() {
		PartnerNearbyFragment.reLocation();
		mRelocationg = true;
		mWaitingDialog = new ProgressDialog(this);
		mWaitingDialog.setMessage("正在获取定位信息，请稍候...");
		mWaitingDialog.show();
	}


	@Override
	protected void onDestroy() {
		mMapView.destroy();
		PartnerNearbyFragment.setMapLocationListner(null);
		if (mBMapMan != null) {
			mBMapMan.destroy();
			mBMapMan = null;
		}
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		BaiduLocationManager.getInstance().stopService();
		mMapView.onPause();
		if (mBMapMan != null) {
			mBMapMan.stop();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		BaiduLocationManager.getInstance().startService();
		mMapView.onResume();
		if (mBMapMan != null) {
			mBMapMan.start();
		}

		addOverlayToMapView();
	}

	@Override
	public void onReceiveLocation(BDLocation location) {
		if (location == null)
			return;

		if (mWaitingDialog != null) {
			mWaitingDialog.dismiss();
			mWaitingDialog = null;
		}


			locData.latitude = location.getLatitude();
			locData.longitude = location.getLongitude();
			locData.accuracy = location.getRadius();
		myLocationOverlay.setData(locData);

	}

	protected void animateToMyLocation() {
		mMapController.animateTo(new GeoPoint((int) (locData.latitude * 1e6),
				(int) (locData.longitude * 1e6)));
	}

	@Override
	public void onReceivePoi(BDLocation poiLocation) {
		if (poiLocation == null) {
			return;
		}
	}

	protected int getPartnerIndex(ProductInfo info) {
		if (info._lat > 100.0f) {
			float temp = info._lat;
			info._lat = info._lng;
			info._lng = temp;
		}
		int index = -1;
		for (PartnerLocation partner : mPartners) {
			index++;
			if (partner.lat == info._lat && partner.lng == info._lng) {
				return index;
			}
		}
		return -1;
	}

	protected void handleProductInfo(List<ProductInfo> list) {
		for (ProductInfo info : list) {
			PartnerLocation partner = null;
			int index = getPartnerIndex(info);
			if (index == -1) {
				partner = new PartnerLocation();
				partner.addProduct(info);
				mPartners.add(partner);
				continue;
			}
			partner = mPartners.get(index);
			partner.addProduct(info);
		}
	}

	@Override
	public void refreshMapWithLocationNearby(List<ProductInfo> locations) {
		if (locations == null || locations.isEmpty())
			return;
		mPartners.clear();
		handleProductInfo(locations);
		addOverlayToMapView();

	}

	protected void addOverlayToMapView() {

		if (mPartners.isEmpty())
			return;

		// clear all overlay
		if (mMapView.getOverlays().size() != 0) {
			mMapView.getOverlays().clear();
			mMapView.refresh();
		}

		Drawable drawable = getResources().getDrawable(R.drawable.ic_poi);
		drawable.setBounds(0, 0, 50, 50);
		// add partner location to map
		for (PartnerLocation partner : mPartners) {
			CustomOverlay overlay = new CustomOverlay(drawable, mMapView);
			overlay.setObserver(this);
			mMapView.getOverlays().add(overlay);
			GeoPoint point = new GeoPoint((int) (partner.lat * 1e6),
					(int) (partner.lng * 1e6));

			OverlayItem item = new OverlayItem(point, "", "");
			overlay.addItem(item);
		}

		// mMapController.setZoom(14);
		mMapView.refresh();

		// add my location to map
		mMapView.getOverlays().add(myLocationOverlay);
		mMapView.refresh();

		if (mRelocationg) {
			mMapController.animateTo(new GeoPoint(
					(int) (locData.latitude * 1e6),
					(int) (locData.longitude * 1e6)));
			mRelocationg = false;
		}
	}

	@Override
	public void onTap(GeoPoint point, MapView view) {
		hidePopupLayer();
	}

	@Override
	public void onTap(int index) {

		if (index < 0 || index > mPartners.size() - 1)
			return;

		showPopupListView(index);
	}

	protected Bitmap generatePopupBitmap(ProductInfo product) {

		float maxWidth = mMapView.getWidth() * 0.6f;
		Paint painter = new Paint(Paint.ANTI_ALIAS_FLAG);
		painter.setTextSize(20);
		String string = product.partner.getTitle();
		float maxHeight = 100;

		Bitmap bitmap = Bitmap.createBitmap((int) maxWidth, (int) maxHeight,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.TRANSPARENT);

		// draw background
		Bitmap background = BitmapFactory.decodeResource(getResources(),
				R.drawable.map_popup_bg);
		byte[] chunk = background.getNinePatchChunk();
		NinePatchDrawable patchy = new NinePatchDrawable(getResources(),
				background, chunk, new Rect(), null);
		patchy.setBounds(new Rect(0, 0, (int) maxWidth, (int) maxHeight));
		patchy.draw(canvas);

		Rect bounds = new Rect();
		painter.getTextBounds(string, 0, string.length() - 1, bounds);

		float fontHeight = bounds.top - bounds.bottom;

		// draw text
		float x = 0.0f, y = 0.0f;
		float textWidth = painter.measureText(string);
		x = (maxWidth - textWidth) / 2;
		y = (maxHeight - fontHeight) / 2 - 10f;
		canvas.drawText(string, x, y, painter);

		return bitmap;
	}

	protected void showPopupListView(int index) {
		PartnerLocation partner = mPartners.get(index);
		if (partner == null)
			return;

		hidePopupLayer();

		CustomOverlay overlay = (CustomOverlay) mMapView.getOverlays().get(
				index);
		mMapController.setCenter(overlay.getCenter());
		mMapView.refresh();

		mPopupListView = (ListView) getLayoutInflater().inflate(
				R.layout.popup_product_view, null);
		SimpleProductAdapter adapter = new SimpleProductAdapter(this,
				partner.products);
		mPopupListView.setAdapter(adapter);

		mPopupListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				ProductInfo info = (ProductInfo) arg0.getAdapter()
						.getItem(arg2);
				if (info == null)
					return;
				CacheManager.getInstance().setCurrentProduct(info);
				showProductActivity();
			}

		});

		GeoPoint point = new GeoPoint((int) (partner.lat * 1E6),
				(int) (partner.lng * 1E6));

		int width = getResources().getDimensionPixelSize(
				R.dimen.map_popup_width);
		int height = getResources().getDimensionPixelSize(
				R.dimen.map_popup_height);
		MapView.LayoutParams params = new MapView.LayoutParams(width, height,
				point, 0, -50, MapView.LayoutParams.BOTTOM_CENTER);
		mMapView.addView(mPopupListView, params);

	}

	protected void hidePopupLayer() {
		if (mPopupListView != null) {
			mMapView.removeView(mPopupListView);
			mPopupListView = null;
		}
	}

	protected void showProductActivity() {
		Intent intent = new Intent(this, ProductActivity.class);
		startActivity(intent);
	}
}
