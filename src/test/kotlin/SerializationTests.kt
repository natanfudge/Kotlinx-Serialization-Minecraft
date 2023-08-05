
import kotlinx.serialization.minecraft.impl.bufferedPacket
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.minecraft.getFrom
import kotlinx.serialization.minecraft.put
import kotlinx.serialization.minecraft.readFrom
import kotlinx.serialization.minecraft.write
import kotlinx.serialization.modules.SerializersModule
import net.minecraft.nbt.NbtCompound
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import utils.*
import kotlin.test.assertEquals

fun testTag(serializer: KSerializer<Any>, obj: Any, context: SerializersModule): TestResult {
    val tag = NbtCompound()
    serializer.put(obj, tag, context = context)
    val back = serializer.getFrom(tag, context = context)
    return TestResult(obj, back, "$tag")
}

fun testByteBuf(serializer: KSerializer<Any>, obj: Any, context: SerializersModule): TestResult {
    val buf = bufferedPacket()
    serializer.write(obj, toBuf = buf, context = context)
    val back = serializer.readFrom(buf, context = context)
    return TestResult(obj, back, "$buf")
}



class SerializationTests {

    @Test
    fun `TagEncoder serializes and deserializes correctly`() {
        bootstrapMinecraft()
        testMethod(::testTag,  verbose = false)
    }

    @Test
    fun `ByteBufEncoder serializes and deserializes correctly`() {
        bootstrapMinecraft()
        testMethod(::testByteBuf,  verbose = false)
    }




    @Test
    fun `TagEncoder can serialize a zoo`() {
        val obj = zoo
        val existing = NbtCompound()
        Zoo.serializer().put(obj, existing)
        val back = Zoo.serializer().getFrom(existing)
        assertEquals(obj, back)
    }

    @Test
    fun `TagEncoder can also serialize into an existing tag using put and getFromTag`() {
        val existing = NbtCompound()
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
        val existing = NbtCompound()
        val cityData1 = CityData(1, "amar")
        val cityData2 = CityData(2, "oomer")
        CityData.serializer().put(cityData1, inTag = existing)

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            CityData.serializer().put(cityData2, inTag = existing)
        }


    }

    @Test
    fun `TagEncoder can encode multiple instances of the same class using a key`() {
        val existing = NbtCompound()
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
    fun `You can use getFrom or readFrom on null`() = testSerializers(CityData.serializer().nullable) {
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
        val buf = NbtCompound()
        val data = lessAbstractTags
        LessAbstractTags.serializer().put(data, buf)
        val back = LessAbstractTags.serializer().getFrom(buf)

        assertEquals(data, back)
    }

    @Test
    fun `NbtCompounds with a UUID can be encoded in a NbtCompound`() {
        val tag = NbtCompound()
        val data = lessNbtCompounds
        LessNbtCompounds.serializer().put(data, tag)
        val back = LessNbtCompounds.serializer().getFrom(tag)
        assertEquals(data, back)

    }


}
