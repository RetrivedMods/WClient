package org.cloudburstmc.protocol.bedrock.data.clock;

import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
public class RemoveTimeMarkerData implements SyncWorldClocksPayload {

    long clockId;
    List<Long> timeMarkerIds = new ArrayList<>();
}
