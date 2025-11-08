package com.retrivedmods.wrelay.config

data class EnhancedServerConfig(

    val maxRetryAttempts: Int = 5,

    val initialRetryDelay: Long = 2000L,

    val maxRetryDelay: Long = 30000L,

    val backoffMultiplier: Double = 2.0,

    val connectionTimeout: Long = 20000L,

    val sessionTimeout: Long = 30000L,

    val timeBetweenConnectionAttempts: Long = 2000L,

    val compatibilityMode: Boolean = true,

    val initialConnectionDelay: Long = 500L,

    val enableConnectionThrottling: Boolean = true,

    val connectionThrottleDelay: Long = 3000L
) {

    companion object {
        val DEFAULT = EnhancedServerConfig()

        val AGGRESSIVE = EnhancedServerConfig(
            maxRetryAttempts = 10,
            initialRetryDelay = 3000L,
            maxRetryDelay = 60000L,
            backoffMultiplier = 2.0,
            connectionTimeout = 30000L,
            sessionTimeout = 45000L,
            timeBetweenConnectionAttempts = 2500L,
            initialConnectionDelay = 2000L,
            connectionThrottleDelay = 8000L
        )

        val FAST = EnhancedServerConfig(
            maxRetryAttempts = 3,
            initialRetryDelay = 1000L,
            maxRetryDelay = 10000L,
            backoffMultiplier = 1.5,
            connectionTimeout = 15000L,
            sessionTimeout = 20000L,
            timeBetweenConnectionAttempts = 1500L,
            initialConnectionDelay = 200L,
            connectionThrottleDelay = 1500L
        )
    }

    fun calculateRetryDelay(attemptNumber: Int): Long {
        val delay = (initialRetryDelay * Math.pow(backoffMultiplier, attemptNumber.toDouble())).toLong()
        return minOf(delay, maxRetryDelay)
    }

    fun isProtectedServer(hostname: String): Boolean {
        return hostname.contains("aternos.me") ||
                hostname.contains("aternos.org") ||
                hostname.contains("aternos.net")
    }
}