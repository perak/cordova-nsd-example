package com.perak.plugin;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import android.net.nsd.NsdServiceInfo;

import com.perak.plugin.NsdHelper;

public class Disco extends CordovaPlugin {

    NsdHelper mNsdHelper;
    private Handler mHandler;
    ChatConnection mConnection;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("initChat")) {
            this.initChat(callbackContext);
        }

        if (action.equals("advertizeChat")) {
            this.advertizeChat(callbackContext);
        }

        if (action.equals("discoverChat")) {
            this.discoverChat(callbackContext);
        }

        if (action.equals("connectChat")) {
            this.connectChat(callbackContext);
        }

        if (action.equals("sendChatMessage")) {
        	String messageString = args.getString(0);
            this.sendChatMessage(callbackContext, messageString);
        }

		PluginResult.Status status = PluginResult.Status.NO_RESULT;
		PluginResult pluginResult = new PluginResult(status);
		pluginResult.setKeepCallback(true);
		callbackContext.sendPluginResult(pluginResult);
		return true;
    }

    private void initChat(CallbackContext callbackContext) {
    	final CallbackContext cbc = callbackContext;
    	try {
	        mHandler = new Handler() {
	                @Override
	            public void handleMessage(Message msg) {
	            	String type = msg.getData().getString("type");
	                String message = msg.getData().getString("msg");
//	            	if(type == "error") {
//	            		cbc.error(message);
//	            		return;
//	            	}

	            	JSONObject data = new JSONObject();
	            	try {
		            	data.put("type", new String(type));
		            	data.put("data", new String(message));
	            	} catch(JSONException e) {

	            	}

					PluginResult result = new PluginResult(PluginResult.Status.OK, data);
					result.setKeepCallback(true);
					cbc.sendPluginResult(result);
	            }
	        };

	        mConnection = new ChatConnection(mHandler);

	        mNsdHelper = new NsdHelper(cordova.getActivity(), mHandler);
	        mNsdHelper.initializeNsd();

    	} catch(Exception e) {
			callbackContext.error("Error " + e);
    	}
    }

    private void advertizeChat(CallbackContext callbackContext) {
    	final CallbackContext cbc = callbackContext;

		if(mConnection.getLocalPort() > -1) {
			mNsdHelper.registerService(mConnection.getLocalPort());
		} else {
			cbc.error("ServerSocket isn't bound.");
		}
    }

    private void discoverChat(CallbackContext callbackContext) {
    	final CallbackContext cbc = callbackContext;
        mNsdHelper.discoverServices();
    }


    private void connectChat(CallbackContext callbackContext) {
    	final CallbackContext cbc = callbackContext;
        NsdServiceInfo service = mNsdHelper.getChosenServiceInfo();
        if (service != null) {
            mConnection.connectToServer(service.getHost(),
                    service.getPort());
        } else {
            cbc.error("No service to connect to!");
        }
    }


    private void sendChatMessage(CallbackContext callbackContext, String messageString) {
        mConnection.sendMessage(messageString);
    }
}
