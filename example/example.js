/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, {
  AppRegistry,
  Component,
  StyleSheet,
  Text,
  View,
  MapView,
  Dimensions,
  Navigator,
    ScrollView,
} from 'react-native';
// import Mapbox from 'react-native-mapbox-gl'
import AMapView from 'react-native-amap-view'
console.log(AMapView);

class Example extends Component {
  constructor(props) {
    super(props)
    this.state = {
        title: '人力资源服务产业园'
    }
    this._hanleMarkeDragEnd = this._hanleMarkeDragEnd.bind(this)
  }
  componentDidMount() {
        setTimeout(()=>{
            this.refs.map.animateToZoomLevel(10)
            AMapView.Search.AMapInputTipsSearch("肯德基","杭州").then((a)=>{
                console.log("AMapInputTipsSearch",a);
            })
            AMapView.Search.AMapWeatherSearch("下城区", true).then((a)=>console.log("AMapWeatherSearch:Live",a))
            AMapView.Search.AMapWeatherSearch("下城区", false).then((a)=>console.log("AMapWeatherSearch:Forecast",a))
        }, 3000)
    }
  render() {
    var {height, width} = Dimensions.get('window')
    console.log('render。。。');
    return (
      <View style={styles.container}>
            <AMapView onPress={(e)=>console.log("Map::onPress", e.nativeEvent)} onLongPress={()=>console.log(arguments)} ref="map" initialRegion={{latitude:30.315888, longitude:120.165817}} apiKey="d7281b2a331af8dbb6b76a2e9c3629df"
                showsUserLocation={true} showsCompass={true} zoomEnabled={true}  showsScale={true} showsTraffic={true} >
                <AMapView.Marker draggable onDragEnd={this._hanleMarkeDragEnd} title={this.state.title} coordinate={{latitude:30.315888, longitude:120.165817}} />
            </AMapView>
      </View>
    );
  }
  _hanleMarkeDragEnd(e) {
    let point = e.nativeEvent.coordinate
    console.log("AMapMarkerDragend:", point);
    AMapView.Search.AMapRegeocodeSearch(point, 1500).then((a)=>{
        console.log("AMapRegeocodeSearch:",a);
        this.setState({title:a[0].formattedAddress});
        return AMapView.Search.AMapGeoCodeSearch(a[0].formattedAddress, a[0].city)
    }).then((a)=>{
        console.log("AMapGeoCodeSearch:",a);
    })
  }
}

class App extends Component {
    render() {
        return (
            <Navigator
    initialRoute={{name: 'My First Scene', index: 0}}
    renderScene={(route, navigator) =>
      <Example
        name={route.name}
        onForward={() => {
          var nextIndex = route.index + 1;
          navigator.push({
            name: 'Scene ' + nextIndex,
            index: nextIndex,
          });
        }}
        onBack={() => {
          if (route.index > 0) {
            navigator.pop();
          }
        }}
      />
    }
  />

        )

    }
}


const styles = StyleSheet.create({
  container: {
    flex: 1,
    flexDirection: 'column',
    // justifyContent: 'center',
    // alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});

AppRegistry.registerComponent('example', () => Example);
