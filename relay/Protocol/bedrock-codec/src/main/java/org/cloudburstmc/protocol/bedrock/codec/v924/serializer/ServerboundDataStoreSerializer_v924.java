package org.cloudburstmc.protocol.bedrock.codec.v924.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v898.serializer.ServerboundDataStoreSerializer_v898;
import org.cloudburstmc.protocol.bedrock.packet.ServerboundDataStorePacket;

public class ServerboundDataStoreSerializer_v924 extends ServerboundDataStoreSerializer_v898 {

    public static final ServerboundDataStoreSerializer_v924 INSTANCE = new ServerboundDataStoreSerializer_v924();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, ServerboundDataStorePacket packet) {
        super.serialize(buffer, helper, packet);

        buffer.writeIntLE(packet.getUpdate().getPathUpdateCount());
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, ServerboundDataStorePacket packet) {
        super.deserialize(buffer, helper, packet);

        packet.getUpdate().setPathUpdateCount((int) buffer.readUnsignedIntLE());
    }
}
