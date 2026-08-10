package org.cloudburstmc.protocol.bedrock.codec.v975.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v712.serializer.DisconnectSerializer_v712;
import org.cloudburstmc.protocol.bedrock.data.DisconnectFailReason;
import org.cloudburstmc.protocol.bedrock.packet.DisconnectPacket;
import org.cloudburstmc.protocol.common.util.TextConverter;
import org.cloudburstmc.protocol.common.util.VarInts;

public class DisconnectSerializer_v975 extends DisconnectSerializer_v712 {

    public static final DisconnectSerializer_v975 INSTANCE = new DisconnectSerializer_v975();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, DisconnectPacket packet) {
        VarInts.writeInt(buffer, packet.getReason().ordinal());
        VarInts.writeUnsignedInt(buffer, packet.isMessageSkipped() ? 1 : 0); //oneOf<DisconnectPacketMessages, null>
        if (!packet.isMessageSkipped()) {
            TextConverter converter = helper.getTextConverter();
            helper.writeString(buffer, converter.serialize(packet.getKickMessage(CharSequence.class)));
            helper.writeString(buffer, converter.serialize(packet.getFilteredMessage(CharSequence.class)));
        }
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, DisconnectPacket packet) {
        packet.setReason(DisconnectFailReason.values()[VarInts.readInt(buffer)]);
        packet.setMessageSkipped(VarInts.readUnsignedInt(buffer) != 0);
        if (!packet.isMessageSkipped()) {
            TextConverter converter = helper.getTextConverter();
            packet.setKickMessage(converter.deserialize(helper.readString(buffer)));
            packet.setFilteredMessage(converter.deserialize(helper.readString(buffer)));
        }
    }
}
