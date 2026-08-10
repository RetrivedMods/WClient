package org.cloudburstmc.protocol.bedrock.codec.v1001.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v748.serializer.InventoryContentSerializer_v748;
import org.cloudburstmc.protocol.bedrock.packet.InventoryContentPacket;
import org.cloudburstmc.protocol.common.util.VarInts;

public class InventoryContentSerializer_v1001 extends InventoryContentSerializer_v748 {

    public static final InventoryContentSerializer_v1001 INSTANCE = new InventoryContentSerializer_v1001();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, InventoryContentPacket packet) {
        VarInts.writeUnsignedInt(buffer, packet.getContainerId());
        helper.writeArray(buffer, packet.getContents(), helper::writeNetworkItemStackDescriptor);
        helper.writeFullContainerName(buffer, packet.getContainerNameData());
        helper.writeNetworkItemStackDescriptor(buffer, packet.getStorageItem());
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, InventoryContentPacket packet) {
        packet.setContainerId(VarInts.readUnsignedInt(buffer));
        helper.readArray(buffer, packet.getContents(), helper::readNetworkItemStackDescriptor);
        packet.setContainerNameData(helper.readFullContainerName(buffer));
        packet.setStorageItem(helper.readNetworkItemStackDescriptor(buffer));
    }
}
