package org.cloudburstmc.protocol.bedrock.codec.v944.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.data.LocatorBarWaypoint;
import org.cloudburstmc.protocol.bedrock.packet.LocatorBarPacket;
import org.cloudburstmc.protocol.common.util.VarInts;

import java.awt.Color;
import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class LocatorBarSerializer_v944 implements BedrockPacketSerializer<LocatorBarPacket> {

    public static final LocatorBarSerializer_v944 INSTANCE = new LocatorBarSerializer_v944();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, LocatorBarPacket packet) {
        helper.writeArray(buffer, packet.getWaypoints(), (buf, payload) -> writePayload(buf, helper, payload));
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, LocatorBarPacket packet) {
        helper.readArray(buffer, packet.getWaypoints(), buf -> readPayload(buf, helper), 40000);
    }

    private void writePayload(ByteBuf buf, BedrockCodecHelper helper, LocatorBarPacket.Payload payload) {
        helper.writeUuid(buf, payload.getGroupHandle());
        writeWaypoint(buf, helper, payload.getWaypoint());
        buf.writeByte(payload.getActionFlag().ordinal());
    }

    private LocatorBarPacket.Payload readPayload(ByteBuf buf, BedrockCodecHelper helper) {
        UUID groupHandle = helper.readUuid(buf);
        LocatorBarWaypoint waypoint = readWaypoint(buf, helper);
        LocatorBarPacket.Action actionFlag = LocatorBarPacket.Action.values()[buf.readUnsignedByte()];
        return new LocatorBarPacket.Payload(actionFlag, groupHandle, waypoint);
    }

    protected void writeWaypoint(ByteBuf buf, BedrockCodecHelper helper, LocatorBarWaypoint waypoint) {
        buf.writeIntLE(waypoint.getUpdateFlag());
        helper.writeOptionalNull(buf, waypoint.getVisible(), ByteBuf::writeBoolean);
        helper.writeOptionalNull(buf, waypoint.getWorldPosition(), (buf1, h, pos) -> {
            h.writeVector3f(buf1, pos.getPosition());
            VarInts.writeInt(buf1, pos.getDimension());
        });
        helper.writeOptionalNull(buf, waypoint.getTextureId(), ByteBuf::writeIntLE);
        helper.writeOptionalNull(buf, waypoint.getColor(), (buf1, h, c) -> buf1.writeIntLE(c.getRGB()));
        helper.writeOptionalNull(buf, waypoint.getClientPositionAuthority(), ByteBuf::writeBoolean);
        helper.writeOptionalNull(buf, waypoint.getEntityUniqueId(), VarInts::writeLong);
    }

    protected LocatorBarWaypoint readWaypoint(ByteBuf buf, BedrockCodecHelper helper) {
        LocatorBarWaypoint waypoint = new LocatorBarWaypoint();
        waypoint.setUpdateFlag((int) buf.readUnsignedIntLE());
        waypoint.setVisible(helper.readOptional(buf, null, ByteBuf::readBoolean));
        waypoint.setWorldPosition(helper.readOptional(buf, null, (buf1, h) ->
                new LocatorBarWaypoint.WorldPosition(h.readVector3f(buf1), VarInts.readInt(buf1))));
        Long id = helper.readOptional(buf, null, ByteBuf::readUnsignedIntLE);
        waypoint.setTextureId(id == null ? null : id.intValue());
        waypoint.setColor(helper.readOptional(buf, null, (buf1, h) ->
                new Color(buf1.readIntLE(), true)));
        waypoint.setClientPositionAuthority(helper.readOptional(buf, null, ByteBuf::readBoolean));
        waypoint.setEntityUniqueId(helper.readOptional(buf, null, VarInts::readLong));
        return waypoint;
    }
}
