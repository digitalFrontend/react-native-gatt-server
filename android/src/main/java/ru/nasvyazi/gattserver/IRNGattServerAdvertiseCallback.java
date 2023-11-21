package ru.nasvyazi.gattserver;

import android.bluetooth.le.AdvertiseSettings;

public interface IRNGattServerAdvertiseCallback {
    public void onStartSuccess(AdvertiseSettings settingsInEffect);
    public void onStartFailure(Integer errorCode);
}
