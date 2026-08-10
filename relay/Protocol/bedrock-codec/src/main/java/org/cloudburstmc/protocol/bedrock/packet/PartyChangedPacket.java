package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.protocol.common.PacketSignal;

/**
 * Sent by the client to provide additional client metadata.
 *
 * @since v944
 */
@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class PartyChangedPacket implements BedrockPacket {

    @Nullable
    private PartyInfo party;

    @Value
    public static class PartyInfo {
        String partyId;
        /**
         * @since v975
         */
        boolean isPartyLeader;
    }

    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.PARTY_CHANGED;
    }

    @Override
    public PartyChangedPacket clone() {
        try {
            return (PartyChangedPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
