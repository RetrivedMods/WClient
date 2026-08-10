package org.cloudburstmc.protocol.bedrock.data.camera;

import lombok.Value;

@Value
public class AimAssistActorPriorityData {

    int presetIndex;
    int categoryIndex;
    int actorIndex;
    int priorityValue;
}
