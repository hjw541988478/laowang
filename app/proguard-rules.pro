# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\adt\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#UMENG
-keep public class com.ywxy.laowang.R$*{
public static final int *;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}

#UMENG PUSH
-keep class com.umeng.message.* {
        public <fields>;
        public <methods>;
}
-keep class com.umeng.message.protobuffer.* {
        public <fields>;
        public <methods>;
}
-keep class com.squareup.wire.* {
        public <fields>;
        public <methods>;
}
-keep class org.android.agoo.impl.*{
        public <fields>;
        public <methods>;
}
-keep class org.android.agoo.service.* {*;}
-keep class org.android.spdy.**{*;}
-keep public class com.ywxy.laowang.R$*{
    public static final int *;
}

#baidu ad
-keep class com.baidu.mobads.** {   public protected *; }

#butterknife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

# Appcompat and support
-keep interface android.support.v7.** { *; }
-keep class android.support.v7.** { *; }
-keep interface android.support.v4.** { *; }

-keep class android.support.v4.** { *; }
-dontwarn android.support.v4.**
# Volley
-keep class com.android.volley.** {*;}
-keep class com.android.volley.toolbox.** {*;}
-keep class com.android.volley.Response$* { *; }
-keep class com.android.volley.Request$* { *; }
-keep class com.android.volley.RequestQueue$* { *; }
-keep class com.android.volley.toolbox.HurlStack$* { *; }
-keep class com.android.volley.toolbox.ImageLoader$* { *; }

-dontwarn uk.co.**
-keep class uk.co.** {*;}

-dontwarn com.android.**
-keep class com.android.** {*;}

-dontwarn com.baidu.**
-keep class com.baidu.** {*;}

-dontwarn com.ta.**
-keep class com.ta.** {*;}

-dontwarn com.umeng.**
-keep class com.umeng.** {*;}

-dontwarn org.android.**
-keep class org.android.** {*;}

-dontwarn org.apache.**
-keep class org.apache.** {*;}

-dontwarn u.**
-keep class u.** {*;}

-keepclassmembers class * implements java.io.Serializable {
    <fields>;
}
