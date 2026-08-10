package org.cloudburstmc.protocol.bedrock.codec.v1001.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v975.serializer.BiomeDefinitionListSerializer_v975;
import org.cloudburstmc.protocol.bedrock.data.biome.*;

import java.util.ArrayList;
import java.util.List;

public class BiomeDefinitionListSerializer_v1001 extends BiomeDefinitionListSerializer_v975 {

    public static final BiomeDefinitionListSerializer_v1001 INSTANCE = new BiomeDefinitionListSerializer_v1001();

    @Override
    protected void writeNoiseGradientSurface(ByteBuf buffer, BedrockCodecHelper helper, BiomeNoiseGradientSurfaceData data) {
        helper.writeArray(buffer, data.getNonReplaceableBlocks(), ByteBuf::writeIntLE);
        helper.writeArray(buffer, data.getGradientBlocks(), (buf, val) -> {
            helper.writeString(buf, val.getNoise());
            buf.writeFloatLE(val.getThreshold());
            buf.writeFloatLE(val.getRangeMin());
            buf.writeFloatLE(val.getRangeMax());
            buf.writeIntLE(val.getBlock());
        });
        helper.writeString(buffer, data.getNoise());
        buffer.writeIntLE(data.getFirstOctave());
        helper.writeArray(buffer, data.getAmplitudes(), ByteBuf::writeFloatLE);
    }

    @Override
    protected BiomeNoiseGradientSurfaceData readNoiseGradientSurface(ByteBuf buffer, BedrockCodecHelper helper) {
        List<Integer> nonReplaceableBlocks = new ArrayList<>();
        helper.readArray(buffer, nonReplaceableBlocks, (buf, h) -> (int) buf.readUnsignedIntLE());
        List<NoiseBlockSpecifier> gradientBlocks = new ArrayList<>();
        helper.readArray(buffer, gradientBlocks, (buf, h) ->
                new NoiseBlockSpecifier(helper.readString(buf), buf.readFloatLE(), buf.readFloatLE(), buf.readFloatLE(), (int) buf.readUnsignedIntLE()));
        String noiseSeedString = helper.readString(buffer);
        int firstOctave = buffer.readIntLE();
        List<Float> amplitudes = new ArrayList<>();
        helper.readArray(buffer, amplitudes, (buf, h) -> buf.readFloatLE());
        return new BiomeNoiseGradientSurfaceData(nonReplaceableBlocks, gradientBlocks, noiseSeedString, firstOctave, amplitudes);
    }
}
