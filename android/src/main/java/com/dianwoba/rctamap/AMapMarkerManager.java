package com.dianwoba.rctamap;

import android.graphics.Color;
import android.view.View;

import com.amap.api.maps2d.model.Marker;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.LayoutShadowNode;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;


import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class AMapMarkerManager extends ViewGroupManager<AMapMarker> {

    public static final int SHOW_INFO_WINDOW = 1;
    public static final int HIDE_INFO_WINDOW = 2;

    public AMapMarkerManager() {
    }

    @Override
    public String getName() {
        return "AMapMarker";
    }

    @Override
    public AMapMarker createViewInstance(ThemedReactContext context) {
        return new AMapMarker(context);
    }

    @ReactProp(name = "coordinate")
    public void setCoordinate(AMapMarker view, ReadableMap map) {
        view.setCoordinate(map);
    }

    @ReactProp(name = "title")
    public void setTitle(AMapMarker view, String title) {
        view.setTitle(title);
    }

    @ReactProp(name = "description")
    public void setDescription(AMapMarker view, String description) {
        view.setSnippet(description);
    }

    // NOTE(lmr):
    // android uses normalized coordinate systems for this, and is provided through the
    // `anchor` property  and `calloutAnchor` instead.  Perhaps some work could be done
    // to normalize iOS and android to use just one of the systems.
//    @ReactProp(name = "centerOffset")
//    public void setCenterOffset(AirMapMarker view, ReadableMap map) {
//
//    }
//
//    @ReactProp(name = "calloutOffset")
//    public void setCalloutOffset(AirMapMarker view, ReadableMap map) {
//
//    }

    @ReactProp(name = "anchor")
    public void setAnchor(AMapMarker view, ReadableMap map) {
        // should default to (0.5, 1) (bottom middle)
        double x = map != null && map.hasKey("x") ? map.getDouble("x") : 0.5;
        double y = map != null && map.hasKey("y") ? map.getDouble("y") : 1.0;
        view.setAnchor(x, y);
    }

    @ReactProp(name = "calloutAnchor")
    public void setCalloutAnchor(AMapMarker view, ReadableMap map) {
        // should default to (0.5, 0) (top middle)
        double x = map != null && map.hasKey("x") ? map.getDouble("x") : 0.5;
        double y = map != null && map.hasKey("y") ? map.getDouble("y") : 0.0;
        view.setCalloutAnchor(x, y);
    }

    @ReactProp(name = "image")
    public void setImage(AMapMarker view, @Nullable String source) {
        view.setImage(source);
    }
//    public void setImage(AirMapMarker view, ReadableMap image) {
//        view.setImage(image);
//    }

    @ReactProp(name = "pinColor", defaultInt = Color.BLUE, customType = "Color")
    public void setPinColor(AMapMarker view, int pinColor) {
        float[] hsv = new float[3];
        Color.colorToHSV(pinColor, hsv);
        // NOTE: android only supports a hue
        view.setMarkerHue(hsv[0]);
    }

    @ReactProp(name = "rotation", defaultFloat = 0.0f)
    public void setMarkerRotation(AMapMarker view, float rotation) {
        view.setRotation(rotation);
    }

    // TODO: 16/5/19
//    @ReactProp(name = "flat", defaultBoolean = false)
//    public void setFlat(AMapMarker view, boolean flat) {
//        view.setFlat(flat);
//    }

    @ReactProp(name = "draggable", defaultBoolean = false)
    public void setDraggable(AMapMarker view, boolean draggable) {
        view.setDraggable(draggable);
    }

    @Override
    public void addView(AMapMarker parent, View child, int index) {
        // if an <Callout /> component is a child, then it is a callout view, NOT part of the
        // marker.
        if (child instanceof AMapCallout) {
            parent.setCalloutView((AMapCallout) child);
        } else {
            super.addView(parent, child, index);
            parent.update();
        }
    }

    @Override
    public void removeViewAt(AMapMarker parent, int index) {
        super.removeViewAt(parent, index);
        parent.update();
    }

    @Override
    @Nullable
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of(
                "showCallout", SHOW_INFO_WINDOW,
                "hideCallout", HIDE_INFO_WINDOW
        );
    }

    @Override
    public void receiveCommand(AMapMarker view, int commandId, @Nullable ReadableArray args) {
        switch (commandId) {
            case SHOW_INFO_WINDOW:
                ((Marker) view.getFeature()).showInfoWindow();
                break;

            case HIDE_INFO_WINDOW:
                ((Marker) view.getFeature()).hideInfoWindow();
                break;
        }
    }

    @Override
    @Nullable
    public Map getExportedCustomDirectEventTypeConstants() {
        Map map = MapBuilder.of(
                "onPress", MapBuilder.of("registrationName", "onPress"),
                "onCalloutPress", MapBuilder.of("registrationName", "onCalloutPress"),
                "onDragStart", MapBuilder.of("registrationName", "onDragStart"),
                "onDrag", MapBuilder.of("registrationName", "onDrag"),
                "onDragEnd", MapBuilder.of("registrationName", "onDragEnd")
        );

        map.putAll(MapBuilder.of(
                "onDragStart", MapBuilder.of("registrationName", "onDragStart"),
                "onDrag", MapBuilder.of("registrationName", "onDrag"),
                "onDragEnd", MapBuilder.of("registrationName", "onDragEnd")
        ));

        return map;
    }

    @Override
    public LayoutShadowNode createShadowNodeInstance() {
        // we use a custom shadow node that emits the width/height of the view
        // after layout with the updateExtraData method. Without this, we can't generate
        // a bitmap of the appropriate width/height of the rendered view.
        return new SizeReportingShadowNode();
    }

    @Override
    public void updateExtraData(AMapMarker view, Object extraData) {
        // This method is called from the shadow node with the width/height of the rendered
        // marker view.
        HashMap<String, Float> data = (HashMap<String, Float>) extraData;
        float width = data.get("width");
        float height = data.get("height");
        view.update((int) width, (int) height);
    }
}
