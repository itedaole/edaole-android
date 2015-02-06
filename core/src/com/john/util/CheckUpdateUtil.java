package com.john.util;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.john.groupbuy.CacheManager;
import com.john.groupbuy.R;
import com.john.groupbuy.lib.FactoryCenter;
import com.john.groupbuy.lib.http.CheckUpdateInfo;
import com.john.groupbuy.lib.http.Interface;
import com.nostra13.universalimageloader.utils.L;

public class CheckUpdateUtil extends BroadcastReceiver{
    private WeakReference<Context> contextRef;
    private static CheckUpdateUtil instance;
    private CheckUpdateTask updateTask;
    private CheckUpdateInfo updateInfo;
    private boolean isRunningTask = false;
    private boolean isDownloading = false;
    private OnClickListener acceptListener;
    private long currentTaskId;
    private final String TAG = getClass().getSimpleName();
    
    private DownloadManager manager ;
    private boolean onLaunchApp = false;
    
    private String apkPath;
    
    private CheckUpdateUtil(){
        //do nothing
    }
    
    public static CheckUpdateUtil getInstance(){
        if(instance == null){
            instance = new CheckUpdateUtil();
        }
        return instance;
    }
    
    private void setContext(Context context){
        if(context == null)
            return;
        contextRef = new WeakReference<Context>(context);
    }
    
    private Context getContext(){
        if(contextRef != null && contextRef.get()!= null)
            return contextRef.get();
        return null;
    }
    
    public void CheckUpdate(Context context , boolean onLaunch){
    	if(context == null)
    		return;
    	setContext(context);
        if(isRunningTask){
            Toast.makeText(getContext(), R.string.check_updateing_hint, Toast.LENGTH_SHORT).show();
            return;
        }
        onLaunchApp = onLaunch;
        execCheckUpdate();
    }
    
    public void onDestory(){
        stopTask();
    }
    
    private void execCheckUpdate(){
        stopTask();
        updateTask = new CheckUpdateTask();
        String format = "/Index/checkUpdate&ver=%d";
        int versionCode = CacheManager.getInstance().getVersionCode();
        String updateUrl  = Interface.DEFAULT_APP_HOST + String.format(format, versionCode);
        updateTask.execute(updateUrl);
        isRunningTask = true;
    }
    
    public void stopTask(){
        if(updateTask != null){
            updateTask.cancel(true);
            updateTask = null;
        }
    }
    
