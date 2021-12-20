# volca-sdk

[![](https://jitpack.io/v/volca-labs/volca-sdk.svg)](https://jitpack.io/#volca-labs/volca-sdk)

## 功能
对于敏感API进行拦截，或控制其调用频率。这样可以确保App及第三方SDK在用户授权前不能访问敏感API，用户授权后也控制敏感API的调用频率。保护用户的同时也更容易上架过审

## 适配环境
- ABI
  - ARM64
  - ARM32(no tested)
  - Thumb-2
- OS
  - Android 4.4(ART Runtime) - 11.0

## 集成
Add it in your root build.gradle at the end of repositories:
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
Step 2. Add the dependency
```
dependencies {
    implementation 'com.github.volca-labs.volca-sdk:volca:v1.0'
}
```

## API列表

VolcaSDK
- `void init(Context context)`: 初始化SDK
- `void hookAll()`: 拦截所有敏感API
- `boolean isAvailable()`: 判断当前运行环境SDK是否生效
- `void block()`: 重新拦截所有敏感API
- `void controlFrequncy()`: 控制敏感API调用频率，避免频繁调用敏感API
- `void disable()`: 不做任何API拦截或控制频率的操作

## 拦截API列表
### android.net.wifi.WifiInfo
- getMacAddress
- getBSSID
- getSSID

### android.telephony.TelephonyManager
- getSubscriberId
- getSimSerialNumber
- getDeviceId
- getImei
- getMeid
- getLine1Number

### android.location.LocationManager
- getLastKnownLocation
- addGpsStatusListener
- requestLocationUpdates
- requestSingleUpdate

### android.net.wifi.WifiManager
- getScanResults
- getConnectionInfo
- startScan

### android.app.ApplicationPackageManager
- getInstalledApplications
- getInstalledModules
- getInstalledPackages

### android.hardware.SensorManager
- getSensorList

## 第三方依赖
[SandHook](https://github.com/asLody/SandHook)

## uniapp 插件
[App上架小助手](https://ext.dcloud.net.cn/plugin?id=6606)