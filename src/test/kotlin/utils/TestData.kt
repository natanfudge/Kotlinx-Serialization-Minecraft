@file:UseSerializers(
    ForUuid::class,
    ForBlockPos::class,
    ForIdentifier::class,
    ForNbtByte::class,
    ForNbtShort::class,
    ForNbtInt::class,
    ForNbtLong::class,
    ForNbtFloat::class,
    ForNbtDouble::class,
    ForNbtNull::class,
    ForNbtByteArray::class,
    ForNbtIntArray::class,
    ForNbtLongArray::class,
    ForNbtString::class,
    ForNbtList::class,
    ForNbtCompound::class,
    ForItemStack::class,
    ForIngredient::class,
    ForVec3d::class,
    ForSoundEvent::class
)

package utils

import drawer.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import net.minecraft.client.sound.SoundInstance
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.*
import net.minecraft.recipe.Ingredient
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
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
    val byte: NbtByte,
    val short: NbtShort,
    val int: NbtInt,
    val long: NbtLong,
    val float: NbtFloat,
    val double: NbtDouble,
    val string: NbtString,
    val end: NbtNull,
    val byteArray: NbtByteArray,
    val intArray: NbtIntArray,
    val longArray: NbtLongArray
)

@Serializable
data class NbtIntArrayWrapper(
    val end: NbtNull,
    val byteArray: NbtByteArray,
    val intArray: NbtIntArray,
    val longArray: NbtLongArray
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
    val mapStringInt: Map<String, Int>,
    val mapIntStringN: Map<Int, String?>,
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
    mapOf("one" to 1, "two" to 2, "three" to 3),
    mapOf(0 to null, 1 to "first", 2 to "second"),
    ZooWithArrays(
        arrayOf(1, 2, 3),
        arrayOf(100, 200, 300),
        arrayOf(null, -1, -2),
        arrayOf(IntData(1), IntData(2))
    )
)

@Serializable
data class OtherFormats(
    val uuid: UUID,
    val uuidList: List<UUID>,
    val blockPos: BlockPos,
    val blockPosList: List<BlockPos>,
    val id: Identifier,
    val vec3d: Vec3d,
    val soundCategory: SoundCategory,
    val attenuationType: SoundInstance.AttenuationType

)


val otherFormats = OtherFormats(
    UUID(1, 222222222),
    listOf(UUID(2, 3), UUID(4, 5), UUID(11111111111111111, 9)),
    BlockPos(78, 12, 2),
    listOf(BlockPos(4, 5, 6), BlockPos(7, 8, 9), BlockPos(10, 11, 12)),
    Identifier("spatialcrafting", "x2crafter_piece"),
    Vec3d(0.2, -123.0, 2323.3),
    SoundCategory.AMBIENT, SoundInstance.AttenuationType.LINEAR
)

@Serializable
data class OtherLazyFormats(val soundEvent: SoundEvent)

val otherLazyFormats = {
    OtherLazyFormats(SoundEvents.AMBIENT_UNDERWATER_LOOP)
}


val zeroNumbers = VariousNumbers(0, 0, 0, 0f, 0, 0.0)
val nullableZeroNumbers = VariousNullableNumbers(null, 0, 0, 0f, 0, 0.0)

val message = MessageWrapper(
    IntMessage(1),
    StringMessage("Asdf")
)
val messageModule = SerializersModule {
    polymorphic(PolymorphicMessage::class) {
        subclass(IntMessage::class , IntMessage.serializer())
                subclass(StringMessage::class , StringMessage.serializer())
    }
}

val tags = Tags(
    NbtByte.of(0),
    NbtShort.of(1),
    NbtInt.of(2),
    NbtLong.of(3),
    NbtFloat.of(3.5f),
    NbtDouble.of(4.23),
    NbtString.of("amar"),
    NbtNull.INSTANCE,
    NbtByteArray(listOf(5.toByte(), 6.toByte(), 7.toByte())),
    NbtIntArray(listOf(8, 9, 10)),
    NbtLongArray(listOf(11L, 12L, 13L))
)

val intArrayTagWrapper = NbtIntArrayWrapper(
    NbtNull.INSTANCE, NbtByteArray(listOf(5.toByte(), 6.toByte(), 7.toByte())),
    NbtIntArray(listOf(8, 9, 10)), NbtLongArray(listOf(11L, 12L, 13L))
)

@Serializable
data class AbstractTags(
    val tag1: NbtElement,
    val tag2: NbtElement,
    val tag3: NbtElement,
    val tag4: NbtElement,
    val tag5: NbtElement,
    val tag6: NbtElement,
    val tag7: NbtElement,
    val tag8: NbtElement,
    val tag9: NbtElement,
    val tag10: NbtElement,
    val tag11: NbtElement,
    val tag12: NbtElement,
    val tag13: NbtElement
)


val abstractTags = AbstractTags(
    NbtByte.of(0),
    NbtShort.of(1),
    NbtInt.of(2),
    NbtLong.of(3),
    NbtFloat.of(3.5f),
    NbtDouble.of(4.23),
    NbtString.of("amar"),
    NbtNull.INSTANCE,
    NbtByteArray(listOf(5.toByte(), 6.toByte(), 7.toByte())),
    NbtIntArray(listOf(8, 9, 10)),
    NbtLongArray(listOf(11L, 12L, 13L)),
    NbtList().apply { add(NbtLong.of(2L)) },
    NbtCompound().apply { putBoolean("as", false) }
)

@Serializable
data class LessAbstractTags(val tag: NbtElement)

val lessAbstractTags = LessAbstractTags(NbtIntArray(listOf(8, 9, 10)))


