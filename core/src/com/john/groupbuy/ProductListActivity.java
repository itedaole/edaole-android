package com.john.groupbuy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import com.john.groupbuy.lib.http.CategoryInfo;

public class ProductListActivity extends BaseActivity {

    public static final String ARG_CATEGORY_INFO = "ARG_CATEGORY_INFO";
    private CategoryInfo currentInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableBackBehavior();
        setContentView(R.layout.activity_product_list);
        Fragment fragment = new ProductListFragment();
        
        if(savedInstanceState == null){
        	Intent intent = getIntent();
        	if (intent != null) 
        		currentInfo = intent.getParcelableExtra(ARG_CATEGORY_INFO);
        }else{
        	currentInfo = savedInstanceState.getParcelable(ARG_CATEGORY_INFO);
        }
        
        if (currentInfo != null)
            setTitle(currentInfo.name);
        else
            setTitle(R.string.all);
        
        if(currentInfo != null){
        	String selectedCatagoryId = currentInfo.id;
        	if (!TextUtils.isEmpty(selectedCatagoryId)) {
        		Bundle args = new Bundle();
        		args.putParcelable(ProductListFragment.ARG_CATEGORTY_DATA, currentInfo);
        		fragment.setArguments(args);
        	}
        }
        FragmentTransaction ts = getSupportFragmentManager().beginTransaction();
        ts.add(R.id.fragment_container, fragment);
        ts.addToBackStack(null);
        ts.commit();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ARG_CATEGORY_INFO, currentInfo);
    }
    
    @Override
    public void onBackPressed() {
    	finish();
    }

}
