package com.devicespooflab.hooks.hooks;

import com.devicespooflab.hooks.utils.HookCallLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Hooks PackageManager to spoof hardware features for Pixel 7 Pro.
 * Reports real device features, hides emulator-specific features.
 */
public class PackageManagerHooks {

    private static final String TAG = "DeviceSpoofLab-PackageManager";

    private static final Set<String> PIXEL_7_PRO_FEATURES = new HashSet<>(Arrays.asList(
        "android.hardware.camera",
        "android.hardware.camera.autofocus",
        "android.hardware.camera.flash",
        "android.hardware.camera.front",
        "android.hardware.camera.any",
        "android.hardware.camera.ar",
        "android.hardware.camera.capability.manual_post_processing",
        "android.hardware.camera.capability.manual_sensor",
        "android.hardware.camera.capability.raw",
        "android.hardware.sensor.accelerometer",
        "android.hardware.sensor.gyroscope",
        "android.hardware.sensor.compass",
        "android.hardware.sensor.barometer",
        "android.hardware.sensor.light",
        "android.hardware.sensor.proximity",
        "android.hardware.sensor.stepcounter",
        "android.hardware.sensor.stepdetector",
        "android.hardware.telephony",
        "android.hardware.telephony.gsm",
        "android.hardware.telephony.cdma",
        "android.hardware.telephony.ims",
        "android.hardware.wifi",
        "android.hardware.wifi.direct",
        "android.hardware.wifi.aware",
        "android.hardware.bluetooth",
        "android.hardware.bluetooth_le",
        "android.hardware.nfc",
        "android.hardware.nfc.hce",
        "android.hardware.nfc.hcef",
        "android.hardware.nfc.ese",
        "android.hardware.nfc.uicc",
        "android.hardware.fingerprint",
        "android.hardware.biometrics.face",
        "android.hardware.touchscreen",
        "android.hardware.touchscreen.multitouch",
        "android.hardware.touchscreen.multitouch.distinct",
        "android.hardware.touchscreen.multitouch.jazzhand",
        "android.hardware.screen.portrait",
        "android.hardware.screen.landscape",
        "android.hardware.location",
        "android.hardware.location.gps",
        "android.hardware.location.network",
        "android.hardware.audio.output",
        "android.hardware.audio.low_latency",
        "android.hardware.audio.pro",
        "android.hardware.microphone",
        "android.hardware.usb.host",
        "android.hardware.usb.accessory",
        "android.hardware.vulkan.level",
        "android.hardware.vulkan.version",
        "android.hardware.vulkan.compute",
        "android.hardware.opengles.aep",
        "android.software.device_admin",
        "android.software.managed_users",
        "android.software.webview",
        "android.software.backup",
        "android.software.app_widgets",
        "android.software.voice_recognizers",
        "android.software.home_screen",
        "android.software.input_methods",
        "android.software.connectionservice",
        "android.software.autofill",
        "android.software.verified_boot",
        "android.software.secure_lock_screen"
    ));

    private static final Set<String> DENIED_FEATURES = new HashSet<>(Arrays.asList(
        "android.hardware.sensor.emulator",
        "goldfish"
    ));

    public static void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> appPackageManagerClass = XposedHelpers.findClassIfExists(
                "android.app.ApplicationPackageManager", lpparam.classLoader);

            if (appPackageManagerClass != null) {
                hookHasSystemFeature(appPackageManagerClass);
                hookGetSystemAvailableFeatures(appPackageManagerClass);
            }
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Failed to hook PackageManager: " + e.getMessage());
        }
    }

    private static void hookHasSystemFeature(Class<?> pmClass) {
        try {
            XposedHelpers.findAndHookMethod(pmClass, "hasSystemFeature",
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        String feature = (String) param.args[0];
                        if (feature == null) return;

                        for (String denied : DENIED_FEATURES) {
                            if (feature.toLowerCase().contains(denied.toLowerCase())) {
                                HookCallLogger.log("PackageManager", "hasSystemFeature", "denied:" + feature);
                                param.setResult(false);
                                return;
                            }
                        }

                        if (PIXEL_7_PRO_FEATURES.contains(feature)) {
                            HookCallLogger.log("PackageManager", "hasSystemFeature", feature);
                            param.setResult(true);
                        }
                    }
                });
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Failed to hook hasSystemFeature(String): " + e.getMessage());
        }

        try {
            XposedHelpers.findAndHookMethod(pmClass, "hasSystemFeature",
                String.class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        String feature = (String) param.args[0];
                        if (feature == null) return;

                        for (String denied : DENIED_FEATURES) {
                            if (feature.toLowerCase().contains(denied.toLowerCase())) {
                                HookCallLogger.log("PackageManager", "hasSystemFeature(version)", "denied:" + feature);
                                param.setResult(false);
                                return;
                            }
                        }

                        if (PIXEL_7_PRO_FEATURES.contains(feature)) {
                            HookCallLogger.log("PackageManager", "hasSystemFeature(version)", feature);
                            param.setResult(true);
                        }
                    }
                });
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Failed to hook hasSystemFeature(String, int): " + e.getMessage());
        }
    }

    private static void hookGetSystemAvailableFeatures(Class<?> pmClass) {
        try {
            XposedHelpers.findAndHookMethod(pmClass, "getSystemAvailableFeatures",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Object[] features = (Object[]) param.getResult();
                        if (features == null) return;

                        Class<?> featureInfoClass = features.getClass().getComponentType();
                        List<Object> filtered = new ArrayList<>();
                        boolean anyRemoved = false;

                        for (Object feature : features) {
                            try {
                                String name = (String) XposedHelpers.getObjectField(feature, "name");
                                if (name != null) {
                                    boolean isDenied = false;
                                    for (String denied : DENIED_FEATURES) {
                                        if (name.toLowerCase().contains(denied.toLowerCase())) {
                                            isDenied = true;
                                            break;
                                        }
                                    }
                                    if (isDenied) {
                                        anyRemoved = true;
                                    } else {
                                        filtered.add(feature);
                                    }
                                } else {
                                    filtered.add(feature);
                                }
                            } catch (Exception e) {
                                filtered.add(feature);
                            }
                        }

                        if (anyRemoved) {
                            HookCallLogger.log("PackageManager", "getSystemAvailableFeatures");
                        }

                        Object typedArray = java.lang.reflect.Array.newInstance(
                            featureInfoClass, filtered.size());
                        for (int i = 0; i < filtered.size(); i++) {
                            java.lang.reflect.Array.set(typedArray, i, filtered.get(i));
                        }
                        param.setResult(typedArray);
                    }
                });
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Failed to hook getSystemAvailableFeatures(): " + e.getMessage());
        }
    }
}
