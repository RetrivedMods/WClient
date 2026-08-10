package org.cloudburstmc.protocol.bedrock.codec.v1001.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v712.serializer.MobArmorEquipmentSerializer_v712;
import org.cloudburstmc.protocol.bedrock.packet.MobArmorEquipmentPacket;
import org.cloudburstmc.protocol.common.util.VarInts;

public class MobArmorEquipmentSerializer_v1001 extends MobArmorEquipmentSerializer_v712 {

    public static final MobArmorEquipmentSerializer_v1001 INSTANCE = new MobArmorEquipmentSerializer_v1001();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, MobArmorEquipmentPacket packet) {
        VarInts.writeUnsignedLong(buffer, packet.getRuntimeEntityId());
        helper.writeNetworkItemStackDescriptor(buffer, packet.getHelmet());
        helper.writeNetworkItemStackDescriptor(buffer, packet.getChestplate());
        helper.writeNetworkItemStackDescriptor(buffer, packet.getLeggings());
        helper.writeNetworkItemStackDescriptor(buffer, packet.getBoots());
        helper.writeNetworkItemStackDescriptor(buffer, packet.getBody());
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, MobArmorEquipmentPacket packet) {
        packet.setRuntimeEntityId(VarInts.readUnsignedLong(buffer));
        packet.setHelmet(helper.readNetworkItemStackDescriptor(buffer));
        packet.setChestplate(helper.readNetworkItemStackDescriptor(buffer));
        packet.setLeggings(helper.readNetworkItemStackDescriptor(buffer));
        packet.setBoots(helper.readNetworkItemStackDescriptor(buffer));
        packet.setBody(helper.readNetworkItemStackDescriptor(buffer));
    }
}
