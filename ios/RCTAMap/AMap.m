/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import "AMap.h"

#import "RCTEventDispatcher.h"
#import "RCTUtils.h"
#import "AMapMarker.h"
#import "UIView+React.h"
#import "AMapPolyline.h"
#import "AMapPolygon.h"
#import "AMapCircle.h"

static NSString *const RCTMapViewKey = @"AMapView";

const CLLocationDegrees AMapDefaultSpan = 0.005;
const NSTimeInterval AMapRegionChangeObserveInterval = 0.1;
const CGFloat AMapZoomBoundBuffer = 0.01;


//@interface RCTView (UIGestureRecognizer)
//
//// this tells the compiler that MKMapView actually implements this method
//- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldReceiveTouch:(UITouch *)touch;
//
//@end


@implementation AMap
{
    UIView *_legalLabel;
    CLLocationManager *_locationManager;
    BOOL _initialRegionSet;

    // Array to manually track RN subviews
    //
    // AIRMap implicitly creates subviews that aren't regular RN children
    // (SMCalloutView injects an overlay subview), which otherwise confuses RN
    // during component re-renders:
    // https://github.com/facebook/react-native/blob/v0.16.0/React/Modules/RCTUIManager.m#L657
    //
    // Implementation based on RCTTextField, another component with indirect children
    // https://github.com/facebook/react-native/blob/v0.16.0/Libraries/Text/RCTTextField.m#L20
    NSMutableArray<UIView *> *_reactSubviews;
    
}

- (instancetype)init
{
    if (self = [super init]) {
        _hasStartedRendering = NO;
        _reactSubviews = [NSMutableArray new];
        
        for (UIView *subView in self.subviews) {
            if ([NSStringFromClass(subView.class) isEqualToString:@"MAAttributionLabel"]) {
                // This check is super hacky, but the whole premise of moving around
                // Apple's internal subviews is super hacky
                _legalLabel = subView;
                break;
            }
        }
        self.calloutView = [SMCalloutView platformCalloutView];
        self.calloutView.delegate = self;
    }
    
    return self;
}

- (void)dealloc
{
    [_regionChangeObserveTimer invalidate];
}

- (void) updateMap {
    if (_mapView) {
        self.mapView.showsScale = _showsScale;
        self.mapView.showsCompass = _showsCompass;
        self.mapView.showTraffic = _showsTraffic;
        if(self.mapView.zoomLevel != _zoomLevel) self.mapView.zoomLevel = _zoomLevel;
        
        if (!_initialRegionSet) {
            _initialRegionSet = YES;
            [self setRegion:_initialRegion animated:YES];
        }
        
        self.mapView.zoomEnabled = _zoomEnabled;
        self.mapView.showsUserLocation = _showsUserLocation;
        self.mapView.mapType = _mapType;
    } else {
        /* We need to have a height/width specified in order to render */
        if (self.bounds.size.height > 0 && self.bounds.size.width > 0) {
            [self createMap];
        }
    }
}

- (void) createMap {
    [MAMapServices sharedServices].apiKey = _apiKey;
    _mapView = [[MAMapView alloc] initWithFrame:self.bounds];
    
    // MAMapView doesn't report tap events, so we attach gesture recognizers to it
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleMapTap:)];
    tap.delegate = self;
    //setting this to NO allows the parent MapView to continue receiving marker selection events
    tap.cancelsTouchesInView = NO;
    
    UILongPressGestureRecognizer *longPress = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(handleMapLongPress:)];
    longPress.delegate = self;
    longPress.cancelsTouchesInView = NO;
    longPress.numberOfTouchesRequired = 1;
    
    [self addGestureRecognizer:tap];
    [self addGestureRecognizer:longPress];
    _mapView.delegate = self;
    
    [self updateMap];
    [self mapDidReady];
    [self addSubview:_mapView];
}

- (void) mapDidReady {
    if (self.onMapReady) {
        self.onMapReady(@{});
    }
    for (UIView* subview in _reactSubviews) {
        [self insetAnnotation:subview];
    }
}

