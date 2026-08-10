package org.cloudburstmc.protocol.bedrock.codec.v898.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.data.datastore.DataStoreChange;
import org.cloudburstmc.protocol.bedrock.data.datastore.DataStoreRemoval;
import org.cloudburstmc.protocol.bedrock.data.datastore.DataStoreUpdate;
import org.cloudburstmc.protocol.bedrock.packet.ClientboundDataStorePacket;
import org.cloudburstmc.protocol.common.util.VarInts;

import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ClientboundDataStoreSerializer_v898 implements BedrockPacketSerializer<ClientboundDataStorePacket> {

    public static final ClientboundDataStoreSerializer_v898 INSTANCE = new ClientboundDataStoreSerializer_v898();

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

    protected void writeDataStoreChange(ByteBuf buffer, BedrockCodecHelper helper, Object value) {
        int type = value instanceof Boolean ? 1 : value instanceof Number ? 2 : value instanceof String ? 4 : value instanceof Map ? 6 : 0;

        buffer.writeIntLE(type);

        switch (type) {
            case 0:
                break;
            case 1:
                buffer.writeBoolean((boolean) value);
                break;
            case 2:
                buffer.writeLongLE(((Number) value).longValue());
                break;
            case 4:
                helper.writeString(buffer, (String) value);
                break;
            case 6:
                VarInts.writeUnsignedInt(buffer, ((Map<?, ?>) value).size());
                ((Map<?, ?>) value).forEach((k, v) -> {
                    helper.writeString(buffer, (String) k);
                    writeDataStoreChange(buffer, helper, v);
                });
                break;
            default:
                throw new IllegalStateException("Invalid data store change data type " + type);
        }
    }

    protected Object readDataStoreChange(ByteBuf buffer, BedrockCodecHelper helper) {
        int type = buffer.readIntLE();

        switch (type) {
            case 0:
                return null;
            case 1:
                return buffer.readBoolean();
            case 2:
                return buffer.readLongLE();
            case 4:
                return helper.readString(buffer);
            case 6:
                int size = VarInts.readUnsignedInt(buffer);
                Map<String, Object> values = new LinkedHashMap<>();
                for (int i = 0; i < size; i++) {
                    values.put(helper.readString(buffer), readDataStoreChange(buffer, helper));
                }
                return values;
            default:
                throw new IllegalStateException("Invalid data store change data type " + type);
        }
    }
}
