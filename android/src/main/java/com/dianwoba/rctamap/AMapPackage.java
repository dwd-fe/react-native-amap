package com.dianwoba.rctamap;

import com.dianwoba.rctamap.search.AMapSearchManager;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AMapPackage implements ReactPackage {
    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        AMapSearchManager searchManager = new AMapSearchManager(reactContext);

        return  Arrays.<NativeModule>asList(
                searchManager
        );
    }

    @Override
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        AMapCalloutManager calloutManager = new AMapCalloutManager();
        AMapMarkerManager annotationManager = new AMapMarkerManager();
        AMapPolylineManager polylineManager = new AMapPolylineManager(reactContext);
        AMapPolygonManager polygonManager = new AMapPolygonManager(reactContext);
        AMapCircleManager circleManager = new AMapCircleManager(reactContext);

        AMapViewManager mapManager = new AMapViewManager(
                annotationManager,
                polylineManager,
                polygonManager,
                circleManager
        );

        return Arrays.<ViewManager>asList(
                calloutManager,
                annotationManager,
                polylineManager,
                polygonManager,
                circleManager,
                mapManager
        );
    }

}
