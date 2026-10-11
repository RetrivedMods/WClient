package com.retrivedmods.wclient.game.utils.combat

import com.retrivedmods.wclient.game.GameSession
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.TickSyncPacket
import kotlin.math.abs

/**
 * Measures relay <-> server round-trip time and keeps an RFC 6298 style smoothed estimate
 * (SRTT / RTTVAR) so a single spike does not yank the aim around.
 *
 * Two sample sources:
 *  1. Active probe: a [TickSyncPacket] is sent every [PROBE_INTERVAL_MS] on protocols that still
 *     support it (removed in 685); the server echoes the request timestamp back, giving a full-stack
 *     RTT (RakNet + server tick processing). On newer protocols the RakNet-level RTT of the relay
 *     <-> server link is sampled instead, because TickSync no longer exists on the wire.
 *  2. Passive: combat feedback (attack -> HURT event) reported by [HitTracker] via [addSample].
 *
 * Note: the relay runs on the same device as the game, so relay<->client latency is ~0 and the
 * relay<->server RTT is what matters for prediction.
 */
class LatencyTracker(private val session: GameSession) {

    companion object {
        private const val PROBE_INTERVAL_MS = 1000L
        private const val MAX_SANE_RTT_MS = 2000L
        private const val TICK_MS = 50f
        private const val ALPHA = 1f / 8f
        private const val BETA = 1f / 4f
    }

    @Volatile
    var smoothedRttMs: Float = -1f
        private set

    @Volatile
    var rttVarMs: Float = 0f
        private set

    val hasEstimate: Boolean get() = smoothedRttMs >= 0f

    /** Ticks between what we see and server-now: half RTT plus one tick of server processing. */
    val lookaheadTicks: Float
        get() = if (!hasEstimate) 2f else (smoothedRttMs / 2f) / TICK_MS + 1f

    private var lastProbeSentAt = 0L
    private val pendingProbes = java.util.concurrent.ConcurrentHashMap<Long, Long>()

    @Synchronized
    fun addSample(rttMs: Long) {
        if (rttMs < 0 || rttMs > MAX_SANE_RTT_MS) return
        val r = rttMs.toFloat()
        if (!hasEstimate) {
            smoothedRttMs = r
            rttVarMs = r / 2f
        } else {
            rttVarMs = (1f - BETA) * rttVarMs + BETA * abs(smoothedRttMs - r)
            smoothedRttMs = (1f - ALPHA) * smoothedRttMs + ALPHA * r
        }
    }

    /** Called from GameSession for every packet; returns true if the packet should be swallowed. */
    fun onPacket(packet: BedrockPacket): Boolean {
        if (packet is TickSyncPacket) {
            val sentAt = pendingProbes.remove(packet.requestTimestamp)
            if (sentAt != null) {
                addSample(System.currentTimeMillis() - sentAt)
                return true // our probe reply - the game never asked for it
            }
        }
        return false
    }

    /** Called once per client tick (PlayerAuthInputPacket). */
    fun tick() {
        val now = System.currentTimeMillis()
        if (now - lastProbeSentAt < PROBE_INTERVAL_MS) return
        lastProbeSentAt = now

        pendingProbes.entries.removeAll { now - it.value > MAX_SANE_RTT_MS * 2 }

        if (!session.supportsTickSyncProbe) {
            // TickSync was removed in protocol 685; sending it on a modern server throws inside the
            // relay encoder (no packet id) and the server could never echo it back. Fall back to the
            // RakNet-level RTT of the relay <-> server link, which the transport measures anyway.
            session.relayUpstreamPingMs()?.let { addSample(it) }
            return
        }

        val probe = TickSyncPacket().apply {
            requestTimestamp = now
            responseTimestamp = 0
        }
        pendingProbes[now] = now
        session.serverBound(probe)
    }

    fun reset() {
        smoothedRttMs = -1f
        rttVarMs = 0f
        pendingProbes.clear()
        lastProbeSentAt = 0L
    }
}
