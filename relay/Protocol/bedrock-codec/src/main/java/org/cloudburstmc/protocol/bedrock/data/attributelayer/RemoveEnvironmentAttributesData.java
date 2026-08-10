package org.cloudburstmc.protocol.bedrock.data.attributelayer;

import lombok.Value;

import java.util.List;

@Value
public class RemoveEnvironmentAttributesData implements AttributeLayerSyncPayload {

    String layerName;
    int dimension;
    List<String> attributes;
}
