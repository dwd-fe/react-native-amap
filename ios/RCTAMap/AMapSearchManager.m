//
//  AMapSearchManager.m
//  RCTAMap
//
//  Created by yuanmarshal on 16/6/1.
//  Copyright © 2016年 dianowba. All rights reserved.
//

#import "AMapSearchManager.h"
#import "RCTBridge.h"
#import "RCTEventDispatcher.h"
#import "RCTViewManager.h"
#import <AMapSearchKit/AMapSearchKit.h>
#import <objc/runtime.h>

@interface AMapSearchObject(RequestId)
@property (nonatomic, strong) NSString* requestId;
@end


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

@implementation AMapSearchManager
{
    AMapSearchAPI *_search;
    AMapInputTipsSearchRequest *_tipsRequest;
}

-(instancetype)init
{
    _search = [[AMapSearchAPI alloc] init];
    _search.delegate = self;
    return self;
}

//static dispatch_queue_t getMethodQueue()
//{
//    static dispatch_queue_t queue;
//    static dispatch_once_t onceToken;
//    dispatch_once(&onceToken, ^{
//        queue = dispatch_queue_create("com.dianwoba.ios.rctamap", DISPATCH_QUEUE_SERIAL);
//    });
//    return queue;
//}

RCT_EXPORT_MODULE();

@synthesize bridge = _bridge;


RCT_EXPORT_METHOD(inputTipsSearch:(NSString *) keys city:(NSString *)city requestId:(NSString *)requestId)
{
    _tipsRequest = [[AMapInputTipsSearchRequest alloc] init];

    _tipsRequest.keywords = keys;
    _tipsRequest.city = city;
    _tipsRequest.requestId = requestId;
    [_search AMapInputTipsSearch:_tipsRequest];
}

RCT_EXPORT_METHOD(weatherSearch:(NSString *)city isLive:(BOOL) isLive requestId:(NSString *)requestId)
{
    AMapWeatherSearchRequest *request = [[AMapWeatherSearchRequest alloc] init];
    request.city = city;
    request.type = isLive? AMapWeatherTypeLive: AMapWeatherTypeForecast;
    request.requestId = requestId;
    [_search AMapWeatherSearch:request];
}

//实现输入提示的回调函数
-(void)onInputTipsSearchDone:(AMapInputTipsSearchRequest*)request response:(AMapInputTipsSearchResponse *)response
{
    //通过AMapInputTipsSearchResponse对象处理搜索结果
    NSMutableArray *arr = [[NSMutableArray alloc] init];
    for (AMapTip *p in response.tips)
    {
        NSDictionary *n = [self amapTipToJson: p];
        [arr addObject:n];
    }
  
    [self.bridge.eventDispatcher sendAppEventWithName:@"ReceiveAMapSearchResult" body:@{
                                                                                     @"requestId":request.requestId, @"data":arr}];
}


-(NSDictionary *)amapTipToJson:(AMapTip *)tip
{
    return @{@"name":tip.name,
             @"location":@{@"latitude":@(tip.location.latitude), @"longitude":@(tip.location.longitude)},
             @"district":tip.district
             };
}

//实现天气查询的回调函数
- (void)onWeatherSearchDone:(AMapWeatherSearchRequest *)request response:(AMapWeatherSearchResponse *)response
{
    NSMutableArray *arr = [[NSMutableArray alloc] init];
    //如果是实时天气
    if(request.type == AMapWeatherTypeLive)
    {
        for (AMapLocalWeatherLive *live in response.lives) {
            [arr addObject:[self dictionaryWithPropertiesOfObject:live]];
        }
    }
    //如果是预报天气
    else
    {
        if(response.forecasts.count == 0)
        {
            return;
        }
        for (AMapLocalWeatherForecast *forecast in response.forecasts) {
            [arr addObject:[self amapLocalWeatherForecastToJson:forecast]];
        }
    }
    
    [self.bridge.eventDispatcher sendAppEventWithName:@"ReceiveAMapSearchResult" body:@{
                                                                                        @"requestId":request.requestId, @"data":arr}];
}


-(NSDictionary *)amapLocalWeatherForecastToJson:(AMapLocalWeatherForecast *) forecast
{
    NSMutableArray *arr = [[NSMutableArray alloc] init];
    for (AMapLocalDayWeatherForecast *cast in forecast.casts) {
        [arr addObject:[self dictionaryWithPropertiesOfObject:cast]];
    }
    
    return @{@"adcode": forecast.adcode,
             @"province": forecast.province,
             @"city": forecast.city,
             @"reportTime": forecast.reportTime,
             @"casts": arr
             };
}



-(void)AMapSearchRequest:(id)request didFailWithError:(NSError *)error
{
    AMapSearchObject *search = (AMapSearchObject *)request;
    [self.bridge.eventDispatcher sendAppEventWithName:@"ReceiveAMapSearchResult" body:@{
                                                                                     @"id":search.requestId, @"error":error}];
}

- (NSDictionary *) dictionaryWithPropertiesOfObject:(id)obj
{
    NSMutableDictionary *dict = [NSMutableDictionary dictionary];
    
    unsigned count;
    objc_property_t *properties = class_copyPropertyList([obj class], &count);
    
    for (int i = 0; i < count; i++) {
        NSString *key = [NSString stringWithUTF8String:property_getName(properties[i])];
        [dict setObject:[obj valueForKey:key] forKey:key];
    }
    
    free(properties);
    
    return [NSDictionary dictionaryWithDictionary:dict];
}

@end
