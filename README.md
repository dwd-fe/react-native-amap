# react-native-amap
React Native AMap component for iOS + Android

react-native-amap-view is a wrapper of AMap's Libraries inspired by react-native-maps and it's usable in Android and iOS

##Demo

![demo-gif](https://raw.githubusercontent.com/dianwoba/react-native-amap/master/doc/assets/demo.gif)

## Installation

`npm install react-native-amap-view --save`

### iOS
Only test on react-native 0.29
* `Add Files to "xxx"` on `Libaries` folder, and select `RCTAMap.xcodeproj`
* In `Link Binary With Libraries`, add `libRCTAMap.a`
* In `Link Binary With Libraries`, add `MAMapKit.framework` and `AMapSearchKit.framework`
* In `Framework Search Paths`, add `$(PROJECT_DIR)/../node_modules/react-native-amap-view/ios`
* `Add Files to "xxx"` on your project, and select `AMap.bundle`
* In `Link Binary With Libraries`, add other libs, see [here](http://lbs.amap.com/api/ios-sdk/guide/create-project/manual-configuration/#t3)
* Make sure `NSAllowsArbitraryLoads` in `Info.plist` is `true`
* Make sure `LSApplicationQueriesSchemes` has `iosamap`
* In `Info.plist`, Add `Privacy - Location Usage Description`=`NSLocationWhenInUseUsageDescription`(for foreground usage) 
  or `NSLocationAlwaysUsageDescription`(for background usage). see [here](http://lbs.amap.com/api/ios-sdk/guide/draw-on-map/draw-location-marker/)

### Android
* `android/setting.gradle`:
```
include ':react-native-amap-view'
project(':react-native-amap-view').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-amap-view/android')
```
* `android/app/build.gradle`:
```
    compile project(":react-native-amap-view")
```
* `MainApplication.java`:
```
import com.dianwoba.rctamap.AMapPackage;
      
      return Arrays.<ReactPackage>asList(
          ...
          , new AMapPackage()
          ...
```
* `AndroidManifest.xml`:
```
    <!-- Geolocation -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" /><!--用于访问GPS定位-->
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" /><!--用于进行网络定位-->
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" /><!--用于访问wifi网络信息，wifi信息会用于进行网络定位-->
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" /><!--这个权限用于获取wifi的获取权限，wifi信息会用来进行网络定位-->
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.ACCESS_LOCATION_EXTRA_COMMANDS" />
    <uses-permission android:name="android.permission.ACCESS_MOCK_LOCATION" />

    <application ...>
      <meta-data android:name="com.amap.api.v2.apikey" android:value="cc5fea37cc1d8bd20434a0020d85ec12"></meta-data>

```

## Usage
```
import AMapView from 'react-native-amap-view'

  render(){
    return (<View style={{flex: 1, backgroundColor: '#f00'}}>
      <AMapView initialRegion={{latitude: 30.315888, longitude: 120.165817}} showsUserLocation>
        <AMapView.Marker pinColor="green" draggable title='xxx' description="这是一个好地方" coordinate={{latitude: 30.315888, longitude: 120.165817}} />
      </AMapView>
    </View>);
  }
```

ATTENTION: Make sure that the ancestor containers of AMapView is flexed, otherwise you will see an empty view!

### User Location
Dont use the `showsUserLocation` property for it has some bugs. 
Instead, use `geolocation` in iOS and [react-native-amap-location](https://github.com/xiaobuu/react-native-amap-location) in android.
Then render a new marker for the user location.
