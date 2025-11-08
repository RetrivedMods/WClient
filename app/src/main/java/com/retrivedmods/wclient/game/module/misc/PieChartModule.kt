package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.overlay.hud.PieChartOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cloudburstmc.protocol.bedrock.packet.AddEntityPacket
import org.cloudburstmc.protocol.bedrock.packet.AddItemEntityPacket
import org.cloudburstmc.protocol.bedrock.packet.AddPlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.LevelSoundEventPacket
import org.cloudburstmc.protocol.bedrock.packet.MobEffectPacket
import org.cloudburstmc.protocol.bedrock.packet.MoveEntityAbsolutePacket
import org.cloudburstmc.protocol.bedrock.packet.MoveEntityDeltaPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.RemoveEntityPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityDataPacket
import org.cloudburstmc.protocol.bedrock.packet.UpdateBlockPacket
import kotlin.system.measureNanoTime

class PieChartModule : Module("PieChart", ModuleCategory.Misc) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val chartSize by intValue("Chart Size", 140, 100..300)
    private val updateRate by intValue("Update Rate (ms)", 100, 50..1000)
    private val showPercentages by boolValue("Show Percentages", true)
    private val showLabels by boolValue("Show Labels", true)
    private val transparentBackground by boolValue("Transparent Background", true)
    private val animateTransitions by boolValue("Animate Transitions", true)
    private val highlightLargest by boolValue("Highlight Largest", true)
    private val chart3DDepth by intValue("3D Depth", 15, 5..30)
    private val chartTilt by floatValue("Chart Tilt", 0.6f, 0.3f..1.0f)
    private val borderWidth by floatValue("Border Width", 1.5f, 0.5f..3.0f)
    private val legendSpacing by intValue("Legend Spacing", 2, 0..10)
    private val legendFontSize by intValue("Legend Font Size", 11, 8..16)

    private val colorIntensity by floatValue("Color Intensity", 1.0f, 0.5f..1.5f)
    private val positionX by intValue("Position X", 20, -200..200)
    private val positionY by intValue("Position Y", 100, 50..500)

    private var lastUpdateTime = 0L
    private val performanceData = mutableMapOf<String, Long>()
    private val frameTimeHistory = mutableListOf<Long>()
    private val maxHistorySize = 60

    private var entityPackets = 0L
    private var movementPackets = 0L
    private var soundPackets = 0L
    private var blockUpdatePackets = 0L
    private var effectPackets = 0L
    private var otherPackets = 0L
    
    private var lastPacketCountTime = 0L
    private val packetCountInterval = 1000L // 1 second

    override fun onEnabled() {
        super.onEnabled()
        try {
            if (isSessionCreated) {
                PieChartOverlay.setOverlayEnabled(true)
                updateInitialSettings()
                startPerformanceMonitoring()
                resetCounters()
            }
        } catch (e: Exception) {
            println("Error enabling PieChart: ${e.message}")
        }
    }

    override fun onDisabled() {
        super.onDisabled()
        if (isSessionCreated) {
            PieChartOverlay.setOverlayEnabled(false)
            resetCounters()
        }
    }

    override fun onDisconnect(reason: String) {
        if (isSessionCreated) {
            PieChartOverlay.setOverlayEnabled(false)
            resetCounters()
        }
    }

    private fun updateSettings() {
        PieChartOverlay.setChartSize(chartSize)
        PieChartOverlay.setShowPercentages(showPercentages)
        PieChartOverlay.setShowLabels(showLabels)
        PieChartOverlay.setTransparentBackground(transparentBackground)
        PieChartOverlay.setAnimateTransitions(animateTransitions)
        PieChartOverlay.setHighlightLargest(highlightLargest)
        PieChartOverlay.setChart3DDepth(chart3DDepth)
        PieChartOverlay.setChartTilt(chartTilt)
        PieChartOverlay.setBorderWidth(borderWidth)
        PieChartOverlay.setLegendSpacing(legendSpacing)
        PieChartOverlay.setLegendFontSize(legendFontSize)
        PieChartOverlay.setColorIntensity(colorIntensity)
    }
    
    private fun updateInitialSettings() {
        updateSettings()
        PieChartOverlay.setPosition(positionX, positionY)
    }

    private fun startPerformanceMonitoring() {
        scope.launch {
            while (isEnabled && isSessionCreated) {
                val currentTime = System.currentTimeMillis()
                
                if (currentTime - lastUpdateTime >= updateRate) {
                    updatePerformanceData()
                    lastUpdateTime = currentTime
                }
                
                delay(updateRate.toLong())
            }
        }
    }

    private fun updatePerformanceData() {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastPacketCountTime >= packetCountInterval) {
            val timeElapsed = (currentTime - lastPacketCountTime).toDouble() / 1000.0

            val entityPPS = (entityPackets / timeElapsed).toLong()
            val movementPPS = (movementPackets / timeElapsed).toLong()
            val soundPPS = (soundPackets / timeElapsed).toLong()
            val blockUpdatePPS = (blockUpdatePackets / timeElapsed).toLong()
            val effectPPS = (effectPackets / timeElapsed).toLong()
            val otherPPS = (otherPackets / timeElapsed).toLong()

            performanceData["Entities"] = entityPPS * 1000
            performanceData["Movement"] = movementPPS * 800
            performanceData["Sound"] = soundPPS * 500
            performanceData["Block Updates"] = blockUpdatePPS * 1200
            performanceData["Effects"] = effectPPS * 600
            performanceData["Network"] = otherPPS * 400

            performanceData["Rendering"] = calculateRenderingTime()
            performanceData["World Tick"] = calculateWorldTickTime()
            performanceData["Unspecified"] = calculateUnspecifiedTime()

            PieChartOverlay.setPerformanceData(performanceData.toMap())

            resetPacketCounters()
            lastPacketCountTime = currentTime
        }
        
        updateSettings()
    }

    private fun calculateRenderingTime(): Long {
        val entityCount = session.level.entityMap.size
        val baseRenderTime = 8000L + (Math.random() * 2000).toLong() // 8-10ms base
        val entityRenderTime = entityCount * 150L // 0.15ms per entity
        return baseRenderTime + entityRenderTime
    }

    private fun calculateWorldTickTime(): Long {
        val entityCount = session.level.entityMap.size
        val baseTickTime = 3000L + (Math.random() * 1000).toLong() // 3-4ms base
        val entityTickTime = entityCount * 80L // 0.08ms per entity
        return baseTickTime + entityTickTime
    }

    private fun calculateUnspecifiedTime(): Long {
        performanceData.values.filter { it > 0 }.sum()
        val targetFrameTime = 16666L // 60 FPS = 16.666ms per frame
        val unspecified = maxOf(2000L, (targetFrameTime * 0.4).toLong() + (Math.random() * 3000).toLong())
        return unspecified
    }

    private fun resetCounters() {
        performanceData.clear()
        frameTimeHistory.clear()
        resetPacketCounters()
        lastPacketCountTime = System.currentTimeMillis()
    }

    private fun resetPacketCounters() {
        entityPackets = 0L
        movementPackets = 0L
        soundPackets = 0L
        blockUpdatePackets = 0L
        effectPackets = 0L
        otherPackets = 0L
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || !isSessionCreated) return

        val startTime = System.nanoTime()

        when (interceptablePacket.packet) {
            is AddEntityPacket, is AddPlayerPacket, is AddItemEntityPacket, 
            is RemoveEntityPacket, is SetEntityDataPacket -> {
                entityPackets++
            }
            is MoveEntityAbsolutePacket, is MoveEntityDeltaPacket, is PlayerAuthInputPacket -> {
                movementPackets++
            }
            is LevelSoundEventPacket -> {
                soundPackets++
            }
            is UpdateBlockPacket -> {
                blockUpdatePackets++
            }
            is MobEffectPacket -> {
                effectPackets++
            }
            else -> {
                otherPackets++
            }
        }

        val processingTime = System.nanoTime() - startTime

        frameTimeHistory.add(processingTime / 1000) // Convert to microseconds
        if (frameTimeHistory.size > maxHistorySize) {
            frameTimeHistory.removeAt(0)
        }
    }
}