package org.cloudburstmc.protocol.bedrock.codec.v924.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.data.SerializableVoxelShape;
import org.cloudburstmc.protocol.bedrock.packet.VoxelShapesPacket;
import org.cloudburstmc.protocol.common.util.VarInts;

import java.util.*;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class VoxelShapesSerializer_v924 implements BedrockPacketSerializer<VoxelShapesPacket> {

    public static final VoxelShapesSerializer_v924 INSTANCE = new VoxelShapesSerializer_v924();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, VoxelShapesPacket packet) {
        helper.writeArray(buffer, packet.getShapes(), (buf, shape) -> {
            buf.writeByte(shape.getCells().getXSize());
            buf.writeByte(shape.getCells().getYSize());
            buf.writeByte(shape.getCells().getZSize());

            helper.writeArray(buf, shape.getCells().getStorage(), (buf2, value) -> buf2.writeByte(value));

            helper.writeArray(buf, shape.getXCoordinates(), ByteBuf::writeFloatLE);
            helper.writeArray(buf, shape.getYCoordinates(), ByteBuf::writeFloatLE);
            helper.writeArray(buf, shape.getZCoordinates(), ByteBuf::writeFloatLE);
        });

        VarInts.writeUnsignedInt(buffer, packet.getNameMap().size());
        packet.getNameMap().forEach((k, v) -> {
            helper.writeString(buffer, k);
            buffer.writeShortLE(v);
        });
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, VoxelShapesPacket packet) {
        List<SerializableVoxelShape> shapes = new ArrayList<>();

        helper.readArray(buffer, shapes, (buf, h) -> {
            short xSize = buf.readUnsignedByte();
            short ySize = buf.readUnsignedByte();
            short zSize = buf.readUnsignedByte();

            List<Short> storage = new ArrayList<>();
            helper.readArray(buf, storage, (ByteBuf::readUnsignedByte));

            SerializableVoxelShape.SerializableCells cells = new SerializableVoxelShape.SerializableCells(xSize, ySize, zSize, storage);

            List<Float> xCoordinates = new ArrayList<>();
            helper.readArray(buf, xCoordinates, (ByteBuf::readFloatLE));

            List<Float> yCoordinates = new ArrayList<>();
            helper.readArray(buf, yCoordinates, (ByteBuf::readFloatLE));

            List<Float> zCoordinates = new ArrayList<>();
            helper.readArray(buf, zCoordinates, (ByteBuf::readFloatLE));

            return new SerializableVoxelShape(cells, xCoordinates, yCoordinates, zCoordinates);
        });

        packet.setShapes(shapes);

        Map<String, Integer> nameMap = new LinkedHashMap<>();

        int size = VarInts.readUnsignedInt(buffer);
        for (int i = 0; i < size; i++) {
            nameMap.put(helper.readString(buffer), buffer.readUnsignedShortLE());
        }

        packet.setNameMap(nameMap);
    }
}
