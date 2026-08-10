package org.cloudburstmc.protocol.bedrock.codec.v924.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v712.serializer.ServerboundDiagnosticsSerializer_v712;
import org.cloudburstmc.protocol.bedrock.data.diagnostics.MemoryCategoryCounter;
import org.cloudburstmc.protocol.bedrock.packet.ServerboundDiagnosticsPacket;

public class ServerboundDiagnosticsSerializer_v924 extends ServerboundDiagnosticsSerializer_v712 {

    public static final ServerboundDiagnosticsSerializer_v924 INSTANCE = new ServerboundDiagnosticsSerializer_v924();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, ServerboundDiagnosticsPacket packet) {
        super.serialize(buffer, helper, packet);

        helper.writeArray(buffer, packet.getMemoryCategoryValues(), ((buf, h, counter) -> {
            buf.writeByte(counter.getCategory().ordinal());
            buf.writeLongLE(counter.getCurrentBytes());
        }));
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, ServerboundDiagnosticsPacket packet) {
        super.deserialize(buffer, helper, packet);

        helper.readArray(buffer, packet.getMemoryCategoryValues(), (buf, h) ->
                new MemoryCategoryCounter(MemoryCategoryCounter.Category.values()[buf.readUnsignedByte()], buffer.readLongLE()));
    }
}
