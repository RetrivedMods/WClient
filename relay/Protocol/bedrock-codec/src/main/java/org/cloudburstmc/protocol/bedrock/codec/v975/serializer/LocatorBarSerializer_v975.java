package org.cloudburstmc.protocol.bedrock.codec.v975.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v944.serializer.LocatorBarSerializer_v944;
import org.cloudburstmc.protocol.bedrock.data.LocatorBarWaypoint;
import org.cloudburstmc.protocol.common.util.VarInts;

import java.awt.Color;

public class LocatorBarSerializer_v975 extends LocatorBarSerializer_v944 {

    public static final LocatorBarSerializer_v975 INSTANCE = new LocatorBarSerializer_v975();

    @Override
    protected void writeWaypoint(ByteBuf buf, BedrockCodecHelper helper, LocatorBarWaypoint waypoint) {
        buf.writeIntLE(waypoint.getUpdateFlag());
        helper.writeOptionalNull(buf, waypoint.getVisible(), ByteBuf::writeBoolean);
        helper.writeOptionalNull(buf, waypoint.getWorldPosition(), (buf1, h, pos) -> {
            h.writeVector3f(buf1, pos.getPosition());
            VarInts.writeInt(buf1, pos.getDimension());
        });
        helper.writeOptionalNull(buf, waypoint.getTexturePath(), helper::writeString);
        helper.writeOptionalNull(buf, waypoint.getIconSize(), helper::writeVector2f);
        helper.writeOptionalNull(buf, waypoint.getColor(), (buf1, h, c) -> buf1.writeIntLE(c.getRGB()));
        helper.writeOptionalNull(buf, waypoint.getClientPositionAuthority(), ByteBuf::writeBoolean);
        helper.writeOptionalNull(buf, waypoint.getEntityUniqueId(), VarInts::writeLong);
    }

    @Override
    protected LocatorBarWaypoint readWaypoint(ByteBuf buf, BedrockCodecHelper helper) {
        LocatorBarWaypoint waypoint = new LocatorBarWaypoint();
        waypoint.setUpdateFlag((int) buf.readUnsignedIntLE());
        waypoint.setVisible(helper.readOptional(buf, null, ByteBuf::readBoolean));
        waypoint.setWorldPosition(helper.readOptional(buf, null, (buf1, h) ->
                new LocatorBarWaypoint.WorldPosition(h.readVector3f(buf1), VarInts.readInt(buf1))));
        waypoint.setTexturePath(helper.readOptional(buf, null, helper::readString));
        waypoint.setIconSize(helper.readOptional(buf, null, helper::readVector2f));
        waypoint.setColor(helper.readOptional(buf, null, (buf1, h) ->
                new Color(buf1.readIntLE(), true)));
        waypoint.setClientPositionAuthority(helper.readOptional(buf, null, ByteBuf::readBoolean));
        waypoint.setEntityUniqueId(helper.readOptional(buf, null, VarInts::readLong));
        return waypoint;
    }
}
