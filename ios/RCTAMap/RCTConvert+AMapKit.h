//
//  RCTConvert+AMapKit.h
//  RCTAMap
//
//  Created by yuanmarshal on 16/5/6.
//  Copyright © 2016年 dianowba. All rights reserved.
//

#import "RCTConvert.h"
#import "AMapCoordinate.h"
#import <MAMapKit/MAMapKit.h>
#import <AMapSearchKit/AMapSearchKit.h>

@class MAAnnotation;
@class MAOverlay;

@interface RCTConvert (AMapKit)

+ (MACoordinateSpan)MACoordinateSpan:(id)json;
+ (MACoordinateRegion)MACoordinateRegion:(id)json;
+ (MAMapType)MAMapType:(id)json;
+ (MAUserTrackingMode)MAUserTrackingMode:(id)json;
+ (AMapCoordinate *) AMapCoordinate:(id)json;
+ (AMapGeoPoint *)AMapGeoPoint:(id)json;

//+ (MAAnnotation *)MAMapAnnotation:(id)json;
//+ (MAOverlay *)MAMapOverlay:(id)json;
//
//+ (NSArray<MAAnnotation *> *)MAAnnotationArray:(id)json;
//+ (NSArray<MAOverlay *> *)MAOverlayArray:(id)json;

@end
