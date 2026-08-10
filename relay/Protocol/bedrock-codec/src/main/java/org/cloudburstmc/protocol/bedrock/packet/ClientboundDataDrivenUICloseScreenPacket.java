package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.protocol.common.PacketSignal;

/**
 * Allows the server to tell the client to close all the Data Driven UI screens.
 * Previously ClientboundDataDrivenUICloseAllScreensPacket
 *
 * @since v924
 */
@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class ClientboundDataDrivenUICloseScreenPacket implements BedrockPacket {

    /**
     * The unique id of th form to close. If not supplied, this will close all forms
     * @since v944
     */
    @Nullable
    private Integer formId;

    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.CLIENTBOUND_DATA_DRIVEN_UI_CLOSE_SCREEN;
    }

    @Override
    public ClientboundDataDrivenUICloseScreenPacket clone() {
        try {
            return (ClientboundDataDrivenUICloseScreenPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
