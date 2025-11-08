import com.radiantbyte.novarelay.NovaRelaySession
import com.radiantbyte.novarelay.listener.NovaRelayPacketListener
import net.kyori.adventure.text.Component
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket

@Suppress("MemberVisibilityCanBePrivate")
class MessagePacketListener(
    val novaRelaySession: NovaRelaySession
) : NovaRelayPacketListener {

    override fun beforeClientBound(packet: BedrockPacket): Boolean {
        if (packet is PlayerAuthInputPacket && packet.tick % 10 == 0L) {
            novaRelaySession.clientBound(TextPacket().apply {
                type = TextPacket.Type.TIP
                // isNeedsTranslation = false
                sourceName = ""
                message = "[NovaRelay] v1.0"
                xuid = ""
                filteredMessage = ""
            })
        }
        return false
    }

}