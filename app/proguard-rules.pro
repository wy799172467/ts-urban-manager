# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/admin/Library/Android/sdk/tools/proguard/proguard-android.txt
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
-dontwarn **
-dontnote **

# for slidingMenu library
-keep class com.jeremyfeinstein.slidingmenu.** { *; }
-keep interface com.jeremyfeinstein.slidingmenu.** { *; }

# for arcgis
-keep class com.esri.** { *; }
-keep interface com.esri.** { *; }

# for library-2.4.1.aar
-keep class com.viewpagerindicator.** { *; }
-keep interface com.viewpagerindicator.** { *; }

# for geonebase-1.1.aar
-keep class geone.base.** { *; }
-keep interface geone.base.** { *; }

# for httpmime
-keep class org.apache.http.entity.mime.** { *; }
-keep interface org.apache.http.entity.mime.** { *; }

# for sqliteassethelper
-keep class com.readystatesofware.** { *; }
-keep interface com.readystatesofware.** { *; }

# for volley
-keep class com.android.volley.** { *; }
-keep interface com.android.volley.** { *; }

# for okhttp3
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# for gson
-keepattributes *Annotation*
-keepattributes Signature
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.examples.android.model.** { *; }

# for Parcelable
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-dontwarn org.codehaus.jackson.**
-keep class org.codehaus.jackson.** {*;}
-keep interface org.codehaus.jackson.** {*;}

-dontwarn jcifs.http.**
-keep class jcifs.** {*;}
-keep interface jcifs.** {*;}