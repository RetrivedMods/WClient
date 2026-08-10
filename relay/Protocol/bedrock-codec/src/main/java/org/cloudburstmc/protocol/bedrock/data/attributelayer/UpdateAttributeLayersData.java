package org.cloudburstmc.protocol.bedrock.data.attributelayer;

import lombok.Value;

import java.util.List;

@Value
public class UpdateAttributeLayersData implements AttributeLayerSyncPayload {

    List<AttributeLayerData> attributeLayers;
}
