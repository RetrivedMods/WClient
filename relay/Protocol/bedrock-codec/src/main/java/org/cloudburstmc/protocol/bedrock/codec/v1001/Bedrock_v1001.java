package org.cloudburstmc.protocol.bedrock.codec.v1001;

import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.codec.v1001.serializer.*;
import org.cloudburstmc.protocol.bedrock.codec.v975.Bedrock_v975;
import org.cloudburstmc.protocol.bedrock.data.PacketRecipient;
import org.cloudburstmc.protocol.bedrock.data.SoundEvent;
import org.cloudburstmc.protocol.bedrock.packet.*;
import org.cloudburstmc.protocol.common.util.TypeMap;

public class Bedrock_v1001 extends Bedrock_v975 {

    protected static final TypeMap<SoundEvent> SOUND_EVENTS = Bedrock_v975.SOUND_EVENTS
            .toBuilder()
            .replace(601, SoundEvent.SLIME_LANDING)
            .insert(602, SoundEvent.ABSORB_BLOCK)
            .insert(603, SoundEvent.EJECT_BLOCK)
            .insert(604, SoundEvent.GEYSER_ERUPTION_START)
            .insert(605, SoundEvent.GEYSER_ERUPTION_ACTIVE)
            .insert(606, SoundEvent.RECORD_BOUNCE)
            .insert(607, SoundEvent.BUCKET_FILL_LAND_ANIMAL)
            .insert(608, SoundEvent.BUCKET_EMPTY_LAND_ANIMAL)
            .insert(609, SoundEvent.GEYSER_CONTINUOUS_ERUPTION_START)
            .insert(610, SoundEvent.GEYSER_CONTINUOUS_ERUPTION_ACTIVE)
            .insert(611, SoundEvent.UNDEFINED)
            .build();

    public static final BedrockCodec CODEC = Bedrock_v975.CODEC.toBuilder()
            .protocolVersion(1001)
            .minecraftVersion("1.26.30")
            .helper(() -> new BedrockCodecHelper_v1001(ENTITY_DATA, GAME_RULE_TYPES, ITEM_STACK_REQUEST_TYPES, CONTAINER_SLOT_TYPES, PLAYER_ABILITIES, TEXT_PROCESSING_ORIGINS))
            .updateSerializer(BiomeDefinitionListPacket.class, BiomeDefinitionListSerializer_v1001.INSTANCE)
            .updateSerializer(BossEventPacket.class, BossEventSerializer_v1001.INSTANCE)
            .updateSerializer(ClientboundAttributeLayerSyncPacket.class, ClientboundAttributeLayerSyncSerializer_v1001.INSTANCE)
            .updateSerializer(ClientCacheBlobStatusPacket.class, ClientCacheBlobStatusSerializer_v1001.INSTANCE)
            .updateSerializer(DebugDrawerPacket.class, DebugDrawerSerializer_v1001.INSTANCE)
            .updateSerializer(GraphicsParameterOverridePacket.class, GraphicsParameterOverrideSerializer_v1001.INSTANCE)
            .updateSerializer(InventoryContentPacket.class, InventoryContentSerializer_v1001.INSTANCE)
            .updateSerializer(InventoryTransactionPacket.class, InventoryTransactionSerializer_v1001.INSTANCE)
            .updateSerializer(LevelSoundEventPacket.class, new LevelSoundEventSerializer_v1001(SOUND_EVENTS))
            .updateSerializer(MobArmorEquipmentPacket.class, MobArmorEquipmentSerializer_v1001.INSTANCE)
            .updateSerializer(ServerboundDiagnosticsPacket.class, ServerboundDiagnosticsSerializer_v1001.INSTANCE)
            .updateSerializer(StartGamePacket.class, StartGameSerializer_v1001.INSTANCE)
            .updateSerializer(SubChunkRequestPacket.class, SubChunkRequestSerializer_v1001.INSTANCE)
            .registerPacket(ClientboundUpdateSoundDataPacket::new, ClientboundUpdateSoundDataSerializer_v1001.INSTANCE, 348, PacketRecipient.CLIENT)
            .registerPacket(SendPartyDestinationCookiePacket::new, SendPartyDestinationCookieSerializer_v1001.INSTANCE, 349, PacketRecipient.CLIENT)
            .registerPacket(PartyDestinationCookieResponsePacket::new, PartyDestinationCookieResponseSerializer_v1001.INSTANCE, 350, PacketRecipient.SERVER)
            .build();
}
