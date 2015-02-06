package com.john.groupbuy;

import java.io.IOException;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.john.groupbuy.adapter.ExpressListAdapter;
import com.john.groupbuy.lib.FactoryCenter;
import com.john.groupbuy.lib.http.ExpressListInfo;
import com.john.groupbuy.lib.http.MyExpressInfo;
import com.john.util.HttpResponseException;

public class ExpressActivity extends BaseActivity implements OnItemClickListener {

    private ExpressListAdapter mExpressAdapter;
    private ExpressListTask mExpressListTask;
    private ListView mListView;
    private LoadingView loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon_list);
        mListView = (ListView) findViewById(R.id.listview);
        mListView.setOnItemClickListener(this);

        loadingView = (LoadingView) findViewById(R.id.loading_view);
        
        requestExpressList();

        enableBackBehavior();
        setTitle(R.string.title_express);
    }

    protected void requestExpressList() {

    	loadingView.showMessage(R.string.loading_data_hint, true);
        mExpressListTask = new ExpressListTask();
        mExpressListTask.execute();

    }

    protected class ExpressListTask extends AsyncTask<String, Void, ExpressListInfo> {

        @Override
        protected ExpressListInfo doInBackground(String... params) {
            ExpressListInfo info = null;
            try {
                info = FactoryCenter.getProcessCenter().getExpressListInfo();
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
        protected void onPostExecute(ExpressListInfo result) {
            super.onPostExecute(result);
            if (result == null || result.getExpressList() == null) {
                Toast.makeText(ExpressActivity.this, getString(R.string.load_data_failure), Toast.LENGTH_SHORT).show();
                loadingView.showMessage(R.string.load_data_failure, false);
                return;
            }
            
            List<MyExpressInfo> data = result.getExpressList();
            if (data.isEmpty()){
            	showNoDataView();
            	return;
            }
            
            loadingView.setVisibility(View.GONE);
            if (mExpressAdapter == null) {
                mExpressAdapter = new ExpressListAdapter(ExpressActivity.this, result.getExpressList());
            } else {
                mExpressAdapter.addListData(data, true);
            }
            mListView.setAdapter(mExpressAdapter);

        }
    }

    protected void showNoDataView() {
    	loadingView.showMessage(R.string.no_data_tips, false);
        mListView.setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

}
