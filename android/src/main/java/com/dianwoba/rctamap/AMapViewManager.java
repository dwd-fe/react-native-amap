package com.dianwoba.rctamap;

import android.os.StrictMode;
import android.util.Log;
import android.view.View;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.MapsInitializer;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.LatLngBounds;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.LayoutShadowNode;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.util.Map;

import javax.annotation.Nullable;

public class AMapViewManager extends ViewGroupManager<AMapView> {

    public static final String REACT_CLASS = "AMap";

    public static final int ANIMATE_TO_REGION = 1;
    public static final int ANIMATE_TO_COORDINATE = 2;
    public static final int FIT_TO_ELEMENTS = 3;
    public static final int ANIMATE_TO_ZOOM_LEVEL = 4;

    private final Map<String, Integer> MAP_TYPES = MapBuilder.of(
            "standard", AMap.MAP_TYPE_NORMAL,
            "satellite", AMap.MAP_TYPE_SATELLITE
    );

    private ReactContext reactContext;

    private AMapMarkerManager markerManager;
    private AMapPolylineManager polylineManager;
    private AMapPolygonManager polygonManager;
    private AMapCircleManager circleManager;

    public AMapViewManager(
            AMapMarkerManager markerManager,
            AMapPolylineManager polylineManager,
            AMapPolygonManager polygonManager,
            AMapCircleManager circleManager) {
        this.markerManager = markerManager;
        this.polylineManager = polylineManager;
        this.polygonManager = polygonManager;
        this.circleManager = circleManager;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected AMapView createViewInstance(ThemedReactContext context) {
        reactContext = context;
        AMapView view = new AMapView(context, this);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            MapsInitializer.initialize(context.getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
            emitMapError("AMap initialize error", "amap_init_error");
        }

        return view;
    }

    @Override
    public void onDropViewInstance(AMapView view) {
        view.doDestroy();
        super.onDropViewInstance(view);
    }

    private void emitMapError(String message, String type) {
        WritableMap error = Arguments.createMap();
        error.putString("message", message);
        error.putString("type", type);

        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("onError", error);
    }

    @ReactProp(name = "region")
    public void setRegion(AMapView view, ReadableMap region) {
        view.setRegion(region);
    }

    @ReactProp(name = "mapType", defaultInt = AMap.MAP_TYPE_NORMAL)
    public void setMapType(AMapView view, @Nullable String mapType) {
        int typeId = MAP_TYPES.get(mapType);
        view.map.setMapType(typeId);
    }

    @ReactProp(name = "showsUserLocation", defaultBoolean = false)
    public void setShowsUserLocation(AMapView view, boolean showUserLocation) {
        view.setShowsUserLocation(showUserLocation);
    }

    @ReactProp(name = "showsTraffic", defaultBoolean = false)
    public void setShowTraffic(AMapView view, boolean showTraffic) {
        view.map.setTrafficEnabled(showTraffic);
    }

//    @ReactProp(name = "showsBuildings", defaultBoolean = false)
//    public void setShowBuildings(AMapView view, boolean showBuildings) {
//        view.map.setBuildingsEnabled(showBuildings);
//    }

    //// TODO: 16/5/19
//    @ReactProp(name = "showsIndoors", defaultBoolean = false)
//    public void setShowIndoors(AMapView view, boolean showIndoors) {
//        view.map.setIndoorEnabled(showIndoors);
//    }

    @ReactProp(name = "showsCompass", defaultBoolean = false)
    public void setShowsCompass(AMapView view, boolean showsCompass) {
        view.map.getUiSettings().setCompassEnabled(showsCompass);
    }

    @ReactProp(name = "showsScale", defaultBoolean = false)
    public void setShowsScale(AMapView view, boolean showsScale) {
        view.map.getUiSettings().setScaleControlsEnabled(showsScale);
    }

    @ReactProp(name = "scrollEnabled", defaultBoolean = false)
    public void setScrollEnabled(AMapView view, boolean scrollEnabled) {
        view.map.getUiSettings().setScrollGesturesEnabled(scrollEnabled);
    }

    @ReactProp(name = "zoomEnabled", defaultBoolean = false)
    public void setZoomEnabled(AMapView view, boolean zoomEnabled) {
        view.map.getUiSettings().setZoomGesturesEnabled(zoomEnabled);
        view.map.getUiSettings().setZoomControlsEnabled(zoomEnabled);
    }

    @ReactProp(name = "zoomLevel", defaultDouble = 10.0)
    public void  setZoomLevel(AMapView view, double zoomLevel) {
        view.setZoomLevel((float) zoomLevel);
    }
//    @ReactProp(name = "rotateEnabled", defaultBoolean = false)
//    public void setRotateEnabled(AMapView view, boolean rotateEnabled) {
//        view.map.getUiSettings().setRotateGesturesEnabled(rotateEnabled);
//    }

//    @ReactProp(name = "pitchEnabled", defaultBoolean = false)
//    public void setPitchEnabled(AMapView view, boolean pitchEnabled) {
//        view.map.getUiSettings().setTiltGesturesEnabled(pitchEnabled);
//    }

    @Override
    public void receiveCommand(AMapView view, int commandId, @Nullable ReadableArray args) {
        Integer duration;
        Double lat;
        Double lng;
        Double lngDelta;
        Double latDelta;
        ReadableMap region;
        Double zoomLevel;

        switch (commandId) {
            case ANIMATE_TO_REGION:
                region = args.getMap(0);
                duration = args.getInt(1);
                lng = region.getDouble("longitude");
                lat = region.getDouble("latitude");
                lngDelta = region.hasKey("longitudeDelta")?region.getDouble("longitudeDelta"):0.0;
                latDelta = region.hasKey("latitudeDelta")?region.getDouble("latitudeDelta"):0.0;
                LatLngBounds bounds = new LatLngBounds(
                        new LatLng(lat - latDelta / 2, lng - lngDelta / 2), // southwest
                        new LatLng(lat + latDelta / 2, lng + lngDelta / 2)  // northeast
                );
                view.animateToRegion(bounds, duration);
                break;

            case ANIMATE_TO_COORDINATE:
                region = args.getMap(0);
                duration = args.getInt(1);
                lng = region.getDouble("longitude");
                lat = region.getDouble("latitude");
                view.animateToCoordinate(new LatLng(lat, lng), duration);
                break;

            case ANIMATE_TO_ZOOM_LEVEL:
                zoomLevel = args.getDouble(0);
                view.setZoomLevel(zoomLevel.floatValue());
                break;
            case FIT_TO_ELEMENTS:
                view.fitToElements(args.getBoolean(0));
                break;
        }
    }

    @Override
    @Nullable
    public Map getExportedCustomDirectEventTypeConstants() {
        Map map = MapBuilder.of(
                "onMapReady", MapBuilder.of("registrationName", "onMapReady"),
                "onPress", MapBuilder.of("registrationName", "onPress"),
                "onLongPress", MapBuilder.of("registrationName", "onLongPress"),
                "onMarkerPress", MapBuilder.of("registrationName", "onMarkerPress"),
                "onMarkerSelect", MapBuilder.of("registrationName", "onMarkerSelect"),
                "onMarkerDeselect", MapBuilder.of("registrationName", "onMarkerDeselect"),
                "onCalloutPress", MapBuilder.of("registrationName", "onCalloutPress")
        );

        map.putAll(MapBuilder.of(
                "onMarkerDragStart", MapBuilder.of("registrationName", "onMarkerDragStart"),
                "onMarkerDrag", MapBuilder.of("registrationName", "onMarkerDrag"),
                "onMarkerDragEnd", MapBuilder.of("registrationName", "onMarkerDragEnd")
        ));

        return map;
    }

    @Override
    @Nullable
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of(
                "animateToRegion", ANIMATE_TO_REGION,
                "animateToCoordinate", ANIMATE_TO_COORDINATE,
                "fitToElements", FIT_TO_ELEMENTS,
                "animateToZoomLevel", ANIMATE_TO_ZOOM_LEVEL
        );
    }

    @Override
    public LayoutShadowNode createShadowNodeInstance() {
        // A custom shadow node is needed in order to pass back the width/height of the map to the
        // view manager so that it can start applying camera moves with bounds.
        return new SizeReportingShadowNode();
    }

    @Override
    public void addView(AMapView parent, View child, int index) {
        parent.addFeature(child, index);
    }

    @Override
    public int getChildCount(AMapView view) {
        return view.getFeatureCount();
    }

    @Override
    public View getChildAt(AMapView view, int index) {
        return view.getFeatureAt(index);
    }

    @Override
    public void removeViewAt(AMapView parent, int index) {
        parent.removeFeatureAt(index);
    }

    @Override
    public void updateExtraData(AMapView view, Object extraData) {
        view.updateExtraData(extraData);
    }

    public void pushEvent(View view, String name, WritableMap data) {
        ReactContext reactContext = (ReactContext) view.getContext();
        reactContext.getJSModule(RCTEventEmitter.class)
                .receiveEvent(view.getId(), name, data);
    }

}
