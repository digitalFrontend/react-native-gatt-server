package ru.nasvyazi.gattserver;

import static android.bluetooth.le.AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED;
import static android.bluetooth.le.AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE;
import static android.bluetooth.le.AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED;
import static android.bluetooth.le.AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR;
import static android.bluetooth.le.AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import kotlin.text.Charsets;


public class RNGattServer {

    private static Integer ENABLE_BLUETOOTH_REQUEST_CODE = 1;
    private static Integer LOCATION_PERMISSION_REQUEST_CODE = 2;
    private static String SERVICE_UUID = "25AE1441-05D3-4C5B-8281-93D4E07420CF";
    private static String CHAR_FOR_READ_UUID = "25AE1442-05D3-4C5B-8281-93D4E07420CF";
    private static String CHAR_FOR_WRITE_UUID = "25AE1443-05D3-4C5B-8281-93D4E07420CF";
    private static String CHAR_FOR_INDICATE_UUID = "25AE1444-05D3-4C5B-8281-93D4E07420CF";
    private static String CCC_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";

    private static boolean isAdvertising = false;

    public static void setIsAdvertising(Context context, boolean value) {
        isAdvertising = value;

        if (value) {
            RNGattServer.prepareAndStartAdvertising(context);
        } else {
            RNGattServer.bleStopAdvertising(context);
        }
    }

    public static void stop(Context context) {
        RNGattServer.bleStopAdvertising(context);
    }

    public static void onTapSend() {
        //bleIndicate()
    }

    private static void prepareAndStartAdvertising(Context context) {

        RNGattServer.ensureBluetoothCanBeUsed(new IRNGattServerEnsureCallback() {
            @Override
            public void callback(boolean isSuccess, String text) {
                if (isSuccess) {
                    bleStartAdvertising(context);
                } else {
                    setIsAdvertising(context, false);
                }
            }
        });




    }

    @SuppressLint("MissingPermission")
    private static void bleStartAdvertising(Context context) {
        RNGattServer.setIsAdvertising(context, true);
        RNGattServer.bleStartGattServer(context);
        RNGattServer.getBleAdvertiser(context).startAdvertising(RNGattServer.getAdvertiseSettings(), RNGattServer.getAdvertiseData(), new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                RNGattServer.advertiseCallback.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                RNGattServer.advertiseCallback.onStartFailure(errorCode);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private static void bleStopGattServer() {
        RNGattServer.gattServer.close();
        RNGattServer.gattServer = null;
        Log.d("TEST", "gattServer closed");
    }

    @SuppressLint("MissingPermission")
    private static void bleStopAdvertising(Context context) {
        RNGattServer.setIsAdvertising(context, false);
        RNGattServer.bleStopGattServer();
        RNGattServer.getBleAdvertiser(context).stopAdvertising(new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                RNGattServer.advertiseCallback.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                RNGattServer.advertiseCallback.onStartFailure(errorCode);
            }
        });
    }

    private static void bleStartGattServer(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d("TEST", "PERMISSION FAILED");
            return;
        }
        BluetoothGattServer gattServer = getBluetoothManager(context).openGattServer(context, RNGattServer.gattServerCallback);
        BluetoothGattService service = new BluetoothGattService(UUID.fromString(SERVICE_UUID), BluetoothGattService.SERVICE_TYPE_PRIMARY);
        BluetoothGattCharacteristic charForRead = new BluetoothGattCharacteristic(UUID.fromString(CHAR_FOR_READ_UUID),
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);

        BluetoothGattCharacteristic charForWrite = new BluetoothGattCharacteristic(UUID.fromString(CHAR_FOR_WRITE_UUID),
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);

        BluetoothGattCharacteristic charForIndicate = new BluetoothGattCharacteristic(UUID.fromString(CHAR_FOR_INDICATE_UUID),
                BluetoothGattCharacteristic.PROPERTY_INDICATE,
                BluetoothGattCharacteristic.PERMISSION_READ);

        BluetoothGattDescriptor charConfigDescriptor = new BluetoothGattDescriptor(UUID.fromString(CCC_DESCRIPTOR_UUID),
                BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE);

        charForIndicate.addDescriptor(charConfigDescriptor);

        service.addCharacteristic(charForRead);
        service.addCharacteristic(charForWrite);
        service.addCharacteristic(charForIndicate);

        boolean result = gattServer.addService(service);

        RNGattServer.gattServer = gattServer;

