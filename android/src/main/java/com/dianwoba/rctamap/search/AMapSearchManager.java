package com.dianwoba.rctamap.search;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.weather.WeatherSearchQuery;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;


/**
 * Created by marshal on 16/6/6.
 */
public class AMapSearchManager extends ReactContextBaseJavaModule {
    private ReactContext reactContext;

    public AMapSearchManager(ReactApplicationContext rContext) {
        super(rContext);
        reactContext = rContext;
    }

    @Override
    public String getName() {
        return "AMapSearchManager";
    }

    @ReactMethod
    public void inputTipsSearch(String requestId, String keys, String city) {
        InputtipsQuery inputtipsQuery = new InputtipsQuery(keys, city);
        MyInputtips request = new MyInputtips(reactContext, requestId);

        request.inputTips.setQuery(inputtipsQuery);
        request.inputTips.requestInputtipsAsyn();
    }

    @ReactMethod
    public void weatherSearch(String requestId, String city, Boolean isLive) {
        WeatherSearchQuery query = new WeatherSearchQuery(city,
                isLive? WeatherSearchQuery.WEATHER_TYPE_LIVE: WeatherSearchQuery.WEATHER_TYPE_FORECAST);
        MyWeatherSearch request = new MyWeatherSearch(reactContext, requestId);

        request.weatherSearch.setQuery(query);
        request.weatherSearch.searchWeatherAsyn();
    }

    @ReactMethod
    public void geocodeSearch(String requestId, String address, String city) {
        MyGeocodeSearch request = new MyGeocodeSearch(reactContext, requestId);
        GeocodeQuery query = new GeocodeQuery(address, city);

        request.geocodeSearch.getFromLocationNameAsyn(query);
    }

    @ReactMethod
    public void regeocodeSearch(String requestId, LatLonPoint point, Float radius) {
        MyGeocodeSearch request = new MyGeocodeSearch(reactContext, requestId);
        RegeocodeQuery query = new RegeocodeQuery(point, radius != null?radius:1000, GeocodeSearch.AMAP);

        request.geocodeSearch.getFromLocationAsyn(query);
    }
}
