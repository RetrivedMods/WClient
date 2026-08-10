package org.cloudburstmc.protocol.bedrock.data.event;

public interface EventData {

    EventDataType getType();

    default int getPayloadType() {
        return -1;
    }
}
