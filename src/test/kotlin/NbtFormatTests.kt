import drawer.nbt.NbtFormat
import drawer.nbt.writeNbt
import kotlinx.serialization.Serializable
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Serializable
data class SimpleObj(val x: Int = 3, val y: String = "asdf")

@Serializable
data class ComplexObj(val list: List<SimpleObj>, val map: Map<String, Int>, val simple: SimpleObj)

@Serializable
data class CustomMap(val map: Map<SimpleObj, SimpleObj>)

class NestedNbtFormatTests {
    private fun CompoundTag.tag(key: String, init: CompoundTag.() -> Unit) = put(key, CompoundTag().apply(init))
    private fun CompoundTag.list(key: String, init: ListTag.() -> Unit) = put(key, ListTag().apply(init))
    private fun ListTag.tag(init: CompoundTag.() -> Unit) = add(CompoundTag().apply(init))
    @Test
    fun `Objects gets serialized to CompoundTag`() {
        val simple = SimpleObj()
        val actual = NbtFormat().writeNbt(simple, SimpleObj.serializer())
        val expected = CompoundTag().apply {
            putInt("x", 3)
            putString("y", "asdf")
        }
        assertEquals(expected, actual)
    }

    @Test
    fun `Lists get serialized to ListTags, maps and objects to CompoundTags`() {
        val obj = ComplexObj(
            list = listOf(SimpleObj(), SimpleObj(x = 2)),
            map = mapOf("3" to 3, "hello" to 2),
            simple = SimpleObj(y = "foo")
        )
        val actual = NbtFormat().writeNbt(obj, ComplexObj.serializer())
        val expected = CompoundTag().apply {
            list("list") {
                tag {
                    putInt("x", 3)
                    putString("y", "asdf")
                }
                tag {
                    putInt("x", 2)
                    putString("y", "asdf")
                }
            }
            tag("map") {
                putInt("3", 3)
                putInt("hello", 2)
            }

            tag("simple") {
                putInt("x", 3)
                putString("y", "foo")
            }
        }

        assertEquals(expected, actual)
    }

    @Test
    fun `Maps with custom objects as keys can be serialized`() {
        val obj = CustomMap(
            mapOf(
                SimpleObj() to SimpleObj(x = 22),
                SimpleObj(x = 2) to SimpleObj(),
                SimpleObj(y = "bar") to SimpleObj(y = "fay")
            )
        )

        val actual = NbtFormat().writeNbt(obj, CustomMap.serializer())
        val expected = CompoundTag().apply {
            list("map") {
                tag {
                    putInt("x", 3)
                    putString("y","asdf")
                }
                tag {
                    putInt("x", 22)
                    putString("y","asdf")
                }
                tag {
                    putInt("x", 2)
                    putString("y","asdf")
                }
                tag {
                    putInt("x", 3)
                    putString("y","asdf")
                }
                tag {
                    putInt("x", 3)
                    putString("y","bar")
                }
                tag {
                    putInt("x", 3)
                    putString("y","fay")
                }
            }
        }

        assertEquals(expected,actual)

        println(actual)
    }
}