@Serializable
data class NbtLists(
    val listTag1: NbtList,
    val listTag2: NbtList,
    val listTag3: NbtList,
    val listTag4: NbtList,
    val listTag5: NbtList
)

//TODO: list tag of compound tag
val listTags = NbtLists(NbtList().apply {
    add(NbtInt.of(1))
    add(NbtInt.of(2))
    add(NbtInt.of(3))
}, NbtList().apply {
    add(NbtString.of("asdf"))
    add(NbtString.of("asdf"))
},
    NbtList().apply {
        add(NbtByteArray(listOf(1.toByte(), 2.toByte(), 3.toByte())))
        add(NbtByteArray(listOf(2.toByte(), 4.toByte(), 3.toByte())))
        add(NbtByteArray(listOf((-13).toByte(), 2.toByte(), 4.toByte())))
    },
    NbtList().apply {
        add(NbtList().apply { add(NbtByte.of(1.toByte())) })
        add(NbtList().apply { add(NbtFloat.of(0.3f)) })
    },
    NbtList().apply {
        NbtCompound().apply {
            putInt("asdf", 1)
        }
        NbtCompound().apply {
            putString("asdf", "ASdf")
        }
    }

)

@Serializable
data class NbtCompounds(val compoundTag1: NbtCompound, val compoundTag2: NbtCompound)

val compoundTags = NbtCompounds(
    NbtCompound().apply {
        put("", tags.intArray)
        put("asdfff", tags.double)
        ForUuid.put(UUID(1, 2), this, key = "amar")
    },
    NbtCompound().apply {
        put("heavy shit", listTags.listTag5)
        putDouble("dd", 12.3)
    }
)

@Serializable
data class LessNbtCompounds(val compoundTag1: NbtCompound/*, val compoundTag2 : NbtCompound*/)

val lessNbtCompounds = LessNbtCompounds(
    NbtCompound().apply {
        ForUuid.put(UUID(1, 2), this, key = "amar")
    }

)

@Serializable
data class ItemStacks(val itemStack1: ItemStack, val itemStack2: ItemStack, val itemStack3: ItemStack) {
    override fun equals(other: Any?): Boolean = other is ItemStacks
            && ItemStack.areEqual(this.itemStack1, other.itemStack1)
            && ItemStack.areEqual(this.itemStack2, other.itemStack2)
            && ItemStack.areEqual(this.itemStack3, other.itemStack3)

    override fun hashCode(): Int {
        var result = itemStack1.hashCode()
        result = 31 * result + itemStack2.hashCode()
        result = 31 * result + itemStack3.hashCode()
        return result
    }
}

val itemStacks = {
    ItemStacks(ItemStack(Items.ACACIA_WOOD, 2), ItemStack.EMPTY, ItemStack.fromNbt(
        NbtCompound().apply {
            putString("id", "birch_planks")
            putByte("Count", 64)
            put("tag", NbtCompound().apply {
                putString("aaa", "hello")
                putInt("waefwe", 222)
            })

        }
    ))

}

@Serializable
data class Ingredients(val ingredient1: Ingredient, val ingredient2: Ingredient, val ingredient3: Ingredient) {
    override fun equals(other: Any?) = other is Ingredients &&
            ingredient1 actuallyEquals ingredient2 &&
            ingredient2 actuallyEquals ingredient2 &&
            ingredient3 actuallyEquals ingredient3

    override fun hashCode(): Int {
        var result = ingredient1.hashCode()
        result = 31 * result + ingredient2.hashCode()
        result = 31 * result + ingredient3.hashCode()
        return result
    }
}

infix fun Ingredient.actuallyEquals(other: Ingredient): Boolean {
    return other.matchingStacks.zip(other.matchingStacks)
        .all { (stack1, stack2) -> ItemStack.areEqual(stack1, stack2) }
}

val ingredients = {
    Ingredients(
        Ingredient.EMPTY,
        Ingredient.ofStacks(itemStacks().itemStack1, itemStacks().itemStack2, itemStacks().itemStack3),
        Ingredient.ofItems(Items.CARROT, Items.IRON_CHESTPLATE, Items.SPAWNER)
    )
}


@Serializable
data class DefaultedLists(
    @Serializable(with = ForDefaultedList::class) val itemStackList: DefaultedList<ItemStack>,
    @Serializable(with = ForDefaultedList::class) val ingredientList: DefaultedList<Ingredient>,
    @Serializable(with = ForDefaultedList::class) val intList: DefaultedList<Int>
) {
    override fun equals(other: Any?): Boolean {
        return other is DefaultedLists &&
                this.itemStackList.zip(other.itemStackList)
                    .all { (thisStack, otherStack) -> ItemStack.areEqual(thisStack, otherStack) }
                && this.ingredientList.zip(other.ingredientList)
            .all { (thisIngredient, otherIngredient) -> thisIngredient actuallyEquals otherIngredient }
                && this.intList == other.intList
    }

    override fun hashCode(): Int {
        var result = itemStackList.hashCode()
        result = 31 * result + ingredientList.hashCode()
        result = 31 * result + intList.hashCode()
        return result
    }
}

val defaultedLists = {
    DefaultedLists(
        DefaultedList.copyOf(
            ItemStack.EMPTY,
            itemStacks().itemStack3,
            itemStacks().itemStack1,
            ItemStack.EMPTY,
            ItemStack(Items.ITEM_FRAME)
        ),
        DefaultedList.ofSize(10, Ingredient.EMPTY),
        DefaultedList.copyOf(0, 1, 2, 3, 4, 5, 6, 7, 8)
    )
}