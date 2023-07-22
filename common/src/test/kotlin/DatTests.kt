@file:UseSerializers(BlockPosSerializer::class, IdentifierSerializer::class, NbtCompoundSerializer::class)

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.minecraft.*
import kotlinx.serialization.modules.SerializersModule
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtIo
import kotlin.test.Test
import utils.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.PushbackInputStream
import java.nio.file.Files
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


private fun testDat(serializer: KSerializer<Any>, obj: Any, context: SerializersModule): TestResult {
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
}

private fun NbtCompound.writeTo(file: File) {
    FileOutputStream(file).use {
        NbtIo.writeCompressed(this, it)
    }
}

private fun File.readNbt(): NbtCompound = PushbackInputStream(FileInputStream(this), 2).use {
    NbtIo.readCompressed(it)
}

