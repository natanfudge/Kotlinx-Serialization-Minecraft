@file:Suppress("EXPERIMENTAL_API_USAGE")

package drawer

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.internal.*
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


@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = net.minecraft.util.math.BlockPos::class)
object ForBlockPos : KSerializer<BlockPos> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BlockPos", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: BlockPos) = encoder.encodeLong(value.asLong())
    override fun deserialize(decoder: Decoder): BlockPos = BlockPos.fromLong(decoder.decodeLong())
}


@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Identifier::class)
object ForIdentifier : KSerializer<Identifier> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Identifier", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Identifier) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): Identifier = Identifier(decoder.decodeString())
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = SoundEvent::class)
object ForSoundEvent : KSerializer<SoundEvent> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SoundEvent", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: SoundEvent) {
        encoder.encodeInt(Registry.SOUND_EVENT.getRawId(value))
    }

    override fun deserialize(decoder: Decoder): SoundEvent = Registry.SOUND_EVENT.get(decoder.decodeInt())
        ?: SoundEvents.ENTITY_ITEM_PICKUP
}


@Serializer(forClass = NbtByte::class)
object ForNbtByte : KSerializer<NbtByte> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NbtByte", PrimitiveKind.BYTE)
    override fun serialize(encoder: Encoder, value: NbtByte) = encoder.encodeByte(value.byteValue())
    override fun deserialize(decoder: Decoder): NbtByte = NbtByte.of(decoder.decodeByte())
}

@Serializer(forClass = NbtShort::class)
object ForNbtShort : KSerializer<NbtShort> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NbtShort", PrimitiveKind.SHORT)
    override fun serialize(encoder: Encoder, value: NbtShort) = encoder.encodeShort(value.shortValue())
    override fun deserialize(decoder: Decoder): NbtShort = NbtShort.of(decoder.decodeShort())
}

@Serializer(forClass = NbtInt::class)
object ForNbtInt : KSerializer<NbtInt> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NbtInt",PrimitiveKind.INT)
    override fun serialize(encoder: Encoder, value: NbtInt) = encoder.encodeInt(value.intValue())
    override fun deserialize(decoder: Decoder): NbtInt = NbtInt.of(decoder.decodeInt())
}

@Serializer(forClass = NbtLong::class)
object ForNbtLong : KSerializer<NbtLong> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NbtLong",PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: NbtLong) = encoder.encodeLong(value.longValue())
    override fun deserialize(decoder: Decoder): NbtLong = NbtLong.of(decoder.decodeLong())
}

@Serializer(forClass = NbtFloat::class)
object ForNbtFloat : KSerializer<NbtFloat> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NbtFloat",PrimitiveKind.FLOAT)
    override fun serialize(encoder: Encoder, value: NbtFloat) = encoder.encodeFloat(value.floatValue())
    override fun deserialize(decoder: Decoder): NbtFloat = NbtFloat.of(decoder.decodeFloat())
}

@Serializer(forClass = NbtDouble::class)
object ForNbtDouble : KSerializer<NbtDouble> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NbtDouble",PrimitiveKind.DOUBLE)
    override fun serialize(encoder: Encoder, value: NbtDouble) = encoder.encodeDouble(value.doubleValue())
    override fun deserialize(decoder: Decoder): NbtDouble = NbtDouble.of(decoder.decodeDouble())
}

@Serializer(forClass = NbtString::class)
object ForNbtString : KSerializer<NbtString> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NbtString",PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: NbtString) = encoder.encodeString(value.asString())
    override fun deserialize(decoder: Decoder): NbtString = NbtString.of(decoder.decodeString())
}

@Serializer(forClass = NbtNull::class)
object ForNbtNull : KSerializer<NbtNull> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NbtNull",PrimitiveKind.BYTE)
    override fun serialize(encoder: Encoder, value: NbtNull) = encoder.encodeByte(0)
    override fun deserialize(decoder: Decoder): NbtNull = NbtNull.INSTANCE.also { decoder.decodeByte() }
}

@Serializer(forClass = NbtByteArray::class)
//TODO: optimizable by making the inner byte array public with a getter mixin
object ForNbtByteArray : KSerializer<NbtByteArray> {
    override val descriptor: SerialDescriptor = PublicedListLikeDescriptorImpl(ForNbtByte.descriptor, "NbtByteArray")

    override fun serialize(encoder: Encoder, value: NbtByteArray) =
        ListSerializer(ForNbtByte).serialize(encoder, value)