- (void) layoutSubviews {
    NSLog(@"layoutSubViews");
    [self updateMap];
    [_mapView setFrame:self.bounds];
}

- (void) insetAnnotation:(UIView*)subview
{
    if ([subview isKindOfClass:[AMapMarker class]]) {
        [_mapView addAnnotation:(id <MAAnnotation>) subview];
    } else if ([subview isKindOfClass:[AMapPolyline class]]) {
        ((AMapPolyline *)subview).map = self;
        [_mapView addOverlay:(id<MAOverlay>)subview];
    } else if ([subview isKindOfClass:[AMapPolygon class]]) {
        ((AMapPolygon *)subview).map = self;
        [_mapView addOverlay:(id<MAOverlay>)subview];
    } else if ([subview isKindOfClass:[AMapCircle class]]) {
        [_mapView addOverlay:(id<MAOverlay>)subview];
    }
}

- (void)insertReactSubview:(id<RCTComponent>)subview atIndex:(NSInteger)atIndex {
    // Our desired API is to pass up markers/overlays as children to the mapview component.
    // This is where we intercept them and do the appropriate underlying mapview action.
    // If _mapView is not ready, just push to _reactSubViews;
    if (_mapView) {
        [self insetAnnotation:subview];
    }
    [_reactSubviews insertObject:(UIView *)subview atIndex:(NSUInteger) atIndex];
}

- (void)removeReactSubview:(id<RCTComponent>)subview {
    // similarly, when the children are being removed we have to do the appropriate
    // underlying mapview action here.
    if ([subview isKindOfClass:[AMapMarker class]]) {
        [_mapView removeAnnotation:(id<MAAnnotation>)subview];
    } else if ([subview isKindOfClass:[AMapPolyline class]]) {
        [_mapView removeOverlay:(id <MAOverlay>) subview];
    } else if ([subview isKindOfClass:[AMapPolygon class]]) {
        [_mapView removeOverlay:(id <MAOverlay>) subview];
    } else if ([subview isKindOfClass:[AMapCircle class]]) {
        [_mapView removeOverlay:(id <MAOverlay>) subview];
    }
    [_reactSubviews removeObject:(UIView *)subview];
}

- (NSArray<id<RCTComponent>> *)reactSubviews {
    return _reactSubviews;
}

#pragma mark Overrides for Callout behavior
// Allow touches to be sent to our calloutview.
// See this for some discussion of why we need to override this: https://github.com/nfarina/calloutview/pull/9
- (UIView *)hitTest:(CGPoint)point withEvent:(UIEvent *)event {

    UIView *calloutMaybe = [self.calloutView hitTest:[self.calloutView convertPoint:point fromView:self] withEvent:event];
    if (calloutMaybe) return calloutMaybe;

    return [super hitTest:point withEvent:event];
}

#pragma mark SMCalloutViewDelegate

- (NSTimeInterval)calloutView:(SMCalloutView *)calloutView delayForRepositionWithSize:(CGSize)offset {

    // When the callout is being asked to present in a way where it or its target will be partially offscreen, it asks us
    // if we'd like to reposition our surface first so the callout is completely visible. Here we scroll the map into view,
    // but it takes some math because we have to deal in lon/lat instead of the given offset in pixels.

    CLLocationCoordinate2D coordinate = _mapView.region.center;

    // where's the center coordinate in terms of our view?
    CGPoint center = [_mapView convertCoordinate:coordinate toPointToView:self];

    // move it by the requested offset
    center.x -= offset.width;
    center.y -= offset.height;

    // and translate it back into map coordinates
    coordinate = [_mapView convertPoint:center toCoordinateFromView:self];

    // move the map!
    [_mapView setCenterCoordinate:coordinate animated:YES];

    // tell the callout to wait for a while while we scroll (we assume the scroll delay for MKMapView matches UIScrollView)
    return kSMCalloutViewRepositionDelayForUIScrollView;
}

#pragma mark MAMapViewDelegate

#pragma mark Polyline stuff

