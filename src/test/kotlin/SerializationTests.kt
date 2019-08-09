import drawer.*
import io.netty.buffer.Unpooled
import kotlinx.serialization.KSerializer
import net.minecraft.client.MinecraftClient
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.PacketByteBuf
import net.minecraft.world.World
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import utils.*
import kotlin.test.assertEquals

fun testTag(serializer: KSerializer<Any>, obj: Any): TestResult {
    // save
    val tag = serializer.convertToTag(obj)
    val back = serializer.fromTag(tag)
    // result
    return TestResult(obj, back, "${tag.size} items $tag")
}

fun testByteBuf(serializer: KSerializer<Any>, obj: Any): TestResult {
    // save
    val buf = PacketByteBuf(Unpooled.buffer())
    serializer.write(obj, toBuf = buf)
    val back = serializer.readFrom(buf)
    // result
    return TestResult(obj, back, "$buf")
}


class SerializationTests {
    @Test
    fun `TagEncoder serializes and deserializes correctly`() {
        testMethod(::testTag, supportsNull = true, verbose = false)
    }

    @Test
    fun `ByteBufEncoder serializes and deserializes correctly`() {
        testMethod(::testByteBuf, supportsNull = true, verbose = false)
    }


    @Test
    fun `TagEncoder can also serialize into an existing tag using put and getFromTag`() {
        val existing = CompoundTag()
        Shop.serializer().put(shop, inTag = existing)
        Zoo.serializer().put(zoo, inTag = existing)
        OtherFormats.serializer().put(otherFormats, existing)
        val shopBack = Shop.serializer().getFrom(existing)
        val zooBack = Zoo.serializer().getFrom(existing)
        val otherFormatsBack = OtherFormats.serializer().getFrom(existing)

        assertEquals(shop, shopBack)
        assertEquals(zoo, zooBack)
        assertEquals(otherFormats, otherFormatsBack)
    }

    @Test
    fun `TagEncoder cannot encode multiple instances of the same class without using a key`() {
        val existing = CompoundTag()
        val cityData1 = CityData(1, "amar")
        val cityData2 = CityData(2, "oomer")
        CityData.serializer().put(cityData1, inTag = existing)

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            CityData.serializer().put(cityData2, inTag = existing)
        }



    }

    @Test
    fun `TagEncoder can encode multiple instances of the same class using a key`() {
        val existing = CompoundTag()
        val cityData1 = CityData(1, "amar")
        val cityData2 = CityData(2, "oomer")
        CityData.serializer().put(cityData1, inTag = existing, key = "key1")
        CityData.serializer().put(cityData2, inTag = existing, key = "key2")

        val cityData1Back = CityData.serializer().getFrom(existing, key = "key1")
        val cityData2Back = CityData.serializer().getFrom(existing, key = "key2")


        assertEquals(cityData1, cityData1Back)
        assertEquals(cityData2, cityData2Back)

    }


}
