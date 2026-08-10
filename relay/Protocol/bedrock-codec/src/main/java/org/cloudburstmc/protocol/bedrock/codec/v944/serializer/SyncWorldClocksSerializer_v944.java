package org.cloudburstmc.protocol.bedrock.codec.v944.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.data.clock.*;
import org.cloudburstmc.protocol.bedrock.packet.SyncWorldClocksPacket;
import org.cloudburstmc.protocol.common.util.VarInts;

import java.util.ArrayList;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class SyncWorldClocksSerializer_v944 implements BedrockPacketSerializer<SyncWorldClocksPacket> {

    public static final SyncWorldClocksSerializer_v944 INSTANCE = new SyncWorldClocksSerializer_v944();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, SyncWorldClocksPacket packet) {
        SyncWorldClocksPayload data = packet.getData();

        if (data instanceof SyncStateData) {
            VarInts.writeUnsignedInt(buffer, 0);
            writeSyncState(buffer, helper, (SyncStateData) data);
        } else if (data instanceof InitializeRegistryData) {
            VarInts.writeUnsignedInt(buffer, 1);
            writeInitializeRegistry(buffer, helper, (InitializeRegistryData) data);
        } else if (data instanceof AddTimeMarkerData) {
            VarInts.writeUnsignedInt(buffer, 2);
            writeAddTimeMarker(buffer, helper, (AddTimeMarkerData) data);
        } else if (data instanceof RemoveTimeMarkerData) {
            VarInts.writeUnsignedInt(buffer, 3);
            writeRemoveTimeMarker(buffer, helper, (RemoveTimeMarkerData) data);
        } else {
            throw new IllegalArgumentException("Not oneOf<SyncStateData, InitializeRegistryData, AddTimeMarkerData, RemoveTimeMarkerData>");
        }
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, SyncWorldClocksPacket packet) {
        int type = VarInts.readUnsignedInt(buffer);

        switch (type) {
            case 0:
                packet.setData(readSyncState(buffer, helper));
                return;
            case 1:
                packet.setData(readInitializeRegistry(buffer, helper));
                return;
            case 2:
                packet.setData(readAddTimeMarker(buffer, helper));
                return;
            case 3:
                packet.setData(readRemoveTimeMarker(buffer, helper));
                return;
        }

        throw new IllegalArgumentException(type + " is not oneOf<SyncStateData, InitializeRegistryData, AddTimeMarkerData, RemoveTimeMarkerData>");
    }

    private void writeSyncState(ByteBuf buf, BedrockCodecHelper helper, SyncStateData data) {
        helper.writeArray(buf, data.getClockData(), (b, entry) -> {
            VarInts.writeUnsignedLong(b, entry.getClockId());
            VarInts.writeInt(b, entry.getTime());
            b.writeBoolean(entry.isPaused());
        });
    }

    private SyncStateData readSyncState(ByteBuf buf, BedrockCodecHelper helper) {
        SyncStateData data = new SyncStateData(new ArrayList<>());

        helper.readArray(buf, data.getClockData(), b -> {
            long id = VarInts.readUnsignedLong(b);
            int time = VarInts.readInt(b);
            boolean paused = b.readBoolean();
            return new SyncWorldClockStateData(id, time, paused);
        }, 256);

        return data;
    }

    private void writeInitializeRegistry(ByteBuf buf, BedrockCodecHelper helper, InitializeRegistryData data) {
        helper.writeArray(buf, data.getClockData(), (b, entry) -> {
            VarInts.writeUnsignedLong(b, entry.getId());
            helper.writeString(b, entry.getName());
            VarInts.writeInt(b, entry.getTime());
            b.writeBoolean(entry.isPaused());

            helper.writeArray(b, entry.getTimeMarkers(), (bb, marker) -> writeTimeMarker(bb, helper, marker));
        });
    }

    private void writeTimeMarker(ByteBuf buf, BedrockCodecHelper helper, TimeMarkerData marker) {
        VarInts.writeUnsignedLong(buf, marker.getId());
        helper.writeString(buf, marker.getName());
        VarInts.writeInt(buf, marker.getTime());
        helper.writeOptionalNull(buf, marker.getPeriod(), ByteBuf::writeIntLE);
    }

    private TimeMarkerData readTimeMarker(ByteBuf buf, BedrockCodecHelper helper) {
        long markerId = VarInts.readUnsignedLong(buf);
        String markerName = helper.readStringMaxLen(buf, 128);
        int markerTime = VarInts.readInt(buf);
        Integer period = helper.readOptional(buf, null, ByteBuf::readIntLE);
        return new TimeMarkerData(markerId, markerName, markerTime, period);
    }

    private InitializeRegistryData readInitializeRegistry(ByteBuf buf, BedrockCodecHelper helper) {
        InitializeRegistryData data = new InitializeRegistryData(new ArrayList<>());

        helper.readArray(buf, data.getClockData(), b -> {
            long id = VarInts.readUnsignedLong(b);
            String name = helper.readStringMaxLen(b, 128);
            int time = VarInts.readInt(b);
            boolean paused = b.readBoolean();

            WorldClockData clock = new WorldClockData(id, name, time, paused, new ArrayList<>());

            helper.readArray(b, clock.getTimeMarkers(), bb -> readTimeMarker(bb, helper), 256);

            return clock;
        }, 256);

        return data;
    }

    private void writeAddTimeMarker(ByteBuf buf, BedrockCodecHelper helper, AddTimeMarkerData data) {
        VarInts.writeUnsignedLong(buf, data.getClockId());
        helper.writeArray(buf, data.getTimeMarkers(), (b, marker) -> writeTimeMarker(b, helper, marker));
    }

    private AddTimeMarkerData readAddTimeMarker(ByteBuf buf, BedrockCodecHelper helper) {
        AddTimeMarkerData data = new AddTimeMarkerData(VarInts.readUnsignedLong(buf), new ArrayList<>());
        helper.readArray(buf, data.getTimeMarkers(), b -> readTimeMarker(b, helper), 256);
        return data;
    }

    private void writeRemoveTimeMarker(ByteBuf buf, BedrockCodecHelper helper, RemoveTimeMarkerData data) {
        VarInts.writeUnsignedLong(buf, data.getClockId());
        helper.writeArray(buf, data.getTimeMarkerIds(), VarInts::writeUnsignedLong);
    }

    private RemoveTimeMarkerData readRemoveTimeMarker(ByteBuf buf, BedrockCodecHelper helper) {
        RemoveTimeMarkerData data = new RemoveTimeMarkerData(VarInts.readUnsignedLong(buf));
        helper.readArray(buf, data.getTimeMarkerIds(), VarInts::readUnsignedLong, 256);
        return data;
    }
}
