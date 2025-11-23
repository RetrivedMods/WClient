package org.cloudburstmc.protocol.bedrock.codec.v860.serializer;

import io.netty.buffer.ByteBuf;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.v630.serializer.ShowStoreOfferSerializer_v630;
import org.cloudburstmc.protocol.bedrock.data.StoreOfferRedirectType;
import org.cloudburstmc.protocol.bedrock.packet.ShowStoreOfferPacket;

import java.util.UUID;

public class ShowStoreOfferSerializer_v860 extends ShowStoreOfferSerializer_v630 {

    public static final ShowStoreOfferSerializer_v860 INSTANCE = new ShowStoreOfferSerializer_v860();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, ShowStoreOfferPacket packet) {
        helper.writeUuid(buffer, UUID.fromString(packet.getOfferId()));
        buffer.writeByte(packet.getRedirectType().ordinal());
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, ShowStoreOfferPacket packet) {
        packet.setOfferId(helper.readUuid(buffer).toString());
        packet.setRedirectType(StoreOfferRedirectType.values()[buffer.readUnsignedByte()]);
    }
}
