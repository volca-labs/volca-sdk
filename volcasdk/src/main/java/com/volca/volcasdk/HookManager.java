package com.volca.volcasdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.util.Log;

import com.swift.sandhook.xposedcompat.XposedCompat;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

class HookManager {

    private static final String TAG = "HookManager";

    /* package */ static final String HOOKED_MAC_ADDRESS = "04:05:06:07:08:09";
    /* package */ static final String HOOKED_ID = "123456";

    private static final String STATE_NOT_WORKING = "STATE_NOT_WORKING";
    private static final String STATE_BLOCK = "STATE_BLOCK";
    private static final String STATE_FREQUENCY_CONTROL = "STATE_FREQUENCY_CONTROL";
    private volatile static String state = STATE_NOT_WORKING;

    private static final String SP_NAME = "VolcaSP";
    private static final String ORIGINAL_INVOKE_TIME_SUFFIX = "[timestamp]";

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    private static final long INTERVAL_ONE_MINUTE = 60 * 1000;
    private static final long INTERVAL_ONE_DAY = 86400 * 1000;

    public static final String DOMAIN_DEVICE_ID = "DeviceID";
    public static final String DOMAIN_LOCATION = "Location";
    public static final String DOMAIN_APP_LIST = "AppList";
    public static final String DOMAIN_OTHERS = "Others";

    private static final Object stateLock = new Object();
    private static final Object spLock = new Object();

    private final static Set<XC_MethodHook.Unhook> unhooks = new HashSet<>();

    private final static class HookConfig {
        public HookConfig(String domain, String methodName, Object returnObject) {
            this.domain = domain;
            this.methodName = methodName;
            this.returnObject = returnObject;
        }

        public String domain;
        public String methodName;
        public Object returnObject;
    }

    public static void init(Context ctx) {
        XposedCompat.cacheDir = ctx.getCacheDir();

        XposedCompat.context = ctx;
        XposedCompat.classLoader = ctx.getClassLoader();
        XposedCompat.isFirstApplication= true;

        context = ctx;
    }

