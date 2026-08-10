package org.cloudburstmc.protocol.bedrock.codec.v924.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.packet.ClientboundDataDrivenUIReloadPacket;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ClientboundDataDrivenUIReloadSerializer_v924 implements BedrockPacketSerializer<ClientboundDataDrivenUIReloadPacket> {

    public static final ClientboundDataDrivenUIReloadSerializer_v924 INSTANCE = new ClientboundDataDrivenUIReloadSerializer_v924();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, ClientboundDataDrivenUIReloadPacket packet) {

    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, ClientboundDataDrivenUIReloadPacket packet) {

    }
}
