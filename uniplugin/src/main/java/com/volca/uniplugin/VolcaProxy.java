package com.volca.uniplugin;

import android.app.Application;
import android.util.Log;

import com.volca.volcasdk.VolcaSDK;

import io.dcloud.feature.uniapp.UniAppHookProxy;

public class VolcaProxy implements UniAppHookProxy {

    @Override
    public void onCreate(Application application) {
        Log.d("VolcaProxy", "onCreate");
//        VolcaSDK.init(application, "2000");
        VolcaSDK.init(application);
        VolcaSDK.hookAll();
    }

    @Override
    public void onSubProcessCreate(Application application) {
        Log.d("VolcaProxy", "onSubProcessCreate");
    }
}
