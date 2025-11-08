package com.retrivedmods.wrelay.util

import com.retrivedmods.wrelay.address.WAddress
import com.retrivedmods.wrelay.config.EnhancedServerConfig
import java.net.InetAddress
import java.util.regex.Pattern

object ServerCompatUtils {

    private val PROTECTED_SERVER_PATTERNS = listOf(
        Pattern.compile(".*\\.aternos\\.me$", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*\\.aternos\\.org$", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*\\.aternos\\.net$", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*aternos.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*\\.nethergames\\.org$", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*nethergames.*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*\\.cubecraft\\.net$", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*cubecraft.*", Pattern.CASE_INSENSITIVE)
    )
    
    private val KNOWN_PROTECTED_IPS = setOf(
        "116.202.224.146",
        "135.181.42.192",
        "168.119.61.4",
        "95.217.163.246"
    )

    fun isProtectedServer(address: WAddress): Boolean {
        return isProtectedHostname(address.hostName) || isProtectedIP(address.hostName)
    }

    fun isProtectedHostname(hostname: String): Boolean {
        return PROTECTED_SERVER_PATTERNS.any { pattern ->
            pattern.matcher(hostname).matches()
        }
    }

    fun isProtectedIP(hostname: String): Boolean {
        try {
            val address = InetAddress.getByName(hostname)
            val ip = address.hostAddress

            if (KNOWN_PROTECTED_IPS.contains(ip)) {
                return true
            }

            return isInProtectedIPRange(ip)
            
        } catch (e: Exception) {
            return false
        }
    }

    private fun isInProtectedIPRange(ip: String): Boolean {
        val parts = ip.split(".")
        if (parts.size != 4) return false
        
        try {
            val first = parts[0].toInt()
            val second = parts[1].toInt()
            

            return when (first) {
                116 -> second in 202..203
                135 -> second in 181..181
                168 -> second in 119..119
                95 -> second in 217..217
                else -> false
            }
        } catch (e: NumberFormatException) {
            return false
        }
    }

    fun getRecommendedConfig(address: WAddress): EnhancedServerConfig {
        if (!isProtectedServer(address)) {
            return EnhancedServerConfig.FAST
        }

        val hostname = address.hostName.lowercase()
        
        return when {
            hostname.contains("nethergames") -> EnhancedServerConfig.AGGRESSIVE
            hostname.contains("cubecraft") -> EnhancedServerConfig.AGGRESSIVE
            hostname.matches(Regex(".*\\d+\\.aternos\\.me")) -> EnhancedServerConfig.DEFAULT
            hostname.contains("aternos.me") && !hostname.matches(Regex(".*\\d+\\.aternos\\.me")) -> EnhancedServerConfig.FAST
            else -> EnhancedServerConfig.AGGRESSIVE
        }
    }

    fun extractServerInfo(hostname: String): ProtectedServerInfo? {
        if (!isProtectedHostname(hostname)) {
            return null
        }
        
        val lowerHostname = hostname.lowercase()

        val serverIdPattern = Pattern.compile("(\\w+)\\.aternos\\.(me|org|net)")
        val matcher = serverIdPattern.matcher(lowerHostname)
        
        if (matcher.find()) {
            val serverId = matcher.group(1)
            val domain = matcher.group(2)
            
            return ProtectedServerInfo(
                serverId = serverId,
                domain = domain,
                isNumericId = serverId.matches(Regex("\\d+")),
                fullHostname = hostname
            )
        }
        
        return null
    }

    fun getConnectionTips(address: WAddress): List<String> {
        if (!isProtectedServer(address)) {
            return emptyList()
        }
        
        val tips = mutableListOf<String>()
        
        tips.add("Protected server detected - using optimized connection settings")
        tips.add("Connection may take longer due to DDoS protection")
        tips.add("Multiple retry attempts will be made with exponential backoff")
        
        val serverInfo = extractServerInfo(address.hostName)
        if (serverInfo != null) {
            if (serverInfo.isNumericId) {
                tips.add("Numeric server ID detected - using standard configuration")
            } else {
                tips.add("Custom server name detected - may have better stability")
            }
        }
        
        return tips
    }

    data class ProtectedServerInfo(
        val serverId: String,
        val domain: String,
        val isNumericId: Boolean,
        val fullHostname: String
    )
}