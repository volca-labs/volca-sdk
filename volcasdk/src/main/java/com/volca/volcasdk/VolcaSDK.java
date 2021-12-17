package com.volca.volcasdk;

import android.content.Context;

public class VolcaSDK {
    /**
     *
     * @param context
     */
    public static void init(Context context) {
        Reporter.init(context);
        HookManager.init(context);
    }

    public static boolean isAvailable() {
        return HookManager.isAvailable();
    }

    public static boolean hookAll() {
        Reporter.report("hookAll");
        return HookManager.hookAll();
    }

    public static void block() {
        Reporter.report("block");
        HookManager.block();
    }

    public static void controlFrequency() {
        Reporter.report("controlFrequency");
        HookManager.controlFrequency();
    }

    public static void disable() {
        Reporter.report("disable");
        HookManager.disable();
    }
}
