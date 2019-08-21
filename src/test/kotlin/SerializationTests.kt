
import drawer.*
import drawer.util.bufferedPacket
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.SerializersModule
import net.minecraft.Bootstrap
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.IntTag
import net.minecraft.nbt.Tag
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import utils.*
import java.util.*
import kotlin.test.assertEquals

fun testTag(serializer: KSerializer<Any>, obj: Any, context: SerialModule): TestResult {
    val tag = CompoundTag()
    serializer.put(obj, tag, context = context)
    val back = serializer.getFrom(tag, context = context)
    return TestResult(obj, back, "$tag")
}

fun testByteBuf(serializer: KSerializer<Any>, obj: Any, context: SerialModule): TestResult {
    val buf = bufferedPacket()
    serializer.write(obj, toBuf = buf, context = context)
    val back = serializer.readFrom(buf, context = context)
    return TestResult(obj, back, "$buf")
}




class SerializationTests {
    var initialized = false
    //TODO: doesn't workj
    @Test
    fun `TagEncoder serializes and deserializes correctly`() {
        if (!initialized) {
            Bootstrap.initialize()
            initialized = true
        }
        testMethod(::testTag, supportsNull = true, verbose = false)

    }


    @Test
    fun `ByteBufEncoder serializes and deserializes correctly`() {
        if (!initialized) {
            Bootstrap.initialize()
            initialized = true
        }
        testMethod(::testByteBuf, supportsNull = true, verbose = false)
    }




    @Test
    fun `TagEncoder can serialize a zoo`() {
        val obj = zoo
        val existing = CompoundTag()
        Zoo.serializer().put(obj, existing)
        val back = Zoo.serializer().getFrom(existing)
        assertEquals(obj, back)
    }

    //TODO: uuid is broken

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

    @Test
    fun `You can use getFrom / readFrom on null`() = testSerializers(CityData.serializer().nullable) {
        val data: CityData? = null
        serialize(data)
        val back = deserialize()

        assertEquals(back, null)
    }


    @Test
    fun `Abstract array tags can be encoded in a ByteBuf`() {
        val buf = bufferedPacket()
        val data = lessAbstractTags
        LessAbstractTags.serializer().write(data, buf)
        val back = LessAbstractTags.serializer().readFrom(buf)

        assertEquals(data, back)
    }

    @Test
    fun `Abstract array tags can be encoded in a Tag`() {
        val buf = CompoundTag()
        val data = lessAbstractTags
        LessAbstractTags.serializer().put(data, buf)
        val back = LessAbstractTags.serializer().getFrom(buf)

        assertEquals(data, back)
    }

    @Test
    fun `CompoundTags with a UUID can be encoded in a CompoundTag`() {
        val tag = CompoundTag()
        val data = lessCompoundTags
        LessCompoundTags.serializer().put(data, tag)
        val back = LessCompoundTags.serializer().getFrom(tag)
        assertEquals(data, back)

    }


}
