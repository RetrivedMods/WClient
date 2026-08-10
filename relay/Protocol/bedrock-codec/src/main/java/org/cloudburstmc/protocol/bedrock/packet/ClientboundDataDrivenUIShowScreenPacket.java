package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.protocol.common.PacketSignal;

/**
 * Allows the server to tell the client to show a Data Driven UI screen.
 *
 * @since v924
 */
@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class ClientboundDataDrivenUIShowScreenPacket implements BedrockPacket {

    private String screenId;

    /**
     * The unique id of this instance of the form for tracking in scripting
     * @since v944
     */
    private int formId;

    /**
     * The optional id of the data associated with this screen
     * @since v944
     */
    @Nullable
    private Integer dataInstanceId;

    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.CLIENTBOUND_DATA_DRIVEN_UI_SHOW_SCREEN;
    }

    @Override
    public ClientboundDataDrivenUIShowScreenPacket clone() {
        try {
            return (ClientboundDataDrivenUIShowScreenPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
