package org.cloudburstmc.protocol.bedrock.codec.v975.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v291.serializer.PlaySoundSerializer_v291;
import org.cloudburstmc.protocol.bedrock.packet.PlaySoundPacket;

public class PlaySoundSerializer_v975 extends PlaySoundSerializer_v291 {

    public static final PlaySoundSerializer_v975 INSTANCE = new PlaySoundSerializer_v975();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, PlaySoundPacket packet) {
        super.serialize(buffer, helper, packet);
        helper.writeOptionalNull(buffer, packet.getServerSoundHandle(), ByteBuf::writeLongLE);
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, PlaySoundPacket packet) {
        super.deserialize(buffer, helper, packet);
        packet.setServerSoundHandle(helper.readOptional(buffer, null, ByteBuf::readLongLE));
    }
}
