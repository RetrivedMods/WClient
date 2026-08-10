package org.cloudburstmc.protocol.bedrock.codec.v1001.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v407.serializer.InventoryTransactionSerializer_v407;
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.*;
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket;
import org.cloudburstmc.protocol.common.util.VarInts;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class InventoryTransactionSerializer_v1001 extends InventoryTransactionSerializer_v407 {

    public static final InventoryTransactionSerializer_v1001 INSTANCE = new InventoryTransactionSerializer_v1001();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, InventoryTransactionPacket packet) {
        int legacyRequestId = packet.getLegacyRequestId();
        VarInts.writeInt(buffer, legacyRequestId);

        if (legacyRequestId < -1 && (legacyRequestId & 1) == 0) {
            buffer.writeBoolean(true);
            helper.writeArray(buffer, packet.getLegacySlots(), (buf, packetHelper, data) -> {
                buf.writeByte(data.getContainerId());
                packetHelper.writeByteArray(buf, data.getSlots());
            });
        } else {
            buffer.writeBoolean(false);
        }

        buffer.writeBoolean(true);
        InventoryTransactionType transactionType = packet.getTransactionType();
        VarInts.writeUnsignedInt(buffer, transactionType.ordinal());

        buffer.writeBoolean(true);
        writeInventoryActions(buffer, helper, packet.getActions());

        switch (transactionType) {
            case ITEM_USE:
                this.writeItemUse(buffer, helper, packet);
                break;
            case ITEM_USE_ON_ENTITY:
                this.writeItemUseOnEntity(buffer, helper, packet);
                break;
            case ITEM_RELEASE:
                this.writeItemRelease(buffer, helper, packet);
                break;
        }
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, InventoryTransactionPacket packet) {
        int legacyRequestId = VarInts.readInt(buffer);
        packet.setLegacyRequestId(legacyRequestId);

        if (buffer.readBoolean()) {
            if (legacyRequestId < -1 && (legacyRequestId & 1) == 0) {
                helper.readArray(buffer, packet.getLegacySlots(), (buf, packetHelper) -> {
                    int containerId = buffer.readUnsignedByte();
                    byte[] slots = packetHelper.readByteArray(buf, 89); // 89 seems to be the largest slot count
                    return new LegacySetItemSlotData(containerId, slots);
                });
            }
        }

        if (!buffer.readBoolean()) {
            throw new IllegalStateException("Expected InventoryTransactionType");
        }
        InventoryTransactionType transactionType = InventoryTransactionType.values()[VarInts.readUnsignedInt(buffer)];
        packet.setTransactionType(transactionType);

        if (!buffer.readBoolean()) {
            throw new IllegalStateException("Expected InventoryActionData");
        }
        readInventoryActions(buffer, helper, packet.getActions());

        switch (transactionType) {
            case ITEM_USE:
                this.readItemUse(buffer, helper, packet);
                break;
            case ITEM_USE_ON_ENTITY:
                this.readItemUseOnEntity(buffer, helper, packet);
                break;
            case ITEM_RELEASE:
                this.readItemRelease(buffer, helper, packet);
                break;
        }
    }

    public void writeItemUse(ByteBuf buffer, BedrockCodecHelper helper, InventoryTransactionPacket packet) {
        VarInts.writeInt(buffer, packet.getActionType());
        buffer.writeByte(packet.getTriggerType().ordinal());
        helper.writeBlockPosition(buffer, packet.getBlockPosition());
        buffer.writeByte(packet.getBlockFace());
        VarInts.writeInt(buffer, packet.getHotbarSlot());
        helper.writeNetworkItemStackDescriptor(buffer, packet.getItemInHand());
        helper.writeVector3f(buffer, packet.getPlayerPosition());
        helper.writeVector3f(buffer, packet.getClickPosition());
        VarInts.writeUnsignedInt(buffer, packet.getBlockDefinition().getRuntimeId());
        buffer.writeByte(packet.getClientInteractPrediction().ordinal());
        buffer.writeByte(packet.getClientCooldownState());
    }

    public void readItemUse(ByteBuf buffer, BedrockCodecHelper helper, InventoryTransactionPacket packet) {
        packet.setActionType(VarInts.readInt(buffer));
        packet.setTriggerType(ItemUseTransaction.TriggerType.values()[buffer.readUnsignedByte()]);
        packet.setBlockPosition(helper.readBlockPosition(buffer));
        packet.setBlockFace(buffer.readUnsignedByte());
        packet.setHotbarSlot(VarInts.readInt(buffer));
        packet.setItemInHand(helper.readNetworkItemStackDescriptor(buffer));
        packet.setPlayerPosition(helper.readVector3f(buffer));
        packet.setClickPosition(helper.readVector3f(buffer));
        packet.setBlockDefinition(helper.getBlockDefinitions().getDefinition(VarInts.readUnsignedInt(buffer)));
        packet.setClientInteractPrediction(ItemUseTransaction.PredictedResult.values()[buffer.readUnsignedByte()]);
        packet.setClientCooldownState(buffer.readByte());
    }

    @Override
    public void readItemUseOnEntity(ByteBuf buffer, BedrockCodecHelper helper, InventoryTransactionPacket packet) {
        packet.setRuntimeEntityId(VarInts.readUnsignedLong(buffer));
        packet.setActionType(VarInts.readInt(buffer));
        packet.setHotbarSlot(VarInts.readInt(buffer));
        packet.setItemInHand(helper.readNetworkItemStackDescriptor(buffer));
        packet.setPlayerPosition(helper.readVector3f(buffer));
        packet.setClickPosition(helper.readVector3f(buffer));
    }

    @Override
    public void writeItemUseOnEntity(ByteBuf buffer, BedrockCodecHelper helper, InventoryTransactionPacket packet) {
        VarInts.writeUnsignedLong(buffer, packet.getRuntimeEntityId());
        VarInts.writeInt(buffer, packet.getActionType());
        VarInts.writeInt(buffer, packet.getHotbarSlot());
        helper.writeNetworkItemStackDescriptor(buffer, packet.getItemInHand());
        helper.writeVector3f(buffer, packet.getPlayerPosition());
        helper.writeVector3f(buffer, packet.getClickPosition());
    }

    @Override
    public void readItemRelease(ByteBuf buffer, BedrockCodecHelper helper, InventoryTransactionPacket packet) {
        packet.setActionType(VarInts.readInt(buffer));
        packet.setHotbarSlot(VarInts.readInt(buffer));
        packet.setItemInHand(helper.readNetworkItemStackDescriptor(buffer));
        packet.setHeadPosition(helper.readVector3f(buffer));
    }

    @Override
    public void writeItemRelease(ByteBuf buffer, BedrockCodecHelper helper, InventoryTransactionPacket packet) {
        VarInts.writeInt(buffer, packet.getActionType());
        VarInts.writeInt(buffer, packet.getHotbarSlot());
        helper.writeNetworkItemStackDescriptor(buffer, packet.getItemInHand());
        helper.writeVector3f(buffer, packet.getHeadPosition());
    }

    public void readInventoryActions(ByteBuf buffer, BedrockCodecHelper helper, List<InventoryActionData> actions) {
        helper.readArray(buffer, actions, (buf, h) -> {
            InventorySource source = readSource(buf);
            int slot = VarInts.readUnsignedInt(buf);
            ItemData fromItem = h.readNetworkItemStackDescriptor(buf);
            ItemData toItem = h.readNetworkItemStackDescriptor(buf);

            return new InventoryActionData(source, slot, fromItem, toItem);
        }, helper.getEncodingSettings().maxInventoryActionsOrRequests());
    }

    public void writeInventoryActions(ByteBuf buffer, BedrockCodecHelper helper, List<InventoryActionData> actions) {
        helper.writeArray(buffer, actions, (buf, h, action) -> {
            writeSource(buf, action.getSource());
            VarInts.writeUnsignedInt(buf, action.getSlot());
            h.writeNetworkItemStackDescriptor(buf, action.getFromItem());
            h.writeNetworkItemStackDescriptor(buf, action.getToItem());
        });
    }

    protected InventorySource readSource(ByteBuf buffer) {
        InventorySource.Type type = InventorySource.Type.byId(VarInts.readUnsignedInt(buffer));

        int containerId = 0;
        InventorySource.Flag flag = null;
        if (buffer.readBoolean() && buffer.readBoolean()) containerId = buffer.readByte();
        if (buffer.readBoolean() && buffer.readBoolean()) flag = InventorySource.Flag.values()[VarInts.readUnsignedInt(buffer)];
        switch (type) {
            case CONTAINER:
                return InventorySource.fromContainerWindowId(containerId);
            case GLOBAL:
                return InventorySource.fromGlobalInventory();
            case WORLD_INTERACTION:
                if (flag == null) throw new IllegalStateException();
                return InventorySource.fromWorldInteraction(flag);
            case CREATIVE:
                return InventorySource.fromCreativeInventory();
            case NON_IMPLEMENTED_TODO:
                return InventorySource.fromNonImplementedTodo(containerId);
            case UNTRACKED_INTERACTION_UI:
                return InventorySource.fromUntrackedInteractionUI(containerId);
            default:
                return InventorySource.fromInvalid();
        }
    }

    protected void writeSource(ByteBuf buffer, InventorySource inventorySource) {
        requireNonNull(inventorySource, "InventorySource was null");

        VarInts.writeUnsignedInt(buffer, inventorySource.getType().id());

        buffer.writeBoolean(true);
        switch (inventorySource.getType()) {
            case CONTAINER:
            case NON_IMPLEMENTED_TODO:
                buffer.writeBoolean(true);
                buffer.writeByte(inventorySource.getContainerId());
                break;
            default:
                buffer.writeBoolean(false);
                break;
        }

        buffer.writeBoolean(true);
        switch (inventorySource.getType()) {
            case WORLD_INTERACTION:
                buffer.writeBoolean(true);
                VarInts.writeUnsignedInt(buffer, inventorySource.getFlag().ordinal());
                break;
            default:
                buffer.writeBoolean(false);
                break;
        }
    }
}
