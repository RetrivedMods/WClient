package com.retrivedmods.wclient.util

import java.util.regex.Pattern

object ServerCompatUtils {

    private val PROTECTED_SERVER_PATTERNS = listOf(
        Pattern.compile(".*\\.aternos\\.me$", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*\\.aternos\\.org$", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*\\.aternos\\.net$", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*aternos.*", Pattern.CASE_INSENSITIVE)
    )

    private val KNOWN_PROTECTED_IPS = setOf(
        "116.202.224.146",
        "135.181.42.192",
        "168.119.61.4",
        "95.217.163.246"
    )

    fun isProtectedHostname(hostname: String): Boolean {
        return PROTECTED_SERVER_PATTERNS.any { pattern ->
            pattern.matcher(hostname).matches()
        }
    }

    fun isProtectedIP(hostname: String): Boolean {
        try {
            if (KNOWN_PROTECTED_IPS.contains(hostname)) {
                return true
            }

            return isInProtectedIPRange(hostname)

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

    fun isProtectedServer(hostname: String): Boolean {
        return isProtectedHostname(hostname) || isProtectedIP(hostname)
    }

    fun extractServerInfo(hostname: String): ProtectedServerInfo? {
        if (!isProtectedHostname(hostname)) {
            return null
        }

        val lowerHostname = hostname.lowercase()

        val serverIdPattern = Pattern.compile("(\\w+)\\.aternos\\.(me|org|net)")
        val matcher = serverIdPattern.matcher(lowerHostname)

        if (matcher.find()) {
            val serverId = matcher.group(1) ?: return null
            val domain = matcher.group(2) ?: return null

            return ProtectedServerInfo(
                serverId = serverId,
                domain = domain,
                isNumericId = serverId.matches(Regex("\\d+")),
                fullHostname = hostname
            )
        }

        return null
    }

    fun getRecommendedConfigType(hostname: String): ServerConfigType {
        if (!isProtectedServer(hostname)) {
            return ServerConfigType.STANDARD
        }

        val lowerHostname = hostname.lowercase()

        return when {
            lowerHostname.matches(Regex("\\d+\\.aternos\\.me")) -> ServerConfigType.DEFAULT

            lowerHostname.contains("aternos.me") && !lowerHostname.matches(Regex(".*\\d+\\.aternos\\.me")) -> ServerConfigType.FAST

            else -> ServerConfigType.AGGRESSIVE
        }
    }

    fun getConnectionTips(hostname: String): List<String> {
        if (!isProtectedServer(hostname)) {
            return emptyList()
        }

        val tips = mutableListOf<String>()

        tips.add("🎯 Protected server detected - using optimized connection settings")
        tips.add("⏱️ Connection may take longer due to DDoS protection")
        tips.add("🔄 Multiple retry attempts will be made with exponential backoff")
        tips.add("🛡️ Enhanced connection stability for protected infrastructure")

        val serverInfo = extractServerInfo(hostname)
        if (serverInfo != null) {
            if (serverInfo.isNumericId) {
                tips.add("🔢 Numeric server ID detected - using standard configuration")
            } else {
                tips.add("📝 Custom server name detected - may have better stability")
            }
        }

        return tips
    }

    fun getTroubleshootingTips(): List<String> {
        return listOf(
            "🔍 Make sure your server is online in the dashboard",
            "⏰ Wait 2-3 minutes between connection attempts",
            "🔄 Try restarting your server if connection fails repeatedly",
            "🌐 Check if other players can connect to rule out server issues",
            "📱 Ensure your device has a stable internet connection",
            "🛡️ Server has DDoS protection that may temporarily block connections"
        )
    }

    fun getStatusMessage(configType: ServerConfigType): String {
        return when (configType) {
            ServerConfigType.FAST -> "⚡ Using Fast configuration for stable server"
            ServerConfigType.DEFAULT -> "🔧 Using Default configuration for standard server"
            ServerConfigType.AGGRESSIVE -> "🔥 Using Aggressive configuration for problematic server"
            ServerConfigType.STANDARD -> "📡 Using standard configuration"
        }
    }

    fun looksLikeProtectedServer(hostname: String): Boolean {
        val lower = hostname.lowercase()
        return lower.contains("aternos") ||
                lower.matches(Regex("\\w+\\.\\w+\\.\\w+")) && lower.length > 10
    }

    data class ProtectedServerInfo(
        val serverId: String,
        val domain: String,
        val isNumericId: Boolean,
        val fullHostname: String
    )

    enum class ServerConfigType {
        FAST,
        DEFAULT,
        AGGRESSIVE,
        STANDARD
    }

    fun getConfigDescription(configType: ServerConfigType): String {
        return when (configType) {
            ServerConfigType.FAST -> "3 retry attempts, 1 second delay - for stable servers"
            ServerConfigType.DEFAULT -> "5 retry attempts, 2 second delay - recommended for most servers"
            ServerConfigType.AGGRESSIVE -> "8 retry attempts, 5 second delay - for problematic servers"
            ServerConfigType.STANDARD -> "Standard configuration for regular servers"
        }
    }

    fun getPopularProtectedServers(): List<PopularProtectedServer> {
        return listOf(
            PopularProtectedServer(
                name = "Example Server 1",
                hostname = "server123.aternos.me",
                port = 19132,
                description = "Example protected server with numeric ID"
            ),
            PopularProtectedServer(
                name = "Example Server 2",
                hostname = "myserver.aternos.me",
                port = 19132,
                description = "Example protected server with custom name"
            ),
            PopularProtectedServer(
                name = "Custom Port Server",
                hostname = "example.aternos.me",
                port = 25565,
                description = "Example server with custom port"
            )
        )
    }

    data class PopularProtectedServer(
        val name: String,
        val hostname: String,
        val port: Int,
        val description: String
    )
}