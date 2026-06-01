package com.devicespooflab.hooks.hooks;

import com.devicespooflab.hooks.utils.ConfigManager;
import com.devicespooflab.hooks.utils.HookCallLogger;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Hooks Google Advertising ID (GAID) to return spoofed value.
 */
public class AdvertisingIdHooks {

    public static void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        Class<?> advertisingIdInfoClass = XposedHelpers.findClassIfExists(
                "com.google.android.gms.ads.identifier.AdvertisingIdClient$Info",
                lpparam.classLoader
        );

        if (advertisingIdInfoClass != null) {
            try {
                XposedHelpers.findAndHookMethod(advertisingIdInfoClass, "getId",
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                HookCallLogger.log("AdvertisingId", "AdvertisingIdClient.Info.getId");
                                param.setResult(ConfigManager.getGAID());
                            }
                        });
            } catch (NoSuchMethodError ignored) {
            }
        }

        Class<?> altAdvertisingIdInfoClass = XposedHelpers.findClassIfExists(
                "com.google.android.gms.common.api.internal.zzx",
                lpparam.classLoader
        );

        if (altAdvertisingIdInfoClass != null) {
            try {
                XposedHelpers.findAndHookMethod(altAdvertisingIdInfoClass, "getId",
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                HookCallLogger.log("AdvertisingId", "zzx.getId");
                                param.setResult(ConfigManager.getGAID());
                            }
                        });
            } catch (NoSuchMethodError ignored) {
            }
        }

        Class<?> advertisingIdClientClass = XposedHelpers.findClassIfExists(
                "com.google.android.gms.ads.identifier.AdvertisingIdClient",
                lpparam.classLoader
        );

        if (advertisingIdClientClass != null) {
            try {
                XposedHelpers.findAndHookMethod(advertisingIdClientClass,
                        "getAdvertisingIdInfo",
                        android.content.Context.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                if (param.getResult() != null) {
                                    HookCallLogger.log("AdvertisingId", "getAdvertisingIdInfo");
                                }
                            }
                        });
            } catch (NoSuchMethodError ignored) {
            }
        }
    }
}
