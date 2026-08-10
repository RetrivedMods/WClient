package org.cloudburstmc.protocol.bedrock.data;

import lombok.Value;

import java.util.UUID;

@Value
public class GatheringsConfigurationJoinInfo {

    UUID experienceId;
    String experienceName;
    UUID worldId;
    String worldName;
    String creatorId;
    UUID targetId;
    String scenarioId;
    String serverId;
}
