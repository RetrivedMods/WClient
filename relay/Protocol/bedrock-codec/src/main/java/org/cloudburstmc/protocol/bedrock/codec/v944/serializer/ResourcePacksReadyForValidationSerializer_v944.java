package org.cloudburstmc.protocol.bedrock.codec.v944.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.packet.ResourcePacksReadyForValidationPacket;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ResourcePacksReadyForValidationSerializer_v944 implements BedrockPacketSerializer<ResourcePacksReadyForValidationPacket> {

    public static final ResourcePacksReadyForValidationSerializer_v944 INSTANCE = new ResourcePacksReadyForValidationSerializer_v944();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, ResourcePacksReadyForValidationPacket packet) {
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, ResourcePacksReadyForValidationPacket packet) {
    }
}
