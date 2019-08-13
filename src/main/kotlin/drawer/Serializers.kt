package drawer

import kotlinx.serialization.*
import kotlinx.serialization.internal.*
import net.minecraft.item.ItemStack
import net.minecraft.nbt.*
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import java.util.*

@Serializer(forClass = net.minecraft.util.math.BlockPos::class)
object ForBlockPos : KSerializer<BlockPos> {
    override val descriptor: SerialDescriptor = LongDescriptor.withName("BlockPos")
    override fun serialize(encoder: Encoder, obj: BlockPos) = encoder.encodeLong(obj.asLong())
    override fun deserialize(decoder: Decoder): BlockPos = BlockPos.fromLong(decoder.decodeLong())
}


@Serializer(forClass = Identifier::class)
object ForIdentifier : KSerializer<Identifier> {
    override val descriptor: SerialDescriptor = LongDescriptor.withName("Identifier")
    override fun serialize(encoder: Encoder, obj: Identifier) = encoder.encodeString(obj.toString())
    override fun deserialize(decoder: Decoder): Identifier = Identifier(decoder.decodeString())
}


@Serializer(forClass = ByteTag::class)
object ForByteTag : KSerializer<ByteTag> {
    override val descriptor: SerialDescriptor = ByteDescriptor.withName("ByteTag")
    override fun serialize(encoder: Encoder, obj: ByteTag) = encoder.encodeByte(obj.byte)
    override fun deserialize(decoder: Decoder): ByteTag = ByteTag(decoder.decodeByte())
}

@Serializer(forClass = ShortTag::class)
object ForShortTag : KSerializer<ShortTag> {
    override val descriptor: SerialDescriptor = ShortDescriptor.withName("ShortTag")
    override fun serialize(encoder: Encoder, obj: ShortTag) = encoder.encodeShort(obj.short)
    override fun deserialize(decoder: Decoder): ShortTag = ShortTag(decoder.decodeShort())
}

@Serializer(forClass = IntTag::class)
object ForIntTag : KSerializer<IntTag> {
    override val descriptor: SerialDescriptor = IntDescriptor.withName("IntTag")
    override fun serialize(encoder: Encoder, obj: IntTag) = encoder.encodeInt(obj.int)
    override fun deserialize(decoder: Decoder): IntTag = IntTag(decoder.decodeInt())
}

@Serializer(forClass = LongTag::class)
object ForLongTag : KSerializer<LongTag> {
    override val descriptor: SerialDescriptor = LongDescriptor.withName("LongTag")
    override fun serialize(encoder: Encoder, obj: LongTag) = encoder.encodeLong(obj.long)
    override fun deserialize(decoder: Decoder): LongTag = LongTag(decoder.decodeLong())
}

@Serializer(forClass = FloatTag::class)
object ForFloatTag : KSerializer<FloatTag> {
    override val descriptor: SerialDescriptor = FloatDescriptor.withName("FloatTag")
    override fun serialize(encoder: Encoder, obj: FloatTag) = encoder.encodeFloat(obj.float)
    override fun deserialize(decoder: Decoder): FloatTag = FloatTag(decoder.decodeFloat())
}

@Serializer(forClass = DoubleTag::class)
object ForDoubleTag : KSerializer<DoubleTag> {
    override val descriptor: SerialDescriptor = DoubleDescriptor.withName("DoubleTag")
    override fun serialize(encoder: Encoder, obj: DoubleTag) = encoder.encodeDouble(obj.double)
    override fun deserialize(decoder: Decoder): DoubleTag = DoubleTag(decoder.decodeDouble())
}

@Serializer(forClass = StringTag::class)
object ForStringTag : KSerializer<StringTag> {
    override val descriptor: SerialDescriptor = StringDescriptor.withName("StringTag")
    override fun serialize(encoder: Encoder, obj: StringTag) = encoder.encodeString(obj.asString())
    override fun deserialize(decoder: Decoder): StringTag = StringTag(decoder.decodeString())
}

@Serializer(forClass = EndTag::class)
object ForEndTag : KSerializer<EndTag> {
    override val descriptor: SerialDescriptor = ByteDescriptor.withName("EndTag")
    override fun serialize(encoder: Encoder, obj: EndTag) = encoder.encodeByte(0)
    override fun deserialize(decoder: Decoder): EndTag = EndTag().also { decoder.decodeByte() }
}

@Serializer(forClass = ByteArrayTag::class)
object ForByteArrayTag : KSerializer<ByteArrayTag> {
    override val descriptor: SerialDescriptor = UnsealedListLikeDescriptorImpl(ForByteTag.descriptor, "ByteArrayTag")

    override fun serialize(encoder: Encoder, obj: ByteArrayTag) =
        ArrayListSerializer(ForByteTag).serialize(encoder, obj)

