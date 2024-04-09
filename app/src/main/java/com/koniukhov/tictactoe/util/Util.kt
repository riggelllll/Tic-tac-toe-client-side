package com.koniukhov.tictactoe.util

import android.content.Context
import android.provider.Settings

object Util {
    fun getAndroidId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}