- (MAOverlayRenderer *)mapView:(MAMapView *)mapView rendererForOverlay:(id <MAOverlay>)overlay{
    if ([overlay isKindOfClass:[AMapPolyline class]]) {
        return ((AMapPolyline *)overlay).renderer;
    } else if ([overlay isKindOfClass:[AMapPolygon class]]) {
        return ((AMapPolygon *)overlay).renderer;
    } else if ([overlay isKindOfClass:[AMapCircle class]]) {
        return ((AMapCircle *)overlay).renderer;
    } else {
        return nil;
    }
}


#pragma mark Annotation Stuff



- (void)mapView:(MAMapView *)mapView didSelectAnnotationView:(MAAnnotationView *)view
{
    if ([view.annotation isKindOfClass:[AMapMarker class]]) {
        [(AMapMarker *)view.annotation showCalloutView];
    }
}

- (void)mapView:(AMap *)mapView didDeselectAnnotationView:(MAAnnotationView *)view {
    if ([view.annotation isKindOfClass:[AMapMarker class]]) {
        [(AMapMarker *)view.annotation hideCalloutView];
    }
}

- (MAAnnotationView *)mapView:(__unused MAMapView *)mapView viewForAnnotation:(AMapMarker *)marker
{
    if (![marker isKindOfClass:[AMapMarker class]]) {
        return nil;
    }
    
    marker.map = self;
    return [marker getAnnotationView];
}

static int kDragCenterContext;

- (void)mapView:(MAMapView *)mapView
 annotationView:(MAAnnotationView *)view
didChangeDragState:(MAAnnotationViewDragState)newState
   fromOldState:(MAAnnotationViewDragState)oldState
{
    if (![view.annotation isKindOfClass:[AMapMarker class]]) return;
    AMapMarker *marker = (AMapMarker *)view.annotation;
    
    BOOL isPinView = [view isKindOfClass:[MAPinAnnotationView class]];
    
    id event = @{
                 @"id": marker.identifier ?: @"unknown",
                 @"coordinate": @{
                         @"latitude": @(marker.coordinate.latitude),
                         @"longitude": @(marker.coordinate.longitude)
                         }
                 };
    
    if (newState == MAAnnotationViewDragStateEnding || newState == MAAnnotationViewDragStateCanceling) {
        if (!isPinView) {
            [view setDragState:MAAnnotationViewDragStateNone animated:NO];
        }
        if (self.onMarkerDragEnd) self.onMarkerDragEnd(event);
        if (marker.onDragEnd) marker.onDragEnd(event);
        
        [view removeObserver:self forKeyPath:@"center"];
    } else if (newState == MAAnnotationViewDragStateStarting) {
        // MapKit doesn't emit continuous drag events. To get around this, we are going to use KVO.
        [view addObserver:self forKeyPath:@"center" options:NSKeyValueObservingOptionNew context:&kDragCenterContext];
        
        if (self.onMarkerDragStart) self.onMarkerDragStart(event);
        if (marker.onDragStart) marker.onDragStart(event);
    }
}

- (void)observeValueForKeyPath:(NSString *)keyPath
                      ofObject:(id)object
                        change:(NSDictionary *)change
                       context:(void *)context
{
    if ([keyPath isEqualToString:@"center"] && [object isKindOfClass:[MAAnnotationView class]]) {
        MAAnnotationView *view = (MAAnnotationView *)object;
        AMapMarker *marker = (AMapMarker *)view.annotation;
        
        // a marker we don't control might be getting dragged. Check just in case.
        if (!marker) return;
        
        AMap *map = marker.map;
        
        // don't waste time calculating if there are no events to listen to it
        if (!map.onMarkerDrag && !marker.onDrag) return;
        
        CGPoint position = CGPointMake(view.center.x - view.centerOffset.x, view.center.y - view.centerOffset.y);
        CLLocationCoordinate2D coordinate = [map.mapView convertPoint:position toCoordinateFromView:map];
        
        id event = @{
                     @"id": marker.identifier ?: @"unknown",
                     @"position": @{
                             @"x": @(position.x),
                             @"y": @(position.y),
                             },
                     @"coordinate": @{
                             @"latitude": @(coordinate.latitude),
                             @"longitude": @(coordinate.longitude),
                             }
                     };
        
        if (map.onMarkerDrag) map.onMarkerDrag(event);
        if (marker.onDrag) marker.onDrag(event);
        
    } else {
        // This message is not for me; pass it on to super.
        [super observeValueForKeyPath:keyPath ofObject:object change:change context:context];
    }
}

