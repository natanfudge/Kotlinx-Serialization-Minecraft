import io.netty.buffer.Unpooled
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.minecraft.Nbt
import kotlinx.serialization.minecraft.readFrom
import kotlinx.serialization.minecraft.write
import kotlinx.serialization.serializer
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.Ingredient
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import org.junit.jupiter.api.Test
import utils.bootstrapMinecraft
import utils.smartAssertEquals
import java.util.*


@Serializable
data class Dog2(val foo: Int)
class ContextualSerializersTest {
    @Test
    fun testCustomContextual() {
        bootstrapMinecraft()
        testDirect(BlockPos(1, 2, 3))
        testDirect(Identifier("foo:bar"))
        testDirect(SoundEvents.AMBIENT_BASALT_DELTAS_ADDITIONS.value())
        testDirect(ItemStack(Items.GLOW_ITEM_FRAME))
        testDirect<Ingredient>(Ingredient.ofItems(Items.ITEM_FRAME))
        testDirect<DefaultedList<Int>>(DefaultedList.ofSize(10, 2))
        testDirect(UUID(12431, 4124124))
        testDirect(Vec3d(0.3, 41.2, 31.6))
    }

    @Test
    fun testBuiltinContextual() {
        bootstrapMinecraft()
        testDirect(1.toByte())
        testDirect(1.toShort())
        testDirect(1)
        testDirect(1L)
        testDirect('f')
        testDirect("Bar")
    }

    @Test
    fun testContextualNullable() {
        bootstrapMinecraft()
        testDirect<Dog2?>(Dog2(123))
        testDirect<BlockPos?>(BlockPos(1, 2, 3))
        testDirect<Identifier?>(Identifier("foo:bar"))
        testDirect<SoundEvent?>(SoundEvents.AMBIENT_BASALT_DELTAS_ADDITIONS.value())
        testDirect<ItemStack?>(ItemStack(Items.GLOW_ITEM_FRAME))
        testDirect<Ingredient?>(Ingredient.ofItems(Items.ITEM_FRAME))
        testDirect<DefaultedList<Int>?>(DefaultedList.ofSize(10, 2))
        testDirect<DefaultedList<Int?>?>(DefaultedList.ofSize(10, 2))
        testDirect<DefaultedList<Int?>>(DefaultedList.ofSize(10, 2))
        testDirect<UUID>(UUID(12431, 4124124))
        testDirect<Vec3d>(Vec3d(0.3, 41.2, 31.6))
        testDirect<Dog2?>(null)
        testDirect<BlockPos?>(null)
        testDirect<Identifier?>(null)
        testDirect<SoundEvent?>(null)
        testDirect<ItemStack?>(null)
        testDirect<Ingredient?>(null)
    }

    private inline fun <reified T> testDirect(exampleValue: T) {
        val serializer = Nbt.serializersModule.serializer<T>()
        val buf = PacketByteBuf(Unpooled.buffer())
        serializer.write(exampleValue, buf)
        smartAssertEquals(exampleValue, serializer.readFrom(buf))
        val asNbt = Nbt.encodeToNbt(serializer, exampleValue)
        val back = Nbt.decodeFromNbt(serializer, asNbt)
        smartAssertEquals(exampleValue, back)

        val asJson = json.encodeToString(serializer, exampleValue)
        smartAssertEquals(exampleValue, json.decodeFromString(asJson))
        val asJsonElement = json.encodeToJsonElement(serializer, exampleValue)
        smartAssertEquals(exampleValue, json.decodeFromJsonElement(asJsonElement))
    }
}

private val json = Json {
    serializersModule = Nbt.serializersModule
    useArrayPolymorphism = true
}
//    contextual(BlockPos::class, BlockPosSerializer)
//    contextual(Identifier::class, IdentifierSerializer)
//    contextual(SoundEvent::class, SoundEventSerializer)
//    contextual(ItemStack::class, ItemStackSerializer)
//    contextual(Ingredient::class, IngredientSerializer)
//    contextual(DefaultedList::class) { args -> DefaultedListSerializer(args[0]) }
//    contextual(UUID::class, UUIDSerializer)
//    contextual(Vec3d::class, Vec3dSerializer)
