package org.cloudburstmc.protocol.bedrock.codec.v975.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v786.serializer.UpdateClientOptionsSerializer_v786;
import org.cloudburstmc.protocol.bedrock.packet.UpdateClientOptionsPacket;

public class UpdateClientOptionsSerializer_v975 extends UpdateClientOptionsSerializer_v786 {

    public static final UpdateClientOptionsSerializer_v975 INSTANCE = new UpdateClientOptionsSerializer_v975();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, UpdateClientOptionsPacket packet) {
        super.serialize(buffer, helper, packet);
        helper.writeOptionalNull(buffer, packet.getFilterProfanityChange(), ByteBuf::writeBoolean);
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, UpdateClientOptionsPacket packet) {
        super.deserialize(buffer, helper, packet);
        packet.setFilterProfanityChange(helper.readOptional(buffer, null, ByteBuf::readBoolean));
    }
}
