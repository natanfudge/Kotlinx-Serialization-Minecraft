@file:Suppress("EXPERIMENTAL_API_USAGE")
@file:OptIn(ExperimentalSerializationApi::class)

package drawer

import drawer.impl.*
import kotlinx.serialization.*
import kotlinx.serialization.builtins.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.item.ItemStack
import net.minecraft.nbt.*
import net.minecraft.recipe.Ingredient
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager
import java.util.*

private val Logger = LogManager.getLogger("Fabric-Drawer")

private inline fun <T> missingField(missingField: String, deserializing: String, defaultValue: () -> T): T {
    Logger.warn("Missing $missingField while deserializing $deserializing")
    return defaultValue()
}


public object BlockPosSerializer : KSerializer<BlockPos> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BlockPos", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: BlockPos): Unit = encoder.encodeLong(value.asLong())
    override fun deserialize(decoder: Decoder): BlockPos = BlockPos.fromLong(decoder.decodeLong())
}


public object IdentifierSerializer : KSerializer<Identifier> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Identifier", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Identifier): Unit = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): Identifier = Identifier(decoder.decodeString())
}

public object SoundEventSerializer : KSerializer<SoundEvent> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SoundEvent", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: SoundEvent) {
        encoder.encodeInt(Registry.SOUND_EVENT.getRawId(value))
    }

    override fun deserialize(decoder: Decoder): SoundEvent = Registry.SOUND_EVENT.get(decoder.decodeInt())
        ?: SoundEvents.ENTITY_ITEM_PICKUP
}


public object NBtByteSerializer : KSerializer<NbtByte> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NbtByte", PrimitiveKind.BYTE)
    override fun serialize(encoder: Encoder, value: NbtByte): Unit = encoder.encodeByte(value.byteValue())
    override fun deserialize(decoder: Decoder): NbtByte = NbtByte.of(decoder.decodeByte())
}

public object NbtShortSerializer : KSerializer<NbtShort> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NbtShort", PrimitiveKind.SHORT)
    override fun serialize(encoder: Encoder, value: NbtShort): Unit = encoder.encodeShort(value.shortValue())
    override fun deserialize(decoder: Decoder): NbtShort = NbtShort.of(decoder.decodeShort())
}

public object NbtIntSerializer : KSerializer<NbtInt> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NbtInt", PrimitiveKind.INT)
    override fun serialize(encoder: Encoder, value: NbtInt): Unit = encoder.encodeInt(value.intValue())
    override fun deserialize(decoder: Decoder): NbtInt = NbtInt.of(decoder.decodeInt())
}

public object NbtLongSerializer : KSerializer<NbtLong> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NbtLong", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: NbtLong): Unit = encoder.encodeLong(value.longValue())
    override fun deserialize(decoder: Decoder): NbtLong = NbtLong.of(decoder.decodeLong())
}

public object NbtFloatSerializer : KSerializer<NbtFloat> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NbtFloat", PrimitiveKind.FLOAT)
    override fun serialize(encoder: Encoder, value: NbtFloat): Unit = encoder.encodeFloat(value.floatValue())
    override fun deserialize(decoder: Decoder): NbtFloat = NbtFloat.of(decoder.decodeFloat())
}

public object NbtDoubleSerializer : KSerializer<NbtDouble> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NbtDouble", PrimitiveKind.DOUBLE)
    override fun serialize(encoder: Encoder, value: NbtDouble): Unit = encoder.encodeDouble(value.doubleValue())
    override fun deserialize(decoder: Decoder): NbtDouble = NbtDouble.of(decoder.decodeDouble())
}

public object NbtStringSerializer : KSerializer<NbtString> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NbtString", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: NbtString): Unit = encoder.encodeString(value.asString())
    override fun deserialize(decoder: Decoder): NbtString = NbtString.of(decoder.decodeString())
}

