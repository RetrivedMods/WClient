package org.cloudburstmc.protocol.bedrock.codec.v944.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v924.serializer.ClientboundDataDrivenUIShowScreenSerializer_v924;
import org.cloudburstmc.protocol.bedrock.packet.ClientboundDataDrivenUIShowScreenPacket;

public class ClientboundDataDrivenUIShowScreenSerializer_v944 extends ClientboundDataDrivenUIShowScreenSerializer_v924 {

    public static final ClientboundDataDrivenUIShowScreenSerializer_v944 INSTANCE = new ClientboundDataDrivenUIShowScreenSerializer_v944();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, ClientboundDataDrivenUIShowScreenPacket packet) {
        super.serialize(buffer, helper, packet);

        buffer.writeIntLE(packet.getFormId());
        helper.writeOptionalNull(buffer, packet.getDataInstanceId(), ByteBuf::writeIntLE);
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, ClientboundDataDrivenUIShowScreenPacket packet) {
        super.deserialize(buffer, helper, packet);

        packet.setFormId((int) buffer.readUnsignedIntLE());
        Long id = helper.readOptional(buffer, null, ByteBuf::readUnsignedIntLE);
        packet.setDataInstanceId(id == null ? null : id.intValue());
    }
}
