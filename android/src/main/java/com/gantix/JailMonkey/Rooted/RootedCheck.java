package com.gantix.JailMonkey.Rooted;

import android.content.Context;

import com.scottyab.rootbeer.RootBeer;
import android.os.Build;

public class RootedCheck {

    private static final String ONEPLUS = "oneplus";
    private static final String MOTO = "moto";
    private static final String XIAOMI = "xiaomi";
    private static final String OPPO = "oppo";

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
}
