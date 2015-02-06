package com.john.groupbuy;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.john.groupbuy.adapter.CategoryAdapter;
import com.john.groupbuy.lib.http.CategoryInfo;

import java.util.ArrayList;
import java.util.List;

public class SelectCategoryActivity extends BaseActivity implements
        OnClickListener, OnItemClickListener {

    final private List<CategoryInfo> categoryInfoList = CacheManager.getInstance()
            .getCategoryList();
    private LinearLayout categoryLayout;
    private int margin = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableBackBehavior();
        setContentView(R.layout.activity_select_category);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        margin = (int) (10 * metrics.density);
        initCategoryViews();

    }

    private void initCategoryViews() {
        findViewById(R.id.all_category).setOnClickListener(this);
        categoryLayout = (LinearLayout) findViewById(R.id.select_category_container);
        if (categoryInfoList == null || categoryInfoList.size() < 2)
            return;
        List<CategoryInfo> subCategoryInfos = categoryInfoList.subList(1,
                categoryInfoList.size());
        for (int i = 0; i < subCategoryInfos.size(); i++) {
            boolean isLastIndex = (i == subCategoryInfos.size() - 1);
            addCategoryToLayout(subCategoryInfos.get(i), isLastIndex);
        }
    }

    private void addCategoryToLayout(CategoryInfo categoryInfo, boolean isLastIndex) {
        if (categoryInfo == null)
            return;

        List<CategoryInfo> categoryList = categoryInfo.getSubClasss();
        if (categoryList == null || categoryList.isEmpty()) {
            categoryList = new ArrayList<CategoryInfo>();
            categoryList.add(categoryInfo);
        }

        View rootView = getLayoutInflater().inflate(
                R.layout.item_gridview_category, null);
        TextView categoryView = (TextView) rootView
                .findViewById(R.id.category_name);
        ExpandableHeightGridView gridView = (ExpandableHeightGridView) rootView
                .findViewById(R.id.sub_category);
        gridView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //disable move event
                if (event.getAction() == MotionEvent.ACTION_MOVE)
                    return true;
                return false;
            }
        });
        gridView.setOnItemClickListener(this);
        categoryView.setText(categoryInfo.name);

        CategoryAdapter adapter = new CategoryAdapter(this);
        gridView.setAdapter(adapter);
        gridView.setExpanded(true);
        adapter.setAdapterList(categoryList);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 0);
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        params.topMargin = margin;
        if (isLastIndex)
            params.bottomMargin = margin * 2;
        categoryLayout.addView(rootView, params);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.all_category) {
            CategoryInfo firstItem = categoryInfoList.get(0);
            Intent intent = new Intent(SelectCategoryActivity.this,
                    ProductListActivity.class);
            intent.putExtra(ProductListActivity.ARG_CATEGORY_INFO, firstItem);
            startActivity(intent);
        }
    }

    @Override
    public void
    onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CategoryInfo categoryInfo = (CategoryInfo) parent.getAdapter().getItem(position);
        if (categoryInfo == null)
            return;
        Intent intent = new Intent(this, ProductListActivity.class);
        intent.putExtra(ProductListActivity.ARG_CATEGORY_INFO, categoryInfo);
        startActivity(intent);
    }

}
