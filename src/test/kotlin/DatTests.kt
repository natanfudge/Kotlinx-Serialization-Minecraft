@file:UseSerializers(BlockPosSerializer::class, IdentifierSerializer::class, NbtCompoundSerializer::class, UUIDSerializer::class)

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.minecraft.*
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtIo
import net.minecraft.util.math.BlockPos
import org.junit.jupiter.api.Test
import utils.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.PushbackInputStream
import java.nio.file.Files
import java.util.UUID
import kotlin.test.assertEquals


@Serializable
internal sealed class Repetition {

    @Serializable
    class RepeatAmount : Repetition() {
        override fun equals(other: Any?): Boolean {
            return this === other
        }

        override fun hashCode(): Int {
            return System.identityHashCode(this)
        }
    }

}

@Serializable
data class NullableList(val listIntN: List<Int?>)

val nullableList = NullableList(listOf(4, 5, null))


private fun <T : Any>testDat(serializer: KSerializer<T>, obj: T, context: SerializersModule = EmptySerializersModule()): TestResult {
    val file = Files.createTempFile("tempDat", ".dat").toFile()
    val tag = NbtCompound()
    serializer.put(obj, tag, context = context)
    tag.writeTo(file)
    val tagBack = file.readNbt()
    val back = serializer.getFrom(tagBack, context = context)
    return TestResult(obj, back, "$tag")
}

class DatTests {
    @Test
    fun `Polymorphic serializer writes a valid dat file`() {
        val obj = Repetition.RepeatAmount()
        val tag = NbtCompound().also { Repetition.serializer().put(obj, it) }
        val file = Files.createTempFile("testschedule",".dat").toFile()
        tag.writeTo(file)
        val back = file.readNbt()
        assertEquals(tag, back)
    }

    @Test
    fun `Nullable list becomes a valid dat file`() {
        val obj = nullableList
        val tag = NbtCompound().also { NullableList.serializer().put(obj, it) }
        val file = Files.createTempFile("testschedule",".dat").toFile()
        tag.writeTo(file)
        val back = file.readNbt()
        assertEquals(tag, back)
    }



    @Test
    fun `All tested objects can be written from and read to dat files`() {
        bootstrapMinecraft()
        testMethod(::testDat, verbose = false)
    }

    @Test
    fun `Various keys in a map may be written to and from a dat file`() {
        val obj = WeirdMapKeys2(
//            mapOf(BlockPos(1,2,3) to 4),
//            mapOf(33.toByte() to 1),
//            mapOf(44.toShort() to 3),
            3,
            mapOf(UUID.randomUUID() to 123),
//            mapOf(2.3f to 34),
//            mapOf(321.23 to 43),
//            mapOf(listOf(1,2,3) to 4)
        )
        val tag = NbtCompound().also { WeirdMapKeys2.serializer().put(obj, it) }
        val file = Files.createTempFile("testkeys",".dat").toFile()
        tag.writeTo(file)
        val back = file.readNbt()
        assertEquals(tag, back)
    }

    @Test
    fun `Polymorphic list can be written to DAT`() {
        val obj = PolymorphicList(list = listOf(Polymorphic.Option1(2), Polymorphic.Option2(3,54.3f), Polymorphic.Option1(414)))
        // Relies on implementation serializing polymorphic list to NbtCompound
        val tag = Nbt.encodeToNbt(obj) as NbtCompound
        val file = Files.createTempFile("testnumbers",".dat").toFile()
        tag.writeTo(file)
        val backNbt = file.readNbt()
        val back: PolymorphicList = Nbt.decodeFromNbt(backNbt)
        assertEquals(obj, back)
    }
}
@Serializable
data class PolymorphicList(val y : Int = 4,val list: List<Polymorphic>, val x: Int = 3)
@Serializable
sealed interface Polymorphic {
    @Serializable
    data class Option1(val x: Int): Polymorphic
    @Serializable
    data class Option2(val y: Int, val z: Float): Polymorphic
}

@Serializable
data class WeirdMapKeys(val map: Map<BlockPos, Int>, val map2: Map<Byte, Int>, val map3: Map<Short, Int>, val map4: Map<String, Int>, val map5: Map<UUID, Int>, val map6: Map<Float, Int>, val map7: Map<Double, Int>, val map8: Map<List<Int>, Int>)
@Serializable
data class WeirdMapKeys2(val map4: Int, val map5: Map<UUID, Int>)

private fun NbtCompound.writeTo(file: File) {
    FileOutputStream(file).use {
        NbtIo.writeCompressed(this, it)
    }
}


private fun File.readNbt(): NbtCompound = PushbackInputStream(FileInputStream(this), 2).use {
    NbtIo.readCompressed(it)
}

