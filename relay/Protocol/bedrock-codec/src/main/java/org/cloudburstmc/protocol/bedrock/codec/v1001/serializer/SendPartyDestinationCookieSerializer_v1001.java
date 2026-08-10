package org.cloudburstmc.protocol.bedrock.codec.v1001.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.packet.SendPartyDestinationCookiePacket;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class SendPartyDestinationCookieSerializer_v1001 implements BedrockPacketSerializer<SendPartyDestinationCookiePacket> {

    public static final SendPartyDestinationCookieSerializer_v1001 INSTANCE = new SendPartyDestinationCookieSerializer_v1001();

    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, SendPartyDestinationCookiePacket packet) {
        helper.writeString(buffer, packet.getCookie());
        helper.writeString(buffer, packet.getIntent().getSerializeName());
        helper.writeString(buffer, packet.getDestinationName());
    }

    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, SendPartyDestinationCookiePacket packet) {
        packet.setCookie(helper.readString(buffer));
        packet.setIntent(SendPartyDestinationCookiePacket.Intent.fromName(helper.readString(buffer)));
        packet.setDestinationName(helper.readString(buffer));
    }
}