public object NbtNullSerializer : KSerializer<NbtNull> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NbtNull", PrimitiveKind.BYTE)
    override fun serialize(encoder: Encoder, value: NbtNull): Unit = encoder.encodeByte(0)
    override fun deserialize(decoder: Decoder): NbtNull = NbtNull.INSTANCE.also { decoder.decodeByte() }
}

public object NbtByteArraySerializer : KSerializer<NbtByteArray> {
    private val serializer = ByteArraySerializer()
    override val descriptor: SerialDescriptor = serializer.descriptor

    override fun serialize(encoder: Encoder, value: NbtByteArray): Unit = serializer.serialize(encoder, value.byteArray)

    override fun deserialize(decoder: Decoder): NbtByteArray = NbtByteArray(serializer.deserialize(decoder))
}

public object NbtIntArraySerializer : KSerializer<NbtIntArray> {
    private val serializer = IntArraySerializer()
    override val descriptor: SerialDescriptor = serializer.descriptor

    override fun serialize(encoder: Encoder, value: NbtIntArray): Unit = serializer.serialize(encoder, value.intArray)

    override fun deserialize(decoder: Decoder): NbtIntArray = NbtIntArray(serializer.deserialize(decoder))
}

public object NbtLongArraySerializer : KSerializer<NbtLongArray> {
    private val serializer = LongArraySerializer()
    override val descriptor: SerialDescriptor = serializer.descriptor

    override fun serialize(encoder: Encoder, value: NbtLongArray): Unit = serializer.serialize(encoder, value.longArray)

    override fun deserialize(decoder: Decoder): NbtLongArray = NbtLongArray(serializer.deserialize(decoder))
}

@OptIn(InternalSerializationApi::class)
public object NbtElementSerializer : KSerializer<NbtElement> {
    override val descriptor: SerialDescriptor = buildSerialDescriptor("kotlinx.serialization.Polymorphic", PolymorphicKind.OPEN) {
        element("type", String.serializer().descriptor)
        element(
            "value",
            buildSerialDescriptor("kotlinx.serialization.Polymorphic<${NbtElement::class.simpleName}>", SerialKind.CONTEXTUAL)
        )
    }

    override fun serialize(encoder: Encoder, value: NbtElement) {
        if (encoder is ICanEncodeTag) encoder.encodeTag(value)
        else PolymorphicSerializer(NbtElement::class).serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): NbtElement {
        if (decoder is ICanDecodeTag) return decoder.decodeTag()
        else return PolymorphicSerializer(NbtElement::class).deserialize(decoder)
    }
}

/**
 * NbtList can only hold one type of tag
 */
public object NbtListSerializer : KSerializer<NbtList> {
    private val serializer = ListSerializer(NbtElementSerializer)
    override val descriptor: SerialDescriptor = serializer.descriptor

    override fun serialize(encoder: Encoder, value: NbtList) {
        serializer.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): NbtList = NbtList(serializer.deserialize(decoder), 0)
}

public object NbtCompoundSerializer : KSerializer<NbtCompound> {
    private val serializer = MapSerializer(String.serializer(), NbtElementSerializer)
    override val descriptor: SerialDescriptor = serializer.descriptor

    override fun serialize(encoder: Encoder, value: NbtCompound) {
        if (encoder is ICanEncodeNbtCompound) {
            encoder.encodeNbtCompound(value)
        } else {
            serializer.serialize(encoder, value.entries)
        }
    }

    override fun deserialize(decoder: Decoder): NbtCompound {
        if (decoder is ICanDecodeNbtCompound) {
            return decoder.decodeNbtCompound()
        }
        return NbtCompound(serializer.deserialize(decoder))
    }
}

