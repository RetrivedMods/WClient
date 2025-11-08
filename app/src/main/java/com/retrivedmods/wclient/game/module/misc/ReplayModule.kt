package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.application.AppContext
import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.*
import org.cloudburstmc.math.vector.Vector3f
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import java.io.File
import kotlin.concurrent.thread

class ReplayModule : Module("replay_mod", ModuleCategory.Misc) {
    private val recordingInterval by intValue("interval", 50, 20..200)
    private val autoSave by boolValue("auto_save", true)
    private val playbackSpeed by floatValue("playback_speed", 1.0f, 0.1f..5.0f)
    private val recordInputs by boolValue("record_inputs", true)
    private val smoothPlayback by boolValue("smooth", true)

    @Serializable
    private data class ReplayFrame(
        val position: Vector3fData,
        val rotation: Vector3fData,
        val inputs: Set<String>,
        val timestamp: Long
    )

    @Serializable
    private data class Vector3fData(
        val x: Float,
        val y: Float,
        val z: Float
    )

    @Serializable
    private data class ReplayMetadata(
        val version: Int = 1,
        val recordedAt: Long = System.currentTimeMillis(),
        val frameCount: Int,
        val duration: Long
    )

    @Serializable
    private data class ReplayData(
        val metadata: ReplayMetadata,
        val frames: List<ReplayFrame>
    )

    private var isRecording = false
    private var isPlaying = false
    private val frames = mutableListOf<ReplayFrame>()
    private var recordingStartTime = 0L
    private var lastRecordTime = 0L
    private var playbackThread: Thread? = null
    private var originalPosition: Vector3f? = null

