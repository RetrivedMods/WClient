package org.cloudburstmc.protocol.bedrock.data.clock;


import lombok.Value;

import java.util.List;

@Value
public class WorldClockData {
    long id;
    String name;
    int time;
    boolean paused;
    List<TimeMarkerData> timeMarkers;
}
