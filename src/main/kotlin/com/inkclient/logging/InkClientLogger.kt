package com.inkclient.logging

import android.util.Log

/**
 * Minimal logger wrapper for InkClient.
 * Ensures consistent branding and simple control over logs.
 */
object InkClientLogger {
    private const val TAG = "InkClient"

    fun d(message: String) = Log.d(TAG, message)
    fun i(message: String) = Log.i(TAG, message)
    fun w(message: String) = Log.w(TAG, message)
    fun e(message: String, t: Throwable? = null) = Log.e(TAG, message, t)
}
