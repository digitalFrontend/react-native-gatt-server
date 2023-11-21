import { NativeModules } from 'react-native'

const { RNGattServer } = NativeModules


const GattServer = {
    setIsAdvertising: state => RNGattServer.setIsAdvertising(state)
}

export default GattServer
