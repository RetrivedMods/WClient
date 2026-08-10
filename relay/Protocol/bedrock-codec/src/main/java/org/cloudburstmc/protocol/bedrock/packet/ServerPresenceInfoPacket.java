package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.protocol.bedrock.data.PresenceConfiguration;
import org.cloudburstmc.protocol.common.PacketSignal;

/**
 * Sent by the server to provide PresenceConfiguration to the client.
 *
 * @since v975
 */
@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class ServerPresenceInfoPacket implements BedrockPacket {

    private PresenceConfiguration presenceConfiguration;

    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.SERVER_PRESENCE_INFO;
    }

    @Override
    public ServerPresenceInfoPacket clone() {
        try {
            return (ServerPresenceInfoPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
