/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import <MAMapKit/MAMapKit.h>
#import <UIKit/UIKit.h>
#import <MAMapKit/MAMapKit.h>
#import "RCTConvert+AMapKit.h"
#import "AMap.h"
#import "RCTComponent.h"
#import "RCTView.h"
#import "SMCalloutView.h"
#import "RCTEventDispatcher.h"

#define MAP_PROP_UPDATE(prop_type, prop_name) \
- (void)set##prop_name:(prop_type)set_val { _##prop_name = set_val; [self updateMap]; } \


extern const CLLocationDegrees AMapDefaultSpan;
extern const NSTimeInterval AMapRegionChangeObserveInterval;
extern const CGFloat AMapZoomBoundBuffer;


@interface AMap: RCTView<SMCalloutViewDelegate, MAMapViewDelegate, UIGestureRecognizerDelegate>


@property (nonatomic, strong) SMCalloutView *calloutView;

@property (nonatomic, assign) MAMapView *mapView;

@property (nonatomic, strong) NSString *apiKey;

@property (nonatomic, assign) BOOL showsUserLocation;
@property (nonatomic, assign) BOOL zoomEnabled;
@property (nonatomic, assign) BOOL showsTraffic;
@property (nonatomic, assign) BOOL showsCompass;
@property (nonatomic, assign) BOOL showsScale;
@property (nonatomic, assign) double zoomLevel;
@property (nonatomic, assign) MAMapType mapType;

@property (nonatomic, assign) BOOL followUserLocation;
@property (nonatomic, assign) BOOL hasStartedRendering;
@property (nonatomic, assign) CGFloat minDelta;
@property (nonatomic, assign) CGFloat maxDelta;
@property (nonatomic, assign) UIEdgeInsets legalLabelInsets;
@property (nonatomic, strong) NSTimer *regionChangeObserveTimer;
@property (nonatomic, assign) MACoordinateRegion initialRegion;

@property (nonatomic, assign) CLLocationCoordinate2D pendingCenter;
@property (nonatomic, assign) MACoordinateSpan pendingSpan;


@property (nonatomic, assign) BOOL ignoreRegionChanges;

@property (nonatomic, copy) RCTBubblingEventBlock onMapReady;
@property (nonatomic, copy) RCTBubblingEventBlock onChange;
@property (nonatomic, copy) RCTBubblingEventBlock onPress;
@property (nonatomic, copy) RCTBubblingEventBlock onLongPress;
@property (nonatomic, copy) RCTDirectEventBlock onMarkerPress;
@property (nonatomic, copy) RCTDirectEventBlock onMarkerSelect;
@property (nonatomic, copy) RCTDirectEventBlock onMarkerDeselect;
@property (nonatomic, copy) RCTDirectEventBlock onMarkerDragStart;
@property (nonatomic, copy) RCTDirectEventBlock onMarkerDrag;
@property (nonatomic, copy) RCTDirectEventBlock onMarkerDragEnd;
@property (nonatomic, copy) RCTDirectEventBlock onCalloutPress;
@property (nonatomic, copy) RCTDirectEventBlock onRegionChange;


- (void)setRegion:(MACoordinateRegion)region animated:(BOOL)animated;

@end
