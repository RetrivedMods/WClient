package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.protocol.bedrock.data.attributelayer.AttributeLayerSyncPayload;
import org.cloudburstmc.protocol.common.PacketSignal;

/**
 * Syncs Attribute Layers to the Client (Currently disabled)
 *
 * @since v944
 */
@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class ClientboundAttributeLayerSyncPacket implements BedrockPacket {

    private AttributeLayerSyncPayload data;

    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.CLIENTBOUND_ATTRIBUTE_LAYER_SYNC;
    }

    @Override
    public ClientboundAttributeLayerSyncPacket clone() {
        try {
            return (ClientboundAttributeLayerSyncPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
