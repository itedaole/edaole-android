package com.groupbuy.yidaole.wxapi;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.os.Handler;
import android.os.Message;

abstract public class BaseTask implements Runnable {
	
	public final static int CODE_FAILURE = 0;
	public final static int CODE_SUCCESS = 1;
	
	private int taskId;
	private Handler handler;
	private Object object;
	protected Gson parser;

	/**
	 * constructor
	 * @param taskId the running task id
	 * @param handler handler for call back 
	 */
	public BaseTask(int taskId, Handler handler) {
		super();
		this.taskId = taskId;
		this.handler = handler;
		GsonBuilder builder = new GsonBuilder();
		builder.serializeNulls();
		parser = builder.create();
	}

	@Override
	public void run() {
		try {
			onRunningTask();
			sendMessage(CODE_SUCCESS);
			return;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		sendMessage(CODE_FAILURE);
	}

	/**
	 * Subclass must implement this function to handle task
	 * @throws JSONException JSON parse error
	 * @throws IOException  IO Exception
	 * @throws ClientProtocolException  HTTP exception
	 */
	protected abstract void onRunningTask() throws JSONException, IOException,
			ClientProtocolException;

	/**
	 * send running result with code {@link #CODE_SUCCESS} or {@link #CODE_FAILURE}
	 * @param code
	 */
	private void sendMessage(int code){
		if(handler == null)
			return;
		Message message = handler.obtainMessage();
		message.arg1 = taskId;
		message.arg2 = code;
		message.obj = getObject();
		handler.sendMessage(message);
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

}
