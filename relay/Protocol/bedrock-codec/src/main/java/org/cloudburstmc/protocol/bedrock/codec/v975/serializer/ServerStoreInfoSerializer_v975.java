package org.cloudburstmc.protocol.bedrock.codec.v975.serializer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodecHelper;
import org.cloudburstmc.protocol.bedrock.codec.BedrockPacketSerializer;
import org.cloudburstmc.protocol.bedrock.data.ClientStoreEntrypointConfiguration;
import org.cloudburstmc.protocol.bedrock.packet.ServerStoreInfoPacket;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerStoreInfoSerializer_v975 implements BedrockPacketSerializer<ServerStoreInfoPacket> {

    public static final ServerStoreInfoSerializer_v975 INSTANCE = new ServerStoreInfoSerializer_v975();

    @Override
    public void serialize(ByteBuf buffer, BedrockCodecHelper helper, ServerStoreInfoPacket packet) {
        helper.writeOptionalNull(buffer, packet.getStore(), (buf, h, store) -> {
            h.writeString(buf, store.getStoreId());
            h.writeString(buf, store.getStoreName());
        });
    }

    @Override
    public void deserialize(ByteBuf buffer, BedrockCodecHelper helper, ServerStoreInfoPacket packet) {
        packet.setStore(helper.readOptional(buffer, null, (buf, h) ->
                new ClientStoreEntrypointConfiguration(h.readString(buf), h.readString(buf))));
    }
}
