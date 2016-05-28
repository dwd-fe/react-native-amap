/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import "AMapCalloutManager.h"

#import "RCTBridge.h"
#import "RCTConvert.h"
#import "RCTConvert+CoreLocation.h"
#import "RCTEventDispatcher.h"
#import "UIView+React.h"
#import "AMapMarker.h"
#import "RCTViewManager.h"
#import "AMapCallout.h"

@interface AMapCalloutManager()

@end

@implementation AMapCalloutManager

RCT_EXPORT_MODULE()

- (UIView *)view
{
    return [AMapCallout new];
}

RCT_EXPORT_VIEW_PROPERTY(tooltip, BOOL)
RCT_EXPORT_VIEW_PROPERTY(onPress, RCTBubblingEventBlock)

@end
