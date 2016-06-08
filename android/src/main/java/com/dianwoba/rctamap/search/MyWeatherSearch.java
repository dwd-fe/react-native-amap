package com.dianwoba.rctamap.search;

import android.content.Context;

import com.amap.api.services.weather.LocalDayWeatherForecast;
import com.amap.api.services.weather.LocalWeatherForecast;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

/**
 * Created by marshal on 16/6/7.
 */
class MyWeatherSearch extends AMapSearch implements WeatherSearch.OnWeatherSearchListener {
    public WeatherSearch weatherSearch;

    public MyWeatherSearch(Context context, String requestId) {
        weatherSearch = new WeatherSearch(context);
        this.setRequestId(requestId);
    }

    @Override
    public void onWeatherLiveSearched(LocalWeatherLiveResult localWeatherLiveResult, int resultId) {
        if (1000 != resultId) {
            this.sendEventWithError("request weatherLive error");
            return;
        }

        WritableArray arrray = Arguments.createArray();
        WritableMap map = Arguments.createMap();

        LocalWeatherLive live = localWeatherLiveResult.getLiveResult();

        map.putString("city", live.getCity());
        map.putString("province", live.getProvince());
        map.putString("adCode", live.getAdCode());
        map.putString("weather", live.getWeather());
        map.putString("temperature", live.getTemperature());
        map.putString("windDirection", live.getWindDirection());
        map.putString("windPower", live.getWindPower());
        map.putString("reportTime", live.getReportTime());

        arrray.pushMap(map);

        this.sendEventWithData(arrray);
    }

    @Override
    public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int resultId) {
        if (1000 != resultId) {
            this.sendEventWithError("request weatherForecast error");
            return;
        }

        WritableArray array = Arguments.createArray();
        WritableMap map = Arguments.createMap();

        LocalWeatherForecast forecast =  localWeatherForecastResult.getForecastResult();

        map.putString("city", forecast.getCity());
        map.putString("city", forecast.getCity());
        map.putString("province", forecast.getProvince());
        map.putString("adCode", forecast.getAdCode());
        map.putString("reportTime", forecast.getReportTime());

        WritableArray casts = Arguments.createArray();
        for (LocalDayWeatherForecast cast: forecast.getWeatherForecast()
                ) {
            WritableMap castMap = Arguments.createMap();
            castMap.putString("date", cast.getDate());
            castMap.putString("dayTemp", cast.getDayTemp());
            castMap.putString("dayWeather", cast.getDayWeather());
            castMap.putString("dayWindDirection", cast.getDayWindDirection());
            castMap.putString("dayWindPower", cast.getDayWindPower());
            castMap.putString("nightTemp", cast.getNightTemp());
            castMap.putString("nightWeather", cast.getNightWeather());
            castMap.putString("nightWindDirection", cast.getNightWindDirection());
            castMap.putString("nightWindPower", cast.getNightWindPower());
            castMap.putString("week", cast.getWeek());

            casts.pushMap(castMap);
        }
        map.putArray("casts", casts);

        array.pushMap(map);

        this.sendEventWithData(array);
    }
}
