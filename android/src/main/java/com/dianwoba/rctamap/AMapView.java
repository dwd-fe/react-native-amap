package com.dianwoba.rctamap;

import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapFragment;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.Projection;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.Circle;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.LatLngBounds;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.Polygon;
import com.amap.api.maps2d.model.Polyline;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.NoSuchKeyException;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.events.EventDispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;

public class AMapView extends MapView implements AMap.InfoWindowAdapter,
        AMap.OnMarkerDragListener, AMap.OnMapLoadedListener {
    public AMap map;

//    private AMapLocationClient mLocationClient = null;//定位发起端
//    private AMapLocationClientOption mLocationOption = null;//定位参数
//    private LocationSource.OnLocationChangedListener mListener = null;//定位监听器

    private LatLngBounds boundsToMove;
    private boolean showUserLocation = false;
    private boolean isMonitoringRegion = false;
    private boolean isTouchDown = false;
    private static final String[] PERMISSIONS = new String[] {
            "android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"};

    private final List<AMapFeature> features = new ArrayList<>();
    private final Map<Marker, AMapMarker> markerMap = new HashMap<>();
    private final Map<Polyline, AMapPolyline> polylineMap = new HashMap<>();
    private final Map<Polygon, AMapPolygon> polygonMap = new HashMap<>();
    private final Map<Circle, AMapCircle> circleMap = new HashMap<>();

    private final ScaleGestureDetector scaleDetector;
    private final GestureDetectorCompat gestureDetector;
    private final AMapViewManager manager;
    private LifecycleEventListener lifecycleListener;
    private boolean paused = false;

    final EventDispatcher eventDispatcher;

    public AMapView(ThemedReactContext context, AMapViewManager manager) {
        super(context);
        this.manager = manager;

        super.onCreate(null);
        super.onResume();

        if (this.getMap() != null) {
            map = this.getMap();
            map.setOnMapLoadedListener(this);
        }

        final AMapView view = this;
        scaleDetector =
                new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
//            @Override
//            public boolean onScale(ScaleGestureDetector detector) {
//                Log.d("AirMapView", "onScale");
//                return false;
//            }

                    @Override
                    public boolean onScaleBegin(ScaleGestureDetector detector) {
                        view.startMonitoringRegion();
                        return true; // stop recording this gesture. let mapview handle it.
                    }
                });

        gestureDetector =
                new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        view.startMonitoringRegion();
                        return false;
                    }

                    @Override
                    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                            float distanceY) {
                        view.startMonitoringRegion();
                        return false;
                    }
                });

        eventDispatcher = context.getNativeModule(UIManagerModule.class).getEventDispatcher();
    }

    @Override
    public void onMapLoaded() {
        Log.i("AMap", "mapLoaded");
        this.map.setInfoWindowAdapter(this);
        this.map.setOnMarkerDragListener(this);

        manager.pushEvent(this, "onMapReady", new WritableNativeMap());

        final AMapView view = this;

        map.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                WritableMap event;

                event = makeClickEventData(marker.getPosition());
                event.putString("action", "marker-press");
                manager.pushEvent(view, "onMarkerPress", event);

                event = makeClickEventData(marker.getPosition());
                event.putString("action", "marker-press");
                manager.pushEvent(markerMap.get(marker), "onPress", event);

                return false; // returning false opens the callout window, if possible
            }
        });

        map.setOnInfoWindowClickListener(new AMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                WritableMap event;

                event = makeClickEventData(marker.getPosition());
                event.putString("action", "callout-press");
                manager.pushEvent(view, "onCalloutPress", event);

                event = makeClickEventData(marker.getPosition());
                event.putString("action", "callout-press");
                AMapMarker markerView = markerMap.get(marker);
                manager.pushEvent(markerView, "onCalloutPress", event);

                event = makeClickEventData(marker.getPosition());
                event.putString("action", "callout-press");
                AMapCallout infoWindow = markerView.getCalloutView();
                if (infoWindow != null) manager.pushEvent(infoWindow, "onPress", event);
            }
        });

        map.setOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                WritableMap event = makeClickEventData(point);
                event.putString("action", "press");
                manager.pushEvent(view, "onPress", event);
            }
        });

        map.setOnMapLongClickListener(new AMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                WritableMap event = makeClickEventData(point);
                event.putString("action", "long-press");
                manager.pushEvent(view, "onLongPress", makeClickEventData(point));
            }
        });

        map.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
                LatLng center = position.target;
                lastBoundsEmitted = bounds;
                eventDispatcher.dispatchEvent(new RegionChangeEvent(getId(), bounds, center, isTouchDown));
                view.stopMonitoringRegion();
            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {

            }


        });

        // We need to be sure to disable location-tracking when app enters background, in-case some
        // other module
        // has acquired a wake-lock and is controlling location-updates, otherwise, location-manager
        // will be left
        // updating location constantly, killing the battery, even though some other location-mgmt
        // module may
        // desire to shut-down location-services.
        lifecycleListener = new LifecycleEventListener() {
            @Override
            public void onHostResume() {
                if (hasPermissions()) {
                    //noinspection MissingPermission
                    map.setMyLocationEnabled(showUserLocation);
                }
                synchronized (AMapView.this) {
                    AMapView.this.onResume();
                    paused = false;
                }
            }

            @Override
            public void onHostPause() {
                if (hasPermissions()) {
                    //noinspection MissingPermission
                    map.setMyLocationEnabled(false);
                }
                synchronized (AMapView.this) {
                    AMapView.this.onPause();
                    paused = true;
                }
            }

            @Override
            public void onHostDestroy() {
                AMapView.this.doDestroy();
            }
        };

        ((ThemedReactContext) getContext()).addLifecycleEventListener(lifecycleListener);
    }

    private boolean hasPermissions() {
        return checkSelfPermission(getContext(), PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(getContext(), PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED;
    }

    /*
    onDestroy is final method so I can't override it.
     */
    public synchronized void doDestroy() {
        if (lifecycleListener != null) {
            ((ThemedReactContext) getContext()).removeLifecycleEventListener(lifecycleListener);
            lifecycleListener = null;
        }
        if (!paused) {
            onPause();
        }
        onDestroy();
    }

    public void setRegion(ReadableMap region) {
        if (region == null) return;
        Double lat, lng, latDelta, lngDelta;

        CameraPosition cameraPosition = map.getCameraPosition();
        LatLng center = cameraPosition.target;
        float zoomLevel = cameraPosition.zoom;
        lat = center.latitude;
        lng = center.longitude;
        latDelta = 0.0;
        lngDelta = 0.0;
        try {
            lng = region.getDouble("longitude");
            lat = region.getDouble("latitude");
            lngDelta = region.getDouble("longitudeDelta");
            latDelta = region.getDouble("latitudeDelta");
        } catch (NoSuchKeyException e) {
            Log.e("AMap", e.getMessage());
        }

        LatLngBounds bounds = new LatLngBounds(
                new LatLng(lat - latDelta / 2, lng - lngDelta / 2), // southwest
                new LatLng(lat + latDelta / 2, lng + lngDelta / 2)  // northeast
        );
        if (super.getHeight() <= 0 || super.getWidth() <= 0) {
            // in this case, our map has not been laid out yet, so we save the bounds in a local
            // variable, and make a guess of zoomLevel 10. Not to worry, though: as soon as layout
            // occurs, we will move the camera to the saved bounds. Note that if we tried to move
            // to the bounds now, it would trigger an exception.
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoomLevel));
            boundsToMove = bounds;
        } else {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoomLevel));
