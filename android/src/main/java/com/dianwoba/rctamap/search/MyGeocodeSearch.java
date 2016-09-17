package com.dianwoba.rctamap.search;

import android.content.Context;

import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Tip;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

/**
 * Created by marshal on 16/6/8.
 */
public class MyGeocodeSearch extends AMapSearch implements GeocodeSearch.OnGeocodeSearchListener {
    public GeocodeSearch geocodeSearch;

    public MyGeocodeSearch(Context context, String requestId) {
        geocodeSearch = new GeocodeSearch(context);
        geocodeSearch.setOnGeocodeSearchListener(this);
        this.setRequestId(requestId);
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int resultId) {
        if (1000 != resultId) {
            this.sendEventWithError("request regeocode error");
            return;
        }
        WritableArray array = Arguments.createArray();
        WritableMap map = Arguments.createMap();
        RegeocodeAddress address = regeocodeResult.getRegeocodeAddress();

        map.putString("formattedAddress", address.getFormatAddress());
        map.putString("province",address.getProvince());
        map.putString("city",address.getCity());
        map.putString("township",address.getTownship());
        map.putString("neighborhood",address.getNeighborhood());
        map.putString("building",address.getBuilding());
        map.putString("district", address.getDistrict());

        array.pushMap(map);

        this.sendEventWithData(array);
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int resultId) {
        if (1000 != resultId) {
            this.sendEventWithError("request geocode error");
            return;
        }

        WritableArray array = Arguments.createArray();
        for (GeocodeAddress address: geocodeResult.getGeocodeAddressList()
                ) {
            WritableMap map = Arguments.createMap();
            map.putString("formatAddress", address.getFormatAddress());
            map.putString("province",address.getProvince());
            map.putString("city",address.getCity());
            map.putString("township",address.getTownship());
            map.putString("neighborhood",address.getNeighborhood());
            map.putString("building",address.getBuilding());
            map.putString("adcode",address.getAdcode());
            map.putString("level",address.getLevel());
            map.putString("district", address.getDistrict());

            WritableMap location = Arguments.createMap();
            location.putDouble("latitude", address.getLatLonPoint().getLatitude());
            location.putDouble("longitude", address.getLatLonPoint().getLongitude());
            map.putMap("location", location);

            array.pushMap(map);
        }
        this.sendEventWithData(array);
    }
}
