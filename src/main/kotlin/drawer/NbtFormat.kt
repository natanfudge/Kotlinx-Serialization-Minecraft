package drawer

import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.ints.IntStack
import kotlinx.serialization.*
import kotlinx.serialization.internal.EnumDescriptor
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import net.minecraft.nbt.*

//TODO: go through everything and make sure it's needed
//TODO: document .nullable
//TODO: think if we want direct tags or nested ones (probably direct to avoid boilerplate)
//TODO: instruct to not use pos, and throw an error if they try to put when one already exists (somehow)
//TODO: tell not to use primitive serializers and point to issue

//TODO: handle invalid state (have a look at @Optional)
//TODO: test invalid state

//TODO: explain in readme how the packet handler already catches errors for you
//TODO: explain that you still need to check the validity of the packet
//TODO: explain in readme why we must have nulls everywhere and be careful
//TODO: test readme (example mod)
//TODO: revisit this claim "Or make myInfo nullable without lateinit if initializing it at first placement is not guaranteed"


//TODO: Later version:
//TODO: Text serializer
//TODO: getNullableFrom / readNullableFrom extension methods for all types  + optional key for all types
//TODO: "Identifiable" serializer
//TODO: SimpleFixedItemInv serializer

internal val TagModule = SerializersModule {
    polymorphic(Tag::class) {
        ByteTag::class with ForByteTag
        ShortTag::class with ForShortTag
        IntTag::class with ForIntTag
        LongTag::class with ForLongTag
        FloatTag::class with ForFloatTag
        DoubleTag::class with ForDoubleTag
        StringTag::class with ForStringTag
        EndTag::class with ForEndTag
        ByteArrayTag::class with ForByteArrayTag
        IntArrayTag::class with ForIntArrayTag
        LongArrayTag::class with ForLongArrayTag
        ListTag::class with ForListTag
        CompoundTag::class with ForCompoundTag
    }
}


/**
 * Keeping this class public for now in case you want to serializer an object directly to tag and vise versa.
 */
class NbtFormat(context: SerialModule = EmptyModule) : AbstractSerialFormat(context + TagModule) {
    private fun String.nullMarked() = "$this\$N"

    /**
     * Converts [obj] into a [CompoundTag] that represents [obj].
     * Later [deserialize] can be called to retrieve an identical instance of [obj] from the [CompoundTag].
     *
     * These functions are not documented because I think they would be confusing.
     * Do you want these to be an official part of the API? Please make an issue.
     */
    fun <T> serialize(serializer: SerializationStrategy<T>, obj: T): CompoundTag {
        return TagEncoder().also { it.encode(serializer, obj) }.compoundTag
    }

    fun <T> deserialize(deserializer: DeserializationStrategy<T>, tag: CompoundTag): T {
        return TagDecoder(tag).decode(deserializer)
    }


    internal inner class TagEncoder(val compoundTag: CompoundTag = CompoundTag()) : NamedValueTagEncoder() {
        override val context: SerialModule = this@NbtFormat.context

        override fun encodeTaggedBoolean(tag: String, value: Boolean) = compoundTag.putBoolean(tag, value)
        override fun beginCollection(desc: SerialDescriptor, collectionSize: Int, vararg typeParams: KSerializer<*>):
                CompositeEncoder {
            encodeTaggedInt(nested("size"), collectionSize)

            return this
        }


        override fun encodeTaggedByte(tag: String, value: Byte) = compoundTag.putByte(tag, value)
        override fun encodeTaggedChar(tag: String, value: Char) = compoundTag.putString(tag, value.toString())
        override fun encodeTaggedDouble(tag: String, value: Double) = compoundTag.putDouble(tag, value)
        override fun encodeTaggedEnum(tag: String, enumDescription: EnumDescriptor, ordinal: Int) =
            compoundTag.putInt(tag, ordinal)

        override fun encodeTaggedFloat(tag: String, value: Float) = compoundTag.putFloat(tag, value)
        override fun encodeTaggedInt(tag: String, value: Int) = compoundTag.putInt(tag, value)
        override fun encodeTaggedLong(tag: String, value: Long) = compoundTag.putLong(tag, value)
        //        override fun encodeTaggedNotNullMark(tag: String) = compoundTag.putByte(tag.nullMarked(), 1)
        override fun encodeTaggedNull(tag: String) {
            compoundTag.putByte(tag.nullMarked(), Null)
        }

        override fun encodeTaggedShort(tag: String, value: Short) = compoundTag.putShort(tag, value)
        override fun encodeTaggedString(tag: String, value: String) = compoundTag.putString(tag, value)
        override fun encodeTaggedUnit(tag: String) = compoundTag.putByte(tag, 2)
        override fun encodeTaggedValue(tag: String, value: Any) {
            if (value is Tag) {
                compoundTag.put(tag, value)
            } else {
                throw SerializationException("Non-serializable ${value::class} is not supported by ${this::class} encoder")
            }

        }

        override fun encodeTaggedTag(key: String, tag: Tag) {
            compoundTag.put(key, tag)
        }


    }

