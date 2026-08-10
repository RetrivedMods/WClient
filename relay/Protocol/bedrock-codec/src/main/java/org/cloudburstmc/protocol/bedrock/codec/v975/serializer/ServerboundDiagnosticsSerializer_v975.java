package org.cloudburstmc.protocol.bedrock.codec.v975.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v924.serializer.ServerboundDiagnosticsSerializer_v924;
import org.cloudburstmc.protocol.bedrock.data.diagnostics.EntityDiagnosticTimingInfo;
import org.cloudburstmc.protocol.bedrock.data.diagnostics.SystemDiagnosticTimingInfo;
import org.cloudburstmc.protocol.bedrock.packet.ServerboundDiagnosticsPacket;

public class ServerboundDiagnosticsSerializer_v975 extends ServerboundDiagnosticsSerializer_v924 {

    public static final ServerboundDiagnosticsSerializer_v975 INSTANCE = new ServerboundDiagnosticsSerializer_v975();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, ServerboundDiagnosticsPacket packet) {
        super.serialize(buffer, helper, packet);

        helper.writeArray(buffer, packet.getEntityDiagnostics(), ((buf, h, info) -> {
            helper.writeString(buf, info.getDisplayName());
            helper.writeString(buf, info.getEntity());
            buf.writeLongLE(info.getTimeInNs());
            buf.writeByte(info.getPercentOfTotal());
        }));

        helper.writeArray(buffer, packet.getSystemDiagnostics(), ((buf, h, info) -> {
            helper.writeString(buf, info.getDisplayName());
            buf.writeLongLE(info.getSystemIndex());
            buf.writeLongLE(info.getTimeInNs());
            buf.writeByte(info.getPercentOfTotal());
        }));
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, ServerboundDiagnosticsPacket packet) {
        super.deserialize(buffer, helper, packet);

        helper.readArray(buffer, packet.getEntityDiagnostics(), (buf, h) ->
                new EntityDiagnosticTimingInfo(
                        helper.readString(buf),
                        helper.readString(buf),
                        buf.readLongLE(),
                        (byte) buf.readUnsignedByte()));

        helper.readArray(buffer, packet.getSystemDiagnostics(), (buf, h) ->
                new SystemDiagnosticTimingInfo(
                        helper.readString(buf),
                        buf.readLongLE(),
                        buf.readLongLE(),
                        (byte) buf.readUnsignedByte()));
    }
}
