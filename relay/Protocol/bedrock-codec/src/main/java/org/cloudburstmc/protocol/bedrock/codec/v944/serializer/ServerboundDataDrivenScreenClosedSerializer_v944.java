package org.cloudburstmc.protocol.bedrock.codec.v944.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.packet.ServerboundDataDrivenScreenClosedPacket;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerboundDataDrivenScreenClosedSerializer_v944 implements BedrockPacketSerializer<ServerboundDataDrivenScreenClosedPacket> {

    public static final ServerboundDataDrivenScreenClosedSerializer_v944 INSTANCE = new ServerboundDataDrivenScreenClosedSerializer_v944();

    private static final List<String> CLOSE_REASONS = Arrays.asList("programmaticclose", "programmaticcloseall", "clientcanceled", "userbusy", "invalidform");

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, ServerboundDataDrivenScreenClosedPacket packet) {
        buffer.writeIntLE(packet.getFormId());
        //helper.writeOptionalNull(buffer, packet.getFormId(), ByteBuf::writeIntLE);
        helper.writeString(buffer, CLOSE_REASONS.get(packet.getCloseReason().ordinal()));
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, ServerboundDataDrivenScreenClosedPacket packet) {
        packet.setFormId((int) buffer.readUnsignedIntLE());
        //Long id = helper.readOptional(buffer, null, ByteBuf::readUnsignedIntLE);
        //packet.setFormId(id == null ? null : id.intValue());
        packet.setCloseReason(ServerboundDataDrivenScreenClosedPacket.CloseReason.values()[CLOSE_REASONS.indexOf(helper.readString(buffer))]);
    }
}
