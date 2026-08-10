package org.cloudburstmc.protocol.bedrock.codec.v924.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.packet.ClientboundDataDrivenUICloseScreenPacket;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ClientboundDataDrivenUICloseScreenSerializer_v924 implements BedrockPacketSerializer<ClientboundDataDrivenUICloseScreenPacket> {

    public static final ClientboundDataDrivenUICloseScreenSerializer_v924 INSTANCE = new ClientboundDataDrivenUICloseScreenSerializer_v924();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, ClientboundDataDrivenUICloseScreenPacket packet) {

    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, ClientboundDataDrivenUICloseScreenPacket packet) {

    }
}
