package org.cloudburstmc.protocol.bedrock.codec.v944.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.packet.ClientboundDataDrivenUICloseScreenPacket;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ClientboundDataDrivenUICloseScreenSerializer_v944 implements BedrockPacketSerializer<ClientboundDataDrivenUICloseScreenPacket> {

    public static final ClientboundDataDrivenUICloseScreenSerializer_v944 INSTANCE = new ClientboundDataDrivenUICloseScreenSerializer_v944();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, ClientboundDataDrivenUICloseScreenPacket packet) {
        helper.writeOptionalNull(buffer, packet.getFormId(), ByteBuf::writeIntLE);
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, ClientboundDataDrivenUICloseScreenPacket packet) {
        packet.setFormId(helper.readOptional(buffer, null, (buf, h) -> buf.readIntLE()));
    }
}
