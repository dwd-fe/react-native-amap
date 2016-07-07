//
//  RCTConvert+AMapKit.m
//  RCTAMap
//
//  Created by yuanmarshal on 16/5/6.
//  Copyright © 2016年 dianowba. All rights reserved.
//

#import "RCTConvert+AMapKit.h"
#import <MAMapKit/MAMapKit.h>
#import "RCTConvert+CoreLocation.h"

@implementation RCTConvert(AMapKit)

+ (MACoordinateSpan)MACoordinateSpan:(id)json
{
    json = [self NSDictionary:json];
    return (MACoordinateSpan){
        [self CLLocationDegrees:json[@"latitudeDelta"]],
        [self CLLocationDegrees:json[@"longitudeDelta"]]
        
    };
}

+ (MACoordinateRegion)MACoordinateRegion:(id)json
{
    json = [self NSDictionary:json];
    return (MACoordinateRegion){
        [self CLLocationCoordinate2D:json],
        [self MACoordinateSpan:json]
    };
}

RCT_ENUM_CONVERTER(MAMapType, (@{
                                 @"standard": @(MAMapTypeStandard),
                                 @"satellite": @(MAMapTypeSatellite),
                                 }), MAMapTypeStandard, integerValue)

RCT_ENUM_CONVERTER(MAUserTrackingMode, (@{
                                          @"none": @(MAUserTrackingModeNone),
                                          @"follow": @(MAUserTrackingModeFollow),
                                          @"followWithHeading": @(MAUserTrackingModeFollowWithHeading)
                                          }), MAUserTrackingModeNone, integerValue)

+ (AMapCoordinate *)AMapCoordinate:(id)json
{
    AMapCoordinate *coord = [AMapCoordinate new];
    coord.coordinate = [self CLLocationCoordinate2D:json];
    return coord;
}

RCT_ARRAY_CONVERTER(AMapCoordinate)

+ (AMapGeoPoint *)AMapGeoPoint:(id)json
{
    json = [self NSDictionary:json];
    return [AMapGeoPoint locationWithLatitude:
                [self CGFloat:json[@"latitude"]]
                                    longitude:[self CGFloat:json[@"longitude"]]];
}


RCT_ENUM_CONVERTER(MAPinAnnotationColor, (@{
                                            @"red": @(MAPinAnnotationColorRed),
                                            @"green": @(MAPinAnnotationColorGreen),
                                            @"purple": @(MAPinAnnotationColorPurple)
                                            }), MAPinAnnotationColorRed, integerValue)

//+ (MAAnnotation *)AMapAnnotation:(id)json
//{
//    json = [self NSDictionary:json];
//    MAAnnotation *annotation = [MAAnnotation new];
//    annotation.coordinate = [self CLLocationCoordinate2D:json];
//    annotation.draggable = [self BOOL:json[@"draggable"]];
//    annotation.title = [self NSString:json[@"title"]];
//    annotation.subtitle = [self NSString:json[@"subtitle"]];
//    annotation.identifier = [self NSString:json[@"id"]];
//    annotation.hasLeftCallout = [self BOOL:json[@"hasLeftCallout"]];
//    annotation.hasRightCallout = [self BOOL:json[@"hasRightCallout"]];
//    annotation.animateDrop = [self BOOL:json[@"animateDrop"]];
//    annotation.tintColor = [self UIColor:json[@"tintColor"]];
//    annotation.image = [self UIImage:json[@"image"]];
//    annotation.viewIndex =
//    [self NSInteger:json[@"viewIndex"] ?: @(NSNotFound)];
//    annotation.leftCalloutViewIndex =
//    [self NSInteger:json[@"leftCalloutViewIndex"] ?: @(NSNotFound)];
//    annotation.rightCalloutViewIndex =
//    [self NSInteger:json[@"rightCalloutViewIndex"] ?: @(NSNotFound)];
//    annotation.detailCalloutViewIndex =
//    [self NSInteger:json[@"detailCalloutViewIndex"] ?: @(NSNotFound)];
//    return annotation;
//}
//
//RCT_ARRAY_CONVERTER(AMapAnnotation)

//+ (RCTMapOverlay *)AMapOverlay:(id)json
//{
//    json = [self NSDictionary:json];
//    NSArray<NSDictionary *> *locations = [self NSDictionaryArray:json[@"coordinates"]];
//    CLLocationCoordinate2D coordinates[locations.count];
//    NSUInteger index = 0;
//    for (NSDictionary *location in locations) {
//        coordinates[index++] = [self CLLocationCoordinate2D:location];
//    }
//    
//    RCTMapOverlay *overlay = [RCTMapOverlay polylineWithCoordinates:coordinates
//                                                              count:locations.count];
//    
//    overlay.strokeColor = [self UIColor:json[@"strokeColor"]];
//    overlay.identifier = [self NSString:json[@"id"]];
//    overlay.lineWidth = [self CGFloat:json[@"lineWidth"] ?: @1];
//    return overlay;
//}
//
//RCT_ARRAY_CONVERTER(AMapOverlay)

@end

