package org.cloudburstmc.protocol.bedrock.data.biome;

import lombok.Value;

@Value
public class BiomeClimateData {
    float temperature;
    float downfall;
    @Deprecated
    float redSporeDensity;
    @Deprecated
    float blueSporeDensity;
    @Deprecated
    float ashDensity;
    @Deprecated
    float whiteAshDensity;
    float snowAccumulationMin;
    float snowAccumulationMax;
}