    override fun deserialize(decoder: Decoder): NbtByteArray =
        NbtByteArray(ListSerializer(ForNbtByte).deserialize(decoder).map { it.byteValue() })
}

//TODO: optimizable by making the inner int array public with a getter mixin
@Serializer(forClass = NbtIntArray::class)
object ForNbtIntArray : KSerializer<NbtIntArray> {
    override val descriptor: SerialDescriptor = PublicedListLikeDescriptorImpl(ForNbtInt.descriptor, "NbtIntArray")

    override fun serialize(encoder: Encoder, value: NbtIntArray) =
        ListSerializer(ForNbtInt).serialize(encoder, value)

    override fun deserialize(decoder: Decoder): NbtIntArray =
        NbtIntArray(ListSerializer(ForNbtInt).deserialize(decoder).map { it.intValue() })
}

//TODO: optimizable by making the inner long array public with a getter mixin
@Serializer(forClass = NbtLongArray::class)
object ForNbtLongArray : KSerializer<NbtLongArray> {
    override val descriptor: SerialDescriptor = PublicedListLikeDescriptorImpl(ForNbtLong.descriptor, "NbtLongArray")

    override fun serialize(encoder: Encoder, value: NbtLongArray) =
        ListSerializer(ForNbtLong).serialize(encoder, value)

    override fun deserialize(decoder: Decoder): NbtLongArray =
        NbtLongArray(ListSerializer(ForNbtLong).deserialize(decoder).map { it.longValue() })
}

