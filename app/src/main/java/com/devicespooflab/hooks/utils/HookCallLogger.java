package com.devicespooflab.hooks.utils;

import de.robv.android.xposed.XposedBridge;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Logs which device-spoof hooks the target app actually triggers.
 *
 * Output: /sdcard/DeviceSpoofLab-Hooks/hook_calls.log
 * Format: TIMESTAMP | pkg=PKG | hook=CATEGORY | method=METHOD [| detail=DETAIL]
 *
 * Each unique (hook, method, detail) triple is written at most once per app
 * session to keep the log readable. The companion shell utility hook_logger.sh
 * (from module-lib) can be used to read and summarise the log at runtime.
 */
public class HookCallLogger {

    private static final String TAG = "DeviceSpoofLab-HookLog";
    public static final String LOG_DIR  = "/sdcard/DeviceSpoofLab-Hooks";
    public static final String LOG_FILE = LOG_DIR + "/hook_calls.log";

    private static volatile String currentPackage = "unknown";
    private static final Set<String> SEEN =
            Collections.synchronizedSet(new HashSet<>());
    private static final SimpleDateFormat SDF =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    /**
     * Called once per loaded package from MainHook, before any hooks fire.
     * Resets the deduplication set so each app session starts fresh.
     */
    public static void setPackage(String pkg) {
        currentPackage = (pkg != null) ? pkg : "unknown";
        SEEN.clear();
    }

    /** Log a hook call without a detail string. */
    public static void log(String hook, String method) {
        log(hook, method, null);
    }

    /**
     * Log a hook call. Only the first occurrence of each unique
     * (hook, method, detail) triple is written per session.
     *
     * @param hook   hook category, e.g. "TelephonyManager"
     * @param method hooked method name, e.g. "getImei"
     * @param detail optional extra context (e.g. a property key)
     */
    public static void log(String hook, String method, String detail) {
        String key = hook + "|" + method + "|" + (detail != null ? detail : "");
        if (!SEEN.add(key)) return;

        String timestamp = SDF.format(new Date());
        String entry = timestamp
                + " | pkg=" + currentPackage
                + " | hook=" + hook
                + " | method=" + method
                + (detail != null ? " | detail=" + detail : "");

        XposedBridge.log(TAG + ": " + entry);
        writeToFile(entry);
    }

    private static void writeToFile(String entry) {
        try {
            File dir = new File(LOG_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) {
                pw.println(entry);
            }
        } catch (Exception e) {
            XposedBridge.log(TAG + ": write failed: " + e.getMessage());
        }
    }
}
