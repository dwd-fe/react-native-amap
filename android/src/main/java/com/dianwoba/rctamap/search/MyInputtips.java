package com.dianwoba.rctamap.search;

import android.content.Context;

import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.Tip;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.List;

/**
 * Created by marshal on 16/6/7.
 */
class MyInputtips extends AMapSearch implements Inputtips.InputtipsListener{

    public Inputtips inputTips;

    public MyInputtips(Context context, String requestId) {
        inputTips = new Inputtips(context, this);
        this.setRequestId(requestId);
    }

    @Override
    public void onGetInputtips(List<Tip> list, int resultId) {
        if (1000 != resultId) {
            this.sendEventWithError("request inputTips error");
            return;
        }

        WritableArray array = Arguments.createArray();
        for (Tip tip: list
                ) {
            WritableMap map = Arguments.createMap();
            map.putString("name", tip.getName());
            map.putString("district", tip.getDistrict());
            WritableMap location = Arguments.createMap();
            location.putDouble("latitude", tip.getPoint().getLatitude());
            location.putDouble("longitude", tip.getPoint().getLongitude());
            map.putMap("location", location);
            array.pushMap(map);
        }
        this.sendEventWithData(array);
    }
}
