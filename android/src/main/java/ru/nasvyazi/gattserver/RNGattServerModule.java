package ru.nasvyazi.gattserver;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


@ReactModule(name = "RNGattServer")
public class RNGattServerModule extends ReactContextBaseJavaModule {
    private final ReactApplicationContext reactContext;

    @SuppressLint("RestrictedApi")
    public RNGattServerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        RNGattServer.setContext(this.reactContext);
    }

    @Override
    public String getName() {
        return "RNGattServer";
    }

    @ReactMethod
    public void setIsAdvertising(boolean state, Promise promise){
        RNGattServer.setIsAdvertising(this.reactContext,state);
        promise.resolve("again shit?");
    }
   
}






















