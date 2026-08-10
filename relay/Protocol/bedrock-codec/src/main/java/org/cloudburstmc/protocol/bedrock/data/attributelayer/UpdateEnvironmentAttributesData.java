package org.cloudburstmc.protocol.bedrock.data.attributelayer;

import lombok.Value;

import java.util.List;

@Value
public class UpdateEnvironmentAttributesData implements AttributeLayerSyncPayload {

    String layerName;
    int dimension;
    List<EnvironmentAttributeData> attributes;
}
