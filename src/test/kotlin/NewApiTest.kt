import io.netty.buffer.Unpooled
import kotlinx.serialization.Serializable
import kotlinx.serialization.minecraft.*
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.BlockPos
import org.junit.jupiter.api.Test
import utils.assertEquals

@Serializable
data class Dog(val woof: Int)
class NewApiTest {
    @Test
    fun newBufApiWorks() {
        val dog = Dog(3)
        val buf = PacketByteBuf(Unpooled.buffer())
        Buf.encodeToByteBuf(dog, buf)
        val back = Buf.decodeFromByteBuf<Dog>(buf)
        assertEquals(dog, back)
    }

    @Test
    fun newNbtApiWorks() {
        val dog = Dog(3)
        val nbt = Nbt.encodeToNbt(dog)
        val back = Nbt.decodeFromNbt<Dog>(nbt)
        assertEquals(dog, back)
    }

    @Test
    fun newBufApiWorksWithBlockPos() {
        val pos = BlockPos(1, 2, 3)
        val buf = PacketByteBuf(Unpooled.buffer())
        Buf.encodeToByteBuf(pos, buf)
        val back = Buf.decodeFromByteBuf<BlockPos>(buf)
        assertEquals(pos, back)
    }

    @Test
    fun newNbtApiWorksWithBlockPos() {
        val pos = BlockPos(1, 2, 3)
        val nbt = Nbt.encodeToNbt(pos)
        val back = Nbt.decodeFromNbt<BlockPos>(nbt)
        assertEquals(pos, back)
    }
}
