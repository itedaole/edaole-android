package com.john.groupbuy;

import java.io.IOException;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.john.groupbuy.DragListView.FooterState;
import com.john.groupbuy.DragListView.OnLoadingMoreListener;
import com.john.groupbuy.adapter.OrderListAdapter;
import com.john.groupbuy.lib.FactoryCenter;
import com.john.groupbuy.lib.http.MyOrderInfo;
import com.john.groupbuy.lib.http.MyOrderInfoItem;
import com.john.groupbuy.lib.http.MyOrderInfoNew;
import com.john.groupbuy.lib.http.PageEntity;
import com.john.groupbuy.lib.http.ProductInfo;
import com.john.util.HttpResponseException;
import com.john.util.LogUtil;

public class OrderActivity extends BaseActivity implements OnItemClickListener,
		OnLoadingMoreListener {

	private DragListView mListView;
	private GetOrderListTask mTask;
	private OrderListAdapter mAdapter;
	private MyOrderInfoItem mCurrentOrderInfo;
	private LoadingView loadingView;
	private PageEntity pageEntity;
	private LoadingType loadingType = LoadingType.LOADING_TYPE_REFRESHING;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_title_list);
		mListView = (DragListView) findViewById(R.id.listview);
		mListView.setOnItemClickListener(this);
		mListView.setOnRefreshListener(this);
		loadingView = (LoadingView) findViewById(R.id.loading_view);
		requestOrderList();
		enableBackBehavior();
		setTitle(R.string.my_order);
		displayLoadingView();
		
		mAdapter = new OrderListAdapter(OrderActivity.this,null);
		mListView.setAdapter(mAdapter);
	}

	protected void requestOrderList() {
		mTask = new GetOrderListTask();
		mTask.execute();
	}

	protected void displayLoadingView() {
		loadingView.showMessage(R.string.loading_data_hint, true);
	}

	protected void hideLoadingView() {
		loadingView.setVisibility(View.GONE);
	}

	protected void showNoDataView() {
		loadingView.showMessage(R.string.no_data_tips, false);
		mListView.setVisibility(View.GONE);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mCurrentOrderInfo = (MyOrderInfoItem) parent.getAdapter().getItem(position);
		if(mCurrentOrderInfo == null)
			return;
		ProductInfo product = mCurrentOrderInfo.team;
		if (product == null) {
			Toast.makeText(this, R.string.proudct_no_details,
					Toast.LENGTH_SHORT).show();
			return;
		}
		CacheManager.getInstance().setCurrentProduct(product);
		Intent intent = new Intent(this, ProductActivity.class);
		startActivity(intent);
	}

	@Override
	public void onRefresh() {
		pageEntity = null;
		loadingType = LoadingType.LOADING_TYPE_REFRESHING;
		requestOrderList();
	}

	@Override
	public boolean couldLoadMore() {
		if(pageEntity != null){
			return !pageEntity.isLastPage();
		}
		return false;
	}

	@Override
	public void onLoadMore() {
		loadingType = LoadingType.LOADING_TYPE_MORE;
		requestOrderList();
	}

	private class GetOrderListTask extends AsyncTask<String, Void, Object> {
		@Override
		protected Object doInBackground(String... params) {
			try {
				int pageIndex = 1;
				if(pageEntity != null && loadingType == LoadingType.LOADING_TYPE_MORE)
					pageIndex = pageEntity.getCurrentPage()+1;
				return FactoryCenter.getProcessCenter().getOrderList(pageIndex,KEY_PAGE_SIZE);
			} catch (HttpResponseException e) {
				LogUtil.warn(e.getMessage(), e);
			} catch (IOException e) {
				LogUtil.warn(e.getMessage(), e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			hideLoadingView();
			if(result == null){
				mListView.onRefreshComplete(true);
				mListView.updateFooterState(FooterState.ERROR);
				return;
			}
			
			if(result instanceof MyOrderInfo){
				MyOrderInfo data = (MyOrderInfo) result;
				mListView.onRefreshComplete(true);
				if(data.getOrderInfoList() == null){
					mListView.updateFooterState(FooterState.ERROR);
				}
				else{
					mAdapter.replaceWith(data.getOrderInfoList());
					mListView.updateFooterState(FooterState.COMPLETED);
					if (mAdapter.getCount() == 0) {
						showNoDataView();
					}
				}
			}else if(result instanceof MyOrderInfoNew){
				MyOrderInfoNew data = (MyOrderInfoNew) result;
				if(loadingType == LoadingType.LOADING_TYPE_MORE){
					if( data.getOrderData() == null){
						mListView.updateFooterState(FooterState.ERROR);
					}
					else{
						mListView.updateFooterState(FooterState.NORMAL);
						mAdapter.addData(data.getOrderData().getOrderList());
						pageEntity = data.getOrderData().getPageEntity();
					}
				}else{
					mListView.onRefreshComplete(true);
					if(data.getOrderData() == null){
						mListView.updateFooterState(FooterState.ERROR);
					}else{
						mAdapter.replaceWith(data.getOrderData().getOrderList());
						mListView.updateFooterState(FooterState.NORMAL);
						pageEntity = data.getOrderData().getPageEntity();
						if (mAdapter.getCount() == 0) {
							showNoDataView();
						}
					}
				}
			}
			if(!couldLoadMore())
				mListView.updateFooterState(FooterState.COMPLETED);
		}
	}
}
