package org.cloudburstmc.protocol.bedrock.codec.v924.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.packet.ClientboundTextureShiftPacket;
import org.cloudburstmc.protocol.common.util.VarInts;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ClientboundTextureShiftSerializer_v924 implements BedrockPacketSerializer<ClientboundTextureShiftPacket> {

    public static final ClientboundTextureShiftSerializer_v924 INSTANCE = new ClientboundTextureShiftSerializer_v924();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, ClientboundTextureShiftPacket packet) {
        buffer.writeByte(packet.getAction().ordinal());
        helper.writeString(buffer, packet.getCollectionName());
        helper.writeString(buffer, packet.getFromStep());
        helper.writeString(buffer, packet.getToStep());
        helper.writeArray(buffer, packet.getAllSteps(), helper::writeString);
        VarInts.writeUnsignedLong(buffer, packet.getCurrentLengthTicks());
        VarInts.writeUnsignedLong(buffer, packet.getTotalLengthTicks());
        buffer.writeBoolean(packet.isEnabled());
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, ClientboundTextureShiftPacket packet) {
        packet.setAction(ClientboundTextureShiftPacket.Action.values()[buffer.readUnsignedByte()]);
        packet.setCollectionName(helper.readString(buffer));
        packet.setFromStep(helper.readString(buffer));
        packet.setToStep(helper.readString(buffer));
        List<String> allSteps = new ArrayList<>();
        helper.readArray(buffer, allSteps, helper::readString);
        packet.setAllSteps(allSteps);
        packet.setCurrentLengthTicks(VarInts.readUnsignedLong(buffer));
        packet.setTotalLengthTicks(VarInts.readUnsignedLong(buffer));
        packet.setEnabled(buffer.readBoolean());
    }
}
