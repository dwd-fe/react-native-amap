//
//  AMapSearchObject+RequestId.m
//  RCTAMap
//
//  Created by yuanmarshal on 16/6/10.
//  Copyright © 2016年 dianowba. All rights reserved.
//

#import "AMapSearchObject+RequestId.h"
#import <objc/runtime.h>

@implementation AMapSearchObject(RequestId)
- (void)setRequestId:(NSString *)requestId
{
    objc_setAssociatedObject(self, @selector(requestId), requestId, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}
- (NSString *)requestId
{
    return  objc_getAssociatedObject(self, @selector(requestId));
}

@end