import com.radiantbyte.novarelay.address.NovaAddress
import com.radiantbyte.novarelay.definition.Definitions
import com.radiantbyte.novarelay.listener.AutoCodecPacketListener
import com.radiantbyte.novarelay.listener.GamingPacketHandler
import com.radiantbyte.novarelay.listener.OnlineLoginPacketListener
import com.radiantbyte.novarelay.listener.TransferPacketListener
import com.radiantbyte.novarelay.util.authorize
import com.radiantbyte.novarelay.util.captureGamePacket
import com.radiantbyte.novarelay.util.refresh

fun main() {
    val localAddress = NovaAddress("0.0.0.0", 19132)
    val remoteAddress = NovaAddress("ntest.easecation.net", 19132)

    Definitions.loadBlockPalette()

    var fullBedrockSession = authorize()
    if (fullBedrockSession.isExpired) {
        fullBedrockSession = fullBedrockSession.refresh()
    }

    captureGamePacket(
        localAddress = localAddress,
        remoteAddress = remoteAddress
    ) {
        listeners.add(AutoCodecPacketListener(this))
        listeners.add(OnlineLoginPacketListener(this, fullBedrockSession))
        listeners.add(GamingPacketHandler(this))
        listeners.add(TransferPacketListener(this))
        listeners.add(MessagePacketListener(this))
    }
    println("Relay started at ${localAddress.hostName}:${localAddress.port}")
}