    override fun deserialize(decoder: Decoder): ByteArrayTag =
        ByteArrayTag(ArrayListSerializer(ForByteTag).deserialize(decoder).map { it.byte })
}

@Serializer(forClass = IntArrayTag::class)
object ForIntArrayTag : KSerializer<IntArrayTag> {
    override val descriptor: SerialDescriptor = UnsealedListLikeDescriptorImpl(ForIntTag.descriptor, "IntArrayTag")

    override fun serialize(encoder: Encoder, obj: IntArrayTag) =
        ArrayListSerializer(ForIntTag).serialize(encoder, obj)

    override fun deserialize(decoder: Decoder): IntArrayTag =
        IntArrayTag(ArrayListSerializer(ForIntTag).deserialize(decoder).map { it.int })
}

@Serializer(forClass = LongArrayTag::class)
object ForLongArrayTag : KSerializer<LongArrayTag> {
    override val descriptor: SerialDescriptor = UnsealedListLikeDescriptorImpl(ForLongTag.descriptor, "LongArrayTag")

    override fun serialize(encoder: Encoder, obj: LongArrayTag) =
        ArrayListSerializer(ForLongTag).serialize(encoder, obj)

    override fun deserialize(decoder: Decoder): LongArrayTag =
        LongArrayTag(ArrayListSerializer(ForLongTag).deserialize(decoder).map { it.long })
}

@Serializer(forClass = Tag::class)
object ForTag : KSerializer<Tag> {
    override val descriptor: SerialDescriptor = PolymorphicClassDescriptor
    override fun serialize(encoder: Encoder, obj: Tag) {
        if (encoder is ICanEncodeTag) encoder.encodeTag(obj)
        else PolymorphicSerializer(Tag::class).serialize(encoder, obj)
        val x = 2
    }

    override fun deserialize(decoder: Decoder): Tag {
        if (decoder is ICanDecodeTag) return decoder.decodeTag()
        else return PolymorphicSerializer(Tag::class).deserialize(decoder) as Tag
    }
}

/**
 * ListTag can only hold one type of tag
 */
@Serializer(forClass = ListTag::class)
object ForListTag : KSerializer<ListTag> {
    override val descriptor: SerialDescriptor = UnsealedListLikeDescriptorImpl(ForTag.descriptor, "ListTag")

    override fun serialize(encoder: Encoder, obj: ListTag) {
        ArrayListSerializer(ForTag).serialize(encoder, obj)
    }

    override fun deserialize(decoder: Decoder): ListTag = ListTag().apply {
        for (tag in ArrayListSerializer(ForTag).deserialize(decoder)) {
            add(tag)
        }
    }
}

@Serializer(forClass = CompoundTag::class)
object ForCompoundTag : KSerializer<CompoundTag> {
    override val descriptor: SerialDescriptor = HashMapClassDesc(StringDescriptor, ForTag.descriptor)

    override fun serialize(encoder: Encoder, obj: CompoundTag) {
        if (encoder is ICanEncodeCompoundTag) {
            encoder.encodeCompoundTag(obj)
        } else {
            HashMapSerializer(StringSerializer, ForTag).serialize(
                encoder,
                obj.keys.map { it to obj.getTag(it)!! }.toMap()
            )
        }

    }

    override fun deserialize(decoder: Decoder): CompoundTag {
        if (decoder is ICanDecodeCompoundTag) {
            return decoder.decodeCompoundTag()
        }
        return CompoundTag().apply {
            for ((key, value) in HashMapSerializer(StringSerializer, ForTag).deserialize(decoder)) {
                put(key, value)
            }
        }
    }
}

//TODO: serialize / deserialize can be optimized specifically in ByteBufEncoder / TagEncoder to use the built-in functions
// PacketByteBuf#writeItemStack, ItemStack#ToTag, etc, Using canEncodeItemStack interface the same way as in Tag.
@Serializer(forClass = ItemStack::class)
object ForItemStack : KSerializer<ItemStack> {
    override val descriptor: SerialDescriptor = object : SerialClassDescImpl("ItemStack") {
        init {
            addElement("id")
            addElement("count")
            addElement("tag")
        }
    }

//    private const val IdIndex = 0
//    private const val CountIndex = 1
//    private const val TagNullabilityIndex = 2
//    private const val TagIndex = 3
//
//    private const val NullValue = 0.toByte()
//    private const val NonNullValue = 1.toByte()


