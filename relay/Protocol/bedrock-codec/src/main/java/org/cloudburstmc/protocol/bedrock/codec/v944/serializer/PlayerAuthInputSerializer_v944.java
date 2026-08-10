package org.cloudburstmc.protocol.bedrock.codec.v944.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v766.serializer.PlayerAuthInputSerializer_v766;
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.ItemUseTransaction;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerAuthInputSerializer_v944 extends PlayerAuthInputSerializer_v766 {

    public static final PlayerAuthInputSerializer_v944 INSTANCE = new PlayerAuthInputSerializer_v944();

    @Override
    protected void writeItemUseTransaction(ByteBuf buffer, BedrockCodecHelper helper, ItemUseTransaction transaction) {
        super.writeItemUseTransaction(buffer, helper, transaction);

        buffer.writeByte(transaction.getClientCooldownState());
    }

    @Override
    protected ItemUseTransaction readItemUseTransaction(ByteBuf buffer, BedrockCodecHelper helper) {
        ItemUseTransaction itemTransaction = super.readItemUseTransaction(buffer, helper);
        itemTransaction.setClientCooldownState(buffer.readByte());
        return itemTransaction;
    }
}
