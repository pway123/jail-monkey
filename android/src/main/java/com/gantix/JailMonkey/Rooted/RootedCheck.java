package com.gantix.JailMonkey.Rooted;

import android.content.Context;
import java.util.HashMap;

import com.scottyab.rootbeer.RootBeer;

import android.os.Build;

public class RootedCheck {

    private static final String ONEPLUS = "oneplus";
    private static final String MOTO = "moto";
    private static final String XIAOMI = "xiaomi";
    private static final String OPPO = "oppo";
    private static HashMap rootedCheckBreakdown = new HashMap();

    /**
     * Checks if the device is rooted.
     *
     * @return <code>true</code> if the device is rooted, <code>false</code> otherwise.
     */
    public static boolean isJailBroken(Context context) {
        CheckApiVersion check;

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            check = new GreaterThan23();
        } else {
            check = new LessThan23();
        }

        return check.checkRooted() || rootBeerCheck(context);
    }

    private static boolean rootBeerCheck(Context context) {
        RootBeer rootBeer = new RootBeer(context);
        Boolean rv;
        final String brand = Build.BRAND.toLowerCase();

        if(brand.contains(ONEPLUS) || brand.contains(MOTO) || brand.contains(XIAOMI) || brand.contains(OPPO)) {
            rv = rootBeer.isRootedWithoutBusyBoxCheck();
        } else {
            rv = rootBeer.isRooted();
        }
        return rv;
    }


    public static HashMap getRootedCheckInfoBreakdown(Context context){
        final String brand = Build.BRAND.toLowerCase();
        RootBeer rootBeer = new RootBeer(context);
        boolean isBrandsToSkipBusybox = (brand.contains(ONEPLUS) || brand.contains(MOTO) || brand.contains(XIAOMI) || brand.contains(OPPO));

        CheckApiVersion check;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            check = new GreaterThan23();
        } else {
            check = new LessThan23();
        }

        rootedCheckBreakdown.put("sdkVersion", android.os.Build.VERSION.SDK_INT);
        rootedCheckBreakdown.put("jailMonkeyIsSuperUserPresent", check.checkRooted());
        rootedCheckBreakdown.put("rootBeerIsRooted", rootBeerCheck(context));
        rootedCheckBreakdown.put("shouldSkipBusyBoxCheck", isBrandsToSkipBusybox);

        return rootedCheckBreakdown;
    }

    public static HashMap getRootBeerRootDetectionBreakdown(Context context){
        HashMap rootedBeerCheckBreakdown = new HashMap();
        RootBeer rootBeer = new RootBeer(context);
        
        rootedBeerCheckBreakdown.put("detectRootManagementApps", rootBeer.detectRootManagementApps());
        rootedBeerCheckBreakdown.put("detectPotentiallyDangerousApps", rootBeer.detectPotentiallyDangerousApps());
        rootedBeerCheckBreakdown.put("checkForBinarySu", rootBeer.checkForBinary("su"));
        rootedBeerCheckBreakdown.put("checkForBinaryBusybox", rootBeer.checkForBinary("busybox"));
        rootedBeerCheckBreakdown.put("checkForDangerousProps", rootBeer.checkForDangerousProps());
        rootedBeerCheckBreakdown.put("checkForRWPaths", rootBeer.checkForRWPaths());
        rootedBeerCheckBreakdown.put("detectTestKeys", rootBeer.detectTestKeys());
        rootedBeerCheckBreakdown.put("checkSuExists", rootBeer.checkSuExists());
        rootedBeerCheckBreakdown.put("checkForRootNative", rootBeer.checkForRootNative());
        rootedBeerCheckBreakdown.put("checkForMagiskBinary", rootBeer.checkForMagiskBinary());

        return rootedBeerCheckBreakdown;
    }
}
