package com.dianwoba.rctamap.search;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

/**
 * Created by marshal on 16/6/7.
 */
class AMapSearch {
    private String requestId;
    public ReactContext reactContext;
    public static String AMAP_SEARCH_RECEIVE_EVENT_NAME = "ReceiveAMapSearchResult";

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    protected void sendEventWithData(WritableArray params) {
        WritableMap map = Arguments.createMap();
        map.putString("requestId", requestId);
        map.putArray("data", params);
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(AMAP_SEARCH_RECEIVE_EVENT_NAME, map);
    }

    protected void sendEventWithError(String errMsg) {
        WritableMap map = Arguments.createMap();
        map.putString("requstId", requestId);
        map.putString("error", errMsg);
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(AMAP_SEARCH_RECEIVE_EVENT_NAME, map);
    }
}