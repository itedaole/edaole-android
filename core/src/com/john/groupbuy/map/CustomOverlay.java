package com.john.groupbuy.map;

import android.graphics.drawable.Drawable;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class CustomOverlay extends ItemizedOverlay<OverlayItem> {
	
	interface CustomOverlayObserver {
		void onTap(GeoPoint point, MapView view);
		void onTap(int index);
	}
	
	protected CustomOverlayObserver mObserver;

    public CustomOverlay(Drawable drawable, MapView view) {
        super(drawable, view);
    }
    
    public void setObserver(CustomOverlayObserver observer){
    	mObserver = observer;
    }
    
    //tap on point
    @Override
    public boolean onTap(GeoPoint point, MapView view) {
        super.onTap(point, view);
        if(mObserver != null)
        	mObserver.onTap(point, view);
        return false;
    }
    
    //tap on index
    @Override
    protected boolean onTap(int index) {
        super.onTap(index);
        if(mObserver != null)
        	mObserver.onTap(index);
        return true;
    }
    
    
}
