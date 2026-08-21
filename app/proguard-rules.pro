-dontwarn **
-renamesourcefileattribute null
-keep class io.netty.** { *; }
-keep class org.cloudburstmc.netty.** { *; }
-keep class org.cloudburstmc.protocol.bedrock.codec.** { *; }
// com.retrivedmods.wclient.util.PacketFieldUtil sets several packet fields (TextPacket.message,
// MovePlayerPacket.onGround, PlayerHotbarPacket.selectHotbarSlot, etc.) via raw reflection using
// string field names, because those fields no longer have public setters in the current Bedrock
// protocol library. Nothing else in the app references those fields by name, so without a keep
// rule R8 is free to rename or strip them in a release build - which is exactly what caused
// NoSuchFieldException crashes (e.g. "Field 'message' not found in ...") the moment a module that
// hits one of these reflective sets actually ran. Keeping the whole packet/data packages (not just
// the specific classes/fields used today) means future PacketFieldUtil usages don't silently
// reintroduce the same crash.
-keep class org.cloudburstmc.protocol.bedrock.packet.** { *; }
-keep class org.cloudburstmc.protocol.bedrock.data.** { *; }
-keep @io.netty.channel.ChannelHandler$Sharable class *
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class net.raphimc.minecraftauth.** { *; }
-keep class net.lenni0451.commons.httpclient.** { *; }
-keep class com.radiantbyte.novaclient.game.AccountManager { *; }
