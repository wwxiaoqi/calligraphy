package com.wwxiaoqi.calligraphy;

import android.annotation.TargetApi;
import android.content.Context;
import android.icu.util.Calendar;
import android.os.Build;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookMain implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam param) throws Throwable {
        String appName = "com.ltzk.mbsf";
        String packageName = "com.stub";
        String className = "StubApp";
        String methodName = "a";

        if (param.packageName.equals(appName)) {

            // ================ 测试用 ================
            XposedBridge.log("加载 Apgitp:" + param.packageName);
            Class<?> clazz = param.classLoader.loadClass(packageName + "." + className);
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getReturnType().toString().contains("Context")) {
                    XposedBridge.log("Debug:" + method.getName());
                }
            }
            // ================ 测试用 ================

            XposedBridge.log("开始 Hook...");
            XposedHelpers.findAndHookMethod(clazz, methodName, Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    // 获取到 360 的 Context 对象，通过这个对象来获取 classloader
                    Context context = (Context) param.args[0];
                    // 获取 360 的 classloader，之后 hook 加固后的代码就使用这个 classloader
                    ClassLoader classLoader = context.getClassLoader();
                    // 替换 classloader hook 加固后的真正代码
                    XposedHelpers.findAndHookMethod(appName + ".bean.UserBean",
                            classLoader, "set_expire", long.class, new XC_MethodHook() {
                                @TargetApi(Build.VERSION_CODES.N)
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) {
                                    long time = Calendar.getInstance().getTimeInMillis();
                                    param.args[0] = time;
                                    XposedBridge.log("Hook set_expire Good...");
                                }
                            });

                    XposedHelpers.findAndHookMethod(appName + ".bean.UserBean",
                            classLoader, "get_expire", new XC_MethodHook() {
                                @TargetApi(Build.VERSION_CODES.N)
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                    super.beforeHookedMethod(param);
                                    long time = Calendar.getInstance().getTimeInMillis();
                                    param.setResult(time);
                                    XposedBridge.log("Hook get_expire Good...");
                                }
                            });
                }
            });
            XposedBridge.log("Hook 结束.");
        }
    }
}