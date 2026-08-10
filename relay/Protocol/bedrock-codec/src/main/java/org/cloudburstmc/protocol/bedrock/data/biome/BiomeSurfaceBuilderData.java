package org.cloudburstmc.protocol.bedrock.data.biome;

import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;

@Value
public class BiomeSurfaceBuilderData {

    @Nullable
    BiomeSurfaceMaterialData surfaceMaterials;
    boolean hasDefaultOverworldSurface;
    boolean hasSwampSurface;
    boolean hasFrozenOceanSurface;
    boolean hasTheEndSurface;
    @Nullable
    BiomeMesaSurfaceData mesaSurface;
    @Nullable
    BiomeCappedSurfaceData cappedSurface;
    @Nullable
    BiomeNoiseGradientSurfaceData noiseGradientSurface;
}
