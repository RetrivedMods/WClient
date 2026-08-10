package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.cloudburstmc.protocol.bedrock.data.LocatorBarWaypoint;
import org.cloudburstmc.protocol.common.PacketSignal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Syncs LocatorBar changes on the server with the client.
 *
 * @since v944
 */
@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class LocatorBarPacket implements BedrockPacket {

    private List<Payload> waypoints = new ArrayList<>();

    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.LOCATOR_BAR;
    }

    @Override
    public LocatorBarPacket clone() {
        try {
            return (LocatorBarPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    @Value
    public static class Payload {
        Action actionFlag;
        UUID groupHandle;
        LocatorBarWaypoint waypoint;
    }

    public enum Action {
        NONE,
        ADD,
        REMOVE,
        UPDATE
    }
}
