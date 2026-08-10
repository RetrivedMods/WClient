package org.cloudburstmc.protocol.bedrock.data.clock;

import lombok.Value;

import java.util.List;

@Value
public class AddTimeMarkerData implements SyncWorldClocksPayload {

    long clockId;
    List<TimeMarkerData> timeMarkers;
}
