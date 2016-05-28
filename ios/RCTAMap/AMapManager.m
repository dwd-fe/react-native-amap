/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import "RCTViewManager.h"
#import "AMapManager.h"

#import "RCTBridge.h"
#import "RCTUIManager.h"
#import "RCTConvert+CoreLocation.h"
#import "RCTConvert+AMapKit.h"
#import "RCTEventDispatcher.h"
#import "AMap.h"
#import "UIView+React.h"
#import "AMapMarker.h"
#import "RCTViewManager.h"
#import "RCTConvert.h"
#import "AMapPolyline.h"
#import "AMapPolygon.h"
#import "AMapCircle.h"
#import "SMCalloutView.h"

#import <MAMapKit/MAMapKit.h>



@implementation AMapManager
RCT_EXPORT_MODULE()

- (UIView *)view
{
    AMap *map = [[AMap alloc] init];
    return map;
}


RCT_EXPORT_VIEW_PROPERTY(apiKey, NSString)
RCT_EXPORT_VIEW_PROPERTY(showsUserLocation, BOOL)
RCT_EXPORT_VIEW_PROPERTY(userTrackingMode, MAUserTrackingMode)
RCT_EXPORT_VIEW_PROPERTY(showsPointsOfInterest, BOOL)
RCT_EXPORT_VIEW_PROPERTY(showsBuildings, BOOL)
RCT_EXPORT_VIEW_PROPERTY(showsCompass, BOOL)
RCT_EXPORT_VIEW_PROPERTY(showsScale, BOOL)
RCT_EXPORT_VIEW_PROPERTY(showsTraffic, BOOL)
RCT_EXPORT_VIEW_PROPERTY(zoomEnabled, BOOL)
RCT_EXPORT_VIEW_PROPERTY(zoomLevel, double)
RCT_EXPORT_VIEW_PROPERTY(rotateEnabled, BOOL)
RCT_EXPORT_VIEW_PROPERTY(scrollEnabled, BOOL)
RCT_EXPORT_VIEW_PROPERTY(pitchEnabled, BOOL)
RCT_EXPORT_VIEW_PROPERTY(maxDelta, CGFloat)
RCT_EXPORT_VIEW_PROPERTY(minDelta, CGFloat)
RCT_EXPORT_VIEW_PROPERTY(legalLabelInsets, UIEdgeInsets)
RCT_EXPORT_VIEW_PROPERTY(mapType, MAMapType)
RCT_EXPORT_VIEW_PROPERTY(onMapReady, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onChange, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onPress, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onLongPress, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onMarkerPress, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onMarkerSelect, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onMarkerDeselect, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onMarkerDragStart, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onMarkerDrag, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onMarkerDragEnd, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onCalloutPress, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(initialRegion, MACoordinateRegion)

RCT_CUSTOM_VIEW_PROPERTY(region, MACoordinateRegion, AMap)
{
    if (json == nil) return;

    // don't emit region change events when we are setting the region
    BOOL originalIgnore = view.ignoreRegionChanges;
    view.ignoreRegionChanges = YES;
    NSLog(@"RCT_CUSTOM_VIEW_PROPERTY-setRegion:%f", [RCTConvert MACoordinateRegion:json].center.latitude);
    [view setRegion:[RCTConvert MACoordinateRegion:json] animated:NO];
    view.ignoreRegionChanges = originalIgnore;
}


#pragma mark exported MapView methods

RCT_EXPORT_METHOD(animateToZoomLevel:(nonnull NSNumber *)reactTag To:(double)zoomLevel {
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
        id view = viewRegistry[reactTag];
        if (![view isKindOfClass:[AMap class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting AMap, got: %@", view);
        } else {
            [[(AMap *)view mapView] setZoomLevel:zoomLevel animated:YES];
        }
    }];
})

RCT_EXPORT_METHOD(animateToRegion:(nonnull NSNumber *)reactTag
        withRegion:(MACoordinateRegion)region
        withDuration:(CGFloat)duration)
{
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
        id view = viewRegistry[reactTag];
        if (![view isKindOfClass:[AMap class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting AMap, got: %@", view);
        } else {
            [AMap animateWithDuration:duration/1000 animations:^{
                [(AMap *)view setRegion:region animated:YES];
            }];
        }
    }];
}

RCT_EXPORT_METHOD(animateToCoordinate:(nonnull NSNumber *)reactTag
        withRegion:(CLLocationCoordinate2D)latlng
        withDuration:(CGFloat)duration)
{
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
        id view = viewRegistry[reactTag];
        if (![view isKindOfClass:[AMap class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting AMap, got: %@", view);
        } else {
            AMap *map = (AMap *)view;
            MACoordinateRegion region;
            region.span = map.mapView.region.span;
            region.center = latlng;
            [AMap animateWithDuration:duration/1000 animations:^{
                [map setRegion:region animated:YES];
            }];
        }
    }];
}

RCT_EXPORT_METHOD(fitToElements:(nonnull NSNumber *)reactTag
        animated:(BOOL)animated)
{
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
        id view = viewRegistry[reactTag];
        if (![view isKindOfClass:[AMap class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting AMap, got: %@", view);
        } else {
            AMap *map = (AMap *)view;
            // TODO(lmr): we potentially want to include overlays here... and could concat the two arrays together.
            [map.mapView showAnnotations:map.mapView.annotations animated:animated];
        }
    }];
}


@end
