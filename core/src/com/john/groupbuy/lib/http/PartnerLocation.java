package com.john.groupbuy.lib.http;

import java.util.ArrayList;
import java.util.List;

public class PartnerLocation {
    public List<ProductInfo> products;
    public String title;
    public float lng;
    public float lat;
    
    public PartnerLocation() {
        products = new ArrayList<ProductInfo>();
    }
    
    public void resetData(){
        lng = 0.0f;
        lat  = 0.0f;
        products.clear();
    }
    
    public void addProduct(ProductInfo info) {
        products.add(info);
        lng = info._lng;
        lat = info._lat;
    }
}
