package org.cloudburstmc.protocol.bedrock.data.biome;

import lombok.Value;

@Value
public class NoiseBlockSpecifier {

    String noise;
    float threshold;
    float rangeMin;
    float rangeMax;
    int block;
}
