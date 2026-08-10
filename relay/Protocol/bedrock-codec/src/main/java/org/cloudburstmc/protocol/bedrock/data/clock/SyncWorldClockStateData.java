package org.cloudburstmc.protocol.bedrock.data.clock;


import lombok.Value;

@Value
public class SyncWorldClockStateData {

    long clockId;
    int time;
    boolean paused;
}
