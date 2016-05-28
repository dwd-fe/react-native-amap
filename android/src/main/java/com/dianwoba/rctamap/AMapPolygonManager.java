package com.dianwoba.rctamap;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;

public class AMapPolygonManager extends ViewGroupManager<AMapPolygon> {
    private final DisplayMetrics metrics;

    public AMapPolygonManager(ReactApplicationContext reactContext) {
        super();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            metrics = new DisplayMetrics();
            ((WindowManager) reactContext.getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay()
                    .getRealMetrics(metrics);
        } else {
            metrics = reactContext.getResources().getDisplayMetrics();
        }
    }

    @Override
    public String getName() {
        return "AMapPolygon";
    }

    @Override
    public AMapPolygon createViewInstance(ThemedReactContext context) {
        return new AMapPolygon(context);
    }

    @ReactProp(name = "coordinates")
    public void setCoordinate(AMapPolygon view, ReadableArray coordinates) {
        view.setCoordinates(coordinates);
    }

    @ReactProp(name = "strokeWidth", defaultFloat = 1f)
    public void setStrokeWidth(AMapPolygon view, float widthInPoints) {
        float widthInScreenPx = metrics.density * widthInPoints; // done for parity with iOS
        view.setStrokeWidth(widthInScreenPx);
    }

    @ReactProp(name = "fillColor", defaultInt = Color.RED, customType = "Color")
    public void setFillColor(AMapPolygon view, int color) {
        view.setFillColor(color);
    }

    @ReactProp(name = "strokeColor", defaultInt = Color.RED, customType = "Color")
    public void setStrokeColor(AMapPolygon view, int color) {
        view.setStrokeColor(color);
    }

//    @ReactProp(name = "geodesic", defaultBoolean = false)
//    public void setGeodesic(AMapPolygon view, boolean geodesic) {
//        view.setGeodesic(geodesic);
//    }

    @ReactProp(name = "zIndex", defaultFloat = 1.0f)
    public void setZIndex(AMapPolygon view, float zIndex) {
        view.setZIndex(zIndex);
    }
}
