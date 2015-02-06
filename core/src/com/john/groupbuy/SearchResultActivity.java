package com.john.groupbuy;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import com.john.groupbuy.adapter.ProductListAdapter;
import com.john.groupbuy.lib.FactoryCenter;
import com.john.groupbuy.lib.http.*;
import com.john.util.HttpResponseException;
import com.john.util.LogUtil;

import java.io.IOException;

public class SearchResultActivity extends BaseActivity implements
        OnItemClickListener, DragListView.OnLoadingMoreListener {

    public final static String FIELD_SEARCH_KEY = "search_key_word";
    private DragListView listView;
    private ProductListAdapter adapter;
    private SearchTask task;
    private LoadingView loadingView;
    private PageEntity pageEntity;
    private String keyWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        enableBackBehavior();
        setTitle(R.string.search);
        initView();
        resolveIntent();
    }

    private void initView() {
        listView = (DragListView) findViewById(R.id.search_result_listview);
        adapter = new ProductListAdapter(SearchResultActivity.this, null);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnRefreshListener(this);
        loadingView = (LoadingView) findViewById(R.id.loading_view);
        loadingView.showMessage(R.string.loading_data_hint, true);
    }

    private void resolveIntent() {
        Intent intent = getIntent();
        if (intent == null)
            return;
        keyWord = intent.getStringExtra(FIELD_SEARCH_KEY);
        searchKey();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        ProductInfo product = (ProductInfo) parent.getAdapter().getItem(position);
        if (product == null)
            return;
        CacheManager.getInstance().setCurrentProduct(product);
        Intent intent = new Intent(SearchResultActivity.this,
                ProductActivity.class);
        startActivity(intent);
    }

    private void searchKey() {
        task = new SearchTask();
        task.execute();
    }

    @Override
    protected void onDestroy() {
        if (task == null) {
            task.cancel(true);
            task = null;
        }
        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        pageEntity = null;
        searchKey();
    }

    @Override
    public boolean couldLoadMore() {
        if (pageEntity == null)
            return true;
        return !pageEntity.isLastPage();
    }

    @Override
    public void onLoadMore() {
        searchKey();
    }

    private class SearchTask extends AsyncTask<String, Void, ProductListInfo> {
        @Override
        protected ProductListInfo doInBackground(String... params) {
            int page = 1;
            if (pageEntity != null)
                page = pageEntity.getCurrentPage() + 1;
            String url = Interface.DEFAULT_APP_HOST
                    + String.format("Tuan/goodsList&key=%s&page=%d", keyWord, page);
            try {
                return FactoryCenter.getProcessCenter()
                        .getProductListByUrl(url);
            } catch (HttpResponseException e) {
                LogUtil.warn(e.getMessage(), e);
            } catch (IOException e) {
                LogUtil.warn(e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(ProductListInfo result) {
            if (loadingView.getVisibility() == View.VISIBLE)
                loadingView.setVisibility(View.GONE);
            listView.onRefreshComplete(true);
            if (result == null || result.getResult() == null ||
                    !result.getResult().isDataAvailable()) {
                // there must be some error,may be not connect network
                listView.updateFooterState(DragListView.FooterState.ERROR);
                showToast(R.string.connecting_error);
                return;
            }

            ResultInfo resultInfo = result.getResult();
            if (resultInfo.isDataEmpty()) {
                listView.updateFooterState(DragListView.FooterState.NO_DATA);
                showToast(R.string.search_no_data_hint);
                return;
            }

            pageEntity = resultInfo.getPageEntity();
            //update  header and footer state
            if (pageEntity.isLastPage()) {
                listView.updateFooterState(DragListView.FooterState.COMPLETED);
            } else {
                listView.updateFooterState(DragListView.FooterState.NORMAL);
            }
            if (pageEntity.isFirstPage()) {
                adapter.setAdapterData(resultInfo.getProductList());
            } else {
                adapter.addAdapterData(resultInfo.getProductList());
            }
        }
    }
}
