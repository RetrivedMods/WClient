package com.retrivedmods.wclient.game.module.visual

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class ChunkBordersModule : Module("chunk_borders", ModuleCategory.Visual) {
    private val color by enumValue("Color", Mode.WHITE, Mode::class.java)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[ChunkBorders] enabled") }

    enum class Mode { WHITE, RED, GREEN, BLUE, YELLOW }
}
