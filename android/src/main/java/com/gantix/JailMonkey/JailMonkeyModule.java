package com.gantix.JailMonkey;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.gantix.JailMonkey.Debug.DebugCheck;

import java.io.BufferedReader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.gantix.JailMonkey.Debug.DebugCheck.isDebuggerAttached;
import static com.gantix.JailMonkey.ExternalStorage.ExternalStorageCheck.isOnExternalStorage;
import static com.gantix.JailMonkey.MockLocation.MockLocationCheck.isMockLocationOn;
import static com.gantix.JailMonkey.Rooted.RootedCheck.isJailBroken;

public class JailMonkeyModule extends ReactContextBaseJavaModule {

  public JailMonkeyModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  public String getName() {
      return "JailMonkey";
  }

  @Override
  public Map<String, Object> getConstants() {
    ReactContext context = getReactApplicationContext();
    final Map<String, Object> constants = new HashMap<>();
    constants.put("isJailBroken", isJailBroken(context));
    constants.put("canMockLocation", isMockLocationOn(context));
    constants.put("isOnExternalStorage", isOnExternalStorage(context));
    constants.put("isDebuggerAttached", isDebuggerAttached(context));
    return constants;
  }

  @ReactMethod
  public void scheduleDebugCheck() {
    final Context ctx = getReactApplicationContext();
    final Activity activity = getCurrentActivity();

    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    scheduler.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        if (DebugCheck.isDebuggerAttached(ctx)) {
          activity.finishAffinity();
          android.os.Process.killProcess(android.os.Process.myPid());
          System.exit(0);
        }
      }
    }, 0, 15, TimeUnit.SECONDS);
  }
  
}
