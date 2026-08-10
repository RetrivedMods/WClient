package org.cloudburstmc.protocol.bedrock.codec.v975.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.packet.InventorySlotPacket;
import org.cloudburstmc.protocol.common.util.VarInts;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class InventorySlotSerializer_v975 implements BedrockPacketSerializer<InventorySlotPacket> {

    public static final InventorySlotSerializer_v975 INSTANCE = new InventorySlotSerializer_v975();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, InventorySlotPacket packet) {
        VarInts.writeUnsignedInt(buffer, packet.getContainerId());
        VarInts.writeUnsignedInt(buffer, packet.getSlot());
        helper.writeOptionalNull(buffer, packet.getContainerNameData(), helper::writeFullContainerName);
        helper.writeOptionalNull(buffer, packet.getStorageItem(), helper::writeNetworkItemStackDescriptor);
        helper.writeNetworkItemStackDescriptor(buffer, packet.getItem());
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, InventorySlotPacket packet) {
        packet.setContainerId(VarInts.readUnsignedInt(buffer));
        packet.setSlot(VarInts.readUnsignedInt(buffer));
        packet.setContainerNameData(helper.readOptional(buffer, null, helper::readFullContainerName));
        packet.setStorageItem(helper.readOptional(buffer, null, helper::readNetworkItemStackDescriptor));
        packet.setItem(helper.readNetworkItemStackDescriptor(buffer));
    }
}