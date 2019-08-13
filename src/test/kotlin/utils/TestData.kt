@file:UseSerializers(
    ForUuid::class,
    ForBlockPos::class,
    ForIdentifier::class,
    ForByteTag::class,
    ForShortTag::class,
    ForIntTag::class,
    ForLongTag::class,
    ForFloatTag::class,
    ForDoubleTag::class,
    ForEndTag::class,
    ForByteArrayTag::class,
    ForIntArrayTag::class,
    ForLongArrayTag::class,
    ForStringTag::class,
    ForListTag::class,
    ForCompoundTag::class,
    ForItemStack::class
)

package utils

import drawer.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.modules.SerializersModule
import net.minecraft.item.ItemStack
import net.minecraft.nbt.*
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import java.util.*

// simple data objects

@Serializable
data class VariousNumbers(
    val int: Int,
    val byte: Byte,
    val short: Short,
    val float: Float,
    val long: Long,
    val double: Double
)

@Serializable
data class VariousNullableNumbers(
    val int: Int?,
    val byte: Byte?,
    val short: Short,
    val float: Float?,
    val long: Long,
    val double: Double
)

@Serializable
data class CityData(
    val id: Int,
    val name: String
)

@Serializable
data class StreetData(
    val id: Int,
    val name: String,
    val city: CityData
)

@Serializable
data class StreetData2(
    val id: Int,
    val name: String,
    val city: CityData?
)

@Serializable
data class CountyData(
    val name: String,
    val cities: List<CityData>
)

@Serializable
data class Tags(
    val byte: ByteTag,
    val short: ShortTag,
    val int: IntTag,
    val long: LongTag,
    val float: FloatTag,
    val double: DoubleTag,
    val string: StringTag,
    val end: EndTag,
    val byteArray: ByteArrayTag,
    val intArray: IntArrayTag,
    val longArray: LongArrayTag
)

@Serializable
data class IntArrayTagWrapper(
    val end: EndTag,
    val byteArray: ByteArrayTag,
    val intArray: IntArrayTag,
    val longArray: LongArrayTag
)


// Shop from Kotlin Koans

@Serializable
data class Shop(val name: String, val customers: List<Customer>)

@Serializable
data class Customer(val name: String, val city: City, val orders: List<Order>) {
    override fun toString() = "$name from ${city.name} with $orders"
}

@Serializable
data class Order(val products: List<Product>, val isDelivered: Boolean) {
    override fun toString() = "$products${if (isDelivered) " delivered" else ""}"
}

@Serializable
data class Product(val name: String, val price: Double) {
    override fun toString() = "'$name' for $price"
}

@Serializable
data class City(val name: String) {
    override fun toString() = name
}

@Serializable
data class OtherFormats(
    val uuid: UUID,
    val uuidList: List<UUID>,
    val blockPos: BlockPos,
    val blockPosList: List<BlockPos>,
    val id: Identifier
)


interface PolymorphicMessage
@Serializable
data class IntMessage(val int: Int) : PolymorphicMessage

@Serializable
data class StringMessage(val string: String) : PolymorphicMessage

@Serializable
data class MessageWrapper(val message: PolymorphicMessage, val stringMessage: StringMessage)


// TestShop from Kotlin Koans

//products
val idea = Product("IntelliJ IDEA Ultimate", 199.0)
val reSharper = Product("ReSharper", 149.0)
val dotTrace = Product("DotTrace", 159.0)
val dotMemory = Product("DotTrace", 129.0)
val dotCover = Product("DotCover", 99.0)
val appCode = Product("AppCode", 99.0)
val phpStorm = Product("PhpStorm", 99.0)
val pyCharm = Product("PyCharm", 99.0)
val rubyMine = Product("RubyMine", 99.0)
val webStorm = Product("WebStorm", 49.0)
val teamCity = Product("TeamCity", 299.0)
val youTrack = Product("YouTrack", 500.0)

//customers
val lucas = "Lucas"
val cooper = "Cooper"
val nathan = "Nathan"
val reka = "Reka"
val bajram = "Bajram"
val asuka = "Asuka"

//cities
val Canberra = City("Canberra")
val Vancouver = City("Vancouver")
val Budapest = City("Budapest")
val Ankara = City("Ankara")
val Tokyo = City("Tokyo")

