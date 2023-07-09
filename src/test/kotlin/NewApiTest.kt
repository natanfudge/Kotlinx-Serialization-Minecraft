import drawer.*
import kotlinx.serialization.Serializable
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import kotlin.test.Test
import kotlin.test.assertEquals

@Serializable
data class Dog(val woof: Int)
class NewApiTest {
    @Test
    fun newBufApiWorks() {
        val dog = Dog(3)
        val buf = PacketByteBufs.create()
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

//TODO: 1. Run refreshVersions
// 2. Optimize a reasonable amount of the Serializers TODOs