public object ItemStackSerializer : KSerializer<ItemStack> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ItemStack") {
        element("id", String.serializer().descriptor)
        element("Count", Int.serializer().descriptor)
        element("tag", NbtCompoundSerializer.descriptor)
    }
    private const val IdIndex = 0
    private const val CountIndex = 1
    private const val TagIndex = 2


    override fun serialize(encoder: Encoder, value: ItemStack) {
        if (encoder is ICanEncodeItemStack) {
            encoder.encodeItemStack(value)
        } else {
            val compositeOutput = encoder.beginStructure(descriptor)
            compositeOutput.encodeStringElement(descriptor, IdIndex, Registry.ITEM.getId(value.item).toString())
            compositeOutput.encodeIntElement(descriptor, CountIndex, value.count)
            compositeOutput.encodeSerializableElement(descriptor, TagIndex, NbtCompoundSerializer.nullable, value.nbt)
            compositeOutput.endStructure(descriptor)
        }
    }


    override fun deserialize(decoder: Decoder): ItemStack {
        if (decoder is ICanDecodeItemStack) {
            return decoder.decodeItemStack()
        }

        val dec = decoder.beginStructure(descriptor)

        var id: String? = null
        var count = 0
        var tag: NbtCompound? = null
        var countExists = false
        if (dec.decodeSequentially()) {
            id = dec.decodeStringElement(descriptor, IdIndex)
            count = dec.decodeIntElement(descriptor, CountIndex)
            tag = dec.decodeSerializableElement(descriptor, TagIndex, NbtCompoundSerializer.nullable)
            countExists = true
        } else {
            loop@ while (true) {
                when (val i = dec.decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break@loop
                    IdIndex -> {
                        id = dec.decodeStringElement(descriptor, i)
                    }

                    CountIndex -> {
                        count = dec.decodeIntElement(descriptor, i)
                        countExists = true
                    }

                    TagIndex -> {
                        tag = dec.decodeNullableSerializableElement(descriptor, TagIndex, NbtCompoundSerializer.nullable)
                    }

                    else -> throw SerializationException("Unknown index $i")
                }
            }
        }


        dec.endStructure(descriptor)
        if (id == null) {
            id = missingField("ID", "ItemStack") { "minecraft:air" }
        }
        if (!countExists) {
            count = missingField("count", "ItemStack") { 0 }
        }

        return ItemStack(
            Registry.ITEM.get(Identifier(id)).also { if (tag != null) it.postProcessNbt(tag) },
            count
        ).apply { this.nbt = tag }
    }


}
public object IngredientSerializer : KSerializer<Ingredient> {
    private val serializer = ArraySerializer(ItemStackSerializer)
    override val descriptor: SerialDescriptor = serializer.descriptor

    override fun serialize(encoder: Encoder, value: Ingredient) {
        if (encoder is ICanEncodeIngredient) {
            encoder.encodeIngredient(value)
        } else {
            serializer.serialize(encoder, value.matchingStacks)
        }
    }

    override fun deserialize(decoder: Decoder): Ingredient {
        if (decoder is ICanDecodeIngredient) {
            return decoder.decodeIngredient()
        } else {
            return Ingredient.ofStacks(*serializer.deserialize(decoder))
        }
    }
}

/**
 * Note: there is no guarantee that the default value will be saved since it's impossible to access it.
 * Nevertheless, the default value doesn't matter after the point the list has been initialized.
 */
public class DefaultedListSerializer<T>(elementSerializer: KSerializer<T>) : KSerializer<DefaultedList<T>> {
    private val serializer = ListSerializer(elementSerializer)
    override val descriptor: SerialDescriptor = serializer.descriptor

    override fun serialize(encoder: Encoder, value: DefaultedList<T>): Unit =
        serializer.serialize(encoder, value)

    override fun deserialize(decoder: Decoder): DefaultedList<T> {
        val list = serializer.deserialize(decoder)
        return DefaultedList(list, list.firstOrNull())
    }
}