fun customer(name: String, city: City, vararg orders: Order) = Customer(name, city, orders.toList())
fun order(vararg products: Product, isDelivered: Boolean = true) = Order(products.toList(), isDelivered)
fun shop(name: String, vararg customers: Customer) = Shop(name, customers.toList())

val shop = shop(
    "jb test shop",
    customer(
        lucas, Canberra,
        order(reSharper),
        order(reSharper, dotMemory, dotTrace)
    ),
    customer(cooper, Canberra),
    customer(
        nathan, Vancouver,
        order(rubyMine, webStorm)
    ),
    customer(
        reka, Budapest,
        order(idea, isDelivered = false),
        order(idea, isDelivered = false),
        order(idea)
    ),
    customer(
        bajram, Ankara,
        order(reSharper)
    ),
    customer(
        asuka, Tokyo,
        order(idea)
    )
)

// Zoo from library tests by Roman Elizarov

enum class Attitude { POSITIVE, NEUTRAL, NEGATIVE }

@Serializable
data class IntData(val intV: Int)

@Serializable
data class Tree(val name: String, val left: Tree? = null, val right: Tree? = null)

@Serializable
data class Zoo(
    val unit: Unit,
    val boolean: Boolean,
    val byte: Byte,
    val short: Short,
    val int: Int,
    val long: Long,
    val float: Float,
    val double: Double,
    val char: Char,
    val string: String,
    val enum: Attitude,
    val intData: IntData,
    val unitN: Unit?,
    val booleanN: Boolean?,
    val byteN: Byte?,
    val shortN: Short?,
    val intN: Int?,
    val longN: Long?,
    val floatN: Float?,
    val doubleN: Double?,
    val charN: Char?,
    val stringN: String?,
    val enumN: Attitude?,
    val intDataN: IntData?,
    val listInt: List<Int>,
    val listIntN: List<Int?>,
    val listNInt: List<Int>?,
    val listNIntN: List<Int?>?,
    val listListEnumN: List<List<Attitude?>>,
    val listIntData: List<IntData>,
    val listIntDataN: List<IntData?>,
    val tree: Tree,
//        val mapStringInt: Map<String,Int>,
//        val mapIntStringN: Map<Int,String?>,
    val arrays: ZooWithArrays
)

@Serializable
data class ZooWithArrays(
    val arrByte: Array<Byte>,
    val arrInt: Array<Int>,
    val arrIntN: Array<Int?>,
    val arrIntData: Array<IntData>

) {
    override fun equals(other: Any?) = other is ZooWithArrays &&
            arrByte.contentEquals(other.arrByte) &&
            arrInt.contentEquals(other.arrInt) &&
            arrIntN.contentEquals(other.arrIntN) &&
            arrIntData.contentEquals(other.arrIntData)
}

val zoo = Zoo(
    Unit, true, 10, 20, 30, 40, 50f, 60.0, 'A', "Str0", Attitude.POSITIVE, IntData(70),
    null, null, 11, 21, 31, 41, 51f, 61.0, 'B', "Str1", Attitude.NEUTRAL, null,
    listOf(1, 2, 3),
    listOf(4, 5, null),
    listOf(6, 7, 8),
    listOf(null, 9, 10),
    listOf(listOf(Attitude.NEGATIVE, null)),
    listOf(IntData(1), IntData(2), IntData(3)),
    listOf(IntData(1), null, IntData(3)),
    Tree("root", Tree("left"), Tree("right", Tree("right.left"), Tree("right.right"))),
//        mapOf("one" to 1, "two" to 2, "three" to 3),
//        mapOf(0 to null, 1 to "first", 2 to "second"),
    ZooWithArrays(
        arrayOf(1, 2, 3),
        arrayOf(100, 200, 300),
        arrayOf(null, -1, -2),
        arrayOf(IntData(1), IntData(2))
    )
)

val otherFormats = OtherFormats(
    UUID(1, 222222222),
    listOf(UUID(2, 3), UUID(4, 5), UUID(11111111111111111, 9)),
    BlockPos(78, 12, 2),
    listOf(BlockPos(4, 5, 6), BlockPos(7, 8, 9), BlockPos(10, 11, 12)),
    Identifier("spatialcrafting", "x2crafter_piece")
)

val zeroNumbers = VariousNumbers(0, 0, 0, 0f, 0, 0.0)
val nullableZeroNumbers = VariousNullableNumbers(null, 0, 0, 0f, 0, 0.0)

