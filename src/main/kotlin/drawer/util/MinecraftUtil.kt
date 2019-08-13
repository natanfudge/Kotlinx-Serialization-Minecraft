package drawer.util

import io.netty.buffer.Unpooled
import net.minecraft.util.PacketByteBuf

internal fun bufferedPacketByteBuf() =PacketByteBuf(Unpooled.buffer())