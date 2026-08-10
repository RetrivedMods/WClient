package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.protocol.common.PacketSignal;

/**
 * Sent from the client to the server when a data driven screen is closed.
 *
 * @since v944
 */
@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class ServerboundDataDrivenScreenClosedPacket implements BedrockPacket {

    private Integer formId;
    private CloseReason closeReason;

    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.SERVERBOUND_DATA_DRIVEN_SCREEN_CLOSED;
    }

    @Override
    public ServerboundDataDrivenScreenClosedPacket clone() {
        try {
            return (ServerboundDataDrivenScreenClosedPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    public enum CloseReason {
        PROGRAMMATIC_CLOSE,
        PROGRAMMATIC_CLOSE_ALL,
        CLIENT_CANCELED,
        USER_BUSY,
        INVALID_FORM,
    }
}
