# QA Lab Launcher ProGuard Rules

# Keep Room entities
-keep class com.qalab.launcher.data.local.entity.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep plugin interface
-keep interface com.qalab.launcher.domain.model.TestPlugin { *; }
-keep class * implements com.qalab.launcher.domain.model.TestPlugin { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
