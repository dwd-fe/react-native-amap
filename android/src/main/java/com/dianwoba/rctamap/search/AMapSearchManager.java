package com.dianwoba.rctamap.search;

import android.content.Context;

import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.weather.LocalDayWeatherForecast;
import com.amap.api.services.weather.LocalWeatherForecast;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.List;

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
    public void inputTipsSearch(String keys, String city, String requestId) {
        InputtipsQuery inputtipsQuery = new InputtipsQuery(keys, city);
        MyInputtips request = new MyInputtips(reactContext, requestId);

        request.inputTips.setQuery(inputtipsQuery);
        request.inputTips.requestInputtipsAsyn();
    }

    @ReactMethod
    public void weatherSearch(String city, Boolean isLive, String requestId) {
        WeatherSearchQuery query = new WeatherSearchQuery(city,
                isLive? WeatherSearchQuery.WEATHER_TYPE_LIVE: WeatherSearchQuery.WEATHER_TYPE_FORECAST);
        MyWeatherSearch request = new MyWeatherSearch(reactContext, requestId);

        request.weatherSearch.setQuery(query);
        request.weatherSearch.searchWeatherAsyn();
    }
}
