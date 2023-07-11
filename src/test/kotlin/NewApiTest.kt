import io.netty.buffer.Unpooled
import kotlinx.serialization.Serializable
import kotlinx.serialization.minecraft.*
import net.minecraft.network.PacketByteBuf
import kotlin.test.Test
import kotlin.test.assertEquals

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
}
