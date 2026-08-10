package org.cloudburstmc.protocol.bedrock.codec.v975.serializer;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v407.serializer.PlayerEnchantOptionsSerializer_v407;
import org.cloudburstmc.protocol.bedrock.data.inventory.EnchantData;
import org.cloudburstmc.protocol.bedrock.data.inventory.EnchantOptionData;
import org.cloudburstmc.protocol.common.util.VarInts;

import java.util.List;

public class PlayerEnchantOptionsSerializer_v975 extends PlayerEnchantOptionsSerializer_v407 {

    public static final PlayerEnchantOptionsSerializer_v975 INSTANCE = new PlayerEnchantOptionsSerializer_v975();

    @Override
    protected void writeOption(ByteBuf buffer, BedrockCodecHelper helper, EnchantOptionData option) {
        buffer.writeByte(option.getCost());
        buffer.writeIntLE(option.getPrimarySlot());
        helper.writeArray(buffer, option.getEnchants0(), this::serializeEnchant);
        helper.writeArray(buffer, option.getEnchants1(), this::serializeEnchant);
        helper.writeArray(buffer, option.getEnchants2(), this::serializeEnchant);
        helper.writeString(buffer, option.getEnchantName());
        VarInts.writeUnsignedInt(buffer, option.getEnchantNetId());
    }

    @Override
    protected EnchantOptionData readOption(ByteBuf buffer, BedrockCodecHelper helper) {
        int cost = buffer.readUnsignedByte();
        int primarySlot = buffer.readIntLE();
        List<EnchantData> enchants1 = new ObjectArrayList<>();
        helper.readArray(buffer, enchants1, this::deserializeEnchant);
        List<EnchantData> enchants2 = new ObjectArrayList<>();
        helper.readArray(buffer, enchants2, this::deserializeEnchant);
        List<EnchantData> enchants3 = new ObjectArrayList<>();
        helper.readArray(buffer, enchants3, this::deserializeEnchant);
        String enchantName = helper.readString(buffer);
        int enchantNetId = VarInts.readUnsignedInt(buffer);
        return new EnchantOptionData(cost, primarySlot, enchants1, enchants2, enchants3, enchantName, enchantNetId);
    }

    @Override
    protected void serializeEnchant(ByteBuf buffer, EnchantData enchant) {
        VarInts.writeUnsignedInt(buffer, enchant.getType());
        buffer.writeByte(enchant.getLevel());
    }

    @Override
    protected EnchantData deserializeEnchant(ByteBuf buffer) {
        int type = VarInts.readUnsignedInt(buffer);
        int level = buffer.readUnsignedByte();
        return new EnchantData(type, level);
    }
}
