package com.perak.plugin;

import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.net.nsd.NsdManager;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class NsdHelper {

    Context mContext;
    private Handler mUpdateHandler;
    private Handler mLogHandler;

    NsdManager mNsdManager;
    NsdManager.ResolveListener mResolveListener;
    NsdManager.DiscoveryListener mDiscoveryListener;
    NsdManager.RegistrationListener mRegistrationListener;

    public static final String SERVICE_TYPE = "_http._tcp.";

    public static final String TAG = "NsdHelper";
    public String mServiceName = "NsdChat";

    NsdServiceInfo mService;

    public NsdHelper(Context context, Handler updateHandler, Handler logHandler) {
        mContext = context;
        mUpdateHandler = updateHandler;
        mLogHandler = logHandler;
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public void initializeNsd() {
        initializeResolveListener();
        initializeDiscoveryListener();
        initializeRegistrationListener();	
		//mNsdManager.init(mContext.getMainLooper(), this);
    }

    public void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                logMessage(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                logMessage(TAG, "Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    logMessage(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(mServiceName)) {
                    logMessage(TAG, "Same machine: " + mServiceName);
                } else if (service.getServiceName().contains(mServiceName)){
                    mNsdManager.resolveService(service, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                logMessage(TAG, "service lost" + service);
                if (mService == service) {
                    mService = null;
                }
            }
            
            @Override
            public void onDiscoveryStopped(String serviceType) {
                logMessage(TAG, "Discovery stopped: " + serviceType);        
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                logMessage(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                logMessage(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                logMessage(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                logMessage(TAG, "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(mServiceName)) {
                    logMessage(TAG, "Same IP.");
                    return;
                }
                mService = serviceInfo;
            }
        };
    }

    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                logMessage(TAG, "Service registered");
                mServiceName = NsdServiceInfo.getServiceName();
            }
            
            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
                logMessage(TAG, "Registration failed");
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                logMessage(TAG, "Service unregistered");
            }
            
            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                logMessage(TAG, "Unregistration failed");
            }
            
        };
    }

    public void registerService(int port) {
        NsdServiceInfo serviceInfo  = new NsdServiceInfo();
        serviceInfo.setPort(port);
        serviceInfo.setServiceName(mServiceName);
        serviceInfo.setServiceType(SERVICE_TYPE);
        
        mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);        
    }

    public void discoverServices() {
        mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }
    
    public void stopDiscovery() {
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }

    public NsdServiceInfo getChosenServiceInfo() {
        return mService;
    }
    
    public void tearDown() {
        mNsdManager.unregisterService(mRegistrationListener);
    }

    public void logMessage(String tag, String msg) {
        Bundle messageBundle = new Bundle();
        messageBundle.putString("msg", msg);

        Message message = new Message();
        message.setData(messageBundle);
        mLogHandler.sendMessage(message);
    }

}
