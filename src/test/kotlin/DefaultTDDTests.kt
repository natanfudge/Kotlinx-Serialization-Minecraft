@file:UseSerializers(UUIDSerializer::class)

import kotlinx.serialization.minecraft.UUIDSerializer
import kotlinx.serialization.minecraft.getFrom
import kotlinx.serialization.minecraft.put
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.nbt.NbtCompound
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


@Serializable
data class DefaultedInt(val int: Int = 2)
@Serializable
data class DefaultedNestedNoValue(val nested: DefaultedInt = DefaultedInt())
@Serializable
data class DefaultedNestedValue(val nested: DefaultedInt = DefaultedInt(3))

@Serializable
data class NullableClass(val value :Int = 2)

class DefaultTDDTests {

    @Test
    fun `TagEncoder can give the default value in case it doesn't exist`(){
        val existing = NbtCompound()
        val back = NullableClass.serializer().getFrom(existing)

        assertEquals(back,NullableClass())
    }

    @Test
    fun `TagEncoder can serialize a defaulted class when the value is not specified`() {
        val obj = DefaultedInt()
        val existing = NbtCompound()
        DefaultedInt.serializer().put(obj, existing)
        val back = DefaultedInt.serializer().getFrom(existing)
        assertEquals(obj, back)
    }
    @Test
    fun `TagEncoder can serialize a defaulted class when the value is specified`() {
        val obj = DefaultedInt(3)
        val existing = NbtCompound()
        DefaultedInt.serializer().put(obj, existing)
        val back = DefaultedInt.serializer().getFrom(existing)
        assertEquals(obj, back)
    }

    @Test
    fun `TagEncoder can serialize a defaulted nested class when the inner value is not specified and the outer is not`() {
        val obj = DefaultedNestedNoValue()
        val existing = NbtCompound()
        DefaultedNestedNoValue.serializer().put(obj, existing)
        val back = DefaultedNestedNoValue.serializer().getFrom(existing)
        assertEquals(obj, back)
    }
    @Test
    fun `TagEncoder can serialize a defaulted nested class when the inner value is specified but the outer is not`() {
        val obj = DefaultedNestedValue()
        val existing = NbtCompound()
        DefaultedNestedValue.serializer().put(obj, existing)
        val back = DefaultedNestedValue.serializer().getFrom(existing)
        assertEquals(obj, back)
    }

    @Test
    fun `TagEncoder can serialize a defaulted nested class when the inner value is not specified but the outer is `() {
        val obj = DefaultedNestedNoValue(DefaultedInt())
        val existing = NbtCompound()
        DefaultedNestedNoValue.serializer().put(obj, existing)
        val back = DefaultedNestedNoValue.serializer().getFrom(existing)
        assertEquals(obj, back)
    }
    @Test
    fun `TagEncoder can serialize a defaulted nested class when the inner value is specified and the outer is specified as well`() {
        val obj = DefaultedNestedValue(DefaultedInt(2))
        val existing = NbtCompound()
        DefaultedNestedValue.serializer().put(obj, existing)
        val back = DefaultedNestedValue.serializer().getFrom(existing)
        assertEquals(obj, back)
    }

}