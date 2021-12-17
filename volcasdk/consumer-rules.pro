-keepattributes *Annotation*

-keep class com.swift.sandhook.** { *; }
-keep class com.volca.volcasdk.** { *; }

-keepclasseswithmembernames class * {
    native <methods>;
}

-verbose