    private void onCheckUpdateCompleted(CheckUpdateInfo info){
    	updateInfo = info;
    	if(info == null){
    		if(!onLaunchApp)
    			Toast.makeText(getContext(), R.string.check_update_failure, Toast.LENGTH_SHORT).show();
    		return;
    	}

    	if(!checkUpdateInfoOk() ){
    		if(!onLaunchApp)
    			Toast.makeText(getContext(), R.string.no_need_update_hint, Toast.LENGTH_SHORT).show();            
    		return;
    	}

    	acceptListener = new OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int which) {
    			//start download apk
    			downloadApk();
    		}
    	};

    	String message = "无";
    	if(!TextUtils.isEmpty(updateInfo.getDes())){
    		message = getContext().getString(R.string.check_update_message)+updateInfo.getDes().replace("\\n", "\n");
    	}

    	if(onLaunchApp){
    		AlertDialog dialog = new AlertDialog.Builder(getContext())
    		.setTitle(R.string.check_update)
    		.setMessage(message)
    		.setPositiveButton(R.string.accept_update, acceptListener)
//    		.setNeutralButton(R.string.never_hint, new OnClickListener() {
//    			@Override
//    			public void onClick(DialogInterface dialog, int which) {
//    				Context context = getContext();
//    				if(context == null)
//    					return;
//    				SharedPreferences  sp = 
//    						context.getSharedPreferences(GlobalKey.SHARE_PREFERS_NAME,
//    								Context.MODE_PRIVATE);
//    				SharedPreferences.Editor editor = sp.edit();
//    				editor.putBoolean(GlobalKey.CHECK_UPDATE_KEY, false);
//    				editor.commit();
//    			}
//    		})
    		.setNegativeButton(R.string.show_next_time, null)
    		.create();

    		dialog.show();
    	}else{
    		AlertDialog dialog = new AlertDialog.Builder(getContext())
    		.setTitle(R.string.check_update)
    		.setMessage(message)
    		.setPositiveButton(R.string.accept_update, acceptListener)
    		.setNegativeButton(R.string.cancel_update, null)
    		.create();

    		dialog.show();
    	}


    }
    
    private boolean checkUpdateInfoOk(){
        if(updateInfo == null)
            return false;
        
        int serverVersionCode = 0;
        try{
            serverVersionCode = Integer.valueOf( updateInfo.getVersionCode());
        }catch (NumberFormatException e){
            e.printStackTrace();
        }
        
        if(serverVersionCode <= CacheManager.getInstance().getVersionCode())
            return false;
        
        if(!TextUtils.isEmpty(updateInfo.getDownloadUrl()))
            return true;
        return false;
    }
    
    private void downloadApk(){
        if(isDownloading)
            return;
        

        String downloadUrl = updateInfo.getDownloadUrl();
        apkPath = getApkFileStoragePath();
        if(TextUtils.isEmpty(apkPath)){
            Toast.makeText(getContext(), R.string.no_space_hint, Toast.LENGTH_SHORT).show();
            return;
        }
        
        File apkFile = new File(apkPath);
        if(apkFile.exists()){
            installPackage(apkPath);
            return;
        }
        
        manager =
                (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request =
                new DownloadManager.Request(Uri.parse(downloadUrl));
        request.setDestinationUri(Uri.parse("file://"+apkPath));
        currentTaskId = manager.enqueue(request);
        isDownloading = true;
    }
    
    private File getExternalCacheDir(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "cache");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                L.e("Unable to create external cache directory");
                return null;
            }
            try {
                new File(appCacheDir, ".nomedia").createNewFile();
            } catch (IOException e) {
                L.e("Can't create \".nomedia\" file in application external cache directory");
            }
        }
        return appCacheDir;
    }
    
    private String getApkFileStoragePath() {
        String path = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dir = getExternalCacheDir(getContext());
            if (dir != null) {
                path = dir.getAbsolutePath() + "/" + getApkFileName();
            } else {
                path = getContext().getDir("temp", Context.MODE_PRIVATE).getAbsolutePath() + "/"
                        + getApkFileName();
            }
        } else {
            path = getContext().getDir("temp", Context.MODE_PRIVATE).getAbsolutePath() + "/"
                    + getApkFileName();
        }
        return path;
    }
    
    private String getApkFileName() {
        return getContext().getPackageName() + "_v" + updateInfo.getVersionCode() + ".apk";
    }
    
    private class CheckUpdateTask extends AsyncTask<String, Void, CheckUpdateInfo>{
        @Override
        protected CheckUpdateInfo doInBackground(String... params) {
            String url = params[0];
            try {
                return FactoryCenter.getProcessCenter().getCheckUpdateInfo(url);
            } catch (HttpResponseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(CheckUpdateInfo result) {
            super.onPostExecute(result);
            isRunningTask = false;
            onCheckUpdateCompleted(result);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null)
            return;
        
        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L);
        if(currentTaskId != id)
            return;
        
        isDownloading = false;
        
        Query query = new Query();
        query.setFilterById(currentTaskId);
        
        Cursor cursor = manager.query(query);
        if(cursor == null)
        	return;
        
        if(cursor.moveToFirst()){
            String uri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            int reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            Log.e(TAG, "file uri is "+uri + " file status is "+status +  " file reason " + reason);
            switch (status) {
                case DownloadManager.STATUS_SUCCESSFUL:
                    Toast.makeText(getContext(), R.string.download_ok_hint, Toast.LENGTH_SHORT).show();
                    installPackage(apkPath);
                    break;
                default:
                    Toast.makeText(getContext(), R.string.download_apk_fail_hint, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        cursor.close();
    }
    
    private void installPackage(String apkPath){
        File apkfile =  new File(apkPath);
        if (!apkfile.exists()) {
            return;
        }
        apkfile.setExecutable(true, false);
        apkfile.setReadable(true, false);
        apkfile.setWritable(true, false);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(apkfile), "application/vnd.android.package-archive");
        getContext().startActivity(intent);
    }
    
}
