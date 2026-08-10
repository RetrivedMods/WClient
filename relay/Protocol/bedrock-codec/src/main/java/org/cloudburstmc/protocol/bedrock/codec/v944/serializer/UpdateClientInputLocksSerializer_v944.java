package org.cloudburstmc.protocol.bedrock.codec.v944.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v560.serializer.UpdateClientInputLocksSerializer_v560;
import org.cloudburstmc.protocol.bedrock.packet.UpdateClientInputLocksPacket;
import org.cloudburstmc.protocol.common.util.VarInts;

public class UpdateClientInputLocksSerializer_v944 extends UpdateClientInputLocksSerializer_v560 {

    public static final UpdateClientInputLocksSerializer_v944 INSTANCE = new UpdateClientInputLocksSerializer_v944();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, UpdateClientInputLocksPacket packet) {
        VarInts.writeUnsignedInt(buffer, packet.getLockComponentData());
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, UpdateClientInputLocksPacket packet) {
        packet.setLockComponentData(VarInts.readUnsignedInt(buffer));
    }
}
