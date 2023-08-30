import kotlinx.serialization.Serializable
import kotlinx.serialization.minecraft.Nbt
import kotlinx.serialization.minecraft.impl.nbt.writeNbt
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import org.junit.jupiter.api.Test
import utils.assertEquals

@Serializable
data class SimpleObj(val x: Int = 3, val y: String = "asdf")

@Serializable
data class ComplexObj(val list: List<SimpleObj>, val map: Map<String, Int>, val simple: SimpleObj)

@Serializable
data class CustomMap(val map: Map<SimpleObj, SimpleObj>)

class NestedNbtFormatTests {
    private fun NbtCompound.compound(key: String, init: NbtCompound.() -> Unit) = put(key, NbtCompound().apply(init))
    private fun NbtCompound.list(key: String, init: NbtList.() -> Unit) = put(key, NbtList().apply(init))
    private fun NbtList.compound(init: NbtCompound.() -> Unit) = add(NbtCompound().apply(init))

    @Test
    fun `Objects gets serialized to NbtCompound`() {
        val simple = SimpleObj()
        val actual = Nbt.writeNbt(simple, SimpleObj.serializer())
        val expected = NbtCompound().apply {
            putInt("x", 3)
            putString("y", "asdf")
        }
        assertEquals(expected, actual)
    }

    @Test
    fun ListsGetSerializedToNbtListsMapsAndObjectsToNbtCompounds() {
        val obj = ComplexObj(
            list = listOf(SimpleObj(), SimpleObj(x = 2)),
            map = mapOf("3" to 3, "hello" to 2),
            simple = SimpleObj(y = "foo")
        )
        val actual = Nbt.writeNbt(obj, ComplexObj.serializer())
        val expected = NbtCompound().apply {
            list("list") {
                compound {
                    putInt("x", 3)
                    putString("y", "asdf")
                }
                compound {
                    putInt("x", 2)
                    putString("y", "asdf")
                }
            }
            compound("map") {
                putInt("3", 3)
                putInt("hello", 2)
            }

            compound("simple") {
                putInt("x", 3)
                putString("y", "foo")
            }
        }

        assertEquals(expected, actual)
    }

    @Test
    fun MapsWithCustomObjectsAsKeysCanBeSerialized() {
        val obj = CustomMap(
            mapOf(
                SimpleObj() to SimpleObj(x = 22),
                SimpleObj(x = 2) to SimpleObj(),
                SimpleObj(y = "bar") to SimpleObj(y = "fay")
            )
        )

        val actual = Nbt.writeNbt(obj, CustomMap.serializer())
        val expected = NbtCompound().apply {

            compound("map") {
                compound("{}") {
                    putInt("x", 22)
                    putString("y", "asdf")
                }
                compound("{\"y\":\"bar\"}") {
                    putInt("x", 3)
                    putString("y", "fay")
                }
                compound("{\"x\":2}") {
                    putInt("x", 3)
                    putString("y", "asdf")
                }
            }
        }

        assertEquals(expected, actual)

        println(actual)
    }
}