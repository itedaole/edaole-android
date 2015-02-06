package com.john.groupbuy;

import java.io.IOException;
import java.util.List;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.john.groupbuy.adapter.HotKeyAdapter;
import com.john.groupbuy.lib.FactoryCenter;
import com.john.groupbuy.lib.http.HotKeyInfo;
import com.john.util.HttpResponseException;

public class SearchActivity extends BaseActivity implements OnClickListener,
		OnItemClickListener {

	private ImageButton mSearchButton;
	private AutoCompleteTextView mSearchEditText;
	private HotKeyTask hotKeyTask;
	private HotKeyAdapter adapter;
	private GridView gridView;
	private LoadingView loadingView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		enableBackBehavior();
		setTitle(R.string.search);

		initView();
		List<String> hotKeys = CacheManager.getInstance().getHotKeys();
		if (hotKeys == null) {
			requestHotKeys();
			return;
		}
		onPostData(hotKeys);

	}

	@Override
	protected void onDestroy() {
		if (hotKeyTask != null) {
			hotKeyTask.cancel(true);
			hotKeyTask = null;
		}
		super.onDestroy();
	}

	private void initView() {
		mSearchEditText = (AutoCompleteTextView) findViewById(R.id.deal_search_txt);
		mSearchEditText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					hideInputMethod();
					quest();
					return true;
				} else if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
					hideInputMethod();
					quest();
				}
				return false;
			}
		});

		mSearchButton = (ImageButton) findViewById(R.id.search_go_btn);
		mSearchButton.setOnClickListener(this);

		adapter = new HotKeyAdapter(this);

		gridView = (GridView) findViewById(R.id.hot_keys_view);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(this);

		loadingView = (LoadingView) findViewById(R.id.loading_view);

	}

	private void requestHotKeys() {
		gridView.setVisibility(View.INVISIBLE);
		loadingView.showMessage(R.string.loading_search_hotkey_hint, true);
		hotKeyTask = new HotKeyTask();
		hotKeyTask.execute();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.search_go_btn) {
			hideInputMethod();
			quest();
		}
	}

	private void quest() {
		if (!checkUserInput())
			return;
		String key = mSearchEditText.getText().toString();
		Intent intent = new Intent(this, SearchResultActivity.class);
		intent.putExtra(SearchResultActivity.FIELD_SEARCH_KEY, key);
		startActivity(intent);
	}

	private boolean checkUserInput() {
		String key = mSearchEditText.getText().toString();
		if (TextUtils.isEmpty(key)) {
			Toast.makeText(this, R.string.search_key_hint, Toast.LENGTH_SHORT)
					.show();
			return false;
		}
		return true;
	}

	protected void hideInputMethod() {
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		if (imm != null && this.getCurrentFocus() != null) {
			imm.hideSoftInputFromWindow(
					this.getCurrentFocus().getWindowToken(), 0);
		}
	}

	private void onPostData(List<String> hotKeys) {
		if (hotKeys.isEmpty()) {
			loadingView.showMessage(R.string.no_search_hot_key_hint, false);
			gridView.setVisibility(View.INVISIBLE);
		} else {
			loadingView.setVisibility(View.GONE);
			gridView.setVisibility(View.VISIBLE);
			adapter.setAdapterData(hotKeys);
		}
	}

	private class HotKeyTask extends AsyncTask<Void, Void, HotKeyInfo> {
		@Override
		protected HotKeyInfo doInBackground(Void... params) {
			try {
				HotKeyInfo info = FactoryCenter.getProcessCenter().getHotKeys();
				return info;
			} catch (HttpResponseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(HotKeyInfo result) {
			if (result == null || result.getResult() == null) {
				loadingView.showMessage(R.string.no_search_hot_key_hint, false);
				return;
			}
			CacheManager.getInstance().setHotKeys(result.getResult());
			onPostData(result.getResult());
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		String key = (String) parent.getAdapter().getItem(position);
		if (TextUtils.isEmpty(key))
			return;
		Intent intent = new Intent(this, SearchResultActivity.class);
		intent.putExtra(SearchResultActivity.FIELD_SEARCH_KEY, key);
		startActivity(intent);
	}

}
