package com.devicespooflab.hooks.hooks;

import android.content.Context;

import com.devicespooflab.hooks.utils.ConfigManager;
import com.devicespooflab.hooks.utils.HookCallLogger;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Hooks WebView User-Agent to match Pixel 7 Pro device profile.
 *
 * Non-aggressive implementation - only spoofs User-Agent string, no Canvas/WebGL.
 */
public class WebViewHooks {

    private static final String TAG = "DeviceSpoofLab-WebView";

    public static void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            hookWebSettings(lpparam);
            hookWebViewConstructor(lpparam);
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Failed to hook WebView: " + e.getMessage());
        }
    }

    private static void hookWebSettings(XC_LoadPackage.LoadPackageParam lpparam) {
        Class<?> webViewClass = XposedHelpers.findClassIfExists(
            "android.webkit.WebView", lpparam.classLoader);

        if (webViewClass == null) {
            return;
        }

        try {
            XposedHelpers.findAndHookMethod(webViewClass, "getSettings",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Object settings = param.getResult();
                        if (settings != null) {
                            String spoofedUA = ConfigManager.getWebViewUserAgent();
                            if (spoofedUA != null) {
                                try {
                                    HookCallLogger.log("WebView", "getSettings/setUserAgentString");
                                    XposedHelpers.callMethod(settings, "setUserAgentString", spoofedUA);
                                } catch (Exception e) {
                                    // Failed to set, that's okay
                                }
                            }
                        }
                    }
                });
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Failed to hook WebView.getSettings(): " + e.getMessage());
        }
    }

    private static void hookWebViewConstructor(XC_LoadPackage.LoadPackageParam lpparam) {
        Class<?> webViewClass = XposedHelpers.findClassIfExists(
            "android.webkit.WebView", lpparam.classLoader);

        if (webViewClass == null) {
            return;
        }

        try {
            XposedHelpers.findAndHookConstructor(webViewClass,
                Context.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            Object webView = param.thisObject;
                            Object settings = XposedHelpers.callMethod(webView, "getSettings");
                            String spoofedUA = ConfigManager.getWebViewUserAgent();
                            if (spoofedUA != null) {
                                HookCallLogger.log("WebView", "constructor(Context)");
                                XposedHelpers.callMethod(settings, "setUserAgentString", spoofedUA);
                            }
                        } catch (Exception e) {
                            // Failed to set UA in constructor, that's okay
                        }
                    }
                });
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Failed to hook WebView constructor: " + e.getMessage());
        }

        try {
            XposedHelpers.findAndHookConstructor(webViewClass,
                Context.class, android.util.AttributeSet.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            Object webView = param.thisObject;
                            Object settings = XposedHelpers.callMethod(webView, "getSettings");
                            String spoofedUA = ConfigManager.getWebViewUserAgent();
                            if (spoofedUA != null) {
                                HookCallLogger.log("WebView", "constructor(Context,AttributeSet)");
                                XposedHelpers.callMethod(settings, "setUserAgentString", spoofedUA);
                            }
                        } catch (Exception e) {
                            // Failed to set UA in constructor, that's okay
                        }
                    }
                });
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Failed to hook WebView(Context, AttributeSet) constructor: " + e.getMessage());
        }

        try {
            XposedHelpers.findAndHookConstructor(webViewClass,
                Context.class, android.util.AttributeSet.class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            Object webView = param.thisObject;
                            Object settings = XposedHelpers.callMethod(webView, "getSettings");
                            String spoofedUA = ConfigManager.getWebViewUserAgent();
                            if (spoofedUA != null) {
                                HookCallLogger.log("WebView", "constructor(Context,AttributeSet,int)");
                                XposedHelpers.callMethod(settings, "setUserAgentString", spoofedUA);
                            }
                        } catch (Exception e) {
                            // Failed to set UA in constructor, that's okay
                        }
                    }
                });
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Failed to hook WebView(Context, AttributeSet, int) constructor: " + e.getMessage());
        }
    }
}
