# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# 高德定位
-keep class com.amap.api.location.**{*;}
-keep class com.amap.api.fence.**{*;}
-keep class com.loc.**{*;}
-keep class com.autonavi.aps.amapapi.model.**{*;}

-keep class com.s2icode.**{*;}
-dontwarn com.s2icode.**
-keep class com.j256.ormlite.**{*;}
-dontwarn com.j256.ormlite.**
-keep class com.loc.ad.**{*;}
-dontwarn com.loc.ad.**
-keep class com.amap.apis.**{*;}
-dontwarn com.amap.apis.**
-keep class com.serenegiant.**{*;}
-dontwarn com.serenegiant.**
-keep class org.greenrobot.eventbus.**{*;}
-dontwarn org.greenrobot.eventbus.**

#
-dontwarn com.facebook.fresco.**
-keep class com.facebook.fresco.**{ *;}
-keep,allowobfuscation @interface com.facebook.common.internal.DoNotStrip
# Do not strip any method/class that is annotated with @DoNotStrip
-keep @com.facebook.common.internal.DoNotStrip class *
-keepclassmembers class * {
    @com.facebook.common.internal.DoNotStrip *;
}
# Keep native methods
-keepclassmembers class * {
    native <methods>;
}
-dontwarn okio.**
-dontwarn com.squareup.okhttp.**
-dontwarn okhttp3.**
-dontwarn javax.annotation.**
-dontwarn com.android.volley.toolbox.**
-keep class com.baidu.ocr.sdk.**{*;}
-dontwarn com.baidu.ocr.**

