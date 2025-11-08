package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.overlay.hud.KeyStrokesOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

class KeyStrokesModule : Module("keystrokes", ModuleCategory.Misc) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isJumping = false
    private var isSneaking = false
    private var lastJump = 0L
    private var currentYaw = 0f
    private var lastPosition = Triple(0.0, 0.0, 0.0)
    private var lastTime = 0L
    private val motionThreshold = 0.02

    private val positionX by intValue("Position X", 90, -100..100)
    private val positionY by intValue("Position Y", 90, -100..100)
    private val keySize by intValue("Key Size", 40, 20..80)
    private val keySpacing by intValue("Key Spacing", 4, 0..20)
    private val showSneak by boolValue("Show Sneak", true)
    private val spacebarWidth by intValue("Spacebar Width", 130, 80..200)
    private val backgroundOpacity by floatValue("Background Opacity", 0.85f, 0.0f..1.0f)
    private val animationSpeed by intValue("Animation Speed", 80, 20..200)
    private val showBackground by boolValue("Show Background", true)
    private val roundedCorners by boolValue("Rounded Corners", true)

    override fun onEnabled() {
        super.onEnabled()
        if (isSessionCreated) {
            KeyStrokesOverlay.setOverlayEnabled(true)
            updateOverlaySettings()
        }
    }

    override fun onDisabled() {
        super.onDisabled()
        if (isSessionCreated) {
            KeyStrokesOverlay.setOverlayEnabled(false)
            scope.launch {
                val keys = if (showSneak) {
                    arrayOf("W", "A", "S", "D", "Space", "Shift")
                } else {
                    arrayOf("W", "A", "S", "D", "Space")
                }
                keys.forEach { key ->
                    KeyStrokesOverlay.setKeyState(key, false)
                }
            }
            isJumping = false
            isSneaking = false
        }
    }

    private fun updateOverlaySettings() {
        KeyStrokesOverlay.setPosition(positionX, positionY)
        KeyStrokesOverlay.setKeySize(keySize)
        KeyStrokesOverlay.setKeySpacing(keySpacing)
        KeyStrokesOverlay.setShowSneak(showSneak)
        KeyStrokesOverlay.setSpacebarWidth(spacebarWidth)
        KeyStrokesOverlay.setBackgroundOpacity(backgroundOpacity)
        KeyStrokesOverlay.setAnimationSpeed(animationSpeed)
        KeyStrokesOverlay.setShowBackground(showBackground)
        KeyStrokesOverlay.setRoundedCorners(roundedCorners)
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        when (val packet = interceptablePacket.packet) {
            is PlayerAuthInputPacket -> {
                val inputData = packet.inputData
                val currentTime = System.currentTimeMillis()
                val currentPosition = packet.position

                currentYaw = packet.rotation.y

                if (inputData.contains(PlayerAuthInputData.START_JUMPING)) {
                    isJumping = true
                    lastJump = currentTime
                } else if (isJumping && currentTime - lastJump > 500) {
                    isJumping = false
                }

                isSneaking = inputData.contains(PlayerAuthInputData.SNEAKING)

                if (lastTime > 0) {
                    val deltaTime = (currentTime - lastTime) / 1000.0

                    val motionX = (currentPosition.x - lastPosition.first) / deltaTime
                    val motionZ = (currentPosition.z - lastPosition.third) / deltaTime

                    val motionMagnitude = sqrt(motionX * motionX + motionZ * motionZ)
                    val keys = if (motionMagnitude > motionThreshold && deltaTime > 0) {
                        calculateMovementKeys(motionX, motionZ, currentYaw)
                    } else {
                        MovementKeys()
                    }

                    scope.launch {
                        KeyStrokesOverlay.setKeyState("W", keys.w)
                        KeyStrokesOverlay.setKeyState("A", keys.a)
                        KeyStrokesOverlay.setKeyState("S", keys.s)
                        KeyStrokesOverlay.setKeyState("D", keys.d)
                        KeyStrokesOverlay.setKeyState("Space", isJumping)
                        if (showSneak) {
                            KeyStrokesOverlay.setKeyState("Shift", isSneaking)
                        }
                    }
                }

                lastPosition = Triple(currentPosition.x.toDouble(), currentPosition.y.toDouble(), currentPosition.z.toDouble())
                lastTime = currentTime
            }
        }
    }

    private fun calculateMovementKeys(motionX: Double, motionZ: Double, yaw: Float): MovementKeys {
        val yawRad = Math.toRadians(yaw.toDouble())
        val motionAngle = atan2(-motionX, motionZ)
        var relativeAngle = motionAngle - yawRad
        while (relativeAngle > PI) relativeAngle -= 2 * PI
        while (relativeAngle < -PI) relativeAngle += 2 * PI
        val relativeDegrees = Math.toDegrees(relativeAngle)
        return when {
            relativeDegrees >= -45 && relativeDegrees <= 45 -> MovementKeys(w = true)
            relativeDegrees > 45 && relativeDegrees < 135 -> MovementKeys(d = true)
            abs(relativeDegrees) >= 135 -> MovementKeys(s = true)
            relativeDegrees > -135 && relativeDegrees < -45 -> MovementKeys(a = true)
            else -> MovementKeys()
        }
    }

    private data class MovementKeys(
        val w: Boolean = false,
        val a: Boolean = false,
        val s: Boolean = false,
        val d: Boolean = false
    )
}
