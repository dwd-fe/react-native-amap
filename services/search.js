import {
    NativeAppEventEmitter,
    DeviceEventEmitter,
    Platform,
    NativeModules,
} from 'react-native'

const {AMapSearchManager} = NativeModules;

let lastId = "", promiseQueue = {}
const requestIdGenerator = (type = "AMapSearch")=> {
    let requestId = `${type}-${+new Date}-${Math.ceil(Math.random()*100000)}`
    if (requestId == lastId) {
        return requestIdGenerator(type)
    }
    lastId = requestId;
    return lastId;
}

const searchRequstFactory = (type) => (...args) => {
    if (AMapSearchManager[type]) {
        let resolve, reject, promise =  new Promise((res, rej)=> {
                resolve = res, reject = rej
            }),
            requestId = requestIdGenerator(type)

        args = [...args, requestId]

        AMapSearchManager[type].apply(null, args)
        promiseQueue[requestId] = {resolve, reject}
        return promise
    }
}

const emitter = Platform.OS == 'ios' ? NativeAppEventEmitter : DeviceEventEmitter
emitter.addListener(
    'ReceiveAMapSearchResult',
    (reminder) => {
        let {data, error, requestId} = reminder
        if(requestId && promiseQueue[requestId]) {
            let promise = promiseQueue[requestId];
            if (data.error) {
                promise.reject(error)
            } else {
                promise.resolve(data)
            }
        }
    }
)

export const AMapInputTipsSearch = searchRequstFactory('inputTipsSearch')
export const AMapWeatherSearch = searchRequstFactory('weatherSearch')