    override fun serialize(encoder: Encoder, obj: ItemStack) {
        ForCompoundTag.serialize(encoder,obj.toTag(CompoundTag()))
//        val compositeOutput = encoder.beginStructure(descriptor)
//        compositeOutput.encodeStringElement(descriptor, IdIndex, Registry.ITEM.getId(obj.item).toString())
//        compositeOutput.encodeByteElement(descriptor, CountIndex, obj.count.toByte())
//        compositeOutput.encodeByteElement(
//            descriptor,
//            TagNullabilityIndex,
//            if (obj.tag == null) NullValue else NonNullValue
//        )
//        if(obj.tag != null){
//            compositeOutput.encodeSerializableElement(descriptor, TagIndex, ForCompoundTag, obj.tag!!)
//        }else{
//            compositeOutput.encodeSerializableElement(descriptor, TagIndex, ForCompoundTag, CompoundTag())
//        }
//
//        compositeOutput.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): ItemStack {
        return ItemStack.fromTag(ForCompoundTag.deserialize(decoder))
//        val dec: CompositeDecoder = decoder.beginStructure(descriptor)
//
//        val index = dec.decodeElementIndex(descriptor)
//        if (index == CompositeDecoder.READ_ALL) {
//            val itemId = dec.decodeStringElement(descriptor, IdIndex)
//            val count = dec.decodeByteElement(descriptor, CountIndex)
//            val tagNullability = dec.decodeByteElement(descriptor, TagNullabilityIndex)
//            val tag = dec.decodeSerializableElement(descriptor, TagIndex, ForCompoundTag)
//            dec.endStructure(descriptor)
//
//
//            return ItemStack(Registry.ITEM.get(itemId),)
//        } else {
//            var most: Long? = null // consider using flags or bit mask if you
//            var least: Long? = null // need to read nullable non-optional properties
//            when (index) {
//                CompositeDecoder.READ_DONE -> throw SerializationException("Read should not be done yet.")
//                ForUuid.MostIndex -> most = dec.decodeLongElement(descriptor, index)
//                ForUuid.LeastIndex -> least = dec.decodeLongElement(descriptor, index)
//                else -> throw SerializationException("Unknown index $index")
//            }
//
//            loop@ while (true) {
//                when (val i = dec.decodeElementIndex(ForUuid.descriptor)) {
//                    CompositeDecoder.READ_DONE -> break@loop
//                    ForUuid.MostIndex -> most = dec.decodeLongElement(descriptor, i)
//                    ForUuid.LeastIndex -> least = dec.decodeLongElement(descriptor, i)
//                    else -> throw SerializationException("Unknown index $i")
//                }
//            }
//            dec.endStructure(ForUuid.descriptor)
//            return UUID(
//                most ?: throw MissingFieldException("most"),
//                least ?: throw MissingFieldException("least")
//            )
//        }
    }


}


@Serializer(forClass = UUID::class)
object ForUuid : KSerializer<UUID> {
    override val descriptor: SerialDescriptor = object : SerialClassDescImpl("Uuid") {
        init {
            addElement("most") // most will have index 0
            addElement("least") // least will have index 1
        }
    }

    private const val MostIndex = 0
    private const val LeastIndex = 1

    override fun serialize(encoder: Encoder, obj: UUID) {
        val compositeOutput = encoder.beginStructure(descriptor)
        compositeOutput.encodeLongElement(descriptor, MostIndex, obj.mostSignificantBits)
        compositeOutput.encodeLongElement(descriptor, LeastIndex, obj.leastSignificantBits)
        compositeOutput.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): UUID {
        val dec: CompositeDecoder = decoder.beginStructure(descriptor)

        val index = dec.decodeElementIndex(descriptor)
        if (index == CompositeDecoder.READ_ALL) {
            val most = dec.decodeLongElement(descriptor, MostIndex)
            val least = dec.decodeLongElement(descriptor, LeastIndex)

            dec.endStructure(descriptor)
            return UUID(most, least)
        } else {
            return handleWeirdInputOrdering(index, dec)
        }
    }

    private fun handleWeirdInputOrdering(index: Int, dec: CompositeDecoder): UUID {
        var most: Long? = null // consider using flags or bit mask if you
        var least: Long? = null // need to read nullable non-optional properties
        when (index) {
            CompositeDecoder.READ_DONE -> throw SerializationException("Read should not be done yet.")
            MostIndex -> most = dec.decodeLongElement(descriptor, index)
            LeastIndex -> least = dec.decodeLongElement(descriptor, index)
            else -> throw SerializationException("Unknown index $index")
        }

        loop@ while (true) {
            when (val i = dec.decodeElementIndex(descriptor)) {
                CompositeDecoder.READ_DONE -> break@loop
                MostIndex -> most = dec.decodeLongElement(descriptor, i)
                LeastIndex -> least = dec.decodeLongElement(descriptor, i)
                else -> throw SerializationException("Unknown index $i")
            }
        }
        dec.endStructure(descriptor)
        return UUID(
            most ?: throw MissingFieldException("most"),
            least ?: throw MissingFieldException("least")
        )
    }
}
