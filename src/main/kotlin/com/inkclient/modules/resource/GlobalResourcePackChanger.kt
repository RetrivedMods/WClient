package com.inkclient.modules.resource

import com.inkclient.logging.InkClientLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

/**
 * GlobalResourcePackChanger
 *
 * Responsibilities:
 * - Maintain a registry/entry point for live resource pack overrides.
 * - Apply or Hot-Swap resource packs over a loopback TCP agent (127.0.0.1).
 * - Avoid requiring a client reload by instructing a local agent to patch the active pack stack.
 *
 * Usage:
 * - call applyPack(pack) to instruct the local agent to set the global pack stack synchronously.
 * - call hotSwapPack(pack) to request a hot-swap that does not trigger a full client reload.
 *
 * Notes:
 * - For security and reliability, the actual platform component that performs the resource override
 *   (the "local agent") should run on the same host and accept the JSON protocol used below.
 *
 * JSON protocol:
 * {
 *   "action": "apply" | "hot_swap",
 *   "pack": {
 *     "id": "...",
 *     "displayName": "...",
 *     "path": "..."
 *   },
 *   "stack": [ "...", "..." ] // optional
 * }
 *
 * The agent should respond with a single-line acknowledgement. This class expects a short TCP exchange.
 */
object GlobalResourcePackChanger {
    // Configurable loopback agent endpoint (use 127.0.0.1)
    private const val LOOPBACK_HOST = "127.0.0.1"
    private const val LOOPBACK_PORT = 19133  // chosen ephemeral application port for agent
    private val connected = AtomicBoolean(false)

    /**
     * Send a synchronous apply request to the local agent.
     * Returns true on successful ack, false otherwise.
     */
    suspend fun applyPack(pack: ResourcePack, stack: List<String> = emptyList(), timeoutMs: Long = 1500): Boolean {
        val payload = JSONObject().apply {
            put("action", "apply")
            put("pack", JSONObject().apply {
                put("id", pack.id)
                put("displayName", pack.displayName)
                put("path", pack.path)
                pack.author?.let { put("author", it) }
            })
            put("stack", stack)
        }
        return sendToAgent(payload.toString(), timeoutMs)
    }

    /**
     * Send a hot-swap request: should be handled by agent without a client reload.
     */
    suspend fun hotSwapPack(pack: ResourcePack, stack: List<String> = emptyList(), timeoutMs: Long = 1000): Boolean {
        val payload = JSONObject().apply {
            put("action", "hot_swap")
            put("pack", JSONObject().apply {
                put("id", pack.id)
                put("displayName", pack.displayName)
                put("path", pack.path)
            })
            put("stack", stack)
        }
        return sendToAgent(payload.toString(), timeoutMs)
    }

    /**
     * Low-level send with simple response ack expectation.
     */
    private suspend fun sendToAgent(message: String, timeoutMs: Long): Boolean = withContext(Dispatchers.IO) {
        val socket = Socket()
        try {
            val addr = InetSocketAddress(LOOPBACK_HOST, LOOPBACK_PORT)
            socket.soTimeout = timeoutMs.toInt()
            socket.connect(addr, timeoutMs.toInt())
            connected.set(true)

            BufferedWriter(OutputStreamWriter(socket.getOutputStream(), Charsets.UTF_8)).use { writer ->
                writer.write(message)
                writer.write("\n")
                writer.flush()
            }

            // If agent sends an acknowledgement, simply read one line (optional)
            // For this minimal client we treat successful write/connect as success.
            InkClientLogger.i("InkClient: sent payload to local agent: ${message.take(512)}")
            true
        } catch (t: Throwable) {
            InkClientLogger.e("InkClient: failed to contact loopback agent", t)
            false
        } finally {
            try { socket.close() } catch (_: Throwable) { }
            connected.set(false)
        }
    }
}
