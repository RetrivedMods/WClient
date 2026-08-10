package org.cloudburstmc.protocol.bedrock.codec.v859.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v827.serializer.CameraInstructionSerializer_v827;
import org.cloudburstmc.protocol.bedrock.data.camera.*;
import org.cloudburstmc.protocol.bedrock.packet.CameraInstructionPacket;
import org.cloudburstmc.protocol.common.util.OptionalBoolean;

import java.util.ArrayList;
import java.util.List;

public class CameraInstructionSerializer_v859 extends CameraInstructionSerializer_v827 {

    public static final CameraInstructionSerializer_v859 INSTANCE = new CameraInstructionSerializer_v859();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, CameraInstructionPacket packet) {
        super.serialize(buffer, helper, packet);
        helper.writeOptionalNull(buffer, packet.getSplineInstruction(), (buf, splineInstruction) -> writeSplineInstruction(buf, helper, splineInstruction));
        helper.writeOptionalNull(buffer, packet.getAttachInstruction(), (buf, attachInstruction) -> buf.writeLongLE(attachInstruction.getUniqueEntityId()));
        helper.writeOptional(buffer, OptionalBoolean::isPresent, packet.getDetachFromEntity(), (buf, optional) -> buf.writeBoolean(optional.getAsBoolean()));
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, CameraInstructionPacket packet) {
        super.deserialize(buffer, helper, packet);
        packet.setSplineInstruction(helper.readOptional(buffer, null, buf -> readSplineInstruction(buf, helper)));
        packet.setAttachInstruction(helper.readOptional(buffer, null, buf -> new CameraAttachToEntityInstruction(buf.readLongLE())));
        packet.setDetachFromEntity(helper.readOptional(buffer, OptionalBoolean.empty(), buf -> OptionalBoolean.of(buf.readBoolean())));
    }

    protected void writeSplineInstruction(ByteBuf buffer, BedrockCodecHelper helper, CameraSplineInstruction splineInstruction) {
        buffer.writeFloatLE(splineInstruction.getTotalTime());
        buffer.writeByte(splineInstruction.getType().ordinal());
        helper.writeArray(buffer, splineInstruction.getCurve(), helper::writeVector3f);
        helper.writeArray(buffer, splineInstruction.getProgressKeyFrames(), (buf2, frame) -> {
            buf2.writeFloatLE(frame.getValue());
            buf2.writeFloatLE(frame.getTime());
        });
        helper.writeArray(buffer, splineInstruction.getRotationOption(), (buf2, rotationOption) -> {
            helper.writeVector3f(buf2, rotationOption.getKeyFrameValues());
            buf2.writeFloatLE(rotationOption.getKeyFrameTimes());
        });
    }

    protected CameraSplineInstruction readSplineInstruction(ByteBuf buffer, BedrockCodecHelper helper) {
        float totalTime = buffer.readFloatLE();
        CameraSplineType type = CameraSplineType.values()[buffer.readUnsignedByte()];
        List<Vector3f> curve = new ArrayList<>();
        helper.readArray(buffer, curve, helper::readVector3f);
        List<CameraSplineInstruction.SplineProgressOption> progressKeyFrames = new ArrayList<>();
        helper.readArray(buffer, progressKeyFrames, buf2 -> {
            float value = buf2.readFloatLE();
            float time = buf2.readFloatLE();
            return new CameraSplineInstruction.SplineProgressOption(value, time, CameraEase.LINEAR);
        });
        List<CameraSplineInstruction.SplineRotationOption> rotationOption = new ArrayList<>();
        helper.readArray(buffer, rotationOption, buf2 -> {
            Vector3f keyFrameValues = helper.readVector3f(buf2);
            float keyFrameTimes = buf2.readFloatLE();
            return new CameraSplineInstruction.SplineRotationOption(keyFrameValues, keyFrameTimes, CameraEase.LINEAR);
        });
        return new CameraSplineInstruction(totalTime, type, curve, progressKeyFrames, rotationOption);
    }
}
