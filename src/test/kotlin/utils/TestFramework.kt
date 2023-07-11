package utils

import io.netty.buffer.Unpooled
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.minecraft.getFrom
import kotlinx.serialization.minecraft.put
import kotlinx.serialization.minecraft.readFrom
import kotlinx.serialization.minecraft.write
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import net.minecraft.Bootstrap
import net.minecraft.MinecraftVersion
import net.minecraft.SharedConstants
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import kotlin.reflect.KFunction

data class TestResult(
    val obj: Any, // original object
    val res: Any, // resulting object
    val ext: Any  // serialized (external) representation
)

interface Case<T : Any> {
    val obj: T
    val name: String
    val context: SerializersModule
    val serializer: KSerializer<T>
}

private var initialized = false

fun bootstrapMinecraft() {
    if (!initialized) {
        SharedConstants.setGameVersion(MinecraftVersion.create())
        Bootstrap.initialize()
        initialized = true
    }
}

class TestCaseInit {
    val cases = mutableListOf<Case<*>>()
    infix fun <T : Any> T.with(serializer: KSerializer<T>) {
        cases.add(EagerCase(this, serializer))
    }

    fun <T : Any> T.with(serializer: KSerializer<T>, module: SerializersModule) {
        cases.add(EagerCase(this, serializer, module))
    }

    infix fun <T : Any> (() -> T).with(serializer: KSerializer<T>) {
        cases.add(LazyCase(this, serializer))
    }

//    class Moduleable
}

fun testCases(init: TestCaseInit.() -> Unit) = TestCaseInit().apply(init)

data class LazyCase<T : Any> @OptIn(ExperimentalSerializationApi::class) constructor(
    val lazyObj: () -> T,
    override val serializer: KSerializer<T>,
    override val context: SerializersModule = EmptySerializersModule
) : Case<T> {
    override val obj: T get() = lazyObj()
    override val name: String get() = obj.javaClass.simpleName

    override fun toString(): String {
        return lazyObj().toString()
    }
}

data class EagerCase<T : Any> @OptIn(ExperimentalSerializationApi::class) constructor(
    override val obj: T,
    override val serializer: KSerializer<T>,
    override val context: SerializersModule = EmptySerializersModule,
    override val name: String = obj.javaClass.simpleName
) : Case<T>

val testCases = testCases {
    CityData(1, "New York") with CityData.serializer()
    StreetData(2, "Broadway", CityData(1, "New York")) with StreetData.serializer()
    StreetData2(2, "Broadway", CityData(1, "New York")) with StreetData2.serializer()
    StreetData2(2, "Broadway", null) with StreetData2.serializer()
    CountyData("US", listOf(CityData(1, "New York"), CityData(2, "Chicago"))) with CountyData.serializer()
    zoo with Zoo.serializer()
    shop with Shop.serializer()
    otherFormats with OtherFormats.serializer()
    otherLazyFormats with OtherLazyFormats.serializer()
    zeroNumbers with VariousNumbers.serializer()
    nullableZeroNumbers with VariousNullableNumbers.serializer()
    tags with Tags.serializer()
    intArrayTagWrapper with NbtIntArrayWrapper.serializer()
    abstractTags with AbstractTags.serializer()
    message.with(MessageWrapper.serializer(), messageModule)
    listTags with NbtLists.serializer()
    compoundTags with NbtCompounds.serializer()
    itemStacks with ItemStacks.serializer()
    ingredients with Ingredients.serializer()
    defaultedLists with DefaultedLists.serializer()
}.cases


@Suppress("UNCHECKED_CAST")
fun <T : Any> testCase(
    serializer: KSerializer<T>,
    obj: T,
    context: SerializersModule,
    method: (KSerializer<Any>, Any, SerializersModule) -> TestResult,
    verbose: Boolean = true

): Boolean {
    if (verbose) println("Start with $obj")
    val result = method(serializer as KSerializer<Any>, obj, context)

    if (verbose) {
        println("Loaded obj ${result.res}")
        println("    equals=${obj == result.res}, sameRef=${obj === result.res}")
        println("Saved form ${result.ext}")
    }
    return obj == result.res
}

fun testCase(
    case: Case<Any>,
    method: (KSerializer<Any>, Any, SerializersModule) -> TestResult,
    verbose: Boolean = true
): Boolean {
    println("Test case ${case.name}")
    try {
        return testCase(case.serializer, case.obj, case.context, method, verbose)
    } catch (e: java.lang.Exception) {
        println("Error executing test case '$case':\n")
        throw e
    }

}


@Suppress("UNCHECKED_CAST")
fun testMethod(
    method: (KSerializer<Any>, Any, SerializersModule) -> TestResult,
    verbose: Boolean = true
): Pair<Int, Int> {
    if (verbose) println("==============================================")
    println("Running with ${(method as KFunction<*>).name}")
    var totalCount = 0
    var failCount = 0
    testCases.forEach { case ->
        if (verbose) println()
        if (!testCase(case as Case<Any>, method, verbose))
            failCount++
        totalCount++
    }
    if (verbose) {
        println("==============================================")
        println("Done with ${method.name}")
        if (failCount > 0)
            println("!!! FAILED $failCount TEST CASES OUT OF $totalCount TEST CASES !!!")
        else
            println("Passed $totalCount test cases")
    }
    if (failCount > 0) throw Exception("Not all tests passed!")
    return Pair(failCount, totalCount)
}

interface SerialContainer<T> {
    fun serialize(obj: T)
    fun deserialize(): T
    val serializer: KSerializer<T>

//    fun deserializeAndCompare() = deserialize()
}

class TagSerialContainer<T>(override val serializer: KSerializer<T>, private val tag: NbtCompound = NbtCompound()) : SerialContainer<T> {
    override fun serialize(obj: T) = serializer.put(obj, tag)
    override fun deserialize(): T = serializer.getFrom(tag)
    @OptIn(ExperimentalSerializationApi::class)
    val innerTag : NbtCompound get() = tag.get(serializer.descriptor.serialName) as NbtCompound
}

class BufSerialContainer<T>(override val serializer: KSerializer<T>, private val buf: PacketByteBuf = PacketByteBuf(Unpooled.buffer())) : SerialContainer<T> {
    override fun serialize(obj: T) = serializer.write(obj, buf)
    override fun deserialize(): T = serializer.readFrom(buf)
}


fun <T> testSerializers(serializer: KSerializer<T>,  bufOnly : Boolean = false, tagOnly:Boolean = false,init: SerialContainer<T>.() -> Unit) {
    if(!bufOnly) TagSerialContainer(serializer).init()
    if(!tagOnly) BufSerialContainer(serializer).init()
}

fun <T> testTagSerializer(serializer: KSerializer<T>,  init: TagSerialContainer<T>.() -> Unit) {
    TagSerialContainer(serializer).init()
}