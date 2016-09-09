# react-native-amap
React Native AMap component for iOS + Android

react-native-amap-view is a wrapper of AMap's Libraries inspired by react-native-maps and it's usable in Android and iOS

##Demo

![demo-gif](https://raw.githubusercontent.com/dianwoba/react-native-amap/master/doc/assets/demo.gif)

## Installation

`npm install react-native-amap-view --save`

### IOS上安装
Test only react-native 0.29
* `Add Files to "xxx"` on `Libaries` folder, and select `RCTAMap.xcodeproj`
* In `Link Binary With Libraries`, add `libRCTAMap.a`
* In `Link Binary With Libraries`, add `MAMapKit.framework` and `AMapSearchKit.framework`
* In `Framework Search Paths`, add `$(PROJECT_DIR)/../node_modules/react-native-amap-view/ios`
* `Add Files to "xxx"` on your project, and select `AMap.bundle`
* In `Link Binary With Libraries`, add other libs, see [here](http://lbs.amap.com/api/ios-sdk/guide/create-project/manual-configuration/#t3)
* Make sure `NSAllowsArbitraryLoads` in `Info.plist` is `true`
* Make sure `LSApplicationQueriesSchemes` has `iosamap`

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
