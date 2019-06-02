package com.gantix.JailMonkey;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;

import java.util.HashMap;
import java.util.Map;
import java.io.*;

import static com.gantix.JailMonkey.ExternalStorage.ExternalStorageCheck.isOnExternalStorage;
import static com.gantix.JailMonkey.MockLocation.MockLocationCheck.isMockLocationOn;
import static com.gantix.JailMonkey.Rooted.RootedCheck.isJailBroken;
import static com.gantix.JailMonkey.Rooted.RootedCheck.getRootedCheckInfoBreakdown;

public class JailMonkeyModule extends ReactContextBaseJavaModule {

  public JailMonkeyModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  public String getName() {
    return "JailMonkey";
  }

  public boolean isMagiskHidden(){
    try {
      File file = new File("/sbin");

      return((file.length()==0) && (file.lastModified()!=0));
    }
    catch(Exception e){
      return false;
    }
  }

  @Override
  public Map<String, Object> getConstants() {
    ReactContext context = getReactApplicationContext();
    final Map<String, Object> constants = new HashMap<>();
    boolean jailBreak = isJailBroken(context);
    boolean isMagiskHidden = isMagiskHidden();
    
    HashMap rootedCheckInfoBreakdown = getRootedCheckInfoBreakdown(context);
    rootedCheckInfoBreakdown.put("isMagiskHidden", isMagiskHidden());
    rootedCheckInfoBreakdown.put("rootBeerRootDetectionBreakdown", getRootBeerRootDetectionBreakdown(context));
    constants.put("getRootedCheckInfoBreakdown", rootedCheckInfoBreakdown);

    constants.put("isJailBroken", jailBreak || isMagiskHidden);
    constants.put("canMockLocation", isMockLocationOn(context));
    constants.put("isOnExternalStorage", isOnExternalStorage(context));
    return constants;
  }
}