    // Interpolation support
    private var lastFrame: ReplayFrame? = null
    private var nextFrame: ReplayFrame? = null
    private var interpolationProgress = 0f

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            if (isRecording) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastRecordTime >= recordingInterval) {
                    frames.add(
                        ReplayFrame(
                            Vector3fData(
                                packet.position.x,
                                packet.position.y,
                                packet.position.z
                            ),
                            Vector3fData(
                                packet.rotation.x,
                                packet.rotation.y,
                                packet.rotation.z
                            ),
                            packet.inputData.map { it.name }.toSet(),
                            currentTime - recordingStartTime
                        )
                    )
                    lastRecordTime = currentTime
                }
            }

            if (isPlaying) {
                interceptablePacket.intercept()
            }
        }
    }

    override fun onEnabled() {
        if (!isSessionCreated) {
            isEnabled = false
            return
        }

        super.onEnabled()
        session.displayClientMessage(
            """
            §l§b[Replay] §r§7Commands:
            §f.replay record §7- Start recording
            §f.replay play §7- Play last recording
            §f.replay stop §7- Stop recording/playback
            §f.replay save <name> §7- Save recording
            §f.replay load <name> §7- Load recording
            """.trimIndent()
        )
    }

    fun startRecording() {
        if (isPlaying) {
            session.displayClientMessage("§cCannot start recording while playing")
            return
        }

        frames.clear()
        isRecording = true
        recordingStartTime = System.currentTimeMillis()
        lastRecordTime = recordingStartTime

        // Store initial position
        originalPosition = session.localPlayer.vec3Position

        session.displayClientMessage("§aStarted recording movement")
    }

    fun stopRecording() {
        if (!isRecording) return

        isRecording = false
        if (autoSave) {
            saveReplay("replay_${System.currentTimeMillis()}")
        }
        session.displayClientMessage("§cStopped recording (${frames.size} frames)")
    }

    fun startPlayback() {
        if (isRecording) {
            session.displayClientMessage("§cCannot start playback while recording")
            return
        }

        if (frames.isEmpty()) {
            session.displayClientMessage("§cNo frames recorded")
            return
        }

        isPlaying = true

        // Store current position before playback
        originalPosition = session.localPlayer.vec3Position

        playbackThread = thread(name = "ReplayPlayback") {
            try {
                frames.forEachIndexed { index, frame ->
                    if (!isPlaying) return@thread

                    val delay = if (index < frames.size - 1) {
                        ((frames[index + 1].timestamp - frame.timestamp) / playbackSpeed).toLong()
                    } else 0L

                    if (smoothPlayback && index < frames.size - 1) {
                        lastFrame = frame
                        nextFrame = frames[index + 1]
                        interpolationProgress = 0f

                        while (interpolationProgress < 1f) {
                            if (!isPlaying) return@thread

                            val interpolatedPosition = interpolatePosition(lastFrame!!, nextFrame!!, interpolationProgress)
                            session.clientBound(MovePlayerPacket().apply {
                                runtimeEntityId = session.localPlayer.runtimeEntityId
                                position = interpolatedPosition
                                rotation = Vector3f.from(
                                    frame.rotation.x,
                                    frame.rotation.y,
                                    frame.rotation.z
                                )
                                mode = MovePlayerPacket.Mode.NORMAL
                            })

                            interpolationProgress += 0.1f
                            Thread.sleep((delay / 10).coerceAtLeast(1))
                        }
                    } else {
                        playFrame(frame)
                        Thread.sleep(delay)
                    }
                }
            } catch (e: InterruptedException) {
                // Playback interrupted
            } finally {
                isPlaying = false
                // Restore original position
                originalPosition?.let { pos ->
                    session.clientBound(MovePlayerPacket().apply {
                        runtimeEntityId = session.localPlayer.runtimeEntityId
                        position = pos
                        rotation = session.localPlayer.vec3Rotation
                        mode = MovePlayerPacket.Mode.NORMAL
                    })
                }
                session.displayClientMessage("§eReplay finished")
            }
        }
    }

    fun stopPlayback() {
        if (!isPlaying) return

        isPlaying = false
        playbackThread?.interrupt()
        playbackThread = null

        // Restore original position
        originalPosition?.let { pos ->
            session.clientBound(MovePlayerPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                position = pos
                rotation = session.localPlayer.vec3Rotation
                mode = MovePlayerPacket.Mode.NORMAL
            })
        }
        session.displayClientMessage("§cPlayback stopped")
    }

    fun saveReplay(name: String) {
        if (frames.isEmpty()) {
            session.displayClientMessage("§cNo frames to save")
            return
        }

        try {
            val replayDir = File(AppContext.instance.filesDir, "replays").apply { mkdirs() }
            val file = File(replayDir, "$name.json")

            val metadata = ReplayMetadata(
                frameCount = frames.size,
                duration = frames.last().timestamp
            )

            val replayData = ReplayData(metadata, frames)
            file.writeText(Json.encodeToString(replayData))

            session.displayClientMessage(
                "§aSaved replay §f$name §7(${metadata.frameCount} frames, ${metadata.duration / 1000}s)"
            )
        } catch (e: Exception) {
            session.displayClientMessage("§cFailed to save replay: ${e.message}")
        }
    }

    fun loadReplay(name: String) {
        try {
            val file = File(File(AppContext.instance.filesDir, "replays"), "$name.json")
            if (!file.exists()) {
                session.displayClientMessage("§cReplay file not found")
                return
            }

            val replayData = Json.decodeFromString<ReplayData>(file.readText())
            frames.clear()
            frames.addAll(replayData.frames)
            session.displayClientMessage("§aLoaded replay with ${frames.size} frames")
        } catch (e: Exception) {
            session.displayClientMessage("§cFailed to load replay: ${e.message}")
        }
    }

    private fun interpolatePosition(from: ReplayFrame, to: ReplayFrame, progress: Float): Vector3f {
        return Vector3f.from(
            lerp(from.position.x, to.position.x, progress),
            lerp(from.position.y, to.position.y, progress),
            lerp(from.position.z, to.position.z, progress)
        )
    }

    private fun lerp(start: Float, end: Float, progress: Float): Float {
        return start + (end - start) * progress
    }

    private fun playFrame(frame: ReplayFrame) {
        session.clientBound(SetEntityMotionPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            motion = Vector3f.from(
                frame.position.x,
                frame.position.y,
                frame.position.z
            )
        })

        session.clientBound(MovePlayerPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            position = Vector3f.from(
                frame.position.x,
                frame.position.y,
                frame.position.z
            )
            rotation = Vector3f.from(
                frame.rotation.x,
                frame.rotation.y,
                frame.rotation.z
            )
            mode = MovePlayerPacket.Mode.NORMAL
        })

        if (recordInputs) {
            session.clientBound(PlayerAuthInputPacket().apply {
                position = Vector3f.from(
                    frame.position.x,
                    frame.position.y,
                    frame.position.z
                )
                rotation = Vector3f.from(
                    frame.rotation.x,
                    frame.rotation.y,
                    frame.rotation.z
                )
                val inputs = frame.inputs.mapNotNull { inputName ->
                    try {
                        PlayerAuthInputData.valueOf(inputName)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }
                inputData.addAll(inputs)
            })
        }
    }

    override fun onDisabled() {
        super.onDisabled()
        stopRecording()
        stopPlayback()
    }
}