    private enum class WriteMode {
        Primitive,
        List,
        Compound
    }

    companion object {
        private val NonPrimitiveKinds = listOf(
            StructureKind.LIST,
            StructureKind.CLASS,
            StructureKind.MAP,
            UnionKind.POLYMORPHIC,
            UnionKind.ENUM_KIND,
            UnionKind.OBJECT,
            UnionKind.SEALED
        )

        private const val Null = 1.toByte()
        private const val NotNull = 0.toByte()

    }

    internal inner class TagDecoder(private val map: CompoundTag) : NamedValueTagDecoder() {


        var posStack: IntStack = IntArrayList()

        private var writeMode = WriteMode.Primitive

        override fun beginStructure(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
            posStack.push(0)
            return this
        }

//            writeMode = when (desc.kind) {
////                PrimitiveKind.INT -> TODO()
////                PrimitiveKind.UNIT -> TODO()
////                PrimitiveKind.BOOLEAN -> TODO()
////                PrimitiveKind.BYTE -> TODO()
////                PrimitiveKind.SHORT -> TODO()
////                PrimitiveKind.LONG -> TODO()
////                PrimitiveKind.FLOAT -> TODO()
////                PrimitiveKind.DOUBLE -> TODO()
////                PrimitiveKind.CHAR -> TODO()
////                PrimitiveKind.STRING -> TODO()
//                StructureKind.CLASS -> WriteMode.Compound
//                StructureKind.LIST -> WriteMode.List
//                StructureKind.MAP -> WriteMode.Compound
//                else -> WriteMode.Compound
////                UnionKind.OBJECT -> TODO()
////                UnionKind.ENUM_KIND -> TODO()
////                UnionKind.SEALED -> TODO()
////                UnionKind.POLYMORPHIC -> TODO()
//            }
//            return this
//        }

        override fun endStructure(desc: SerialDescriptor) {
            posStack.popInt()
        }


        override val context: SerialModule = this@NbtFormat.context
        override fun decodeCollectionSize(desc: SerialDescriptor): Int {
            return decodeTaggedInt(nested("size"))
        }

        override fun decodeTaggedBoolean(tag: String) = map.getBoolean(tag)
        override fun decodeTaggedByte(tag: String) = map.getByte(tag)
        override fun decodeTaggedChar(tag: String) = map.getString(tag).toCharArray()[0]
        override fun decodeTaggedDouble(tag: String) = map.getDouble(tag)
        override fun decodeTaggedEnum(tag: String, enumDescription: EnumDescriptor) =
            map.getInt(tag)

        override fun decodeTaggedFloat(tag: String) = map.getFloat(tag)
        override fun decodeTaggedInt(tag: String) = map.getInt(tag)

        override fun decodeTaggedLong(tag: String) = map.getLong(tag)
        override fun decodeTaggedNull(tag: String): Nothing? = null
        override fun decodeTaggedNotNullMark(tag: String) = map.getByte(tag.nullMarked()) != Null
        override fun decodeTaggedShort(tag: String) = map.getShort(tag)
        override fun decodeTaggedString(tag: String): String = map.getString(tag)
        override fun decodeTaggedUnit(tag: String) = Unit
        override fun decodeTaggedTag(key: String): Tag = map.getTag(key)!!


        private fun incrementPos() = posStack.push(posStack.popInt() + 1)

        private fun buildDescriptionParts(): List<String> {
            val parts = mutableListOf<String>()
            for (key in map.keys) {
                for (i in key.length - 1 downTo 0) {
                    if (key[i] == '.') parts.add(key.substring(0, i))
                }
            }
            return parts.distinct()
        }

        //TODO: hack, replace with a nested compound tag format
        private val descriptionParts = buildDescriptionParts()

        override fun decodeElementIndex(desc: SerialDescriptor): Int {
            var position = posStack.topInt()
            val size = if (desc.kind == StructureKind.LIST) decodeCollectionSize(desc) else desc.elementsCount
            while (position < size) {
                incrementPos()

                val name = desc.getTag(position)
                if (name in map.keys || name in descriptionParts || name.nullMarked() in map.keys || name.nullMarked() in descriptionParts) {
                    return position
                }
                position = posStack.topInt()

            }
            return CompositeDecoder.READ_DONE


        }

    }


}








