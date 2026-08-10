package org.cloudburstmc.protocol.bedrock.codec.v1001.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v486.serializer.SubChunkRequestSerializer_v486;
import org.cloudburstmc.protocol.bedrock.packet.SubChunkRequestPacket;
import org.cloudburstmc.protocol.common.util.VarInts;

public class SubChunkRequestSerializer_v1001 extends SubChunkRequestSerializer_v486 {

    public static final SubChunkRequestSerializer_v1001 INSTANCE = new SubChunkRequestSerializer_v1001();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, SubChunkRequestPacket packet) {
        VarInts.writeInt(buffer, packet.getDimension());
        helper.writeArray(buffer, packet.getPositionOffsets(), this::writeSubChunkOffset);
        writeSubChunkPos(buffer, packet.getSubChunkPosition());
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, SubChunkRequestPacket packet) {
        packet.setDimension(VarInts.readInt(buffer));
        helper.readArray(buffer, packet.getPositionOffsets(), this::readSubChunkOffset, 8192);
        packet.setSubChunkPosition(readSubChunkPos(buffer));
    }

    protected void writeSubChunkPos(ByteBuf buffer, Vector3i offsetPosition) {
        buffer.writeIntLE(offsetPosition.getX());
        buffer.writeIntLE(offsetPosition.getY());
        buffer.writeIntLE(offsetPosition.getZ());
    }

    protected Vector3i readSubChunkPos(ByteBuf buffer) {
        return Vector3i.from(buffer.readIntLE(), buffer.readIntLE(), buffer.readIntLE());
    }
}
