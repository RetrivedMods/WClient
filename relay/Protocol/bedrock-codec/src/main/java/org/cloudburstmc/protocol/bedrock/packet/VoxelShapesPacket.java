package org.cloudburstmc.protocol.bedrock.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cloudburstmc.protocol.bedrock.data.SerializableVoxelShape;
import org.cloudburstmc.protocol.common.PacketSignal;

import java.util.List;
import java.util.Map;

/**
 * Syncs client with server voxel shape data on world join. This packet contains a copy of all behavior pack voxel shapes data and is used by StartGamePacket.
 * Sends the serializable voxel shapes data to the client as it's needed on both the client and server. This packet should always be sent before StartGamePacket. (since v975)
 *
 * @since v924
 */
@Data
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class VoxelShapesPacket implements BedrockPacket {

    private List<SerializableVoxelShape> shapes;
    private Map<String, Integer> nameMap;
    /**
     * @since v944
     */
    private int customShapeCount;

    @Override
    public final PacketSignal handle(BedrockPacketHandler handler) {
        return handler.handle(this);
    }

    public BedrockPacketType getPacketType() {
        return BedrockPacketType.VOXEL_SHAPES;
    }

    @Override
    public VoxelShapesPacket clone() {
        try {
            return (VoxelShapesPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
