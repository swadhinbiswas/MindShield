package com.focusguard;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;

import java.util.List;

public class BlockingModule extends ReactContextBaseJavaModule {

    private static ReactApplicationContext reactContext;

    public BlockingModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
    }

    @Override
    public String getName() {
        return "BlockingModule";
    }

    @ReactMethod
    public void isAccessibilityServiceEnabled(Promise promise) {
        boolean enabled = false;
        try {
            AccessibilityManager am = (AccessibilityManager) reactContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
            List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
            
            for (AccessibilityServiceInfo service : enabledServices) {
                if (service.getId().contains(reactContext.getPackageName())) {
                    enabled = true;
                    break;
                }
            }
            promise.resolve(enabled);
        } catch (Exception e) {
            promise.reject("ERROR", e.getMessage());
        }
    }

    @ReactMethod
    public void openAccessibilitySettings() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        reactContext.startActivity(intent);
    }

    @ReactMethod
    public void setProtectionActive(boolean active, Promise promise) {
        try {
            SharedPreferences prefs = reactContext.getSharedPreferences("FocusGuardPrefs", Context.MODE_PRIVATE);
            prefs.edit().putBoolean("protection_active", active).apply();
            
            // Notify service to update state
            BlockingAccessibilityService.updateState();
            
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject("ERROR", e.getMessage());
        }
    }
    
    @ReactMethod
    public void isProtectionActive(Promise promise) {
        try {
            SharedPreferences prefs = reactContext.getSharedPreferences("FocusGuardPrefs", Context.MODE_PRIVATE);
            boolean active = prefs.getBoolean("protection_active", false);
            promise.resolve(active);
        } catch (Exception e) {
            promise.reject("ERROR", e.getMessage());
        }
    }

    @ReactMethod
    public void setBlockedPlatforms(boolean youtube, boolean instagram, boolean facebook, boolean tiktok, boolean snapchat, Promise promise) {
        try {
            SharedPreferences prefs = reactContext.getSharedPreferences("FocusGuardPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("block_youtube_shorts", youtube);
            editor.putBoolean("block_instagram_reels", instagram);
            editor.putBoolean("block_facebook_reels", facebook);
            editor.putBoolean("block_tiktok", tiktok);
            editor.putBoolean("block_snapchat_spotlight", snapchat);
            editor.apply();
            
            // Notify service to update state
            BlockingAccessibilityService.updateState();
            
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject("ERROR", e.getMessage());
        }
    }

    public static void emitBlockEvent(String platform, long timestamp) {
        if (reactContext != null) {
            WritableMap params = Arguments.createMap();
            params.putString("platform", platform);
            params.putDouble("timestamp", (double)timestamp);
            
            reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("onContentBlocked", params);
        }
    }
    
    @ReactMethod
    public void addListener(String eventName) {
        // Required for NativeEventEmitter
    }

    @ReactMethod
    public void removeListeners(Integer count) {
        // Required for NativeEventEmitter
    }
}
