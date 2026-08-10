package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.protocol.bedrock.data.ClientStoreEntrypointConfiguration;
import org.cloudburstmc.protocol.common.PacketSignal;

/**
 * Sent by the server to provide ClientStoreEntryPointConfiguration to the client.
 *
 * @since v975
 */
@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class ServerStoreInfoPacket implements BedrockPacket {

    @Nullable
    private ClientStoreEntrypointConfiguration store;

    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.SERVER_STORE_INFO;
    }

    @Override
    public ServerStoreInfoPacket clone() {
        try {
            return (ServerStoreInfoPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
