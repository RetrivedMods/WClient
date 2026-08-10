package org.cloudburstmc.protocol.bedrock.codec.v944.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v924.serializer.VoxelShapesSerializer_v924;
import org.cloudburstmc.protocol.bedrock.packet.VoxelShapesPacket;

public class VoxelShapesSerializer_v944 extends VoxelShapesSerializer_v924 {

    public static final VoxelShapesSerializer_v944 INSTANCE = new VoxelShapesSerializer_v944();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, VoxelShapesPacket packet) {
        super.serialize(buffer, helper, packet);

        buffer.writeShortLE(packet.getCustomShapeCount());
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, VoxelShapesPacket packet) {
        super.deserialize(buffer, helper, packet);

        packet.setCustomShapeCount(buffer.readUnsignedShortLE());
    }
}
