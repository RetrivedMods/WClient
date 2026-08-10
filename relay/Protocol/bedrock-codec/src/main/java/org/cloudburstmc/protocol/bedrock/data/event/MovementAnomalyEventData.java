package org.cloudburstmc.protocol.bedrock.data.event;

import lombok.Value;

@Value
@Deprecated
public class MovementAnomalyEventData implements EventData {
    private final int eventType;
    private final float cheatingScore;
    private final float averagePositionDelta;
    private final float totalPositionDelta;
    private final float minPositionDelta;
    private final float maxPositionDelta;

    @Override
    public EventDataType getType() {
        return EventDataType.MOVEMENT_ANOMALY;
    }
}
