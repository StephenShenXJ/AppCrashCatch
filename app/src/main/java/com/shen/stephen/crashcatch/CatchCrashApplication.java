package com.shen.stephen.crashcatch;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.shen.stephen.crashcatch.Util.AppUncaughtExceptionHandler;
import com.shen.stephen.crashcatch.Util.SdcardConfig;

/**
 * Created by ShenXz on 8/1/2019.
 */

public class CatchCrashApplication extends Application{

    private static CatchCrashApplication mInstance = null;

    public static CatchCrashApplication getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException("Application is not created.");
        }
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        SdcardConfig.getInstance().initSdcard();
        AppUncaughtExceptionHandler crashHandler = AppUncaughtExceptionHandler.getInstance();
        crashHandler.init(getApplicationContext());
    }

    /**
     * get local App install info
     *
     * @return
     */
    public PackageInfo getLocalPackageInfo() {
        return getPackageInfo(getPackageName());
    }

    /**
     * get App install info
     *
     * @return
     */
    public PackageInfo getPackageInfo(String packageName) {
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info;
    }
}
