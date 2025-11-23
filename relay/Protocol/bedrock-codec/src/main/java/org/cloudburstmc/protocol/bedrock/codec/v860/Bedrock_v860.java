package org.cloudburstmc.protocol.bedrock.codec.v860;

import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.codec.v291.serializer.EntityEventSerializer_v291;
import org.cloudburstmc.protocol.bedrock.codec.v844.Bedrock_v844;
import org.cloudburstmc.protocol.bedrock.codec.v859.serializer.*;
import org.cloudburstmc.protocol.bedrock.codec.v860.serializer.BiomeDefinitionListSerializer_v860;
import org.cloudburstmc.protocol.bedrock.codec.v860.serializer.CameraInstructionSerializer_v860;
import org.cloudburstmc.protocol.bedrock.codec.v860.serializer.GraphicsParameterOverrideSerializer_v860;
import org.cloudburstmc.protocol.bedrock.codec.v860.serializer.ShowStoreOfferSerializer_v860;
import org.cloudburstmc.protocol.bedrock.data.PacketRecipient;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityEventType;
import org.cloudburstmc.protocol.bedrock.packet.*;
import org.cloudburstmc.protocol.common.util.TypeMap;

public class Bedrock_v860 extends Bedrock_v844 {

    protected static final TypeMap<EntityEventType> ENTITY_EVENTS = Bedrock_v844.ENTITY_EVENTS.toBuilder()
            .insert(79, EntityEventType.SHAKE_WETNESS_STOP)
            .build();

    public static final BedrockCodec CODEC = Bedrock_v844.CODEC.toBuilder()
            .raknetProtocolVersion(11)
            .protocolVersion(860)
            .minecraftVersion("1.21.124")
            .updateSerializer(AnimatePacket.class, AnimateSerializer_v859.INSTANCE)
            .updateSerializer(BiomeDefinitionListPacket.class, BiomeDefinitionListSerializer_v860.INSTANCE)
            .updateSerializer(CameraInstructionPacket.class, CameraInstructionSerializer_v860.INSTANCE)
            .updateSerializer(EntityEventPacket.class, new EntityEventSerializer_v291(ENTITY_EVENTS))
            .updateSerializer(ShowStoreOfferPacket.class, ShowStoreOfferSerializer_v860.INSTANCE)
            .registerPacket(GraphicsParameterOverridePacket::new, GraphicsParameterOverrideSerializer_v860.INSTANCE, 331, PacketRecipient.CLIENT)
            .build();
}
