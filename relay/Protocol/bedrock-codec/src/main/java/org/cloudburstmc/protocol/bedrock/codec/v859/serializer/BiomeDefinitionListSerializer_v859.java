package org.cloudburstmc.protocol.bedrock.codec.v859.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v844.serializer.BiomeDefinitionListSerializer_v844;
import org.cloudburstmc.protocol.bedrock.data.biome.*;
import org.cloudburstmc.protocol.common.util.SequencedHashSet;

import java.util.ArrayList;
import java.util.List;

public class BiomeDefinitionListSerializer_v859 extends BiomeDefinitionListSerializer_v844 {

    public static final BiomeDefinitionListSerializer_v859 INSTANCE = new BiomeDefinitionListSerializer_v859();

    @Override
    protected void writeDefinitionChunkGen(ByteBuf buffer, BedrockCodecHelper helper, BiomeDefinitionChunkGenData definitionChunkGen,
                                           SequencedHashSet<String> strings) {
        super.writeDefinitionChunkGen(buffer, helper, definitionChunkGen, strings);
        helper.writeOptionalNull(buffer, definitionChunkGen.getBiomeReplacements(), this::writeBiomeReplacementsData);
    }

    @Override
    protected BiomeDefinitionChunkGenData readDefinitionChunkGen(ByteBuf buffer, BedrockCodecHelper helper, List<String> strings) {
        BiomeClimateData climate = helper.readOptional(buffer, null, this::readClimate);
        List<BiomeConsolidatedFeatureData> consolidatedFeatures = helper.readOptional(buffer, null,
                (buf, aHelper) -> this.readConsolidatedFeatures(buf, aHelper, strings));
        BiomeMountainParamsData mountainParams = helper.readOptional(buffer, null, this::readMountainParamsData);
        BiomeSurfaceMaterialAdjustmentData surfaceMaterialAdjustment = helper.readOptional(buffer, null,
                (buf, aHelper) -> this.readSurfaceMaterialAdjustment(buf, aHelper, strings));
        BiomeSurfaceMaterialData surfaceMaterial = helper.readOptional(buffer, null, this::readSurfaceMaterial);
        boolean hasDefaultOverworldSurface = buffer.readBoolean();
        boolean hasSwampSurface = buffer.readBoolean();
        boolean hasFrozenOceanSurface = buffer.readBoolean();
        boolean hasTheEndSurface = buffer.readBoolean();
        BiomeMesaSurfaceData mesaSurface = helper.readOptional(buffer, null, this::readMesaSurface);
        BiomeCappedSurfaceData cappedSurface = helper.readOptional(buffer, null, this::readCappedSurface);
        BiomeOverworldGenRulesData overworldGenRules = helper.readOptional(buffer, null,
                (buf, aHelper) -> this.readOverworldGenRules(buf, aHelper, strings));
        BiomeMultinoiseGenRulesData multinoiseGenRules = helper.readOptional(buffer, null, this::readMultinoiseGenRules);
        BiomeLegacyWorldGenRulesData legacyWorldGenRules = helper.readOptional(buffer, null,
                (buf, aHelper) -> this.readLegacyWorldGenRules(buf, aHelper, strings));
        List<BiomeReplacementData> replacementsData = helper.readOptional(buffer, null, this::readBiomeReplacementsData);

        return new BiomeDefinitionChunkGenData(climate, consolidatedFeatures,
                mountainParams, surfaceMaterialAdjustment,
                surfaceMaterial, hasDefaultOverworldSurface, hasSwampSurface,
                hasFrozenOceanSurface, hasTheEndSurface,
                mesaSurface, cappedSurface,
                overworldGenRules, multinoiseGenRules,
                legacyWorldGenRules, replacementsData, null, null, null);
    }

    protected void writeBiomeReplacementsData(ByteBuf buffer, BedrockCodecHelper helper, List<BiomeReplacementData> replacementsData) {
        helper.writeArray(buffer, replacementsData, (buf, h, replacementData) -> {
            buffer.writeShortLE(replacementData.getBiome());
            buffer.writeShortLE(replacementData.getDimension());
            helper.writeArray(buffer, replacementData.getTargetBiomes(), (buf1, value) -> buf1.writeShortLE(value));
            buffer.writeFloatLE(replacementData.getAmount());
            buffer.writeFloatLE(replacementData.getNoiseFrequencyScale());
            buffer.writeIntLE(replacementData.getReplacementIndex());
        });
    }

    protected List<BiomeReplacementData> readBiomeReplacementsData(ByteBuf buffer, BedrockCodecHelper helper) {
        List<BiomeReplacementData> replacementsData = new ArrayList<>();
        helper.readArray(buffer, replacementsData, (buf, h) -> {
            int biome = buf.readUnsignedShortLE();
            int dimension = buf.readUnsignedShortLE();
            List<Short> targetBiomes = new ArrayList<>();
            h.readArray(buf, targetBiomes, (buf2, aHelper) -> (short) buf2.readUnsignedShortLE());
            float amount = buf.readFloatLE();
            float noiseFrequencyScale = buf.readFloatLE();
            int replacementIndex = (int) buf.readUnsignedIntLE();

            return new BiomeReplacementData(biome, dimension, targetBiomes, amount, noiseFrequencyScale, replacementIndex);
        });
        return replacementsData;
    }
}
