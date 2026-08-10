package org.cloudburstmc.protocol.bedrock.codec.v924.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.packet.ClientboundDataDrivenUIShowScreenPacket;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ClientboundDataDrivenUIShowScreenSerializer_v924 implements BedrockPacketSerializer<ClientboundDataDrivenUIShowScreenPacket> {

    public static final ClientboundDataDrivenUIShowScreenSerializer_v924 INSTANCE = new ClientboundDataDrivenUIShowScreenSerializer_v924();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, ClientboundDataDrivenUIShowScreenPacket packet) {
        helper.writeString(buffer, packet.getScreenId());
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, ClientboundDataDrivenUIShowScreenPacket packet) {
        packet.setScreenId(helper.readString(buffer));
    }
}
