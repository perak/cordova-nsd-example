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
    private Handler mHandler;

    NsdManager mNsdManager;
    NsdManager.ResolveListener mResolveListener;
    NsdManager.DiscoveryListener mDiscoveryListener;
    NsdManager.RegistrationListener mRegistrationListener;

    public static final String SERVICE_TYPE = "_http._tcp.";

    public static final String TAG = "NsdHelper";
    public String mServiceName = "NsdChat";

    NsdServiceInfo mService;

    public NsdHelper(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
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
				sendNotification("log", "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
				sendNotification("log", "Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
					sendNotification("log", "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(mServiceName)) {
					sendNotification("log", "Same machine: " + mServiceName);
                } else if (service.getServiceName().contains(mServiceName)){
					sendNotification("log", "Service found: " + service.getServiceName());
                    mNsdManager.resolveService(service, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
				sendNotification("log", "service lost" + service);
                if (mService == service) {
                    mService = null;
                }
            }
            
            @Override
            public void onDiscoveryStopped(String serviceType) {
				sendNotification("log", "Discovery stopped: " + serviceType);        
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
				sendNotification("error", "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
				sendNotification("error", "Stop discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
				sendNotification("log", "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
				sendNotification("log", "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(mServiceName)) {
					sendNotification("log", "Same IP.");
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
                mServiceName = NsdServiceInfo.getServiceName();
				sendNotification("log", "Service registered: " + mServiceName);
            }
            
            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
				sendNotification("error", "Service registration failed");
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
				sendNotification("log", "Service unregistered");
            }
            
            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
				sendNotification("error", "Service unregistration failed");
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

    public void sendNotification(String type, String msg) {
        Bundle messageBundle = new Bundle();
        messageBundle.putString("type", type);
        messageBundle.putString("msg", msg);

        Message message = new Message();
        message.setData(messageBundle);
        mHandler.sendMessage(message);
    }

}
