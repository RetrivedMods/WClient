package org.cloudburstmc.protocol.bedrock.codec.v1001.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v924.serializer.GraphicsParameterOverrideSerializer_v924;
import org.cloudburstmc.protocol.bedrock.data.GraphicsOverrideParameterType;
import org.cloudburstmc.protocol.bedrock.packet.GraphicsParameterOverridePacket;
import org.cloudburstmc.protocol.common.util.VarInts;

import java.util.LinkedHashMap;
import java.util.Map;

public class GraphicsParameterOverrideSerializer_v1001 extends GraphicsParameterOverrideSerializer_v924 {

    public static final GraphicsParameterOverrideSerializer_v1001 INSTANCE = new GraphicsParameterOverrideSerializer_v1001();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, GraphicsParameterOverridePacket packet) {
        helper.writeArray(buffer, packet.getValues().entrySet(), (buf, aHelper, entry) -> {
            buf.writeFloatLE(entry.getKey());
            helper.writeVector3f(buf, entry.getValue());
        });
        helper.writeOptionalNull(buffer, packet.getFloatValue(), ByteBuf::writeFloatLE);
        helper.writeOptionalNull(buffer, packet.getVec3Value(), (buf, h, v) -> h.writeVector3f(buf, v));
        helper.writeString(buffer, packet.getBiomeIdentifier());
        helper.writeOptionalNull(buffer, packet.getPlayerIdentifier(), (buf, h, v) -> h.writeString(buf, v)); // new
        buffer.writeByte(packet.getParameterType().ordinal());
        buffer.writeBoolean(packet.isReset());
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, GraphicsParameterOverridePacket packet) {
        Map<Float, Vector3f> values = new LinkedHashMap<>();
        int length = VarInts.readUnsignedInt(buffer);
        for (int i = 0; i < length; i++) {
            float key = buffer.readFloatLE();
            Vector3f value = helper.readVector3f(buffer);
            values.put(key, value);
        }
        packet.setValues(values);
        packet.setFloatValue(helper.readOptional(buffer, null, ByteBuf::readFloatLE));
        packet.setVec3Value(helper.readOptional(buffer, null, (buf, h) -> h.readVector3f(buf)));
        packet.setBiomeIdentifier(helper.readString(buffer));
        packet.setPlayerIdentifier(helper.readOptional(buffer, null, (buf, h) -> h.readString(buf))); // new
        packet.setParameterType(GraphicsOverrideParameterType.values()[buffer.readUnsignedByte()]);
        packet.setReset(buffer.readBoolean());
    }
}
