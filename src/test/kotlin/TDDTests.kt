@file:UseSerializers(ForUuid::class, ForBlockPos::class, ForIdentifier::class,ForVec3d::class)

import drawer.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.internal.nullable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.IntTag
import net.minecraft.nbt.Tag
import net.minecraft.util.math.Vec3d
import org.junit.jupiter.api.Test
import utils.*
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
data class PolymorphicTag(val tag: Tag)

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
data class IntStringMap(val intString: Map<Int,String>)

val intStringMap = IntStringMap(mapOf(0 to "foo"))

class TDDTests {
    @Test
    fun `TagEncoder can serialize simple data`() {
        val obj = SimpleData("amar", 2)
        val existing = CompoundTag()
        SimpleData.serializer().put(obj, existing)
        val back = SimpleData.serializer().getFrom(existing)
        assertEquals(obj, back)
    }


    @Test
    fun `TagEncoder can serialize lists`() {
        val obj = SimpleList(listOf("asd", "Wef", "1232"))
        val existing = CompoundTag()
        SimpleList.serializer().put(obj, existing)
        val back = SimpleList.serializer().getFrom(existing)
        assertEquals(obj, back)
    }

    @Test
    fun `TagEncoder can serialize nested data`() {
        val obj = NestedData(SimpleData("amar", 2), 123123L)
        val existing = CompoundTag()
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
        val existing = CompoundTag()
        SimpleNullable.serializer().put(obj, existing)
        val back = SimpleNullable.serializer().getFrom(existing)
        assertEquals(obj, back)
    }

    @Test
    fun `TagEncoder can serialize an oddly sized nullable list with null`() {
        val obj = SimpleNullable(
            listOf(4, 5,6, null),
            listOf(6, 7, 8)
        )
        val existing = CompoundTag()
        SimpleNullable.serializer().put(obj, existing)
        val back = SimpleNullable.serializer().getFrom(existing)
        assertEquals(obj, back)
    }

    @Test
    fun `TagEncoder can serialize an oddly sized nullable list`() {
        val obj = SimpleNullable(
            listOf(4, 5,6),
            listOf(6, 7, 8)
        )
        val existing = CompoundTag()
        SimpleNullable.serializer().put(obj, existing)
        val back = SimpleNullable.serializer().getFrom(existing)
        assertEquals(obj, back)
    }

    @Test
    fun `TagEncoder can serialize a UUID`() {
        val obj = Uuid(UUID(123, 345))
        val existing = CompoundTag()
        Uuid.serializer().put(obj, existing)
        val back = Uuid.serializer().getFrom(existing)
        assertEquals(obj, back)
    }

    @Test
    fun `TagEncoder can serialize polymorphic Data`() {
        val context = SerializersModule {
            polymorphic(PolymorphicData::class) {
                ImplData1::class with ImplData1.serializer()
                ImplData2::class with ImplData2.serializer()
            }
        }
        val obj = PolymorphicDataHolder(ImplData1(1))
        val existing = CompoundTag()
        PolymorphicDataHolder.serializer().put(obj, existing, context = context)
        val back = PolymorphicDataHolder.serializer().getFrom(existing, context = context)
        assertEquals(obj, back)
    }

    @Test
    fun `TagEncoder can serialize polymorphic tags`() {
        val obj = PolymorphicTag(IntTag.of(1))
        val existing = CompoundTag()
        PolymorphicTag.serializer().put(obj, existing)
        val back = PolymorphicTag.serializer().getFrom(existing)
        assertEquals(obj, back)
    }

    @Test
    fun `TagEncoder can serialize a nullable string`() {
        val obj = NullableString("Str1")
        val existing = CompoundTag()
        NullableString.serializer().put(obj, existing)
        val back = NullableString.serializer().getFrom(existing)
        assertEquals(obj, back)
    }

    @Test
    fun `TagEncoder can serialize a tree structure`() {
        val obj = TreeHolder(
            SimpleTree(SimpleTree(null))
        )
        val existing = CompoundTag()
        TreeHolder.serializer().put(obj, existing)
        val back = TreeHolder.serializer().getFrom(existing)
        assertEquals(obj, back)
    }


    @Test
    fun `TagEncoder can serialize a nullable value`() {
        val obj: CityData? = CityData(1, "asd")
        val existing = CompoundTag()
        CityData.serializer().put(obj, existing)
        val back = CityData.serializer().nullable.getFrom(existing)
        assertEquals(obj, back)
    }

    @Test
    fun `ByteBufEncoder can serialize a nullable value`() {
        val obj: CityData? = CityData(1, "asd")
        val existing = CompoundTag()
        CityData.serializer().put(obj, existing)
        val back = CityData.serializer().nullable.getFrom(existing)
        assertEquals(obj, back)
    }

    @Test
    fun `TagEncoder can serialize null`() {
        val obj: CityData? = null
        val existing = CompoundTag()
        CityData.serializer().put(obj, existing)
        val back = CityData.serializer().nullable.getFrom(existing)
        assertEquals(obj, back)
    }

    @Test
    fun `ByteBufEncoder can serialize null`() {
        val obj: CityData? = null
        val existing = CompoundTag()
        CityData.serializer().put(obj, existing)
        val back = CityData.serializer().nullable.getFrom(existing)
        assertEquals(obj, back)
    }

    @Test
    fun `Vec3d Serializer works`() {
        val json = Json(JsonConfiguration.Stable)
        val obj = Vec3dContainer(Vec3d(0.2, -123.0, 2323.3))
        val str = json.stringify(Vec3dContainer.serializer(),obj)
        val back = json.parse(Vec3dContainer.serializer(),str)
        assertEquals(obj,back)
    }

    @Test
    fun `Can serialize a string to int map`() {
        val obj =  intStringMap
        val existing = CompoundTag()
        IntStringMap.serializer().put(obj, existing)
        val back = IntStringMap.serializer().getFrom(existing)
        assertEquals(obj, back)
    }
}