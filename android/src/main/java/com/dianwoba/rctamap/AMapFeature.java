package com.dianwoba.rctamap;

import android.content.Context;

import com.amap.api.maps2d.AMap;
import com.facebook.react.views.view.ReactViewGroup;


public abstract class AMapFeature extends ReactViewGroup {
    public AMapFeature(Context context) {
        super(context);
    }

    public abstract void addToMap(AMap map);

    public abstract void removeFromMap(AMap map);

    public abstract Object getFeature();
}
