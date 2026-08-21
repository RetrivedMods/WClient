package com.retrivedmods.wclient.game

import net.raphimc.minecraftauth.msa.data.MsaConstants
import net.raphimc.minecraftauth.msa.model.MsaApplicationConfig

object RealmsAuthFlow {

    // Bedrock Android title ID + the "title auth" scope - same values the old MinecraftAuth 4.x
    // MicrosoftConstants.BEDROCK_ANDROID_TITLE_ID / SCOPE_TITLE_AUTH pointed to. Using a "title"
    // client ID is what lets BedrockAuthManager also fetch a Realms XSTS token later, on demand -
    // in 5.x there's no separate "Realms-capable chain" to build up front like there was in 4.x.
    val BEDROCK_ANDROID_APPLICATION_CONFIG: MsaApplicationConfig =
        MsaApplicationConfig(MsaConstants.BEDROCK_ANDROID_TITLE_ID, MsaConstants.SCOPE_TITLE_AUTH)

}
