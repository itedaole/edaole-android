package com.john.groupbuy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.john.groupbuy.DragListView.FooterState;
import com.john.groupbuy.DragListView.OnLoadingMoreListener;
import com.john.groupbuy.adapter.PopupWindowListAdapter;
import com.john.groupbuy.adapter.PopupWindowListAdapter.OnItemClickedListener;
import com.john.groupbuy.adapter.ProductListAdapter;
import com.john.groupbuy.lib.FactoryCenter;
import com.john.groupbuy.lib.http.*;
import com.john.groupbuy.lib.http.ProductListInfo;
import com.john.util.HttpResponseException;
import com.john.util.LogUtil;
import com.john.util.Utility;
import com.umeng.analytics.MobclickAgent;

/**
 * @author luckystar
 * 
 */
public class ProductListFragment extends Fragment implements OnClickListener,
        OnLoadingMoreListener {

	public final static String ALL_PRODUCT_TYPE_ID = "0";
	public static final String ARG_DISPLAY_CATEGORY = "ARG_DISPLAY_CATEGORY";
	public final static String ARG_CATEGORTY_DATA = "ARG_CATEGORTY_DATA";
//	public static final int INIT_PAGE_NUMBER = 1;
//
//	private final int ITEM_IDS[] = new int[] { R.id.category_1,
//	        R.id.category_2, R.id.category_3, R.id.category_4, R.id.category_5,
//	        R.id.category_6, R.id.category_7, R.id.category_8 };

	private DragListView productListView;
	private ProductListAdapter adapter;
	private CategoryListTask categoryTask;
	private ProductListTask productListTask;
	private AutoLoginTask loginTask;
	private List<CategoryInfo> categoryList;
	private List<CategoryInfo> subCategoryList;
	private List<CategoryInfo> sortOrderList;
	private Button categoryBtn;
	private Button sortOrderBtn;
	private Button cityAction;
	private List<ProductInfo> productList;
	private PopupWindowHolder popupHolder;
	private PopupWindow popupWindow;
	private LoadingView loadingView;
	private FrameLayout headerView;
	private LinearLayout actionGroup;
	private PageEntity pageEntity;

	private String currentCategoryId = ALL_PRODUCT_TYPE_ID;
	private String currentSortId = "sort_order";
	private String cityId = GlobalKey.ALL_CITY_ID;
	private String cityName;
	private boolean showSortList = false;
	private boolean showCatrgory = false;
	private static List<CityItem> sCityList = null;

	private OnClickListener categoryClickedListener;
	private String currentTitle;

	private TextView actionBarTitle;

	private boolean refreshing = false;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		MobclickAgent.onError(activity);
		currentTitle = getString(R.string.app_name);
		setTitle(currentTitle);
	}

	private void setTitle(String title) {
		if (actionBarTitle != null)
			actionBarTitle.setText(title);
		currentTitle = title;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		BaseActivity activity = (BaseActivity) getActivity();
		if (activity == null)
			return;
		ActionBar actionbar = activity.getSupportActionBar();
		actionbar.setDisplayShowCustomEnabled(true);
		actionbar.setCustomView(R.layout.action_bar_main);

		cityAction = (Button) actionbar.getCustomView().findViewById(
		        R.id.city_button);
		cityAction.setOnClickListener(this);
		if(!TextUtils.isEmpty(cityName))
			cityAction.setText(cityName);
		actionbar.getCustomView().findViewById(R.id.search_button)
		        .setOnClickListener(this);
		actionBarTitle = (TextView) actionbar.getCustomView().findViewById(
		        R.id.action_bar_title);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			setTitle(currentTitle);
			BaseActivity activity = (BaseActivity) getActivity();
			activity.getSupportActionBar().setDisplayShowCustomEnabled(true);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	        Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.product_fragment, null);

		productListView = (DragListView) rootView
		        .findViewById(R.id.products_list_view);
		loadingView = (LoadingView) rootView.findViewById(R.id.loading_layout);
		actionGroup = (LinearLayout) rootView.findViewById(R.id.action_group);
		categoryBtn = (Button) rootView.findViewById(R.id.category_btn);
		categoryBtn.setOnClickListener(this);
		sortOrderBtn = (Button) rootView.findViewById(R.id.sort_btn);
		sortOrderBtn.setOnClickListener(this);

		// initial sort list
		initSortList();
		popupHolder = new PopupWindowHolder();
		productList = new ArrayList<ProductInfo>();

		productListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
			        int position, long id) {
				Object object = (ProductInfo) parent.getAdapter().getItem(
				        position);
				if (object == null)
					return;
				CacheManager.getInstance().setCurrentProduct(
				        (ProductInfo) object);
				Intent intent = new Intent(getActivity(), ProductActivity.class);
				startActivity(intent);
			}
		});

		productListView.setOnRefreshListener(this);

		// obtain city id from share preferences
		SharedPreferences sharedPref = getActivity().getSharedPreferences(
		        GlobalKey.SHARE_PREFERS_NAME, Context.MODE_PRIVATE);
		cityId = sharedPref.getString(GlobalKey.SELECTED_CITY_ID,
		        GlobalKey.ALL_CITY_ID);
		cityName = sharedPref.getString(GlobalKey.SELECTED_CITY_NAME,
		        GlobalKey.ALL_CITY_NAME);

		if (cityAction != null)
			cityAction.setText(cityName);

		resolveArgument();

		if (showCatrgory) {
			headerView = new FrameLayout(getActivity());
			productListView.addHeaderView(headerView);
			actionGroup.setVisibility(View.GONE);
		} else {
			loadingView.setVisibility(View.GONE);
		}

		adapter = new ProductListAdapter(getActivity(), productList);
		productListView.setAdapter(adapter);

		// update categories list
		if(categoryList == null)
			categoryList = CacheManager.getInstance().getCategoryList();
		if(subCategoryList == null)
			subCategoryList = new ArrayList<CategoryInfo>();
		if (showCatrgory) {
			if (categoryList.isEmpty()) {
				stopTask(categoryTask);
				categoryTask = new CategoryListTask();
				categoryTask.execute();
				loadingView.showMessage(R.string.loading_data_hint, true);
			} else {
				updateCategoryItems();
			}
		}

		// fetch product list from network
		loadMoreProductList();
		// auto login if need
		if (CacheManager.getInstance().isNeedAutoLogin()) {
			autoLogin();
			CacheManager.getInstance().setNeedAutoLogin(false);
		}

		return rootView;
	}
	
	private void resolveArgument(){
		Bundle bundle = getArguments();
		if(bundle == null)
			return;
		showCatrgory = bundle.getBoolean(ARG_DISPLAY_CATEGORY, false);
		CategoryInfo categoryInfo = bundle.getParcelable(ARG_CATEGORTY_DATA);
		if(categoryInfo == null || TextUtils.isEmpty(categoryInfo.id))
			return;
		if (categoryInfo.id.equalsIgnoreCase(CategoryInfo.CATEGORY_NEW)) {
			currentSortId = "begin_time";
			sortOrderBtn.setText(R.string.latest_publish);
			setTitle(getString(R.string.all));
			popupHolder.sortAdapter.setSelectedIndex(5);
		} else {
			currentCategoryId = categoryInfo.id;
			String selectedName = categoryInfo.name;
			if (!TextUtils.isEmpty(selectedName)) {
				categoryBtn.setText(selectedName);
				currentTitle = selectedName;
				setTitle(currentTitle);
			}
			if(categoryList == null )
				categoryList = CacheManager.getInstance().getCategoryList();
			if(categoryList == null || categoryList.isEmpty())
				return;

			int mainIndex = categoryInfo.getMainIndex();
			int subIndex = categoryInfo.getSubIndex();
			if(mainIndex == -1)
				mainIndex = 0;
			popupHolder.mainMenuAdapter.setSelectedIndex(mainIndex);
			subCategoryList = categoryList.get(mainIndex).getSubClasss();
			if(subCategoryList == null || subCategoryList.size() == 0)
				return;
			if(subIndex == -1)
				subIndex = 0;
			popupHolder.subMenuAdapter.setList(subCategoryList);
			popupHolder.subMenuAdapter.setSelectedIndex(subIndex);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		super.onCreateOptionsMenu(menu, inflater);
	}

	public static void setCityList(List<CityItem> citylist) {
		sCityList = citylist;
	}

	public static List<CityItem> getCityList() {
		return sCityList;
	}

	private void updateCategoryItems() {
		if (categoryList == null || categoryList.size() < 1 || !showCatrgory)
			return;
		if (categoryClickedListener == null)
			categoryClickedListener = new OnClickListener() {
				@Override
				public void onClick(View v) {
					// Animation animation = AnimationUtils.loadAnimation(
					// getActivity(), R.anim.fade_out);
					// v.startAnimation(animation);

					CategoryInfo category = (CategoryInfo) v.getTag();
					if (category == null)
						return;

					if (category.id == CategoryInfo.CATEGORY_ALL) {
						// show select category activity
						Intent intent = new Intent(getActivity(),
						        SelectCategoryActivity.class);
						startActivity(intent);
					} else {
						Intent intent = new Intent(getActivity(),
						        ProductListActivity.class);
						intent.putExtra(ProductListActivity.ARG_CATEGORY_INFO,
						        category);
						startActivity(intent);
					}

				}
			};

		final List<CategoryInfo> list = categoryList.subList(1, categoryList
		        .size());
//		headerView.findViewById(R.id.category_top).setVisibility(View.VISIBLE);
//		if (list.size() > 2)
//			headerView.findViewById(R.id.category_bottom).setVisibility(
//			        View.VISIBLE);
		headerView.removeAllViews();
		int deviceWidth = getResources().getDisplayMetrics().widthPixels;
		int cellSize = deviceWidth / 4;
		//int headerHeight = cellSize*2;
		int headerHeight = cellSize;

		AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(
				AbsListView.LayoutParams.MATCH_PARENT,
				headerHeight);
		headerView.setLayoutParams(layoutParams);
		
		int index = 0;
		for (CategoryInfo category : list) {
			if (index >= 4) {//原来是6
				break;
			}
			setCategoryData(index, category,cellSize);
			index++;
		}
		/*新注释掉的
		setCategoryData(index, new CategoryInfo(getString(R.string.all),
		        CategoryInfo.CATEGORY_ALL),cellSize);
		index++;
		setCategoryData(index, new CategoryInfo(
		        getString(R.string.catagory_new), CategoryInfo.CATEGORY_NEW),cellSize);
		//zzz
		 * */
		 
	}
	private void setCategoryData(int index, CategoryInfo category,int cellSize) {
//		int id = ITEM_IDS[index];
//		Button button = (Button) headerView.findViewById(id);
//		button.setOnClickListener(categoryClickedListener);
//		button.setVisibility(View.VISIBLE);
//		button.setTag(category);
//		button.setText(category.getDisplayName());
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View item = inflater.inflate(R.layout.category_header_item, null);
		ImageView imageView = (ImageView) item.findViewById(R.id.category_item_icon);
		imageView.setImageResource(getIconResource(category.id));
		TextView textView = (TextView) item.findViewById(R.id.category_item_label);
		textView.setText(category.name);
		item.setOnClickListener(categoryClickedListener);
		item.setTag(category);

		int col = index % 4;
		int row = index / 4;
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(0,0);
		params.width = cellSize;
		params.height = cellSize;
		params.leftMargin = col * cellSize;
		params.topMargin = row * cellSize;
		headerView.addView(item,params);
	}

	private int getIconResource(String id) {
		if (id.equalsIgnoreCase(CategoryInfo.CATEGORY_NEW))
			return R.drawable.category_new;

		try {
			int nid = Integer.valueOf(id);
			switch (nid)
			{
				case 0:
					return R.drawable.category_all;
				case 5:
					return R.drawable.category_5;
				case 6:
					return R.drawable.category_6;
				case 7:
					return R.drawable.category_7;
				case 9:
					return R.drawable.category_9;
			}
		}catch (NumberFormatException e){
			e.printStackTrace();
		}
		return R.drawable.category_all;
	}

	protected class AutoLoginTask extends AsyncTask<String, Void, LoginResult> {

		@Override
		protected LoginResult doInBackground(String... params) {
			final String username = params[0];
			final String password = params[1];

			LoginResult loginResult = null;
			try {
				loginResult = FactoryCenter.getUserInfoCenter().userLogin(
				        username, password);
			} catch (HttpResponseException e) {
				LogUtil.warn(e.getMessage(), e);
			} catch (IOException e) {
				LogUtil.warn(e.getMessage(), e);
			}
			return loginResult;
		}

		@Override
		protected void onPostExecute(LoginResult result) {
			if (result != null) {
				if (result.mErrorInfo == null) {
					GroupBuyApplication.sIsUserLogin = true;
					GroupBuyApplication.sIsPartnerLogin = false;
					GroupBuyApplication.sBindingPhone = result.mResultInfo.mobile;
					GroupBuyApplication.sUserInfo = result.mResultInfo;
				}
			}
		}
	}

	private void loadMoreProductList() {
		execProductListTask();
		productListView.updateFooterState(FooterState.LOADING);
	}

	private void execProductListTask() {
		stopTask(productListTask);
		productListTask = new ProductListTask();
		productListTask.execute();
	}

	private void hidePopupWindow() {
		if (popupWindow != null) {
			popupWindow.dismiss();
			popupWindow = null;
		}
	}

	protected class PopupWindowHolder {
		public ListView mainMenu;
		public ListView subMenu;
		public PopupWindowListAdapter mainMenuAdapter;
		public PopupWindowListAdapter subMenuAdapter;
		public PopupWindowListAdapter sortAdapter;

		public PopupWindowHolder() {
			mainMenuAdapter = new PopupWindowListAdapter(getActivity(),
			        new ArrayList<CategoryInfo>());
			mainMenuAdapter.setSubMenu(false);
			subMenuAdapter = new PopupWindowListAdapter(getActivity(),
			        new ArrayList<CategoryInfo>());
			subMenuAdapter.setSubMenu(true);
			sortAdapter = new PopupWindowListAdapter(getActivity(),
			        sortOrderList);
			sortAdapter.setListener(new OnItemClickedListener() {
				// item click listener for sort list
				@Override
				public void onItemClicked(CategoryInfo info, int index) {
					hidePopupWindow();
					setInitPageNumber();
					currentSortId = info.id;
					sortOrderBtn.setText(info.name);
					loadingView.showMessage(R.string.loading_data_hint, true);
					loadMoreProductList();
				}
			});
			sortAdapter.setSubMenu(false);
		}

		public void inflate(View rootView) {
			mainMenu = (ListView) rootView.findViewById(R.id.pop_up_menu);
			if (showSortList) {
				mainMenu.setAdapter(sortAdapter);
				if (sortAdapter.getSelectedIndex() != -1)
					mainMenu.setSelection(sortAdapter.getSelectedIndex());
			} else {
				mainMenu.setAdapter(mainMenuAdapter);
				mainMenuAdapter.setList(categoryList);
				if (mainMenuAdapter.getSelectedIndex() != -1)
					mainMenu.setSelection(mainMenuAdapter.getSelectedIndex());
			}

			mainMenuAdapter.setListener(new OnItemClickedListener() {
				@Override
				public void onItemClicked(CategoryInfo info, int index) {
					// toggled when user press main menu
					if (info == null) {
						hidePopupWindow();
						return;
					}

					subCategoryList = info.getSubClasss();
					subMenuAdapter.setList(subCategoryList);

					if (info.getSubClasss() == null
					        || info.getSubClasss().isEmpty()) {
						hidePopupWindow();
						setInitPageNumber();
						currentCategoryId = info.id;
						categoryBtn.setText(info.name);
						setTitle(info.name);
						loadingView.showMessage(R.string.loading_data_hint,
						        true);
						loadMoreProductList();
						return;
					}
					int height = popupWindow.getHeight();
					int originWidth = popupWindow.getWidth();
					int width = calculateWidth();
					if (originWidth != width) {
						popupWindow.update(width, height);
					}
				}
			});

			subMenu = (ListView) rootView.findViewById(R.id.pop_up_submenu);
			if (showSortList) {
				subMenu.setVisibility(View.GONE);
				return;
			}
			subMenu.setAdapter(subMenuAdapter);
			if (subMenuAdapter.getSelectedIndex() != -1)
				subMenu.setSelection(subMenuAdapter.getSelectedIndex());
			subMenuAdapter.setListener(new OnItemClickedListener() {
				@Override
				public void onItemClicked(CategoryInfo info, int index) {
					// toggled when user click sub menu
					hidePopupWindow();
					if (info == null)
						return;
					setInitPageNumber();
					currentCategoryId = info.id;
					categoryBtn.setText(info.name);
					setTitle(info.name);
					loadingView.showMessage(R.string.loading_data_hint, true);
					loadMoreProductList();
				}
			});
		}
	}

	protected class CategoryListTask extends
	        AsyncTask<String, Void, CategoryListInfo> {
		@Override
		protected CategoryListInfo doInBackground(String... params) {
			CategoryListInfo info = null;
			try {
				info = FactoryCenter.getProcessCenter().getCouponCategory();
			} catch (HttpResponseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return info;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(CategoryListInfo result) {
			super.onPostExecute(result);
			loadingView.setVisibility(View.GONE);
			if (result != null && result.result != null) {
				List<CategoryInfo> categories = result.result;
				
				Iterator<CategoryInfo> iterator = categories.iterator();
				while(iterator.hasNext()){
					CategoryInfo categoryInfo = iterator.next();
					if(categoryInfo.isAvailable())
						categoryInfo.insertSelfCategory();
					else
						iterator.remove();
				}
				categoryList = categories;
				//TODO
				CategoryInfo firstCategory = new CategoryInfo();
				firstCategory.id = ALL_PRODUCT_TYPE_ID;
				firstCategory.name = getString(R.string.all_product);
				categoryList.add(0, firstCategory);
				CacheManager.getInstance().setCategoryList(categoryList);
				updateCategoryItems();
			}else{
				Toast.makeText(getActivity(), R.string.connecting_error, Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class ProductListTask extends
	        AsyncTask<String, Void, ProductListInfo> {
		public ProductListTask() {
			super();
		}

		@Override
		protected ProductListInfo doInBackground(String... params) {
			ProductListInfo res = null;
			try {
				String url = getRequestArguments();
				res = FactoryCenter.getProcessCenter().getProductListByUrl(url);
			} catch (HttpResponseException e) {
				LogUtil.warn(e.getMessage(), e);
			} catch (IOException e) {
				LogUtil.warn(e.getMessage(), e);
			}

			return res;
		}

		@Override
		protected void onPostExecute(ProductListInfo result) {
			loadingView.setVisibility(View.GONE);
			if (refreshing) {
				productList.clear();
				productListView.onRefreshComplete(true);
				refreshing = false;
			}
			if (result == null || result.getResult() == null) {
				productListView.updateFooterState(FooterState.ERROR);
				Toast.makeText(getActivity(), R.string.connecting_error,
				        Toast.LENGTH_SHORT).show();
				return;
			}

			pageEntity = result.getResult().getPageEntity();
			if (pageEntity.getCount() == 0) {
				productListView.onRefreshComplete(true);
				productList.clear();
				adapter.notifyDataSetChanged();
				productListView.updateFooterState(FooterState.NO_DATA);
				Toast.makeText(getActivity(), R.string.no_product_tips,
				        Toast.LENGTH_SHORT).show();
				return;
			}

			productList.addAll(result.getResult().getProductList());
			adapter.notifyDataSetChanged();
			if (pageEntity.isLastPage()) {
				productListView.updateFooterState(FooterState.COMPLETED);
			} else {
				productListView.updateFooterState(FooterState.NORMAL);
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.category_btn) {
			showSortList = false;
			showPopupWindow();
		} else if (v.getId() == R.id.sort_btn) {
			showSortList = true;
			showPopupWindow();
		} else if (v.getId() == R.id.search_button) {
			startActivity(new Intent(getActivity(), SearchActivity.class));
		} else if (v.getId() == R.id.city_button) {
			// show city list activity
			startActivityForResult(new Intent(getActivity(),
			        CityListActivity.class), 1);
		}

	}

	protected void showPopupWindow() {
		if (popupWindow != null) {
			popupWindow = null;
		}
		LayoutInflater inflater = (LayoutInflater) getActivity()
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View popupView = inflater.inflate(R.layout.pop_up_window, null);
		popupHolder.inflate(popupView);

		int width = 0;
		if (showSortList)
			width = sortOrderBtn.getWidth();
		else {
			width = actionGroup.getWidth();
		}

		int height = (int) (productListView.getHeight() * 0.8f);

		int heightHint = calculateHeight(0, 40);
		if (heightHint < height)
			height = heightHint;

		int widthHint = calculateWidth();
		if (widthHint < width)
			width = widthHint;

		popupWindow = new PopupWindow(popupView, width, height, true);
		popupWindow.setTouchable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setTouchInterceptor(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					hidePopupWindow();
					return true;
				}
				return false;
			}
		});

		// You must set the background of popup window
		popupWindow.setBackgroundDrawable(getResources().getDrawable(
		        R.drawable.popup_bg));
		// popupWindow.setAnimationStyle(R.style.PopupAnimation);
		if (showSortList)
			popupWindow.showAsDropDown(sortOrderBtn);
		else
			popupWindow.showAsDropDown(actionGroup);

		popupWindow.update();
	}

	public Rect locateView(View v) {
		int[] loc_int = new int[2];
		if (v == null)
			return null;
		try {
			v.getLocationOnScreen(loc_int);
		} catch (NullPointerException npe) {
			// Happens when the view doesn't exist on screen anymore.
			return null;
		}
		Rect location = new Rect();
		location.left = loc_int[0];
		location.top = loc_int[1];
		location.right = location.left + v.getWidth();
		location.bottom = location.top + v.getHeight();
		return location;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
			cityAction.setText(data.getStringExtra("city"));
			cityId = data.getStringExtra("id");
			// save current city id
			SharedPreferences sharedPref = getActivity().getSharedPreferences(
			        GlobalKey.SHARE_PREFERS_NAME, Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putString(GlobalKey.SELECTED_CITY_ID, cityId);
			editor.putString(GlobalKey.SELECTED_CITY_NAME,
			        data.getStringExtra("city"));
			editor.commit();

			setInitPageNumber();
			loadMoreProductList();
		}
	}

	@Override
	public void onRefresh() {
		refreshing = true;
		pageEntity = null;
		if (showCatrgory && (categoryList == null || categoryList.isEmpty())) {
			stopTask(categoryTask);
			categoryTask = new CategoryListTask();
			categoryTask.execute();
		}
		execProductListTask();
	}

	@Override
	public void onLoadMore() {
		loadMoreProductList();
	}

	protected void autoLogin() {
		String[] results = Utility.readUserConfig(getActivity());
		if (TextUtils.isEmpty(results[0]) || TextUtils.isEmpty(results[1])) {
			return;
		}
		AutoLoginTask task = new AutoLoginTask();
		task.execute(results[0], results[1]);
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(getActivity());
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(getActivity());
		setTitle(currentTitle);
	}

	protected String getRequestArguments() {
		int pageNum = 1;
		if (pageEntity != null && !pageEntity.isLastPage())
			pageNum = pageEntity.getCurrentPage() + 1;
		String url = Interface.DEFAULT_APP_HOST
		        + String.format(
		                "Tuan/goodsList&type=%s&city_id=%s&page=%d&orderby=%s",
		                currentCategoryId, cityId, pageNum, currentSortId);
		return url;
	}

	protected void initSortList() {
		sortOrderList = new ArrayList<CategoryInfo>();
		sortOrderList.add(new CategoryInfo(getString(R.string.order_default),
		        "sort_order"));
		sortOrderList.add(new CategoryInfo(getString(R.string.more_expensive),
		        "team_price"));
		sortOrderList.add(new CategoryInfo(getString(R.string.more_cheap),
		        "-team_price"));
		sortOrderList.add(new CategoryInfo(getString(R.string.more_popular),
		        "now_number"));
		sortOrderList.add(new CategoryInfo(getString(R.string.low_popular),
		        "-now_number"));
		sortOrderList.add(new CategoryInfo(getString(R.string.latest_publish),
		        "begin_time"));
		sortOrderList.add(new CategoryInfo(
		        getString(R.string.oldest_published), "-begin_time"));
	}

	@Override
	public boolean couldLoadMore() {
		return (pageEntity != null) && !pageEntity.isLastPage();
	}

	private void stopTask(AsyncTask<?, ?, ?> task) {
		if (task != null && task.getStatus() != Status.FINISHED)
			task.cancel(true);
	}

	@Override
	public void onDestroyView() {
		stopTask(loginTask);
		stopTask(categoryTask);
		stopTask(productListTask);
		super.onDestroyView();
	}

	private void setInitPageNumber() {
		pageEntity = null;
		if (adapter != null)
			adapter.clearAdapterData();
	}

	private int calculateHeight(int itemDivider, int itemHeight) {
		int size = 0;
		if (showSortList)
			size = sortOrderList.size();
		else
			size = categoryList.size();
		float density = getResources().getDisplayMetrics().density;
		int diverHeight = (int) (itemDivider * density) * (size - 1);
		int height = (int) (itemHeight * density) * size;
		return height + diverHeight;
	}

	private int calculateWidth() {

		if (showSortList)
			return sortOrderBtn.getWidth();

		int size = 0;
		if (subCategoryList != null)
			size = subCategoryList.size();

		int width = productListView.getWidth();
		if (size == 0) {
			popupHolder.subMenu.setVisibility(View.GONE);
			width /= 2;
		} else {
			popupHolder.subMenu.setVisibility(View.VISIBLE);
		}
		return width;
	}

}
