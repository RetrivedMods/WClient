package com.retrivedmods.wclient.game.module.misc

import android.content.Intent
import com.retrivedmods.wclient.application.AppContext
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

/**
 * TexturePackChanger: Instant-action module. When toggled it launches the system picker for
 * .mcpack/.mcaddon/.mcworld files (mime hints provided). It disables itself immediately to act
 * like a one-shot button.
 */
class TexturePackChanger : Module("TexturePackChanger", ModuleCategory.Misc, defaultEnabled = false) {

    override fun onEnabled() {
        super.onEnabled()
        try {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                    "application/x-minecraftpack",
                    "application/x-minecraft-resourcepack",
                    "application/x-minecraft-addon",
                    "application/octet-stream"
                ))
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            AppContext.instance.startActivity(intent)
        } catch (t: Throwable) {
            // safe swallow; optional logging if you want
        } finally {
            // behave as a one-shot button
            setEnabled(false)
        }
    }
}
