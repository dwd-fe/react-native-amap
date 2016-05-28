package com.dianwoba.rctamap;

import android.content.Context;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Polygon;
import com.amap.api.maps2d.model.PolygonOptions;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;


import java.util.ArrayList;
import java.util.List;

public class AMapPolygon extends AMapFeature {

    private PolygonOptions polygonOptions;
    private Polygon polygon;

    private List<LatLng> coordinates;
    private int strokeColor;
    private int fillColor;
    private float strokeWidth;
    private boolean geodesic;
    private float zIndex;

    public AMapPolygon(Context context) {
        super(context);
    }

    public void setCoordinates(ReadableArray coordinates) {
        // it's kind of a bummer that we can't run map() or anything on the ReadableArray
        this.coordinates = new ArrayList<>(coordinates.size());
        for (int i = 0; i < coordinates.size(); i++) {
            ReadableMap coordinate = coordinates.getMap(i);
            this.coordinates.add(i,
                    new LatLng(coordinate.getDouble("latitude"), coordinate.getDouble("longitude")));
        }
        if (polygon != null) {
            polygon.setPoints(this.coordinates);
        }
    }

    public void setFillColor(int color) {
        this.fillColor = color;
        if (polygon != null) {
            polygon.setFillColor(color);
        }
    }

    public void setStrokeColor(int color) {
        this.strokeColor = color;
        if (polygon != null) {
            polygon.setStrokeColor(color);
        }
    }

    public void setStrokeWidth(float width) {
        this.strokeWidth = width;
        if (polygon != null) {
            polygon.setStrokeWidth(width);
        }
    }
    
    //// TODO: 16/5/19  
//    public void setGeodesic(boolean geodesic) {
//        this.geodesic = geodesic;
//        if (polygon != null) {
//            polygon.setGeodesic(geodesic);
//        }
//    }

    public void setZIndex(float zIndex) {
        this.zIndex = zIndex;
        if (polygon != null) {
            polygon.setZIndex(zIndex);
        }
    }

    public PolygonOptions getPolygonOptions() {
        if (polygonOptions == null) {
            polygonOptions = createPolygonOptions();
        }
        return polygonOptions;
    }

    private PolygonOptions createPolygonOptions() {
        PolygonOptions options = new PolygonOptions();
        options.addAll(coordinates);
        options.fillColor(fillColor);
        options.strokeColor(strokeColor);
        options.strokeWidth(strokeWidth);
        //// TODO: 16/5/19  
//        options.geodesic(geodesic);
        options.zIndex(zIndex);
        return options;
    }

    @Override
    public Object getFeature() {
        return polygon;
    }

    @Override
    public void addToMap(AMap map) {
        polygon = map.addPolygon(getPolygonOptions());
    }

    @Override
    public void removeFromMap(AMap map) {
        polygon.remove();
    }
}