- (void)mapView:(MAMapView *)mapView didUpdateUserLocation:(MAUserLocation *)location
{
    if (mapView.showsUserLocation) {
        MACoordinateRegion region;
        region.span.latitudeDelta = AMapDefaultSpan;
        region.span.longitudeDelta = AMapDefaultSpan;
        region.center = location.coordinate;
        [mapView setRegion:region animated:YES];
        
        // Move to user location only for the first time it loads up.
        mapView.showsUserLocation = NO;
    }
}

- (void)mapView:(MAMapView *)mapView regionWillChangeAnimated:(__unused BOOL)animated
{
    [self _regionChanged:mapView];
    
    self.regionChangeObserveTimer = [NSTimer timerWithTimeInterval:AMapRegionChangeObserveInterval
                                                               target:self
                                                             selector:@selector(_onTick:)
                                                             userInfo:@{ RCTMapViewKey: mapView }
                                                              repeats:YES];
    
    [[NSRunLoop mainRunLoop] addTimer:self.regionChangeObserveTimer forMode:NSRunLoopCommonModes];
}

- (void)mapView:(MAMapView *)mapView regionDidChangeAnimated:(__unused BOOL)animated
{
    [self.regionChangeObserveTimer invalidate];
    self.regionChangeObserveTimer = nil;
    
    [self _regionChanged:mapView];
    
    // Don't send region did change events until map has
    // started rendering, as these won't represent the final location
    if (self.hasStartedRendering) {
        [self _emitRegionChangeEvent:mapView continuous:NO];
    };
    
    self.pendingCenter = mapView.region.center;
    self.pendingSpan = mapView.region.span;
}

- (void)mapViewWillStartRenderingMap:(MAMapView *)mapView
{
    self.hasStartedRendering = YES;
    [self _emitRegionChangeEvent:mapView continuous:NO];
}


#pragma mark Private

- (void)_onTick:(NSTimer *)timer
{
    [self _regionChanged:timer.userInfo[RCTMapViewKey]];
}

- (void)_regionChanged:(MAMapView *)mapView
{
    BOOL needZoom = NO;
    CGFloat newLongitudeDelta = 0.0f;
    MACoordinateRegion region = mapView.region;
    
    // On iOS 7, it's possible that we observe invalid locations during
    // initialization of the map. Filter those out.
    if (!CLLocationCoordinate2DIsValid(region.center)) {
        return;
    }
    
    // Calculation on float is not 100% accurate. If user zoom to max/min and then
    // move, it's likely the map will auto zoom to max/min from time to time.
    // So let's try to make map zoom back to 99% max or 101% min so that there is
    // some buffer, and moving the map won't constantly hit the max/min bound.
    if (self.maxDelta > FLT_EPSILON &&
        region.span.longitudeDelta > self.maxDelta) {
        needZoom = YES;
        newLongitudeDelta = self.maxDelta * (1 - AMapZoomBoundBuffer);
    } else if (self.minDelta > FLT_EPSILON &&
               region.span.longitudeDelta < self.minDelta) {
        needZoom = YES;
        newLongitudeDelta = self.minDelta * (1 + AMapZoomBoundBuffer);
    }
    if (needZoom) {
        region.span.latitudeDelta =
        region.span.latitudeDelta / region.span.longitudeDelta * newLongitudeDelta;
        region.span.longitudeDelta = newLongitudeDelta;
        mapView.region = region;
    }
    
    // Continously observe region changes
    [self _emitRegionChangeEvent:mapView continuous:YES];
}

