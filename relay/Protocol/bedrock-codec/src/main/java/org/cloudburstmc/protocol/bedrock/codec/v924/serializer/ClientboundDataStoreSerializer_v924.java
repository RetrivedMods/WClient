package org.cloudburstmc.protocol.bedrock.codec.v924.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v898.serializer.ClientboundDataStoreSerializer_v898;
import org.cloudburstmc.protocol.bedrock.data.datastore.DataStoreChange;
import org.cloudburstmc.protocol.bedrock.data.datastore.DataStoreRemoval;
import org.cloudburstmc.protocol.bedrock.data.datastore.DataStoreUpdate;
import org.cloudburstmc.protocol.bedrock.packet.ClientboundDataStorePacket;
import org.cloudburstmc.protocol.common.util.VarInts;

public class ClientboundDataStoreSerializer_v924 extends ClientboundDataStoreSerializer_v898 {

    public static final ClientboundDataStoreSerializer_v924 INSTANCE = new ClientboundDataStoreSerializer_v924();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, ClientboundDataStorePacket packet) {
        helper.writeArray(buffer, packet.getUpdates(), (buf, action) -> {
            VarInts.writeUnsignedInt(buffer, action.getType());

            switch (action.getType()) {
                case 0:
                    helper.writeString(buffer, ((DataStoreUpdate) action).getDataStoreName());
                    helper.writeString(buffer, ((DataStoreUpdate) action).getProperty());
                    helper.writeString(buffer, ((DataStoreUpdate) action).getPath());

                    Object value = ((DataStoreUpdate) action).getData();

                    int type = value instanceof Number ? 0 : value instanceof Boolean ? 1 : value instanceof String ? 2 : -1;

                    VarInts.writeUnsignedInt(buffer, type);

                    switch (type) {
                        case 0:
                            buffer.writeDoubleLE(((Number) value).doubleValue());
                            break;
                        case 1:
                            buffer.writeBoolean((boolean) value);
                            break;
                        case 2:
                            helper.writeString(buffer, (String) value);
                            break;
                        default:
                            throw new IllegalStateException("Invalid data store data type " + type);
                    }

                    buffer.writeIntLE(((DataStoreUpdate) action).getUpdateCount());
                    buffer.writeIntLE(((DataStoreUpdate) action).getPathUpdateCount());
                    break;
                case 1:
                    helper.writeString(buffer, ((DataStoreChange) action).getDataStoreName());
                    helper.writeString(buffer, ((DataStoreChange) action).getProperty());
                    buffer.writeIntLE(((DataStoreChange) action).getUpdateCount());
                    writeDataStoreChange(buffer, helper, ((DataStoreChange) action).getNewValue());
                    break;
                case 2:
                    helper.writeString(buf, ((DataStoreRemoval) action).getDataStoreName());
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown data store action");
            }
        });
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, ClientboundDataStorePacket packet) {
        helper.readArray(buffer, packet.getUpdates(), (buf -> {
            switch (VarInts.readUnsignedInt(buffer)) {
                case 0:
                    DataStoreUpdate update = new DataStoreUpdate();
                    update.setDataStoreName(helper.readString(buffer));
                    update.setProperty(helper.readString(buffer));
                    update.setPath(helper.readString(buffer));

                    int type = VarInts.readUnsignedInt(buffer);

                    switch (type) {
                        case 0:
                            update.setData(buffer.readDoubleLE());
                            break;
                        case 1:
                            update.setData(buffer.readBoolean());
                            break;
                        case 2:
                            update.setData(helper.readString(buffer));
                            break;
                        default:
                            throw new IllegalStateException("Invalid data store data type: " + type);
                    }

                    update.setUpdateCount((int) buffer.readUnsignedIntLE());
                    update.setPathUpdateCount((int) buffer.readUnsignedIntLE());
                    return update;
                case 1:
                    DataStoreChange change = new DataStoreChange();
                    change.setDataStoreName(helper.readString(buf));
                    change.setProperty(helper.readString(buffer));
                    change.setUpdateCount((int) buffer.readUnsignedIntLE());
                    change.setNewValue(readDataStoreChange(buffer, helper));
                    return change;
                case 2:
                    DataStoreRemoval removal = new DataStoreRemoval();
                    removal.setDataStoreName(helper.readString(buf));
                    return removal;
                default:
                    throw new UnsupportedOperationException("Unknown data store action");
            }
        }));
    }
}
