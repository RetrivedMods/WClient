package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.protocol.bedrock.data.camera.AimAssistActorPriorityData;
import org.cloudburstmc.protocol.common.PacketSignal;

import java.util.List;

/**
 * Camera aim-assist actor priority data sent from the server to clients.
 * Sent by the server to clients for updating the actor priority for client aim-assist systems.
 *
 * @since v924
 */
@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class CameraAimAssistActorPriorityPacket implements BedrockPacket {

    private List<AimAssistActorPriorityData> priorityData;

    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.CAMERA_AIM_ASSIST_ACTOR_PRIORITY;
    }

    @Override
    public CameraAimAssistActorPriorityPacket clone() {
        try {
            return (CameraAimAssistActorPriorityPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
