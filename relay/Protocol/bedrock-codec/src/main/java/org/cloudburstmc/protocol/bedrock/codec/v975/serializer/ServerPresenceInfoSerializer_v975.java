package org.cloudburstmc.protocol.bedrock.codec.v975.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.packet.ServerPresenceInfoPacket;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerPresenceInfoSerializer_v975 implements BedrockPacketSerializer<ServerPresenceInfoPacket> {

    public static final ServerPresenceInfoSerializer_v975 INSTANCE = new ServerPresenceInfoSerializer_v975();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, ServerPresenceInfoPacket packet) {
        helper.writeOptionalNull(buffer, packet.getPresenceConfiguration(), helper::writePresenceConfiguration);
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, ServerPresenceInfoPacket packet) {
        packet.setPresenceConfiguration(helper.readOptional(buffer, null, helper::readPresenceConfiguration));
    }
}
