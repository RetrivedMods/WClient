package org.cloudburstmc.protocol.bedrock.data.clock;

import lombok.Value;

import java.util.List;

@Value
public class InitializeRegistryData implements SyncWorldClocksPayload {

    List<WorldClockData> clockData;
}