//TODO: optimizable by using the exisiting encoding system
@OptIn(InternalSerializationApi::class)
@Serializer(forClass = NbtElement::class)
object ForTag : KSerializer<NbtElement> {
    override val descriptor: SerialDescriptor =         buildSerialDescriptor("kotlinx.serialization.Polymorphic", PolymorphicKind.OPEN) {
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
//TODO: optimizable by making the inner List<Tag> public with a getter mixin
@Serializer(forClass = NbtList::class)
object ForNbtList : KSerializer<NbtList> {
    override val descriptor: SerialDescriptor = PublicedListLikeDescriptorImpl(ForTag.descriptor, "NbtList")

    override fun serialize(encoder: Encoder, value: NbtList) {
        ListSerializer(ForTag).serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): NbtList = NbtList().apply {
        for (tag in ListSerializer(ForTag).deserialize(decoder)) {
            add(tag)
        }
    }
}

//TODO: optimizable by making the inner Map<String,Tag> public with a getter mixin
@Serializer(forClass = NbtCompound::class)
object ForNbtCompound : KSerializer<NbtCompound> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("NbtCompound"){
        mapSerialDescriptor(PrimitiveSerialDescriptor("Key", PrimitiveKind.STRING), ForTag.descriptor)
    }

    override fun serialize(encoder: Encoder, value: NbtCompound) {
        if (encoder is ICanEncodeNbtCompound) {
            encoder.encodeNbtCompound(value)
        } else {
            MapSerializer(String.serializer(), ForTag).serialize(
                encoder,
                value.keys.map { it to value.get(it)!! }.toMap()
            )
        }

    }

    override fun deserialize(decoder: Decoder): NbtCompound {
        if (decoder is ICanDecodeNbtCompound) {
            return decoder.decodeNbtCompound()
        }
        return NbtCompound().apply {
            for ((key, value) in MapSerializer(String.serializer(), ForTag).deserialize(decoder)) {
                put(key, value)
            }
        }
    }
}

//TODO: serialize / deserialize can be optimized specifically in ByteBufEncoder / TagEncoder to use the built-in functions
// PacketByteBuf#writeItemStack, ItemStack#ToTag, etc, Using canEncodeItemStack interface the same way as in Tag.
//TODO: optimizable by making the inner TagCompound public with a getter mixin
@Serializer(forClass = ItemStack::class)
object ForItemStack : KSerializer<ItemStack> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ItemStack") {
            element("id",String.serializer().descriptor)
        element("count",Int.serializer().descriptor)
            element("tag",ForNbtCompound.descriptor)

    }
    private const val IdIndex = 0
    private const val CountIndex = 1
    private const val TagIndex = 2


    override fun serialize(encoder: Encoder, value: ItemStack) {
        val compositeOutput = encoder.beginStructure(descriptor)
        compositeOutput.encodeStringElement(descriptor, IdIndex, Registry.ITEM.getId(value.item).toString())
        compositeOutput.encodeIntElement(descriptor, CountIndex, value.count)
        compositeOutput.encodeSerializableElement(descriptor, TagIndex, ForNbtCompound.nullable, value.nbt)
        compositeOutput.endStructure(descriptor)
    }


    override fun deserialize(decoder: Decoder): ItemStack {
        val dec = decoder.beginStructure(descriptor)

        var id: String? = null
        var count = 0
        var tag: NbtCompound? = null
        var countExists = false
        if(dec.decodeSequentially()){
            id = dec.decodeStringElement(descriptor, IdIndex)
            count = dec.decodeIntElement(descriptor, CountIndex)
            tag = dec.decodeSerializableElement(descriptor, TagIndex, ForNbtCompound.nullable)
            countExists = true
        } else{
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
                        tag = dec.decodeNullableSerializableElement(descriptor, TagIndex, ForNbtCompound.nullable)
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


//TODO: optimizable by making the inner Array<ItemStack> public with a getter mixin
@Serializer(forClass = Ingredient::class)
object ForIngredient : KSerializer<Ingredient> {
    override val descriptor: SerialDescriptor = PublicedListLikeDescriptorImpl(ForItemStack.descriptor, "Ingredient")

    private val helperSerializer = ListSerializer(ForItemStack)

    override fun serialize(encoder: Encoder, value: Ingredient) {
        if (encoder is ICanEncodeIngredient) {
            encoder.encodeIngredient(value)
        } else {
            // This is the only choice to serialize Ingredient in other formats
            val buf = bufferedPacket()
            value.write(buf)
            val stackArrayLength = buf.readVarInt()
            val matchingStacks = List(stackArrayLength) { buf.readItemStack() }
            helperSerializer.serialize(encoder, matchingStacks)
        }
    }

    override fun deserialize(decoder: Decoder): Ingredient {
        if (decoder is ICanDecodeIngredient) {
            return decoder.decodeIngredient()
        } else {
            // This is the only choice to serialize Ingredient in other formats
            val matchingStacks = helperSerializer.deserialize(decoder)
            val buf = bufferedPacket().apply {
                writeVarInt(matchingStacks.size)
                for (matchingStack in matchingStacks) {
                    writeItemStack(matchingStack)
                }
            }
            return Ingredient.fromPacket(buf)
        }
    }
}

//TODO: optimizable by making the inner List<E> public with a setter mixin so we don't need to convert lists to array here

/**
 * Note: there is no guarantee that the default value will be saved since it's impossible to access it.
 * Nontheless, the default value doesn't matter after the point the list has been initialized.
 */
@Serializer(forClass = DefaultedList::class)
class ForDefaultedList<T>(elementSerializer: KSerializer<T>) : KSerializer<DefaultedList<T>> {
    override val descriptor: SerialDescriptor =
        PublicedListLikeDescriptorImpl(elementSerializer.descriptor, "DefaultedList")

    private val helperSerializer = ListSerializer(elementSerializer)

    override fun serialize(encoder: Encoder, value: DefaultedList<T>) =
        helperSerializer.serialize(encoder, value)

    override fun deserialize(decoder: Decoder): DefaultedList<T> {
        val list = helperSerializer.deserialize(decoder)
        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "UNCHECKED_CAST")
        list as java.util.Collection<T>

        @Suppress("UNCHECKED_CAST")
        return DefaultedList.copyOf(list.firstOrNull(), *list.toArray()) as DefaultedList<T>
    }
}

@Serializer(forClass = UUID::class)
object ForUuid : KSerializer<UUID> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Uuid"){
        element("most",Long.serializer().descriptor)
        element("least",Long.serializer().descriptor)
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

    //TODO: boxing can be avoided here
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


@Serializer(forClass = Vec3d::class)
object ForVec3d : KSerializer<Vec3d> {
//    private val helperSerializer = TripleSerializer(DoubleSerializer, DoubleSerializer, DoubleSerializer)


    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Vec3d"){
                                                                         element("x",Double.serializer().descriptor)
                                                                         element("y",Double.serializer().descriptor)
                                                                         element("z",Double.serializer().descriptor)
    }
//    override val descriptor: SerialDescriptor  = helperSerializer.descriptor

    //TODO: learn wtf patch is


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
        if(dec.decodeSequentially()){
            x = dec.decodeDoubleElement(descriptor, XIndex)
            y = dec.decodeDoubleElement(descriptor, YIndex)
            z = dec.decodeDoubleElement(descriptor, ZIndex)
            xExists = true
            yExists = true
            zExists = true
        } else{
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


