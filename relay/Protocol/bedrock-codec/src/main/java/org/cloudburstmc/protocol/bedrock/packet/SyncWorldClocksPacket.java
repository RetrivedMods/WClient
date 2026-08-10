package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.protocol.bedrock.data.clock.SyncWorldClocksPayload;
import org.cloudburstmc.protocol.common.PacketSignal;

/**
 * Initializes and syncs world clocks from the server to clients. (Currently disabled)
 * Sent from the server when a client joins to initialize all world clocks for the client and periodically to all clients to keep them in sync.
 * It is also sent to all clients when a world clock's paused state changes or when time markers are added or removed.
 *
 * @since v944
 */
@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class SyncWorldClocksPacket implements BedrockPacket {

    private SyncWorldClocksPayload data;

    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.SYNC_WORLD_CLOCKS;
    }

    @Override
    public SyncWorldClocksPacket clone() {
        try {
            return (SyncWorldClocksPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
