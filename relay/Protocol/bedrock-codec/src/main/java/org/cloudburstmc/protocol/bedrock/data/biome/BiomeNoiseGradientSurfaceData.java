package org.cloudburstmc.protocol.bedrock.data.biome;

import lombok.Value;

import java.util.List;

@Value
public class BiomeNoiseGradientSurfaceData {

    List<Integer> nonReplaceableBlocks;
    List<NoiseBlockSpecifier> gradientBlocks;
    // NoiseDescriptor
    String noise; // "name" in v1001 or "seed" in v975
    int firstOctave;
    List<Float> amplitudes;
}
