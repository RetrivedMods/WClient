package org.cloudburstmc.protocol.bedrock.codec.v944.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.packet.PartyChangedPacket;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class PartyChangedSerializer_v944 implements BedrockPacketSerializer<PartyChangedPacket> {

    public static final PartyChangedSerializer_v944 INSTANCE = new PartyChangedSerializer_v944();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, PartyChangedPacket packet) {
        helper.writeOptionalNull(buffer, packet.getParty(), this::writeParty);
    }

    protected void writeParty(ByteBuf buffer, BedrockCodecHelper helper, PartyChangedPacket.PartyInfo info) {
        helper.writeString(buffer, info.getPartyId());
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, PartyChangedPacket packet) {
        packet.setParty(helper.readOptional(buffer, null, this::readParty));
    }

    protected PartyChangedPacket.PartyInfo readParty(ByteBuf buffer, BedrockCodecHelper helper) {
        return new PartyChangedPacket.PartyInfo(helper.readString(buffer), false);
    }
}
