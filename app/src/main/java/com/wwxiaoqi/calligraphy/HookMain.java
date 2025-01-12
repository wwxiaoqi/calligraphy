package com.wwxiaoqi.calligraphy;

import android.annotation.TargetApi;
import android.icu.util.Calendar;
import android.os.Build;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookMain implements IXposedHookLoadPackage {

    private static final String APP_NAME = "com.ltzk.mbsf";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(APP_NAME)) return;
        hook(lpparam);
    }

    private void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log("开始 Hook...");

        XposedHelpers.findAndHookMethod(APP_NAME + ".bean.UserBean", lpparam.classLoader,
                "set_expire", long.class, new XC_MethodHook() {
            @TargetApi(Build.VERSION_CODES.N)
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                long timeInMillis = Calendar.getInstance().getTimeInMillis();
                long j2 = 6307200000000L + timeInMillis;
                param.args[0] = j2;
                XposedBridge.log("Hook set_expire Good...");
            }
        });

        XposedHelpers.findAndHookMethod(APP_NAME + ".bean.UserBean", lpparam.classLoader,
                "get_expire", new XC_MethodHook() {
            @TargetApi(Build.VERSION_CODES.N)
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                long timeInMillis = Calendar.getInstance().getTimeInMillis();
                long j2 = 6307200000000L + timeInMillis;
                param.setResult(j2);
                XposedBridge.log("Hook get_expire Good...");
            }
        });

        XposedBridge.log("Hook 结束.");

    }
}