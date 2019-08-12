package drawer

import kotlinx.serialization.*
import kotlinx.serialization.internal.*
import net.minecraft.nbt.*
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import java.util.*


object Serializers {
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
        override val descriptor: SerialDescriptor =UnsealedListLikeDescriptorImpl(ForByteTag.descriptor,"ByteArrayTag")

        override fun serialize(encoder: Encoder, obj: ByteArrayTag) =
            ArrayListSerializer(ForByteTag).serialize(encoder, obj)

        override fun deserialize(decoder: Decoder): ByteArrayTag =
            ByteArrayTag(ArrayListSerializer(ForByteTag).deserialize(decoder).map { it.byte })
    }

    @Serializer(forClass = IntArrayTag::class)
    object ForIntArrayTag : KSerializer<IntArrayTag> {
        override val descriptor: SerialDescriptor = UnsealedListLikeDescriptorImpl(ForIntTag.descriptor,"IntArrayTag")

        override fun serialize(encoder: Encoder, obj: IntArrayTag) =
            ArrayListSerializer(ForIntTag).serialize(encoder, obj)

        override fun deserialize(decoder: Decoder): IntArrayTag =
            IntArrayTag(ArrayListSerializer(ForIntTag).deserialize(decoder).map { it.int })
    }

    @Serializer(forClass = LongArrayTag::class)
    object ForLongArrayTag : KSerializer<LongArrayTag> {
        override val descriptor: SerialDescriptor =UnsealedListLikeDescriptorImpl(ForLongTag.descriptor,"LongArrayTag")

        override fun serialize(encoder: Encoder, obj: LongArrayTag) =
            ArrayListSerializer(ForLongTag).serialize(encoder, obj)

        override fun deserialize(decoder: Decoder): LongArrayTag =
            LongArrayTag(ArrayListSerializer(ForLongTag).deserialize(decoder).map { it.long })
    }

    @Serializer(forClass = Tag::class)
    object ForTag : KSerializer<Tag>{
       override val descriptor: SerialDescriptor = PolymorphicClassDescriptor
        override fun serialize(encoder: Encoder, obj: Tag) {
            PolymorphicSerializer(Tag::class).serialize(encoder,obj)
        }
         override fun deserialize(decoder: Decoder): Tag {
            return PolymorphicSerializer(Tag::class).deserialize(decoder) as Tag
        }
    }

    /**
     * ListTag can only hold one type of tag
     */
    @Serializer(forClass = ListTag::class)
    class ForListTag : KSerializer<ListTag>{
        override val descriptor: SerialDescriptor = UnsealedListLikeDescriptorImpl(ForTag.descriptor,"ListTag")

        override fun serialize(encoder: Encoder, obj: ListTag) {
            ArrayListSerializer(ForTag).serialize(encoder,obj)
        }
        override fun deserialize(decoder: Decoder): ListTag = ListTag().apply {
            for(tag in ArrayListSerializer(ForTag).deserialize(decoder)){
                add(tag)
            }
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
    }




//
//    @Serializer(forClass = McItemStack::class)
//    object ItemStack : KSerializer<McItemStack> {
//        override val descriptor: SerialDescriptor = object : SerialClassDescImpl("ItemStack") {
//            init {
//                addElement("id") //  will have index 0
//                addElement("count") //  will have index 1
//                addElement("tag")
//            }
//        }
//
//        private const val IdIndex = 0
//        private const val CountIndex = 1
//        private const val TagIndex = 2
//
//        override fun serialize(encoder: Encoder, obj: McItemStack) {
//            val compositeOutput = encoder.beginStructure(descriptor)
//            compositeOutput.encodeStringElement(descriptor, IdIndex, Registry.ITEM.getId(obj.item).toString())
//            compositeOutput.encodeByteElement(descriptor, CountIndex, obj.count.toByte())
//            compositeOutput.encodeNullableSerializableElement(descriptor,)
//            compositeOutput.endStructure(descriptor)
//            compoundTag_1.putString("id", )
//            compoundTag_1.putByte("Count", this.count as Byte)
//            if (this.tag != null) {
//                compoundTag_1.put("tag", this.tag)
//            }
//
//            return compoundTag_1
//        }
//
//        override fun deserialize(decoder: Decoder): McItemStack {
//
//        }
//
//
//    }


}