- (void)_emitRegionChangeEvent:(MAMapView *)mapView continuous:(BOOL)continuous
{
    if (self.onChange) {
        MACoordinateRegion region = mapView.region;
        if (!CLLocationCoordinate2DIsValid(region.center)) {
            return;
        }
        
        self.onChange(@{
                           @"continuous": @(continuous),
                           @"region": @{
                                   @"latitude": @(RCTZeroIfNaN(region.center.latitude)),
                                   @"longitude": @(RCTZeroIfNaN(region.center.longitude)),
                                   @"latitudeDelta": @(RCTZeroIfNaN(region.span.latitudeDelta)),
                                   @"longitudeDelta": @(RCTZeroIfNaN(region.span.longitudeDelta)),
                                   }
                           });
    }
}

#pragma mark Gesture Recognizer Handlers and Delegate

- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldReceiveTouch:(UITouch *)touch {
    if ([touch.view isDescendantOfView:self.calloutView])
        return NO;
    else
        return YES;
}

- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldRecognizeSimultaneouslyWithGestureRecognizer:(UIGestureRecognizer *)otherGestureRecognizer
{
    return YES;
}


- (void)handleMapTap:(UITapGestureRecognizer *)recognizer {
    AMap *map = (AMap *)recognizer.view;
    if (!map.onPress) return;
    
    CGPoint touchPoint = [recognizer locationInView:map];
    CLLocationCoordinate2D coord = [map.mapView convertPoint:touchPoint toCoordinateFromView:map];
    
    map.onPress(@{
                  @"coordinate": @{
                          @"latitude": @(coord.latitude),
                          @"longitude": @(coord.longitude),
                          },
                  @"position": @{
                          @"x": @(touchPoint.x),
                          @"y": @(touchPoint.y),
                          },
                  });
    
}

- (void)handleMapLongPress:(UILongPressGestureRecognizer *)recognizer {
    // NOTE: android only does the equivalent of "began", so we only send in this case
    if (recognizer.state != UIGestureRecognizerStateBegan) return;
    AMap *map = (AMap *)recognizer.view;
    if (!map.onLongPress) return;
    
    CGPoint touchPoint = [recognizer locationInView:map];
    CLLocationCoordinate2D coord = [map.mapView convertPoint:touchPoint toCoordinateFromView:map];
    
    map.onLongPress(@{
                      @"coordinate": @{
                              @"latitude": @(coord.latitude),
                              @"longitude": @(coord.longitude),
                              },
                      @"position": @{
                              @"x": @(touchPoint.x),
                              @"y": @(touchPoint.y),
                              },
                      });
}

#pragma mark Accessors

MAP_PROP_UPDATE(NSString *, apiKey);
MAP_PROP_UPDATE(BOOL, showsScale);
MAP_PROP_UPDATE(BOOL, showsCompass);
MAP_PROP_UPDATE(BOOL, showsTraffic);
MAP_PROP_UPDATE(BOOL, zoomEnabled);
MAP_PROP_UPDATE(double, zoomLevel);
MAP_PROP_UPDATE(MAMapType, mapType);


- (void)setShowsUserLocation:(BOOL)showsUserLocation
{
    if (self.showsUserLocation != showsUserLocation) {
        if (showsUserLocation && !_locationManager) {
            _locationManager = [CLLocationManager new];
            if ([_locationManager respondsToSelector:@selector(requestWhenInUseAuthorization)]) {
                [_locationManager requestWhenInUseAuthorization];
            }
        }
        [self updateMap];
    }
}

- (void)setRegion:(MACoordinateRegion)region animated:(BOOL)animated
{
    // If location is invalid, abort
    if (!CLLocationCoordinate2DIsValid(region.center)) {
        return;
    }
    

    // If new span values are nil, use old values instead
    if (!region.span.latitudeDelta) {
        region.span.latitudeDelta = _mapView.region.span.latitudeDelta;
    }
    if (!region.span.longitudeDelta) {
        region.span.longitudeDelta = _mapView.region.span.longitudeDelta;
    }
    // Animate/move to new position
    [_mapView setRegion:region animated:animated];

}


- (void)setInitialRegion:(MACoordinateRegion)initialRegion {
    if (!_initialRegionSet) {
        _initialRegion = initialRegion;
        [self updateMap];
    }
}

@end
