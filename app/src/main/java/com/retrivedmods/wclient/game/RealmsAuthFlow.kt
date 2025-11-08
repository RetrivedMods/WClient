package com.retrivedmods.wclient.game

import net.raphimc.minecraftauth.MinecraftAuth
import net.raphimc.minecraftauth.step.AbstractStep
import net.raphimc.minecraftauth.step.bedrock.session.StepFullBedrockSession
import net.raphimc.minecraftauth.util.MicrosoftConstants

object RealmsAuthFlow {

    val BEDROCK_DEVICE_CODE_LOGIN_WITH_REALMS: AbstractStep<*, StepFullBedrockSession.FullBedrockSession> = 
        MinecraftAuth.builder()
            .withClientId(MicrosoftConstants.BEDROCK_ANDROID_TITLE_ID)
            .withScope(MicrosoftConstants.SCOPE_TITLE_AUTH)
            .deviceCode()
            .withDeviceToken("Android")
            .sisuTitleAuthentication(MicrosoftConstants.BEDROCK_XSTS_RELYING_PARTY)
            .buildMinecraftBedrockChainStep(true, true)

}