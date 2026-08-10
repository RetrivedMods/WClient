package org.cloudburstmc.protocol.bedrock.codec.v924;

import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.codec.EntityDataTypeMap;
import org.cloudburstmc.protocol.bedrock.codec.v786.serializer.LevelSoundEventSerializer_v786;
import org.cloudburstmc.protocol.bedrock.codec.v898.BedrockCodecHelper_v898;
import org.cloudburstmc.protocol.bedrock.codec.v898.Bedrock_v898;
import org.cloudburstmc.protocol.bedrock.codec.v924.serializer.*;
import org.cloudburstmc.protocol.bedrock.data.PacketRecipient;
import org.cloudburstmc.protocol.bedrock.data.SoundEvent;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataFormat;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes;
import org.cloudburstmc.protocol.bedrock.packet.*;
import org.cloudburstmc.protocol.common.util.TypeMap;

public class Bedrock_v924 extends Bedrock_v898 {

    protected static final EntityDataTypeMap ENTITY_DATA = Bedrock_v898.ENTITY_DATA
            .toBuilder()
            .insert(EntityDataTypes.ARROW_SHOOTER_ID, 17, EntityDataFormat.LONG)
            .insert(EntityDataTypes.FIREWORK_DIRECTION, 17, EntityDataFormat.VECTOR3F)
            .insert(EntityDataTypes.FIREWORK_SHOOTER_ID, 18, EntityDataFormat.LONG)
            .insert(EntityDataTypes.AIM_ASSIST_PRIORITY_PRESET_ID, 136, EntityDataFormat.INT)
            .insert(EntityDataTypes.AIM_ASSIST_PRIORITY_CATEGORY_ID, 137, EntityDataFormat.INT)
            .insert(EntityDataTypes.AIM_ASSIST_PRIORITY_ACTOR_ID, 138, EntityDataFormat.INT)
            .build();

    protected static final TypeMap<SoundEvent> SOUND_EVENTS = Bedrock_v898.SOUND_EVENTS
            .toBuilder()
            .replace(578, SoundEvent.SADDLE_IN_WATER)
            .insert(579, SoundEvent.STONE_SPEAR_ATTACK_HIT)
            .insert(580, SoundEvent.IRON_SPEAR_ATTACK_HIT)
            .insert(581, SoundEvent.COPPER_SPEAR_ATTACK_HIT)
            .insert(582, SoundEvent.GOLDEN_SPEAR_ATTACK_HIT)
            .insert(583, SoundEvent.DIAMOND_SPEAR_ATTACK_HIT)
            .insert(584, SoundEvent.NETHERITE_SPEAR_ATTACK_HIT)
            .insert(585, SoundEvent.STONE_SPEAR_ATTACK_MISS)
            .insert(586, SoundEvent.IRON_SPEAR_ATTACK_MISS)
            .insert(587, SoundEvent.COPPER_SPEAR_ATTACK_MISS)
            .insert(588, SoundEvent.GOLDEN_SPEAR_ATTACK_MISS)
            .insert(589, SoundEvent.DIAMOND_SPEAR_ATTACK_MISS)
            .insert(590, SoundEvent.NETHERITE_SPEAR_ATTACK_MISS)
            .insert(591, SoundEvent.STONE_SPEAR_USE)
            .insert(592, SoundEvent.IRON_SPEAR_USE)
            .insert(593, SoundEvent.COPPER_SPEAR_USE)
            .insert(594, SoundEvent.GOLDEN_SPEAR_USE)
            .insert(595, SoundEvent.DIAMOND_SPEAR_USE)
            .insert(596, SoundEvent.NETHERITE_SPEAR_USE)
            .insert(597, SoundEvent.UNDEFINED)

            .build();

    public static final BedrockCodec CODEC = Bedrock_v898.CODEC.toBuilder()
            .protocolVersion(924)
            .minecraftVersion("1.26.0")
            .helper(() -> new BedrockCodecHelper_v898(ENTITY_DATA, GAME_RULE_TYPES, ITEM_STACK_REQUEST_TYPES, CONTAINER_SLOT_TYPES, PLAYER_ABILITIES, TEXT_PROCESSING_ORIGINS))
            .updateSerializer(BiomeDefinitionListPacket.class, BiomeDefinitionListSerializer_v924.INSTANCE)
            .updateSerializer(BookEditPacket.class, BookEditSerializer_v924.INSTANCE)
            .updateSerializer(CameraAimAssistPresetsPacket.class, CameraAimAssistPresetsSerializer_v924.INSTANCE)
            .updateSerializer(CameraInstructionPacket.class, CameraInstructionSerializer_v924.INSTANCE)
            .updateSerializer(ClientboundDataStorePacket.class, ClientboundDataStoreSerializer_v924.INSTANCE)
            .updateSerializer(DebugDrawerPacket.class, DebugDrawerSerializer_v924.INSTANCE)
            .updateSerializer(GraphicsParameterOverridePacket.class, GraphicsParameterOverrideSerializer_v924.INSTANCE)
            .updateSerializer(LevelSoundEventPacket.class, new LevelSoundEventSerializer_v786(SOUND_EVENTS))
            .updateSerializer(ServerboundDataStorePacket.class, ServerboundDataStoreSerializer_v924.INSTANCE)
            .updateSerializer(ServerboundDiagnosticsPacket.class, ServerboundDiagnosticsSerializer_v924.INSTANCE)
            .updateSerializer(StartGamePacket.class, StartGameSerializer_v924.INSTANCE)
            .updateSerializer(TextPacket.class, TextSerializer_v924.INSTANCE)
            .registerPacket(ClientboundDataDrivenUIShowScreenPacket::new, ClientboundDataDrivenUIShowScreenSerializer_v924.INSTANCE, 333, PacketRecipient.CLIENT)
            .registerPacket(ClientboundDataDrivenUICloseScreenPacket::new, ClientboundDataDrivenUICloseScreenSerializer_v924.INSTANCE, 334, PacketRecipient.CLIENT)
            .registerPacket(ClientboundDataDrivenUIReloadPacket::new, ClientboundDataDrivenUIReloadSerializer_v924.INSTANCE, 335, PacketRecipient.CLIENT)
            .registerPacket(ClientboundTextureShiftPacket::new, ClientboundTextureShiftSerializer_v924.INSTANCE, 336, PacketRecipient.CLIENT)
            .registerPacket(VoxelShapesPacket::new, VoxelShapesSerializer_v924.INSTANCE, 337, PacketRecipient.CLIENT)
            .registerPacket(CameraSplinePacket::new, CameraSplineSerializer_v924.INSTANCE, 338, PacketRecipient.CLIENT)
            .registerPacket(CameraAimAssistActorPriorityPacket::new, CameraAimAssistActorPrioritySerializer_v924.INSTANCE, 339, PacketRecipient.CLIENT)
            .build();
}
