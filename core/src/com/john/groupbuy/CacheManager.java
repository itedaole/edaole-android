package com.john.groupbuy;

import java.util.ArrayList;
import java.util.List;

import com.john.groupbuy.lib.http.CategoryInfo;
import com.john.groupbuy.lib.http.CouponItemInfo;
import com.john.groupbuy.lib.http.ProductInfo;
import com.john.groupbuy.lib.http.WXConfig;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class CacheManager {
    private static CacheManager instance;
    private List<CategoryInfo> categoryList;
    private List<CouponItemInfo> couponList;
    private List<String> hotKeys;
    private ProductInfo currentProduct;
    private String versionName;
    private int versionCode;
    private int detailMaxLine = 0;
    private boolean needUpdate = true;
    private boolean needAutoLogin = true;
    private WXConfig wxConfig;
    private boolean zuitu = false;
    private String wxAppid;
    private boolean supportPush = false;
    private boolean supportUN;
    private boolean useGoogleMap;
    private List<ProductInfo> cachedProductList;
    private MixpanelAPI mixpanelAPI;

    public String getCustomId() {
        return customId;
    }

    public void setCustomId(String customId) {
        this.customId = customId;
    }

    private String customId;


    public boolean isZuitu() {
        return zuitu;
    }

    public void setZuitu(boolean zuitu) {
        this.zuitu = zuitu;
    }

    private CacheManager() {
        categoryList = new ArrayList<CategoryInfo>();
    }

    public static CacheManager getInstance() {
        if (instance == null)
            instance = new CacheManager();
        return instance;
    }

    public boolean isSupportUN() {
        return supportUN;
    }

    public void setSupportUN(boolean supportUN) {
        this.supportUN = supportUN;
    }

    public int getDetailMaxLine() {
        return detailMaxLine;
    }

    public void setDetailMaxLine(int detailMaxLine) {
        this.detailMaxLine = detailMaxLine;
    }

    public void reset() {
        needAutoLogin = true;
        needUpdate = true;
    }

    public List<CategoryInfo> getCategoryList() {
        return categoryList;
    }

    public void setCategoryList(List<CategoryInfo> categoryList) {
        if (categoryList == null || categoryList.isEmpty())
            return;
        for (int i = 0; i < categoryList.size(); i++) {
            CategoryInfo categoryInfo = categoryList.get(i);
            categoryInfo.setMainIndex(i);
            List<CategoryInfo> subList = categoryInfo.getSubClasss();
            if (subList == null || subList.isEmpty())
                continue;
            for (int j = 0; j < subList.size(); j++) {
                CategoryInfo subInfo = subList.get(j);
                subInfo.setMainIndex(i);
                subInfo.setSubIndex(j);
            }
        }
        this.categoryList = categoryList;
    }

    public ProductInfo getCurrentProduct() {
        return currentProduct;
    }

    public void setCurrentProduct(ProductInfo currentProduct) {
        this.currentProduct = currentProduct;
    }

    public List<String> getHotKeys() {
        return hotKeys;
    }

    public void setHotKeys(List<String> hotKeys) {
        this.hotKeys = hotKeys;
    }

    public List<CouponItemInfo> getCouponList() {
        return couponList;
    }

    public void setCouponList(List<CouponItemInfo> couponList) {
        this.couponList = couponList;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public boolean isNeedUpdate() {
        return needUpdate;
    }

    public void setNeedUpdate(boolean needUpdate) {
        this.needUpdate = needUpdate;
    }

    public boolean isNeedAutoLogin() {
        return needAutoLogin;
    }

    public void setNeedAutoLogin(boolean needAutoLogin) {
        this.needAutoLogin = needAutoLogin;
    }

    public WXConfig getWxConfig() {
        return wxConfig;
    }

    public void setWxConfig(WXConfig wxConfig) {
        this.wxConfig = wxConfig;
    }

    public void setWxAppId(String wxAppid) {
        this.setWxAppid(wxAppid);
    }

    public String getWxAppid() {
        return wxAppid;
    }

    public void setWxAppid(String wxAppid) {
        this.wxAppid = wxAppid;
    }

    public boolean supportWeiXin() {
        return wxAppid != null && wxAppid.length() != 0;
    }

    public void setSupportPush(boolean supportPush) {
        this.supportPush = supportPush;
    }

    public boolean isSupportPush() {
        return supportPush;
    }

    public void setUseGoogleMap(boolean useGoogleMap) {
        this.useGoogleMap = useGoogleMap;
    }

    public boolean isUseGoogleMap() {
        return useGoogleMap;
    }

    public void setCachedProductList(List<ProductInfo> cachedProductList) {
        this.cachedProductList = cachedProductList;
    }

    public List<ProductInfo> getCachedProductList() {
        return cachedProductList;
    }

    public void setMixpanelAPI(MixpanelAPI mixpanelAPI) {
        this.mixpanelAPI = mixpanelAPI;
    }

    public MixpanelAPI getMixpanelAPI() {
        return mixpanelAPI;
    }
}
