package com.labrats.mindshield

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.EventChannel

class MainActivity: FlutterActivity() {
    private val CHANNEL = "com.focusguard/blocking"
    private val EVENT_CHANNEL = "com.focusguard/blocking_events"

    companion object {
        var eventSink: EventChannel.EventSink? = null

        @JvmStatic
        fun emitBlockEvent(platform: String, timestamp: Long) {
            val event = mapOf(
                "platform" to platform,
                "timestamp" to timestamp
            )
            eventSink?.success(event)
        }
    }

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "isAccessibilityServiceEnabled" -> {
                    result.success(isAccessibilityServiceEnabled())
                }
                "openAccessibilitySettings" -> {
                    openAccessibilitySettings()
                    result.success(null)
                }
                "setProtectionActive" -> {
                    val active = call.argument<Boolean>("active") ?: false
                    setProtectionActive(active)
                    result.success(true)
                }
                "isProtectionActive" -> {
                    result.success(isProtectionActive())
                }
                "setBlockedPlatforms" -> {
                    val youtube = call.argument<Boolean>("youtube") ?: true
                    val instagram = call.argument<Boolean>("instagram") ?: true
                    val facebook = call.argument<Boolean>("facebook") ?: true
                    val tiktok = call.argument<Boolean>("tiktok") ?: true
                    val snapchat = call.argument<Boolean>("snapchat") ?: true
                    setBlockedPlatforms(youtube, instagram, facebook, tiktok, snapchat)
                    result.success(true)
                }
                else -> {
                    result.notImplemented()
                }
            }
        }

        EventChannel(flutterEngine.dartExecutor.binaryMessenger, EVENT_CHANNEL).setStreamHandler(
            object : EventChannel.StreamHandler {
                override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                    eventSink = events
                }

                override fun onCancel(arguments: Any?) {
                    eventSink = null
                }
            }
        )
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        for (service in enabledServices) {
            if (service.id.contains(packageName)) {
                return true
            }
        }
        return false
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun setProtectionActive(active: Boolean) {
        val prefs = getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("flutter.protection_active", active).apply()
        BlockingAccessibilityService.updateState()
    }

    private fun isProtectionActive(): Boolean {
        val prefs = getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE)
        return prefs.getBoolean("flutter.protection_active", false)
    }

    private fun setBlockedPlatforms(youtube: Boolean, instagram: Boolean, facebook: Boolean, tiktok: Boolean, snapchat: Boolean) {
        val prefs = getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean("flutter.block_youtube_shorts", youtube)
        editor.putBoolean("flutter.block_instagram_reels", instagram)
        editor.putBoolean("flutter.block_facebook_reels", facebook)
        editor.putBoolean("flutter.block_tiktok", tiktok)
        editor.putBoolean("flutter.block_snapchat_spotlight", snapchat)
        editor.apply()
        BlockingAccessibilityService.updateState()
    }
}
