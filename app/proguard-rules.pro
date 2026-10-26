-keepattributes Signature,*Annotation*,SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keep class com.example.data.** { *; }
-keepclassmembers class com.example.data.** { <init>(...); }
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
