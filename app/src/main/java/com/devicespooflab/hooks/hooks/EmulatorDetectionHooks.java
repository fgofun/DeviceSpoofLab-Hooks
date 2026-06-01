package com.devicespooflab.hooks.hooks;

import com.devicespooflab.hooks.utils.HookCallLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Hooks to hide emulator-specific files and artifacts.
 *
 * This is a DEFENSIVE hook - it only hides emulator files that don't exist
 * on real devices. Safe to use on real rooted devices.
 *
 * Property-based detection (ro.kernel.qemu, ro.boot.qemu) is handled by
 * SystemPropertiesHooks.
 */
public class EmulatorDetectionHooks {

    private static final String TAG = "DeviceSpoofLab-Emulator";

    private static final String[] EMULATOR_FILES = {
        "/dev/qemu_pipe",
        "/dev/goldfish_pipe",
        "/sys/qemu_trace",
        "/system/lib/libc_malloc_debug_qemu.so",
        "/system/lib64/libc_malloc_debug_qemu.so",
        "/sys/devices/virtual/misc/goldfish_pipe",
        "/sys/devices/virtual/misc/goldfish_sync"
    };

    private static final String[] EMULATOR_KEYWORDS = {
        "goldfish",
        "ranchu",
        "vbox",
        "qemu"
    };

    public static void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            hookFileExists();
            hookFileListFiles();
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Failed to hook emulator detection: " + e.getMessage());
        }
    }

    private static void hookFileExists() {
        try {
            XposedHelpers.findAndHookMethod(File.class, "exists",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        File file = (File) param.thisObject;
                        String path = file.getAbsolutePath();

                        for (String emuFile : EMULATOR_FILES) {
                            if (path.equals(emuFile) || path.contains(emuFile)) {
                                HookCallLogger.log("EmulatorDetection", "File.exists", path);
                                param.setResult(false);
                                return;
                            }
                        }

                        String lowerPath = path.toLowerCase();
                        for (String keyword : EMULATOR_KEYWORDS) {
                            if (lowerPath.contains(keyword)) {
                                HookCallLogger.log("EmulatorDetection", "File.exists(keyword)", keyword);
                                param.setResult(false);
                                return;
                            }
                        }
                    }
                });
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Failed to hook File.exists(): " + e.getMessage());
        }
    }

    private static void hookFileListFiles() {
        try {
            XposedHelpers.findAndHookMethod(File.class, "listFiles",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        File[] files = (File[]) param.getResult();
                        if (files == null) return;
                        List<File> filtered = filterEmulatorFiles(Arrays.asList(files));
                        if (filtered.size() < files.length) {
                            HookCallLogger.log("EmulatorDetection", "File.listFiles");
                        }
                        param.setResult(filtered.toArray(new File[0]));
                    }
                });

            XposedHelpers.findAndHookMethod(File.class, "listFiles",
                java.io.FileFilter.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        File[] files = (File[]) param.getResult();
                        if (files == null) return;
                        List<File> filtered = filterEmulatorFiles(Arrays.asList(files));
                        if (filtered.size() < files.length) {
                            HookCallLogger.log("EmulatorDetection", "File.listFiles(FileFilter)");
                        }
                        param.setResult(filtered.toArray(new File[0]));
                    }
                });

            XposedHelpers.findAndHookMethod(File.class, "listFiles",
                java.io.FilenameFilter.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        File[] files = (File[]) param.getResult();
                        if (files == null) return;
                        List<File> filtered = filterEmulatorFiles(Arrays.asList(files));
                        if (filtered.size() < files.length) {
                            HookCallLogger.log("EmulatorDetection", "File.listFiles(FilenameFilter)");
                        }
                        param.setResult(filtered.toArray(new File[0]));
                    }
                });
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Failed to hook File.listFiles(): " + e.getMessage());
        }
    }

    private static List<File> filterEmulatorFiles(List<File> files) {
        List<File> filtered = new ArrayList<>();
        for (File file : files) {
            String name = file.getName().toLowerCase();
            String path = file.getAbsolutePath().toLowerCase();
            boolean isEmulatorFile = false;

            for (String keyword : EMULATOR_KEYWORDS) {
                if (name.contains(keyword) || path.contains(keyword)) {
                    isEmulatorFile = true;
                    break;
                }
            }

            if (!isEmulatorFile) {
                for (String emuFile : EMULATOR_FILES) {
                    if (path.equals(emuFile.toLowerCase()) || path.contains(emuFile.toLowerCase())) {
                        isEmulatorFile = true;
                        break;
                    }
                }
            }

            if (!isEmulatorFile) {
                filtered.add(file);
            }
        }
        return filtered;
    }
}
