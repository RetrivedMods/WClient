package org.cloudburstmc.protocol.bedrock.codec.v924.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.data.camera.AimAssistActorPriorityData;
import org.cloudburstmc.protocol.bedrock.packet.CameraAimAssistActorPriorityPacket;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CameraAimAssistActorPrioritySerializer_v924 implements BedrockPacketSerializer<CameraAimAssistActorPriorityPacket> {

    public static final CameraAimAssistActorPrioritySerializer_v924 INSTANCE = new CameraAimAssistActorPrioritySerializer_v924();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, CameraAimAssistActorPriorityPacket packet) {
        helper.writeArray(buffer, packet.getPriorityData(), (buf, value) -> {
            buf.writeIntLE(value.getPresetIndex());
            buf.writeIntLE(value.getCategoryIndex());
            buf.writeIntLE(value.getActorIndex());
            buf.writeIntLE(value.getPriorityValue());
        });
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, CameraAimAssistActorPriorityPacket packet) {
        List<AimAssistActorPriorityData> priorityData = new ArrayList<>();
        helper.readArray(buffer, priorityData, (buf, h) ->
                new AimAssistActorPriorityData(buf.readIntLE(), buf.readIntLE(), buf.readIntLE(), buf.readIntLE()));
        packet.setPriorityData(priorityData);
    }
}
