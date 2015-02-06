package com.john.groupbuy.citylist;

import java.util.Comparator;

import com.john.groupbuy.lib.http.CityItem;

public class PinyinComparator implements Comparator<CityItem> {
	@Override
	public int compare(CityItem lhs, CityItem rhs) {
        String str1 =lhs.getPingying();
        String str2 =rhs.getPingying();
        return str1.compareTo(str2);
	}

}