//            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
            boundsToMove = null;
        }
    }

    public void setShowsUserLocation(boolean showUserLocation) {
        this.showUserLocation = showUserLocation; // hold onto this for lifecycle handling
        if (hasPermissions()) {
            //noinspection MissingPermission
            map.setMyLocationEnabled(showUserLocation);
        }
    }

    public void  setZoomLevel(float zoomLevel) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo(zoomLevel);
        map.animateCamera(cameraUpdate);
    }

    //定位
//    private void initLoc() {
//        //初始化定位
//        mLocationClient = new AMapLocationClient(getApplicationContext());
//        //设置定位回调监听
//        mLocationClient.setLocationListener(this);
//        //初始化定位参数
//        mLocationOption = new AMapLocationClientOption();
//        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
//        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
//        //设置是否返回地址信息（默认返回地址信息）
//        mLocationOption.setNeedAddress(true);
//        //设置是否只定位一次,默认为false
//        mLocationOption.setOnceLocation(false);
//        //设置是否强制刷新WIFI，默认为强制刷新
//        mLocationOption.setWifiActiveScan(true);
//        //设置是否允许模拟位置,默认为false，不允许模拟位置
//        mLocationOption.setMockEnable(false);
//        //设置定位间隔,单位毫秒,默认为2000ms
//        mLocationOption.setInterval(2000);
//        //给定位客户端对象设置定位参数
//        mLocationClient.setLocationOption(mLocationOption);
//        //启动定位
//        mLocationClient.startLocation();
//    }

    public void addFeature(View child, int index) {
        // Our desired API is to pass up annotations/overlays as children to the mapview component.
        // This is where we intercept them and do the appropriate underlying mapview action.
        if (child instanceof AMapMarker) {
            AMapMarker annotation = (AMapMarker) child;
            annotation.addToMap(map);
            features.add(index, annotation);
            Marker marker = (Marker) annotation.getFeature();
            markerMap.put(marker, annotation);
        } else if (child instanceof AMapPolyline) {
            AMapPolyline polylineView = (AMapPolyline) child;
            polylineView.addToMap(map);
            features.add(index, polylineView);
            Polyline polyline = (Polyline) polylineView.getFeature();
            polylineMap.put(polyline, polylineView);
        } else if (child instanceof AMapPolygon) {
            AMapPolygon polygonView = (AMapPolygon) child;
            polygonView.addToMap(map);
            features.add(index, polygonView);
            Polygon polygon = (Polygon) polygonView.getFeature();
            polygonMap.put(polygon, polygonView);
        } else if (child instanceof AMapCircle) {
            AMapCircle circleView = (AMapCircle) child;
            circleView.addToMap(map);
            features.add(index, circleView);
            Circle circle = (Circle) circleView.getFeature();
            circleMap.put(circle, circleView);
        } else {
            // TODO(lmr): throw? User shouldn't be adding non-feature children.
        }
    }

    public int getFeatureCount() {
        return features.size();
    }

    public View getFeatureAt(int index) {
        return features.get(index);
    }

    public void removeFeatureAt(int index) {
        AMapFeature feature = features.remove(index);
        feature.removeFromMap(map);

        if (feature instanceof AMapMarker) {
            markerMap.remove(feature.getFeature());
        } else if (feature instanceof AMapPolyline) {
            polylineMap.remove(feature.getFeature());
        } else if (feature instanceof AMapPolygon) {
            polygonMap.remove(feature.getFeature());
        } else if (feature instanceof AMapCircle) {
            circleMap.remove(feature.getFeature());
        }
    }

    public WritableMap makeClickEventData(LatLng point) {
        WritableMap event = new WritableNativeMap();

        WritableMap coordinate = new WritableNativeMap();
        coordinate.putDouble("latitude", point.latitude);
        coordinate.putDouble("longitude", point.longitude);
        event.putMap("coordinate", coordinate);

        Projection projection = map.getProjection();
        Point screenPoint = projection.toScreenLocation(point);

        WritableMap position = new WritableNativeMap();
        position.putDouble("x", screenPoint.x);
        position.putDouble("y", screenPoint.y);
        event.putMap("position", position);

        return event;
    }

    public void updateExtraData(Object extraData) {
        // if boundsToMove is not null, we now have the MapView's width/height, so we can apply
        // a proper camera move
        if (boundsToMove != null) {
            HashMap<String, Float> data = (HashMap<String, Float>) extraData;
            float width = data.get("width");
            float height = data.get("height");
            map.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(
                            boundsToMove,
                            (int) width,
                            (int) height,
                            0
                    )
            );
            boundsToMove = null;
        }
    }

    public void animateToRegion(LatLngBounds bounds, int duration) {
        startMonitoringRegion();
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0), duration, null);
    }

    public void animateToCoordinate(LatLng coordinate, int duration) {
        startMonitoringRegion();
        map.animateCamera(CameraUpdateFactory.newLatLng(coordinate), duration, null);
    }

    public void fitToElements(boolean animated) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (AMapFeature feature : features) {
            if (feature instanceof AMapMarker) {
                Marker marker = (Marker) feature.getFeature();
                builder.include(marker.getPosition());
            }
            // TODO(lmr): may want to include shapes / etc.
        }
        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 50);
        if (animated) {
            startMonitoringRegion();
            map.animateCamera(cu);
        } else {
            map.moveCamera(cu);
        }
    }

    // InfoWindowAdapter interface

    @Override
    public View getInfoWindow(Marker marker) {
        AMapMarker markerView = markerMap.get(marker);
        return markerView.getCallout();
    }

    @Override
    public View getInfoContents(Marker marker) {
        AMapMarker markerView = markerMap.get(marker);
        return markerView.getInfoContents();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        scaleDetector.onTouchEvent(ev);
        gestureDetector.onTouchEvent(ev);

        int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                isTouchDown = true;
                break;
            case (MotionEvent.ACTION_MOVE):
                startMonitoringRegion();
                break;
            case (MotionEvent.ACTION_UP):
                isTouchDown = false;
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    // Timer Implementation

    public void startMonitoringRegion() {
        if (isMonitoringRegion) return;
        timerHandler.postDelayed(timerRunnable, 100);
        isMonitoringRegion = true;
    }

    public void stopMonitoringRegion() {
        if (!isMonitoringRegion) return;
        timerHandler.removeCallbacks(timerRunnable);
        isMonitoringRegion = false;
    }

    private LatLngBounds lastBoundsEmitted;

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {

            LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
            if (lastBoundsEmitted == null ||
                    LatLngBoundsUtils.BoundsAreDifferent(bounds, lastBoundsEmitted)) {
                LatLng center = map.getCameraPosition().target;
                lastBoundsEmitted = bounds;
                eventDispatcher.dispatchEvent(new RegionChangeEvent(getId(), bounds, center, true));
            }

            timerHandler.postDelayed(this, 100);
        }
    };

    @Override
    public void onMarkerDragStart(Marker marker) {
        WritableMap event = makeClickEventData(marker.getPosition());
        manager.pushEvent(this, "onMarkerDragStart", event);

        AMapMarker markerView = markerMap.get(marker);
        event = makeClickEventData(marker.getPosition());
        manager.pushEvent(markerView, "onDragStart", event);
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        WritableMap event = makeClickEventData(marker.getPosition());
        manager.pushEvent(this, "onMarkerDrag", event);

        AMapMarker markerView = markerMap.get(marker);
        event = makeClickEventData(marker.getPosition());
        manager.pushEvent(markerView, "onDrag", event);
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        WritableMap event = makeClickEventData(marker.getPosition());
        manager.pushEvent(this, "onMarkerDragEnd", event);

        AMapMarker markerView = markerMap.get(marker);
        event = makeClickEventData(marker.getPosition());
        manager.pushEvent(markerView, "onDragEnd", event);
    }
}