public object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Uuid") {
        element("most", Long.serializer().descriptor)
        element("least", Long.serializer().descriptor)
    }

    private const val MostIndex = 0
    private const val LeastIndex = 1

    override fun serialize(encoder: Encoder, value: UUID) {
        val compositeOutput = encoder.beginStructure(descriptor)
        compositeOutput.encodeLongElement(descriptor, MostIndex, value.mostSignificantBits)
        compositeOutput.encodeLongElement(descriptor, LeastIndex, value.leastSignificantBits)
        compositeOutput.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): UUID {
        val dec: CompositeDecoder = decoder.beginStructure(descriptor)

        val index = dec.decodeElementIndex(descriptor)
        if (dec.decodeSequentially()) {
            val most = dec.decodeLongElement(descriptor, MostIndex)
            val least = dec.decodeLongElement(descriptor, LeastIndex)

            dec.endStructure(descriptor)
            return UUID(most, least)
        } else {
            return handleUnorthodoxInputOrdering(index, dec)
        }
    }

    // boxing can be avoided here
    private fun handleUnorthodoxInputOrdering(index: Int, dec: CompositeDecoder): UUID {
        var most: Long? = null // consider using flags or bit mask if you
        var least: Long? = null // need to read nullable non-optional properties
        when (index) {
            CompositeDecoder.DECODE_DONE -> throw SerializationException("Read should not be done yet.")
            MostIndex -> most = dec.decodeLongElement(descriptor, index)
            LeastIndex -> least = dec.decodeLongElement(descriptor, index)
            else -> throw SerializationException("Unknown index $index")
        }

        loop@ while (true) {
            when (val i = dec.decodeElementIndex(descriptor)) {
                CompositeDecoder.DECODE_DONE -> break@loop
                MostIndex -> most = dec.decodeLongElement(descriptor, i)
                LeastIndex -> least = dec.decodeLongElement(descriptor, i)
                else -> throw SerializationException("Unknown index $i")
            }
        }
        dec.endStructure(descriptor)
        return UUID(
            most ?: missingField("most", "UUID") { 0L },
            least ?: missingField("least", "UUID") { 0L }
        )
    }
}


public object Vec3dSerializer : KSerializer<Vec3d> {


    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Vec3d") {
        element("x", Double.serializer().descriptor)
        element("y", Double.serializer().descriptor)
        element("z", Double.serializer().descriptor)
    }

    private const val XIndex = 0
    private const val YIndex = 1
    private const val ZIndex = 2

    override fun serialize(encoder: Encoder, value: Vec3d) {
        val compositeOutput = encoder.beginStructure(descriptor)
        compositeOutput.encodeDoubleElement(descriptor, XIndex, value.x)
        compositeOutput.encodeDoubleElement(descriptor, YIndex, value.y)
        compositeOutput.encodeDoubleElement(descriptor, ZIndex, value.z)
        compositeOutput.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): Vec3d {
        val dec: CompositeDecoder = decoder.beginStructure(descriptor)

        var x = 0.0
        var y = 0.0
        var z = 0.0
        var xExists = false
        var yExists = false
        var zExists = false
        if (dec.decodeSequentially()) {
            x = dec.decodeDoubleElement(descriptor, XIndex)
            y = dec.decodeDoubleElement(descriptor, YIndex)
            z = dec.decodeDoubleElement(descriptor, ZIndex)
            xExists = true
            yExists = true
            zExists = true
        } else {
            loop@ while (true) {
                when (val i = dec.decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break@loop
                    XIndex -> {
                        x = dec.decodeDoubleElement(descriptor, i)
                        xExists = true
                    }

                    YIndex -> {
                        y = dec.decodeDoubleElement(descriptor, i)
                        yExists = true
                    }

                    ZIndex -> {
                        z = dec.decodeDoubleElement(descriptor, i)
                        zExists = true
                    }

                    else -> throw SerializationException("Unknown index $i")
                }
            }
        }


        dec.endStructure(descriptor)
        if (!xExists) x = missingField("x", "Vec3d") { 0.0 }
        if (!yExists) y = missingField("y", "Vec3d") { 0.0 }
        if (!zExists) z = missingField("z", "Vec3d") { 0.0 }

        return Vec3d(x, y, z)
    }
}