        Log.d("TEST", "addService " + (result ? "SUCCESS":"FAIL"));
    }

    private static BluetoothGattServerCallback gattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("TEST","Central did connect");
            } else {
                Log.d("TEST","Central did disconnect");
                RNGattServer.subscribedDevices.remove(device);
            }
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            Log.d("TEST", "onNotificationSent status="+status);
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            String log = "onCharacteristicRead offset="+offset;
            if (characteristic.getUuid().equals(UUID.fromString(CHAR_FOR_READ_UUID))) {
                String strValue = "4F0001001310BC01";
                RNGattServer.gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, strValue.getBytes(Charsets.UTF_8));
                Log.d("TEST", log+"\nresponse=success, value=\"$strValue\"");
            } else {
                RNGattServer.gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null);
                Log.d("TEST", log+"\"\\nresponse=failure, unknown UUID\\n${characteristic.uuid}\"");
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            String log = "onCharacteristicWrite offset="+String.valueOf(offset)+" responseNeeded="+String.valueOf(responseNeeded)+" preparedWrite="+String.valueOf(preparedWrite);
            if (characteristic.getUuid().equals(UUID.fromString(CHAR_FOR_WRITE_UUID))) {
                String strValue = value == null || value.length == 0 ?  "" : new String(value, Charsets.UTF_8);
                if (responseNeeded) {
                    RNGattServer.gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, strValue.getBytes(Charsets.UTF_8));
                    Log.d("TEST", log+"\nresponse=success, value=\"$strValue\"");
                } else {
                    Log.d("TEST", log+"\nresponse=notNeeded, value=\"$strValue\"");
                }
            } else {
                if (responseNeeded) {
                    RNGattServer.gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null);
                    Log.d("TEST", log+"\nresponse=failure, unknown UUID\n${characteristic.uuid}");
                } else {
                    Log.d("TEST", log+"\nresponse=notNeeded, unknown UUID\n${characteristic.uuid}");
                }
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            String strLog = "onDescriptorWriteRequest";
            if (descriptor.getUuid().equals(UUID.fromString(CCC_DESCRIPTOR_UUID))) {
                int status = BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED;
                if (descriptor.getCharacteristic().getUuid().equals(UUID.fromString(CHAR_FOR_INDICATE_UUID))) {
                    if (Arrays.equals(value, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)) {
                        subscribedDevices.add(device);
                        status = BluetoothGatt.GATT_SUCCESS;
                        strLog += ", subscribed";
                    } else if (Arrays.equals(value, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)) {
                        subscribedDevices.remove(device);
                        status = BluetoothGatt.GATT_SUCCESS;
                        strLog += ", unsubscribed";
                    }
                }
                if (responseNeeded) {
                    RNGattServer.gattServer.sendResponse(device, requestId, status, 0, null);
                }
            } else {
                strLog += " unknown uuid=${descriptor.uuid}";
                if (responseNeeded) {
                    RNGattServer.gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null);
                }
            }
            Log.d("TEST", strLog);
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            String log = "onDescriptorReadRequest";
            if (descriptor.getUuid().equals(UUID.fromString(CCC_DESCRIPTOR_UUID))) {
                byte[] returnValue = null;
                if (subscribedDevices.contains(device)) {
                    log += " CCCD response=ENABLE_NOTIFICATION";
                    returnValue = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                } else {
                    log += " CCCD response=DISABLE_NOTIFICATION";
                    returnValue = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                }
                RNGattServer.gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, returnValue);
            } else {
                log += " unknown uuid=${descriptor.uuid}";
                RNGattServer.gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null);
            }
            Log.d("TEST", log);
        }
    };

    private static BluetoothManager bluetoothManager = null;
    private static BluetoothAdapter bluetoothAdapter = null;
    private static BluetoothLeAdvertiser bleAdvertiser = null;



    public static BluetoothManager getBluetoothManager(Context context){
        if (bluetoothManager == null){
            bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        }
        return bluetoothManager;
    }

    public static BluetoothAdapter getBluetoothAdapter(Context context){
        if (bluetoothAdapter == null){
            if (bluetoothManager == null){
                bluetoothAdapter = RNGattServer.getBluetoothManager(context).getAdapter();
            } else {
                bluetoothAdapter = bluetoothManager.getAdapter();
            }
        }

        return bluetoothAdapter;
    }

    public static BluetoothLeAdvertiser getBleAdvertiser(Context context){

        if (bleAdvertiser == null){
            if (bluetoothAdapter == null){
                if (bluetoothManager == null){
                    bleAdvertiser = RNGattServer.getBluetoothManager(context).getAdapter().getBluetoothLeAdvertiser();
                } else {
                    bleAdvertiser = RNGattServer.getBluetoothAdapter(context).getBluetoothLeAdvertiser();
                }
            } else {
                bleAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
            }
        }

        return bleAdvertiser;
    }

    public static BluetoothGattServer gattServer = null;
    public static Set<BluetoothDevice> subscribedDevices = new HashSet<>();

    enum AskType {
        AskOnce,
        InsistUntilSuccess
    }

    private static void ensureBluetoothCanBeUsed(IRNGattServerEnsureCallback completion) {

        RNGattServer.enableBluetooth(AskType.AskOnce, new IRNGattServerEnableCallback() {
            @Override
            public void callback(boolean isEnabled) {
                if (!isEnabled) {
                    completion.callback(false, "Bluetooth disabled");
//                    return @enableBluetooth
                }

                grantLocationPermission(AskType.AskOnce, new IRNGattServerEnableCallback() {
                    @Override
                    public void callback(boolean isGranted) {
                        if (!isGranted) {
                            completion.callback(false, "Location permission denied");
//                        return@grantLocationPermission
                        }

                        completion.callback(true, "BLE ready for use");
                    }
                });


            }
        });
    }




    private static void grantLocationPermission(AskType askType, IRNGattServerEnableCallback completion) {
        completion.callback(true);
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || isLocationPermissionGranted) {
//            completion(true)
//        } else {
//            runOnUiThread {
//                val requestCode = LOCATION_PERMISSION_REQUEST_CODE
//                val wantedPermission = Manifest.permission.ACCESS_FINE_LOCATION
//
//                // prepare motivation message
//                val builder = AlertDialog.Builder(this)
//                builder.setTitle("Location permission required")
//                builder.setMessage("BLE advertising requires location access, starting from Android 6.0")
//                builder.setPositiveButton(android.R.string.ok) { _, _ ->
//                        requestPermission(wantedPermission, requestCode)
//                }
//                builder.setCancelable(false)
//
//                // set permission result handler
//                permissionResultHandlers[requestCode] = { permissions, grantResults ->
//                        val isSuccess = grantResults.firstOrNull() != PackageManager.PERMISSION_DENIED
//                if (isSuccess || askType != AskType.InsistUntilSuccess) {
//                    permissionResultHandlers.remove(requestCode)
//                    completion(isSuccess)
//                } else {
//                    // show motivation message again
//                    builder.create().show()
//                }
//                }
//
//                // show motivation message
//                builder.create().show()
//            }
//        }
    }

    private static void enableBluetooth(AskType askType, IRNGattServerEnableCallback completion) {
        if (bluetoothAdapter.isEnabled()) {
            completion.callback(true);
        } else {
            String intentString = BluetoothAdapter.ACTION_REQUEST_ENABLE;
            Integer requestCode = ENABLE_BLUETOOTH_REQUEST_CODE;

            Log.d("TEST", "HAVENT ENOUGH PERMISSIONS");

//            // set activity result handler
//            activityResultHandlers[requestCode] = { result -> Unit
//                    val isSuccess = result == Activity.RESULT_OK
//            if (isSuccess || askType != AskType.InsistUntilSuccess) {
//                activityResultHandlers.remove(requestCode)
//                completion(isSuccess)
//            } else {
//                // start activity for the request again
//                startActivityForResult(Intent(intentString), requestCode)
//            }
//            }

//            // start activity for the request
//            startActivityForResult(Intent(intentString), requestCode)
        }
    }

    private static AdvertiseSettings advertiseSettings = null;
    public static AdvertiseSettings getAdvertiseSettings(){
        if (RNGattServer.advertiseSettings == null){
            RNGattServer.advertiseSettings = new AdvertiseSettings.Builder().setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED).setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM).setConnectable(true).build();
        }

        return RNGattServer.advertiseSettings;
    }

    private static AdvertiseData advertiseData = null;

    public static AdvertiseData getAdvertiseData(){
        if (RNGattServer.advertiseData == null){
            RNGattServer.advertiseData = new AdvertiseData.Builder()
                    .setIncludeDeviceName(false) // don't include name, because if name size > 8 bytes, ADVERTISE_FAILED_DATA_TOO_LARGE
                    .addServiceUuid(new ParcelUuid(UUID.fromString(SERVICE_UUID)))
                    .build();
        }

        return RNGattServer.advertiseData;
    }

    private static Context safeContext = null;
    public static void setContext(Context context){
        safeContext = context;
    }

    private static IRNGattServerAdvertiseCallback advertiseCallback = new IRNGattServerAdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.d("TEST", "Advertise start success\n$SERVICE_UUID");
        }

        @Override
        public void onStartFailure(Integer errorCode) {
            String desc = "";
            switch (errorCode) {
                case  (ADVERTISE_FAILED_DATA_TOO_LARGE):
                    desc = "\nADVERTISE_FAILED_DATA_TOO_LARGE";
                    break;
                case (ADVERTISE_FAILED_TOO_MANY_ADVERTISERS):
                    desc = "\nADVERTISE_FAILED_TOO_MANY_ADVERTISERS";
                    break;
                case (ADVERTISE_FAILED_ALREADY_STARTED):
                    desc = "\nADVERTISE_FAILED_ALREADY_STARTED";
                    break;
                case (ADVERTISE_FAILED_INTERNAL_ERROR):
                    desc = "\nADVERTISE_FAILED_INTERNAL_ERROR";
                    break;
                case (ADVERTISE_FAILED_FEATURE_UNSUPPORTED):
                    desc = "\nADVERTISE_FAILED_FEATURE_UNSUPPORTED";
                    break;
                default:
                    desc = "";
                    break;
            }
            Log.d("TEST", "Advertise start failed: errorCode="+String.valueOf(errorCode)+" "+desc);

            setIsAdvertising(safeContext,false);
        }
    };

    private static BluetoothGattCharacteristic charForIndicate = null;
    public static BluetoothGattCharacteristic getCharForIndicate(){
        if (charForIndicate == null){
            charForIndicate = RNGattServer.gattServer.getService(UUID.fromString(SERVICE_UUID)).getCharacteristic(UUID.fromString(CHAR_FOR_INDICATE_UUID));
        }

        return charForIndicate;
    }



}
