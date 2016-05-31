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

  componentDidMount() {
        // console.log(this.refs.map)
        setTimeout(()=>{
            this.refs.map.animateToZoomLevel(10)
        }, 3000)

    }
  render() {
    var {height, width} = Dimensions.get('window')
    console.log('render。。。');
    return (
      <View style={styles.container}>

        <View style={{flex:1}}>
                <Text>hello
                </Text>
        </View>
            <AMapView onPress={(e)=>console.log("Map::onPress", e.nativeEvent)} onLongPress={()=>console.log(arguments)} ref="map" initialRegion={{latitude:39.001, longitude:116.002}} apiKey="d7281b2a331af8dbb6b76a2e9c3629df"
                showsUserLocation={true} showsCompass={true} zoomEnabled={true}  showsScale={true} showsTraffic={true} >
                <AMapView.Marker coordinate={{latitude:39.001, longitude:116.002}} />
            </AMapView>
      </View>
    );
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
