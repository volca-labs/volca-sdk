package com.volca.volcasdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class SandHookInstrumentedTest {

    public static final String TAG = "VolcaTest";

    private static Context context;

    @BeforeClass
    public static void init() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        HookManager.init(context);
        HookManager.hookAll();

//        SandHookManager.controlFrequency();
//        SandHookManager.unhookAll();
    }

    @AfterClass
    public static void cancel() {
        HookManager.unhookAll();
    }

    @Test
    public void testWifiInfo() {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String mac = wifiInfo.getMacAddress();
        String bssid = wifiInfo.getBSSID();
        String ssid = wifiInfo.getSSID();
        assertEquals(HookManager.HOOKED_MAC_ADDRESS, mac);
        assertEquals(HookManager.HOOKED_ID, bssid);
        assertEquals(HookManager.HOOKED_ID, ssid);
    }

//    @Test
//    public void testNetworkCapabilities() {
//        NetworkCapabilities cap = new NetworkCapabilities();
//        TransportInfo info = cap.getTransportInfo();
//        assertEquals(null, info);
//    }
//
//    @Test
//    public void testNetworkInterface() throws SocketException {
//        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
//        while (nets != null && nets.hasMoreElements()) {
//            NetworkInterface net = nets.nextElement();
//            byte[] address = net.getHardwareAddress();
//            assertEquals(SandHookManager.HOOKED_MAC_ADDRESS, address.toString());
//        }
//    }
//
//    @Test
//    public void testBluetoothAdapter() {
//
//    }

    @Test
    public void testTelephonyManager() {
        TelephonyManager manager = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        assertEquals(HookManager.HOOKED_ID, manager.getSubscriberId());
        assertEquals(HookManager.HOOKED_ID, manager.getSimSerialNumber());
        assertEquals(HookManager.HOOKED_ID, manager.getDeviceId());
        assertEquals(HookManager.HOOKED_ID, manager.getImei());
        assertEquals(HookManager.HOOKED_ID, manager.getImei(0));
        assertEquals(HookManager.HOOKED_ID, manager.getMeid());
        assertEquals(HookManager.HOOKED_ID, manager.getMeid(0));
        assertEquals(HookManager.HOOKED_ID, manager.getLine1Number());
//        manager.getCardIdForDefaultEuicc();
//        manager.getUiccCardsInfo();
//        assertEquals(SandHookManager.HOOKED_ID, manager.getNai());
//        manager.listen(new PhoneStateListener() {
//            @Override
//            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
//                Log.d(TAG, signalStrength.toString());
//            }
//        }, PhoneStateListener.LISTEN_CALL_STATE);
//        manager.getAllCellInfo();
//        manager.getCellLocation();
//        manager.getNeighboringCellInfo();
//        manager.requestCellInfoUpdate(new Executor() {
//            @Override
//            public void execute(Runnable command) {
//
//            }
//        }, new TelephonyManager.CellInfoCallback() {
//            @Override
//            public void onCellInfo(@NonNull List<CellInfo> cellInfo) {
//
//            }
//        });
//        manager.requestNetworkScan(new NetworkScanRequest(), new Executor() {
//            @Override
//            public void execute(Runnable command) {
//
//            }
//        }, new TelephonyScanManager.NetworkScanCallback() {
//            @Override
//            public void onResults(List<CellInfo> results) {
//                super.onResults(results);
//            }
//        });
//        manager.registerTelephonyCallback(new Executor() {
//            @Override
//            public void execute(Runnable command) {
//
//            }
//        }, new TelephonyCallback(){
//
//        });
    }

//    @Test
//    public void testSettingsSecure() {
//
//    }
//
//    @Test
//    public void testBuild() {
//        assertEquals(SandHookManager.HOOKED_ID, Build.SERIAL);
//        assertEquals(SandHookManager.HOOKED_ID_2, Build.getSerial());
//    }
//
//    @Test
//    public void testSubscriptionInfo() {
//
//    }
//
//    @Test
//    public void testPhoneAccount() {
//
//    }
//
//    @Test
//    public void testTelecomManager() {
//
//    }
//
//    @Test
//    public void testAccountManager() {
//
//    }

    @Test
    public void testLocationManager() {
        LocationManager manager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean result = manager.addGpsStatusListener(new GpsStatus.Listener() {
            @Override
            public void onGpsStatusChanged(int i) {
                Log.d(TAG, "onGpsStatusChanged: " + i);
            }
        });
        assertFalse(result);

        List<String> providers = manager.getProviders(false);
//        List<String> providers = Arrays.asList(LocationManager.GPS_PROVIDER);
//        if (providers == null) return;
        Log.d(TAG, "providers size: " + providers.size());
        for (String provider : providers) {
            assertNull(manager.getLastKnownLocation(provider));

            manager.requestLocationUpdates(provider, 500, 1, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    assertNull(null);
                }
            }, Looper.getMainLooper());

            manager.requestSingleUpdate(provider, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    assertNull(null);
                }
            }, Looper.getMainLooper());
        }
    }

    @Test
    public void testWifiManager() {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        assertNotNull(wifiInfo);
        assertFalse(wifiManager.startScan());
        List<?> results = wifiManager.getScanResults();
        assertEquals(0, results.size());
    }

//    @Test
//    public void testBluetoothLeScanner() {
//
//    }
//
//    @Test
//    public void testActivityManager() {
//
//    }

    @Test
    public void testPackageManager() {
        // PackageManager 是一个抽象类
        PackageManager manager = context.getPackageManager();

        List<?> applications = manager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        List<?> modules = manager.getInstalledModules(PackageManager.GET_UNINSTALLED_PACKAGES);
        List<?> packages = manager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);

        Log.d(TAG, String.format("testPackageManager: %d %d %d", applications.size(), modules.size(), packages.size()));

        assertEquals(0, applications.size());
        assertEquals(0, modules.size());
        assertEquals(0, packages.size());
    }

    @Test
    public void testSensorManager() {
        SensorManager sensorManager = (SensorManager) context.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        List<?> sensors = sensorManager.getSensorList(SensorManager.SENSOR_ALL);
        assertEquals(0, sensors.size());
    }
}