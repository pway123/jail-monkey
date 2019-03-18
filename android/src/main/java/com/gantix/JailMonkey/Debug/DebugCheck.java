package com.gantix.JailMonkey.Debug;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Debug;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class DebugCheck {

    private static String TRACER_PID = "TracerPid";

    public static boolean isDebuggerAttached(Context context) {
        return hasTracerPid() || isDebuggerConnected() || isDebuggableInManifest(context);
    }

    private static boolean hasTracerPid() {
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(new FileInputStream("/proc/self/status")), 1000)) {
            
            String line;

            while ((line = reader.readLine()) != null) {
                // look for line beginning with "TracerPid"
                if (line.length() > TRACER_PID.length()) {
                    if (line.substring(0, TRACER_PID.length()).equalsIgnoreCase(TRACER_PID)) {
                        Integer pid = Integer.decode(line.substring(TRACER_PID.length() + 1).trim());
                        if (pid > 0) {
                            return true;
                        }
                        break;
                    }
                }
            }

        } catch (Exception e) {
            // do nothing
        }

        return false;
    }

    private static boolean isDebuggerConnected() {
        return Debug.isDebuggerConnected();
    }

    private static boolean isDebuggableInManifest(Context context) {
        boolean debuggable = false;

        PackageManager pm = context.getPackageManager();
        try {
          ApplicationInfo appInfo = pm.getApplicationInfo(context.getPackageName(), 0);
          debuggable = (0 != (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE));
        } catch(PackageManager.NameNotFoundException e) {
          /*debuggable variable will remain false*/
        }

        return debuggable;
    }
}