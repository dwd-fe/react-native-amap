package com.dianwoba.rctamap;
import android.util.Log;
import android.graphics.Color;
import android.graphics.BitmapFactory;
import android.os.StrictMode;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.PolylineOptions;
import com.amap.api.maps2d.model.PolygonOptions;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.Callback;
//import com.facebook.react.uimanager.ReactProp;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import javax.annotation.Nullable;


public class RCTAMapViewManager extends ViewGroupManager<MapView> {
    public static final String RCT_CLASS = "RCTAMapView";

    private MapView mapView;

    @Override
    public String getName() {
        return RCT_CLASS;
    }

    @Override
    protected MapView createViewInstance(ThemedReactContext content) {
        mapView = new MapView(content);
        mapView.onCreate(null);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        return mapView;
    }

    public MapView getMapView() {
        return mapView;
    }

    @ReactProp(name = "mapType")
    public void setMode(MapView view, int type) {
        view.getMap().setMapType(type);
    }

    @ReactProp(name = "showsUserLocation", defaultBoolean = false)
    public void showsUserLocation(MapView view, Boolean value) {
        view.getMap().setMyLocationEnabled(value);
    }

//    @ReactProp(name = "onMapClick", defaultBoolean = true)
//    public void onMapClick(final MapView view, Boolean value) {
//        view.getMap().setOnMapClickListener(new AMap.OnMapClickListener() {
//            @Override
//            public void onMapClick(LatLng point) {
//                WritableMap event = Arguments.createMap();
//                event.putDouble("latitude", point.latitude);
//                event.putDouble("longitude", point.longitude);
//                ReactContext reactContext = (ReactContext) view.getContext();
//                reactContext
//                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
//                        .emit("onMapClick", event);
//            }
//        });
//    }

    @ReactProp(name = "onRegionChange", defaultBoolean = false)
    public void onRegionChange(final MapView view, Boolean value) {
        view.getMap().setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                WritableMap event = Arguments.createMap();
                WritableMap location = Arguments.createMap();
                location.putDouble("latitude", position.target.latitude);
                location.putDouble("longitude", position.target.longitude);
                location.putDouble("zoom", position.zoom);
                event.putMap("src", location);
                ReactContext reactContext = (ReactContext) view.getContext();
                reactContext
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit("onRegionChange", event);
            }
            @Override
            public void onCameraChangeFinish(CameraPosition position) {
                WritableMap event = Arguments.createMap();
                WritableMap location = Arguments.createMap();
                location.putDouble("latitude", position.target.latitude);
                location.putDouble("longitude", position.target.longitude);
                location.putDouble("zoom", position.zoom);
                event.putMap("src", location);
                ReactContext reactContext = (ReactContext) view.getContext();
                reactContext
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit("onRegionChangeComplete", event);
            }
        });
    }

//    @ReactProp(name = "annotations")
//    public void setAnnotations(MapView view, @Nullable ReadableArray value) {
//        setAnnotations(view, value, true);
//    }

    public void setAnnotations(MapView view, @Nullable ReadableArray value, boolean clearMap) {
        AMap map = view.getMap();
        if (value == null || value.size() < 1) {
            Log.e(RCT_CLASS, "Error: No annotations");
        } else {
            // 是否清除地图上覆盖物
            if (clearMap) {
                map.clear();
            }
            int size = value.size();
            for (int i = 0; i < size; i++) {
                // 循环添加覆盖物
                ReadableMap annotation = value.getMap(i);
                String type = annotation.getString("type");
                if (type.equals("point")) {
                    double latitude = annotation.getArray("coordinates").getDouble(0);
                    double longitude = annotation.getArray("coordinates").getDouble(1);
                    LatLng markerCenter = new LatLng(latitude, longitude);
                    MarkerOptions marker = new MarkerOptions();
                    marker.position(markerCenter);
                    if (annotation.hasKey("title")) {
                        String title = annotation.getString("title");
                        marker.title(title);
                    }
                    if (annotation.hasKey("subtitle")) {
                        String subtitle = annotation.getString("subtitle");
                        marker.snippet(subtitle);
                    }
                    //marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.location));
                    map.addMarker(marker);
                } else if (type.equals("polyline")) {
                    int coordSize = annotation.getArray("coordinates").size();
                    PolylineOptions polyline = new PolylineOptions();
                    for (int p = 0; p < coordSize; p++) {
                        double latitude = annotation.getArray("coordinates").getArray(p).getDouble(0);
                        double longitude = annotation.getArray("coordinates").getArray(p).getDouble(1);
                        polyline.add(new LatLng(latitude, longitude));
                    }
                    if (annotation.hasKey("dotted")) {
                        boolean dotted = annotation.getBoolean("dotted");
                        polyline.setDottedLine(dotted);
                    }
                    if (annotation.hasKey("strokeColor")) {
                        int strokeColor = Color.parseColor(annotation.getString("strokeColor"));
                        polyline.color(strokeColor);
                    }
                    if (annotation.hasKey("strokeWidth")) {
                        float strokeWidth = annotation.getInt("strokeWidth");
                        polyline.width(strokeWidth);
                    }
                    map.addPolyline(polyline);
                } else if (type.equals("polygon")) {
                    int coordSize = annotation.getArray("coordinates").size();
                    PolygonOptions polygon = new PolygonOptions();
                    for (int p = 0; p < coordSize; p++) {
                        double latitude = annotation.getArray("coordinates").getArray(p).getDouble(0);
                        double longitude = annotation.getArray("coordinates").getArray(p).getDouble(1);
                        polygon.add(new LatLng(latitude, longitude));
                    }
                    if (annotation.hasKey("fillColor")) {
                        int fillColor = Color.parseColor(annotation.getString("fillColor"));
                        polygon.fillColor(fillColor);
                    }
                    if (annotation.hasKey("strokeColor")) {
                        int strokeColor = Color.parseColor(annotation.getString("strokeColor"));
                        polygon.strokeColor(strokeColor);
                    }
                    if (annotation.hasKey("strokeWidth")) {
                        float strokeWidth = annotation.getInt("strokeWidth");
                        polygon.strokeWidth(strokeWidth);
                    }
                    map.addPolygon(polygon);
                }
            }
        }
    }

    @ReactProp(name = "region")
    public void setRegion(MapView view, @Nullable ReadableMap center) {
        AMap map = view.getMap();
        if (center != null) {
            double latitude = center.getDouble("latitude");
            double longitude = center.getDouble("longitude");
            float zoom = center.getInt("zoom");
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(latitude, longitude))
                    .zoom(zoom)
                    .build();
            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }else{
            Log.w(RCT_CLASS, "No CenterCoordinate provided");
        }
    }
}
