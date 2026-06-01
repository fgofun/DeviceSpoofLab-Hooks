package com.devicespooflab.hooks.hooks;

import com.devicespooflab.hooks.utils.ConfigManager;
import com.devicespooflab.hooks.utils.HookCallLogger;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Hooks TelephonyManager methods to spoof device identifiers.
 * Spoofs: IMEI, MEID, IMSI, ICCID, phone number
 */
public class TelephonyHooks {

    public static void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        Class<?> telephonyManager = XposedHelpers.findClassIfExists(
                "android.telephony.TelephonyManager",
                lpparam.classLoader
        );

        if (telephonyManager == null) {
            return;
        }

        try {
            XposedHelpers.findAndHookMethod(telephonyManager, "getDeviceId",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            HookCallLogger.log("TelephonyManager", "getDeviceId");
                            param.setResult(ConfigManager.getIMEI());
                        }
                    });
        } catch (NoSuchMethodError ignored) {
        }

        try {
            XposedHelpers.findAndHookMethod(telephonyManager, "getDeviceId", int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            HookCallLogger.log("TelephonyManager", "getDeviceId(slot)");
                            param.setResult(ConfigManager.getIMEI());
                        }
                    });
        } catch (NoSuchMethodError ignored) {
        }

        try {
            XposedHelpers.findAndHookMethod(telephonyManager, "getImei",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            HookCallLogger.log("TelephonyManager", "getImei");
                            param.setResult(ConfigManager.getIMEI());
                        }
                    });
        } catch (NoSuchMethodError ignored) {
        }

        try {
            XposedHelpers.findAndHookMethod(telephonyManager, "getImei", int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            HookCallLogger.log("TelephonyManager", "getImei(slot)");
                            param.setResult(ConfigManager.getIMEI());
                        }
                    });
        } catch (NoSuchMethodError ignored) {
        }

        try {
            XposedHelpers.findAndHookMethod(telephonyManager, "getMeid",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            HookCallLogger.log("TelephonyManager", "getMeid");
                            param.setResult(ConfigManager.getMEID());
                        }
                    });
        } catch (NoSuchMethodError ignored) {
        }

        try {
            XposedHelpers.findAndHookMethod(telephonyManager, "getMeid", int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            HookCallLogger.log("TelephonyManager", "getMeid(slot)");
                            param.setResult(ConfigManager.getMEID());
                        }
                    });
        } catch (NoSuchMethodError ignored) {
        }

        try {
            XposedHelpers.findAndHookMethod(telephonyManager, "getSubscriberId",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            HookCallLogger.log("TelephonyManager", "getSubscriberId");
                            param.setResult(ConfigManager.getIMSI());
                        }
                    });
        } catch (NoSuchMethodError ignored) {
        }

        try {
            XposedHelpers.findAndHookMethod(telephonyManager, "getSubscriberId", int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            HookCallLogger.log("TelephonyManager", "getSubscriberId(slot)");
                            param.setResult(ConfigManager.getIMSI());
                        }
                    });
        } catch (NoSuchMethodError ignored) {
        }

        try {
            XposedHelpers.findAndHookMethod(telephonyManager, "getSimSerialNumber",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            HookCallLogger.log("TelephonyManager", "getSimSerialNumber");
                            param.setResult(ConfigManager.getICCID());
                        }
                    });
        } catch (NoSuchMethodError ignored) {
        }

        try {
            XposedHelpers.findAndHookMethod(telephonyManager, "getSimSerialNumber", int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            HookCallLogger.log("TelephonyManager", "getSimSerialNumber(slot)");
                            param.setResult(ConfigManager.getICCID());
                        }
                    });
        } catch (NoSuchMethodError ignored) {
        }

        try {
            XposedHelpers.findAndHookMethod(telephonyManager, "getLine1Number",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            HookCallLogger.log("TelephonyManager", "getLine1Number");
                            param.setResult(ConfigManager.getPhoneNumber());
                        }
                    });
        } catch (NoSuchMethodError ignored) {
        }

        try {
            XposedHelpers.findAndHookMethod(telephonyManager, "getLine1Number", int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            HookCallLogger.log("TelephonyManager", "getLine1Number(slot)");
                            param.setResult(ConfigManager.getPhoneNumber());
                        }
                    });
        } catch (NoSuchMethodError ignored) {
        }

        try {
            XposedHelpers.findAndHookMethod(telephonyManager, "getNetworkOperator",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            String mccMnc = ConfigManager.getSystemProperty("gsm.operator.numeric", null);
                            if (mccMnc != null) {
                                HookCallLogger.log("TelephonyManager", "getNetworkOperator");
                                param.setResult(mccMnc);
                            }
                        }
                    });
        } catch (NoSuchMethodError ignored) {
        }

        try {
            XposedHelpers.findAndHookMethod(telephonyManager, "getNetworkOperatorName",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            String operatorName = ConfigManager.getSystemProperty("gsm.operator.alpha", null);
                            if (operatorName != null) {
                                HookCallLogger.log("TelephonyManager", "getNetworkOperatorName");
                                param.setResult(operatorName);
                            }
                        }
                    });
        } catch (NoSuchMethodError ignored) {
        }

        try {
            XposedHelpers.findAndHookMethod(telephonyManager, "getSimOperator",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            String simMccMnc = ConfigManager.getSystemProperty("gsm.sim.operator.numeric", null);
                            if (simMccMnc != null) {
                                HookCallLogger.log("TelephonyManager", "getSimOperator");
                                param.setResult(simMccMnc);
                            }
                        }
                    });
        } catch (NoSuchMethodError ignored) {
        }

        try {
            XposedHelpers.findAndHookMethod(telephonyManager, "getSimOperatorName",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            String simOperatorName = ConfigManager.getSystemProperty("gsm.sim.operator.alpha", null);
                            if (simOperatorName != null) {
                                HookCallLogger.log("TelephonyManager", "getSimOperatorName");
                                param.setResult(simOperatorName);
                            }
                        }
                    });
        } catch (NoSuchMethodError ignored) {
        }

        try {
            XposedHelpers.findAndHookMethod(telephonyManager, "getSimCountryIso",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            String simCountry = ConfigManager.getSystemProperty("gsm.sim.operator.iso-country", null);
                            if (simCountry != null) {
                                HookCallLogger.log("TelephonyManager", "getSimCountryIso");
                                param.setResult(simCountry);
                            }
                        }
                    });
        } catch (NoSuchMethodError ignored) {
        }

        try {
            XposedHelpers.findAndHookMethod(telephonyManager, "getNetworkCountryIso",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            String networkCountry = ConfigManager.getSystemProperty("gsm.operator.iso-country", null);
                            if (networkCountry != null) {
                                HookCallLogger.log("TelephonyManager", "getNetworkCountryIso");
                                param.setResult(networkCountry);
                            }
                        }
                    });
        } catch (NoSuchMethodError ignored) {
        }
    }
}
