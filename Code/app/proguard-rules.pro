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

# ═══════════════════════════════════════════════════════════════
# FIREBASE RULES
# ═══════════════════════════════════════════════════════════════
-keepattributes Signature
-keepattributes *Annotation*

# Firebase Realtime Database & Firestore
-keepclassmembers class com.example.demolition.User { *; }
-keepclassmembers class com.example.demolition.StudentReport { *; }
-keepclassmembers class com.example.demolition.models.** { *; }

# ═══════════════════════════════════════════════════════════════
# GSON RULES
# ═══════════════════════════════════════════════════════════════
-keepattributes Signature
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Keep data model classes used with Gson
-keepclassmembers class com.example.demolition.models.** {
    <fields>;
    <init>();
}

# ═══════════════════════════════════════════════════════════════
# NAVIGATION COMPONENT
# ═══════════════════════════════════════════════════════════════
-keepnames class * extends android.os.Parcelable
-keepnames class * extends java.io.Serializable

# ═══════════════════════════════════════════════════════════════
# NATIVE LIBRARIES (llama.cpp)
# ═══════════════════════════════════════════════════════════════
-keep class com.example.demolition.ai.** { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}