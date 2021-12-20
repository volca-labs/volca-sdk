package com.volca.uniplugin;

import android.util.Log;

import com.volca.volcasdk.VolcaSDK;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.common.UniModule;

public class VolcaModule extends UniModule {

    @UniJSMethod(uiThread = false)
    public boolean isAvailable() {
        return VolcaSDK.isAvailable();
    }

    @UniJSMethod(uiThread = false)
    public void block() {
        VolcaSDK.block();
    }

    @UniJSMethod(uiThread = false)
    public void controlFrequency() {
        VolcaSDK.controlFrequency();
    }

    @UniJSMethod(uiThread = false)
    public void disable() {
        VolcaSDK.disable();
    }
}
