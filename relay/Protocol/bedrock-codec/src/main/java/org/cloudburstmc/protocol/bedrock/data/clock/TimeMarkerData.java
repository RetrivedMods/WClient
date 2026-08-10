package org.cloudburstmc.protocol.bedrock.data.clock;

import lombok.Value;

@Value
public class TimeMarkerData {

    long id;
    String name;
    int time;
    Integer period; // changelog says required but whatever
}
