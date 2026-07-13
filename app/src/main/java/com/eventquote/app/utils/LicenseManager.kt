package com.eventquote.app.utils

import android.content.Context

/**
 * Simple offline license key manager.
 *
 * Valid key is hardcoded and compared directly — no crypto computation at runtime,
 * so it works identically across all Android devices.
 *
 * To give a customer access, share the VALID_KEY below.
 * To change the key for a new version, update VALID_KEY and rebuild.
 */
object LicenseManager {

    private const val PREFS_NAME = "eq_license"
    private const val KEY_LICENSE = "license_key"
    private const val KEY_ACTIVATED = "activated"

    // ✅ Your app's license key — share this with your customers.
    // Change this before each new version if needed.
    private const val VALID_KEY = "EQ26-EVNT-QUOT-PRO1"

    fun isActivated(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ACTIVATED, false)
    }

    fun getSavedKey(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LICENSE, "") ?: ""
    }

    /**
     * Validates the entered key and activates the app if correct.
     * Case-insensitive, dashes and spaces ignored.
     */
    fun activate(context: Context, enteredKey: String): Boolean {
        val entered = enteredKey.trim().uppercase()
            .replace("-", "")
            .replace(" ", "")
        val valid = VALID_KEY.replace("-", "")

        return if (entered == valid) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_ACTIVATED, true)
                .putString(KEY_LICENSE, enteredKey.trim().uppercase())
                .apply()
            true
        } else {
            false
        }
    }

    fun deactivate(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().clear().apply()
    }
}