    private static void hookMethods(final String className, final HookConfig[] hookConfigs) {
        try {
            Class<?> clazz = Class.forName(className);
            for (Method method : clazz.getDeclaredMethods()) {
                HookConfig config = null;
                for (HookConfig c : hookConfigs) {
                    if (method.getName().equals(c.methodName)) {
                        config = c;
                        break;
                    }
                }
                if (config == null) continue;

                final HookConfig hookConfig = config;
                if (!Modifier.isAbstract(method.getModifiers())) {
                    final String signature = method.toString();
                    XC_MethodHook.Unhook unhook = XposedBridge.hookMethod(method, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
//                            Log.d(TAG, "hookMethods beforeHookedMethod: " + param.method);
                            if (state.equals(STATE_BLOCK)) {
                                param.setResult(hookConfig.returnObject);

                            } else if (state.equals(STATE_FREQUENCY_CONTROL)) {
                                synchronized (spLock) {
                                    // using the cached result
                                    SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
                                    if (hookConfig.domain.equals(DOMAIN_DEVICE_ID)) {
                                        if (sp.contains(signature)) {
                                            if (new Date().getTime() - sp.getLong(signature + ORIGINAL_INVOKE_TIME_SUFFIX, 0)  <= INTERVAL_ONE_DAY) {
                                                // 缓存存在且未过期，则使用缓存
                                                param.setResult(sp.getString(signature, HOOKED_ID));
                                                Log.d(TAG, "using cached: " + signature + ", " + sp.getString(signature, HOOKED_ID));
                                            }
                                        }
                                    } else if (hookConfig.domain.equals(DOMAIN_LOCATION) || hookConfig.domain.equals(DOMAIN_APP_LIST)) {
                                        // 这两个domain的API不需要缓存
                                        if (sp.contains(signature + ORIGINAL_INVOKE_TIME_SUFFIX)) {
                                            if (new Date().getTime() - sp.getLong(signature + ORIGINAL_INVOKE_TIME_SUFFIX, 0)  <= INTERVAL_ONE_MINUTE) {
                                                // 未过期，使用假数据控制频率
                                                param.setResult(hookConfig.returnObject);
                                            }
                                        }
                                    } else if (hookConfig.domain.equals(DOMAIN_OTHERS)) {
                                        // 不做拦截
                                        Log.d(TAG, "domain :" + hookConfig.domain);
                                    } else {
                                        Log.e(TAG, "domain error:" + hookConfig.domain);
                                    }
                                }
                            } else if (state.equals(STATE_NOT_WORKING)) {
                                // nothing to do
                            }
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            // 调用了原方法才会走这里，在这里进行cache保存
                            synchronized (spLock) {
                                SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                if (state.equals(STATE_FREQUENCY_CONTROL)) {
                                    // cache result
                                    if (hookConfig.domain.equals(DOMAIN_DEVICE_ID) && !sp.contains(signature)) {
                                        Object result = param.getResult();
                                        if (result != null) {
                                            editor.putString(signature, (String) result);
                                            Log.d(TAG, "cache: " + signature + ", " + result);
                                        }
                                    }
                                }
                                editor.putLong(signature + ORIGINAL_INVOKE_TIME_SUFFIX, new Date().getTime());
                                editor.apply();
                            }
                        }
                    });

                    unhooks.add(unhook);
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "hookMethods ERROR: " + t.getMessage());
            t.printStackTrace();
        }
    }

    public static boolean isAvailable() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            Log.e(TAG, "hook not available!");
            return false;
        }
        return true;
    }

    public static boolean hookAll() {
        if (!isAvailable()) {
            return false;
        }

        synchronized (stateLock) {
            state = STATE_BLOCK;
        }

        // 首批hook API：ID信息，位置信息，应用列表
        hookMethods("android.net.wifi.WifiInfo", new HookConfig[] {
                new HookConfig(DOMAIN_DEVICE_ID, "getMacAddress", HOOKED_MAC_ADDRESS),
                new HookConfig(DOMAIN_LOCATION, "getBSSID", HOOKED_ID),
                new HookConfig(DOMAIN_LOCATION, "getSSID", HOOKED_ID)
        });
        WifiInfo wifiInfo = null;
        try {
            wifiInfo = (WifiInfo) XposedHelpers.findConstructorExact(WifiInfo.class).newInstance();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        hookMethods("android.net.wifi.WifiManager", new HookConfig[] {
                new HookConfig(DOMAIN_LOCATION, "getScanResults", new ArrayList<>()),
                new HookConfig(DOMAIN_LOCATION, "getConnectionInfo", wifiInfo),
                new HookConfig(DOMAIN_LOCATION, "startScan", false)
        });
//        hookMethods("android.net.NetworkCapabilities", new String[]{"getTransportInfo"}, new Object[]{null});
//        hookMethods("java.net.NetworkInterface", new String[]{"getHardwareAddress"}, new Object[]{HOOKED_MAC_ADDRESS.getBytes()});
//        hookMethods("android.bluetooth.BluetoothAdapter",
//                new String[]{"getAddress", "getBondedDevices", "startDiscovery"},
//                new Object[]{"", null, null});
//        hookMethods("android.telephony.TelephonyManager",
//                new String[]{"getSubscriberId", "getSimSerialNumber", "getDeviceId", "getImei", "getMeid", "getLine1Number", "getCardIdForDefaultEuicc", "getUiccCardsInfo", "getNai", "listen", "getAllCellInfo", "getCellLocation", "getNeighboringCellInfo", "requestCellInfoUpdate", "requestNetworkScan", "registerTelephonyCallback"},
//                new Object[]{HOOKED_ID, HOOKED_ID, HOOKED_ID, HOOKED_ID, HOOKED_ID, HOOKED_ID, null, null, HOOKED_ID, null, null, null, null, null, null, null});
        // 简化版TelephonyManager
        hookMethods("android.telephony.TelephonyManager", new HookConfig[] {
                new HookConfig(DOMAIN_DEVICE_ID, "getSubscriberId", HOOKED_ID),
                new HookConfig(DOMAIN_DEVICE_ID, "getSimSerialNumber", HOOKED_ID),
                new HookConfig(DOMAIN_DEVICE_ID, "getDeviceId", HOOKED_ID),
                new HookConfig(DOMAIN_DEVICE_ID, "getImei", HOOKED_ID),
                new HookConfig(DOMAIN_DEVICE_ID, "getMeid", HOOKED_ID),
                new HookConfig(DOMAIN_DEVICE_ID, "getLine1Number", HOOKED_ID)
        });
//        hookMethods("android.provider.Settings$Secure", new String[]{"getString"}, new Object[]{""});
//        XposedHelpers.setStaticObjectField(android.os.Build.class, "SERIAL", HOOKED_ID); // TODO 设置字段的hook逻辑待梳理
//        hookMethods("android.os.Build", new String[]{"getSerial"}, new Object[]{HOOKED_ID_2});
//        hookMethods("android.telephony.SubscriptionInfo", new String[]{"getNumber", "getIccId"}, new Object[]{"", ""});
//        hookMethods("android.telecom.PhoneAccount", new String[]{"getAddress"}, new Object[]{null});
//        hookMethods("android.telecom.TelecomManager", new String[]{"getLine1Number"}, new Object[]{""});
//        hookMethods("android.accounts.AccountManager", new String[]{"getAccounts", "getAccountsByType"}, new Object[]{null, null});
        hookMethods("android.location.LocationManager", new HookConfig[] {
                new HookConfig(DOMAIN_LOCATION, "getLastKnownLocation", null),
                new HookConfig(DOMAIN_LOCATION, "addGpsStatusListener", false),
                new HookConfig(DOMAIN_LOCATION, "requestSingleUpdate", null),
                new HookConfig(DOMAIN_LOCATION, "requestLocationUpdates", null)
        });
//        hookMethods("android.bluetooth.BluetoothManager", new String[]{"getConnectedDevices", "getDevicesMatchingConnectionStates"}, new Object[]{null, null});
//        hookMethods("android.bluetooth.le.BluetoothLeScanner", new String[]{"startScan"}, new Object[]{null});
//        hookMethods("android.app.ActivityManager", new String[]{"getRunningTasks", "getRunningAppProcesses", "getRecentTasks", "getRunningServices"}, new Object[]{null, null, null, null});
        // 注意：android.content.pm.PackageManager 是一个抽象类, android.app.ApplicationPackageManager 才是实际的类
        hookMethods("android.app.ApplicationPackageManager", new HookConfig[] {
                new HookConfig(DOMAIN_APP_LIST, "getInstalledApplications", new ArrayList<>()),
                new HookConfig(DOMAIN_APP_LIST, "getInstalledModules", new ArrayList<>()),
                new HookConfig(DOMAIN_APP_LIST, "getInstalledPackages", new ArrayList<>())
        });

        hookMethods("android.hardware.SensorManager", new HookConfig[] {
                new HookConfig(DOMAIN_OTHERS, "getSensorList", new ArrayList<>())
        });

        return true;
    }

    public static void unhookAll() {
        synchronized (stateLock) {
            state = STATE_NOT_WORKING;
        }
        for (XC_MethodHook.Unhook unhook : unhooks) {
            unhook.unhook();
        }
        unhooks.clear();
    }

    public static void block() {
        synchronized (stateLock) {
            state = STATE_BLOCK;
        }
    }

    public static void controlFrequency() {
        synchronized (stateLock) {
            state = STATE_FREQUENCY_CONTROL;
        }
    }

    public static void disable() {
        synchronized (stateLock) {
            state = STATE_NOT_WORKING;
        }
    }


}
