import drawer.nbt.NbtFormat
import drawer.nbt.writeNbt
import kotlinx.serialization.Serializable
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Serializable
data class SimpleObj(val x: Int = 3, val y: String = "asdf")

@Serializable
data class ComplexObj(val list: List<SimpleObj>, val map: Map<String, Int>, val simple: SimpleObj)

@Serializable
data class CustomMap(val map: Map<SimpleObj, SimpleObj>)

class NestedNbtFormatTests {
    private fun NbtCompound.tag(key: String, init: NbtCompound.() -> Unit) = put(key, NbtCompound().apply(init))
    private fun NbtCompound.list(key: String, init: NbtList.() -> Unit) = put(key, NbtList().apply(init))
    private fun NbtList.tag(init: NbtCompound.() -> Unit) = add(NbtCompound().apply(init))
    @Test
    fun `Objects gets serialized to NbtCompound`() {
        val simple = SimpleObj()
        val actual = NbtFormat().writeNbt(simple, SimpleObj.serializer())
        val expected = NbtCompound().apply {
            putInt("x", 3)
            putString("y", "asdf")
        }
        assertEquals(expected, actual)
    }

    @Test
    fun `Lists get serialized to NbtLists, maps and objects to NbtCompounds`() {
        val obj = ComplexObj(
            list = listOf(SimpleObj(), SimpleObj(x = 2)),
            map = mapOf("3" to 3, "hello" to 2),
            simple = SimpleObj(y = "foo")
        )
        val actual = NbtFormat().writeNbt(obj, ComplexObj.serializer())
        val expected = NbtCompound().apply {
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
        val expected = NbtCompound().apply {
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