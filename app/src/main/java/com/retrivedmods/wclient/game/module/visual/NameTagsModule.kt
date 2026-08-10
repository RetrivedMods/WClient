package com.retrivedmods.wclient.game.module.visual

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class NameTagsModule : Module("name_tags", ModuleCategory.Visual) {
    private val mode by enumValue("Mode", Mode.SHADOW, Mode::class.java)
    private val scale by floatValue("Scale", 1.0f, 0.5f..3.0f)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[NameTags] enabled") }

    enum class Mode { SHADOW, HEALTH, BOTH }
}
