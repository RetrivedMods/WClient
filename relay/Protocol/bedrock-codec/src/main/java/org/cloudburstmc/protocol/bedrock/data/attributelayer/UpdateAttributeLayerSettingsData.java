package org.cloudburstmc.protocol.bedrock.data.attributelayer;

import lombok.Value;

@Value
public class UpdateAttributeLayerSettingsData implements AttributeLayerSyncPayload {

    String layerName;
    int dimension;
    AttributeLayerSettings settings;
}
