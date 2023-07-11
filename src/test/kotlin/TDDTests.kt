@file:UseSerializers(UUIDSerializer::class, BlockPosSerializer::class, IdentifierSerializer::class, Vec3dSerializer::class)

import kotlinx.serialization.minecraft.impl.bufferedPacket
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.Json
import kotlinx.serialization.minecraft.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtInt
import net.minecraft.util.math.Vec3d
import org.junit.jupiter.api.Test
import utils.CityData
import java.util.*
import kotlin.test.assertEquals

@Serializable
data class SimpleData(val name: String, val age: Int)

@Serializable
data class SimpleList(val names: List<String>)

@Serializable
data class NestedData(val nested: SimpleData, val primitive: Long)

@Serializable
data class SimpleNullable(
    val listIntN: List<Int?>,
    val listNInt: List<Int>?
)

@Serializable
data class Uuid(val uuid: UUID)


interface PolymorphicData

@Serializable
data class ImplData1(val num: Int) : PolymorphicData

@Serializable
data class ImplData2(val str: String) : PolymorphicData

@Serializable
data class PolymorphicDataHolder(val data: PolymorphicData)


@Serializable
data class PolymorphicTag(val tag: NbtElement)

@Serializable
data class NullableString(val stringN: String?)

@Serializable
data class SimpleTree(val down: SimpleTree?)

@Serializable
data class TreeHolder(
    val tree: SimpleTree
)

@Serializable
data class Vec3dContainer(
    val vec3d: Vec3d
)

@Serializable
data class IntStringMap(val intString: Map<Int, String>)
@Serializable
data class NullableProperty(val nullable: Int?)

val intStringMap = IntStringMap(mapOf(0 to "foo"))

class TDDTests {
    @Test
    fun `TagEncoder can serialize simple data`() {
        val obj = SimpleData("amar", 2)
        val existing = NbtCompound()
        SimpleData.serializer().put(obj, existing)
        val back = SimpleData.serializer().getFrom(existing)
        assertEquals(obj, back)
    }


    @Test
    fun `TagEncoder can serialize lists`() {
        val obj = SimpleList(listOf("asd", "Wef", "1232"))
        val existing = NbtCompound()
        SimpleList.serializer().put(obj, existing)
        val back = SimpleList.serializer().getFrom(existing)
        assertEquals(obj, back)
    }

    @Test
    fun `TagEncoder can serialize nested data`() {
        val obj = NestedData(SimpleData("amar", 2), 123123L)
        val existing = NbtCompound()
        NestedData.serializer().put(obj, existing)
        val back = NestedData.serializer().getFrom(existing)
        assertEquals(obj, back)
    }

    @Test
    fun `TagEncoder can serialize a nullable list data`() {
        val obj = SimpleNullable(
            listOf(4, 5, null),
            listOf(6, 7, 8, 9)
        )
        val existing = NbtCompound()
        SimpleNullable.serializer().put(obj, existing)
        val back = SimpleNullable.serializer().getFrom(existing)
        assertEquals(obj, back)
    }

    @Test
    fun `TagEncoder can serialize an oddly sized nullable list with null`() {
        val obj = SimpleNullable(
            listOf(4, 5, 6, null),
            listOf(6, 7, 8)
        )
        val existing = NbtCompound()
        SimpleNullable.serializer().put(obj, existing)
        val back = SimpleNullable.serializer().getFrom(existing)
        assertEquals(obj, back)
    }

    @Test
    fun `TagEncoder can serialize an oddly sized nullable list`() {
        val obj = SimpleNullable(
            listOf(4, 5, 6),
            listOf(6, 7, 8)
        )
        val existing = NbtCompound()
        SimpleNullable.serializer().put(obj, existing)
        val back = SimpleNullable.serializer().getFrom(existing)
        assertEquals(obj, back)
    }

    @Test
    fun `TagEncoder can serialize a UUID`() {
        val obj = Uuid(UUID(123, 345))
        val existing = NbtCompound()
        Uuid.serializer().put(obj, existing)
        val back = Uuid.serializer().getFrom(existing)
        assertEquals(obj, back)
    }

    @Test
    fun `TagEncoder can serialize polymorphic Data`() {
        val context = SerializersModule {
            polymorphic(PolymorphicData::class) {
                subclass(ImplData1::class, ImplData1.serializer())
                subclass(ImplData2::class, ImplData2.serializer())
            }
        }
        val obj = PolymorphicDataHolder(ImplData1(1))
        val existing = NbtCompound()
        PolymorphicDataHolder.serializer().put(obj, existing, context = context)
        val back = PolymorphicDataHolder.serializer().getFrom(existing, context = context)
        assertEquals(obj, back)
    }