val message = MessageWrapper(
    IntMessage(1),
    StringMessage("Asdf")
)
val messageModule = SerializersModule {
    polymorphic(PolymorphicMessage::class) {
        IntMessage::class with IntMessage.serializer()
        StringMessage::class with StringMessage.serializer()
    }
}

val tags = Tags(
    ByteTag(0),
    ShortTag(1),
    IntTag(2),
    LongTag(3),
    FloatTag(3.5f),
    DoubleTag(4.23),
    StringTag("amar"),
    EndTag(),
    ByteArrayTag(listOf(5.toByte(), 6.toByte(), 7.toByte())),
    IntArrayTag(listOf(8, 9, 10)),
    LongArrayTag(listOf(11L, 12L, 13L))
)

val intArrayTagWrapper = IntArrayTagWrapper(
    EndTag(), ByteArrayTag(listOf(5.toByte(), 6.toByte(), 7.toByte())),
    IntArrayTag(listOf(8, 9, 10)), LongArrayTag(listOf(11L, 12L, 13L))
)

@Serializable
data class AbstractTags(
    val tag1: Tag,
    val tag2: Tag,
    val tag3: Tag,
    val tag4: Tag,
    val tag5: Tag,
    val tag6: Tag,
    val tag7: Tag,
    val tag8: Tag,
    val tag9: Tag,
    val tag10: Tag,
    val tag11: Tag,
    val tag12: Tag,
    val tag13: Tag
)


val abstractTags = AbstractTags(
    ByteTag(0),
    ShortTag(1),
    IntTag(2),
    LongTag(3),
    FloatTag(3.5f),
    DoubleTag(4.23),
    StringTag("amar"),
    EndTag(),
    ByteArrayTag(listOf(5.toByte(), 6.toByte(), 7.toByte())),
    IntArrayTag(listOf(8, 9, 10)),
    LongArrayTag(listOf(11L, 12L, 13L)),
    ListTag().apply { add(LongTag(2L)) },
    CompoundTag().apply { putBoolean("as", false) }
)

@Serializable
data class LessAbstractTags(val tag: Tag)

val lessAbstractTags = LessAbstractTags(IntArrayTag(listOf(8, 9, 10)))


@Serializable
data class ListTags(
    val listTag1: ListTag,
    val listTag2: ListTag,
    val listTag3: ListTag,
    val listTag4: ListTag,
    val listTag5: ListTag
)

//TODO: list tag of compound tag
val listTags = ListTags(ListTag().apply {
    add(IntTag(1))
    add(IntTag(2))
    add(IntTag(3))
}, ListTag().apply {
    add(StringTag("asdf"))
    add(StringTag("asdf"))
},
    ListTag().apply {
        add(ByteArrayTag(listOf(1.toByte(), 2.toByte(), 3.toByte())))
        add(ByteArrayTag(listOf(2.toByte(), 4.toByte(), 3.toByte())))
        add(ByteArrayTag(listOf((-13).toByte(), 2.toByte(), 4.toByte())))
    },
    ListTag().apply {
        add(ListTag().apply { add(ByteTag(1.toByte())) })
        add(ListTag().apply { add(FloatTag(0.3f)) })
    },
    ListTag().apply {
        CompoundTag().apply {
            putInt("asdf", 1)
        }
        CompoundTag().apply {
            putString("asdf", "ASdf")
        }
    }

)

@Serializable
data class CompoundTags(val compoundTag1: CompoundTag, val compoundTag2: CompoundTag)

val compoundTags = CompoundTags(
    CompoundTag().apply {
        put("", tags.intArray)
        put("asdfff", tags.double)
        putUuid("amar", UUID(1, 2))
    },
    CompoundTag().apply {
        put("heavy shit", listTags.listTag5)
        putDouble("dd", 12.3)
    }
)

@Serializable
data class LessCompoundTags(val compoundTag1: CompoundTag/*, val compoundTag2 : CompoundTag*/)

val lessCompoundTags = LessCompoundTags(
    CompoundTag().apply {
        putUuid("amar", UUID(1, 2))
    }

)

//TODO: test this in spatial crafting because it can't be used in a non-minecraft context...
@Serializable
data class ItemStacks(val itemStack1 : ItemStack, val itemStack2: ItemStack, val itemStacks : List<ItemStack>)
