package org.cloudburstmc.protocol.bedrock.codec.v1001.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v776.serializer.BossEventSerializer_v776;
import org.cloudburstmc.protocol.bedrock.packet.BossEventPacket;
import org.cloudburstmc.protocol.common.util.VarInts;

public class BossEventSerializer_v1001 extends BossEventSerializer_v776 {

    public static final BossEventSerializer_v1001 INSTANCE = new BossEventSerializer_v1001();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, BossEventPacket packet) {
        VarInts.writeLong(buffer, packet.getBossUniqueEntityId());
        VarInts.writeLong(buffer, packet.getPlayerUniqueEntityId());
        buffer.writeByte(packet.getAction().ordinal());
        helper.writeString(buffer, helper.getTextConverter().serialize(packet.getTitle(CharSequence.class)));
        helper.writeString(buffer, helper.getTextConverter().serialize(packet.getFilteredTitle(CharSequence.class)));
        buffer.writeFloatLE(packet.getHealthPercentage());
        buffer.writeByte(packet.getColor());
        buffer.writeByte(packet.getOverlay());
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, BossEventPacket packet) {
        packet.setBossUniqueEntityId(VarInts.readLong(buffer));
        packet.setPlayerUniqueEntityId(VarInts.readLong(buffer));
        packet.setAction(BossEventPacket.Action.values()[buffer.readUnsignedByte()]);
        packet.setTitle(helper.getTextConverter().deserialize(helper.readString(buffer)));
        packet.setFilteredTitle(helper.getTextConverter().deserialize(helper.readString(buffer)));
        packet.setHealthPercentage(buffer.readFloatLE());
        packet.setColor(buffer.readUnsignedByte());
        packet.setOverlay(buffer.readUnsignedByte());
    }
}