    @Test
    fun `TagEncoder can serialize polymorphic tags`() {
        val obj = PolymorphicTag(NbtInt.of(1))
        val existing = NbtCompound()
        PolymorphicTag.serializer().put(obj, existing)
        val back = PolymorphicTag.serializer().getFrom(existing)
        assertEquals(obj, back)
    }

    @Test
    fun `TagEncoder can serialize a nullable string`() {
        val obj = NullableString("Str1")
        val existing = NbtCompound()
        NullableString.serializer().put(obj, existing)
        val back = NullableString.serializer().getFrom(existing)
        assertEquals(obj, back)
    }

    @Test
    fun `TagEncoder can serialize a tree structure`() {
        val obj = TreeHolder(
            SimpleTree(SimpleTree(null))
        )
        val existing = NbtCompound()
        TreeHolder.serializer().put(obj, existing)
        val back = TreeHolder.serializer().getFrom(existing)
        assertEquals(obj, back)
    }

    @Test
    fun `TagEncoder can serialize a nullable property`() {
        val obj = NullableProperty(null)
        val existing = NbtCompound()
        NullableProperty.serializer().put(obj, existing)
        val back = NullableProperty.serializer().getFrom(existing)
        assertEquals(obj, back)
    }


    @Test
    fun `TagEncoder can serialize a nullable value`() {
        val obj: CityData? = CityData(1, "asd")
        val existing = NbtCompound()
        CityData.serializer().put(obj, existing)
        val back = CityData.serializer().getFrom(existing)
        assertEquals(obj, back)
    }

    @Test
    fun `ByteBufEncoder can serialize a nullable value`() {
        val obj: CityData? = CityData(1, "asd")
        val existing = bufferedPacket()
        CityData.serializer().nullable.write(obj, existing)
        val back = CityData.serializer().nullable.readFrom(existing)
        assertEquals(obj, back)
    }

    @Test
    fun `TagEncoder can serialize null`() {
        val obj: CityData? = null
        val existing = NbtCompound()
        CityData.serializer().put(obj, existing)
        val back = CityData.serializer().nullable.getFrom(existing)
        assertEquals(obj, back)
    }

    @Test
    fun `ByteBufEncoder can serialize null`() {
        val obj: CityData? = null
        val existing = NbtCompound()
        CityData.serializer().put(obj, existing)
        val back = CityData.serializer().nullable.getFrom(existing)
        assertEquals(obj, back)
    }

    @Test
    fun `Vec3d Serializer works`() {
        val json = Json
        val obj = Vec3dContainer(Vec3d(0.2, -123.0, 2323.3))
        val str = json.encodeToString(Vec3dContainer.serializer(), obj)
        val back = json.decodeFromString(Vec3dContainer.serializer(), str)
        assertEquals(obj, back)
    }

    @Test
    fun `Can serialize a string to int map`() {
        val obj = intStringMap
        val existing = NbtCompound()
        IntStringMap.serializer().put(obj, existing)
        val back = IntStringMap.serializer().getFrom(existing)
        assertEquals(obj, back)
    }

//    @Test
//    fun `Tree gets serialized properly to ByteBuf`() {
//        val buf = bufferedPacket()
//        val zoo = lesserZoo
//        LesserZoo.serializer().write(zoo, buf)
//        val result = LesserZoo.serializer().readFrom(buf)
//        assertEquals(zoo, result)
//    }
}


val lesserZoo = LesserZoo(
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0
)

@Serializable
data class LesserZoo(
    val unit: Int,
    val boolean: Int,
    val byte: Int,
    val short: Int,
    val int: Int,
    val long: Int,
    val float: Int,
    val double: Int,
    val char: Int,
    val string: Int,
    val enum: Int,
    val intData: Int,
    val unitN: Int,
    val booleanN: Int,
    val byteN: Int,
    val shortN: Int,
    val intN: Int,
    val longN: Int,
    val floatN: Int,
    val doubleN: Int,
    val charN: Int,
    val stringN: Int,
    val enumN: Int,
    val intDataN: Int,
    val listInt: Int,
    val listIntN: Int,
    val listNInt: Int,
    val listNIntN: Int,
    val listListEnumN: Int,
    val listIntData: Int,
    val listIntDataN: Int,
    val tree: Long
)
