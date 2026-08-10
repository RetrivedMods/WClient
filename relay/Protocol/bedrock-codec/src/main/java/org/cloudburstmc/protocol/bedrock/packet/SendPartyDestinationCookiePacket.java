package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.protocol.bedrock.data.camera.CameraSplineType;
import org.cloudburstmc.protocol.common.PacketSignal;

import java.util.HashMap;
import java.util.Map;

/**
 * Sent by the server to a client with a party destination cookie.
 *
 * @since v1001
 */
@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class SendPartyDestinationCookiePacket implements BedrockPacket {

    private String cookie;
    private Intent intent;
    private String destinationName;

    @Override
    public PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    @Override
    public BedrockPacketType getPacketType() {
        return BedrockPacketType.SEND_PARTY_DESTINATION_COOKIE;
    }

    @Override
    public BedrockPacket clone() {
        try {
            return (SendPartyDestinationCookiePacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    public enum Intent {
        NOTIFY("notify"), OPT_IN("optin"), OPT_OUT("optout");

        private static final Map<String, Intent> serializeNames = new HashMap<>(values().length);
        static {
            for (Intent value : values()) {
                serializeNames.put(value.getSerializeName(), value);
            }
        }

        private final String serializeName;

        Intent(String serializeName) {
            this.serializeName = serializeName;
        }

        public String getSerializeName() {
            return this.serializeName;
        }

        public static Intent fromName(String serializeName) {
            return serializeNames.get(serializeName);
        }
    }
}
