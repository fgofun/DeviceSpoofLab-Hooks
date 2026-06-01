package com.devicespooflab.hooks.hooks;

import android.app.ActivityManager;
import android.os.Debug;

import com.devicespooflab.hooks.utils.HookCallLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.RandomAccessFile;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Hooks to spoof hardware specifications (CPU cores, RAM, CPU frequency, etc.)
 * to match real Pixel 7 Pro hardware.
 *
 * Real Pixel 7 Pro specs:
 * - CPU: Google Tensor G2 (8 cores: 2x2.85GHz + 2x2.35GHz + 4x1.80GHz)
 * - RAM: 12GB LPDDR5
 * - Architecture: ARM64-v8a
 */
public class HardwareHooks {

    private static final String TAG = "DeviceSpoofLab-Hardware";

    private static final int PIXEL_7_PRO_CORES = 8;
    private static final long PIXEL_7_PRO_RAM_BYTES = 12L * 1024 * 1024 * 1024;
    private static final long PIXEL_7_PRO_RAM_KB    = 12L * 1024 * 1024;

    public static void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            hookRuntimeCores();
            hookActivityManagerMemory(lpparam);
            hookDebugMemory();
            hookFileReads();
            XposedBridge.log(TAG + ": Successfully hooked hardware specs");
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Failed to hook hardware: " + e.getMessage());
        }
    }

    private static void hookRuntimeCores() {
        try {
            XposedHelpers.findAndHookMethod(Runtime.class, "availableProcessors",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        HookCallLogger.log("Hardware", "Runtime.availableProcessors");
                        param.setResult(PIXEL_7_PRO_CORES);
                    }
                });
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Failed to hook Runtime.availableProcessors(): " + e.getMessage());
        }
    }

    private static void hookActivityManagerMemory(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> activityManagerClass = XposedHelpers.findClassIfExists(
                "android.app.ActivityManager", lpparam.classLoader);

            if (activityManagerClass == null) {
                return;
            }

            XposedHelpers.findAndHookMethod(activityManagerClass, "getMemoryInfo",
                ActivityManager.MemoryInfo.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        ActivityManager.MemoryInfo memInfo = (ActivityManager.MemoryInfo) param.args[0];
                        if (memInfo != null) {
                            HookCallLogger.log("Hardware", "ActivityManager.getMemoryInfo");
                            long originalTotal = memInfo.totalMem;
                            memInfo.totalMem = PIXEL_7_PRO_RAM_BYTES;
                            if (originalTotal > 0) {
                                double usedRatio = 1.0 - ((double) memInfo.availMem / originalTotal);
                                memInfo.availMem = (long) (PIXEL_7_PRO_RAM_BYTES * (1.0 - usedRatio));
                            }
                        }
                    }
                });

            XposedHelpers.findAndHookMethod(activityManagerClass, "getMemoryClass",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        HookCallLogger.log("Hardware", "ActivityManager.getMemoryClass");
                        param.setResult(512);
                    }
                });

            XposedHelpers.findAndHookMethod(activityManagerClass, "getLargeMemoryClass",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        HookCallLogger.log("Hardware", "ActivityManager.getLargeMemoryClass");
                        param.setResult(1024);
                    }
                });

        } catch (Exception e) {
            XposedBridge.log(TAG + ": Failed to hook ActivityManager memory: " + e.getMessage());
        }
    }

    private static void hookDebugMemory() {
        try {
            XposedHelpers.findAndHookMethod(Debug.class, "getNativeHeapSize",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        long originalSize = (Long) param.getResult();
                        HookCallLogger.log("Hardware", "Debug.getNativeHeapSize");
                        param.setResult(originalSize * 4);
                    }
                });
        } catch (Exception e) {
            // Method might not exist on all Android versions
        }
    }

    private static void hookFileReads() {
        try {
            XposedHelpers.findAndHookConstructor(BufferedReader.class,
                java.io.Reader.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        // Intercept readLine() calls instead
                    }
                });
        } catch (Exception e) {
            // Ignore
        }

        try {
            XposedHelpers.findAndHookMethod(RandomAccessFile.class, "readLine",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        String line = (String) param.getResult();
                        if (line != null && line.startsWith("MemTotal:")) {
                            HookCallLogger.log("Hardware", "RandomAccessFile.readLine", "/proc/meminfo:MemTotal");
                            param.setResult("MemTotal:       " + PIXEL_7_PRO_RAM_KB + " kB");
                        }
                    }
                });
        } catch (Exception e) {
            // Ignore
        }

        try {
            XposedHelpers.findAndHookMethod(File.class, "exists",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        // Let /proc/cpuinfo and /proc/meminfo pass through normally
                    }
                });
        } catch (Exception e) {
            // Ignore
        }
    }
}
