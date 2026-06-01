package com.devicespooflab.hooks.hooks;

import com.devicespooflab.hooks.utils.ConfigManager;
import com.devicespooflab.hooks.utils.HookCallLogger;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Hooks Build.getSerial() method ONLY.
 *
 * IMPORTANT: We do NOT modify Build.* static fields here!
 *
 * Why?
 * - Critical Build fields (FINGERPRINT, MODEL, BRAND, etc.) are spoofed at boot via Magisk
 * - This prevents Cronet and other security libraries from detecting tampering
 * - Apps that read Build fields directly will get boot-spoofed values
 * - Apps that use SystemProperties reflection are caught by SystemPropertiesHooks
 *
 * This hook only handles:
 * - Build.getSerial() method (cannot be spoofed at boot, must hook at runtime)
 */
public class BuildHooks {

    private static final String TAG = "DeviceSpoofLab-Build";

    public static void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> buildClass = XposedHelpers.findClassIfExists("android.os.Build", lpparam.classLoader);
            if (buildClass == null) {
                XposedBridge.log(TAG + ": Build class not found");
                return;
            }

            hookGetSerial(buildClass);
            XposedBridge.log(TAG + ": Successfully hooked Build.getSerial()");

        } catch (Exception e) {
            XposedBridge.log(TAG + ": Failed to hook Build methods: " + e.getMessage());
        }
    }

    private static void hookGetSerial(Class<?> buildClass) {
        try {
            XposedHelpers.findAndHookMethod(buildClass, "getSerial",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        HookCallLogger.log("Build", "getSerial");
                        param.setResult(ConfigManager.getSerial());
                    }
                });
        } catch (NoSuchMethodError e) {
            // Method doesn't exist on Android < 8
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Failed to hook getSerial(): " + e.getMessage());
        }
    }
}
