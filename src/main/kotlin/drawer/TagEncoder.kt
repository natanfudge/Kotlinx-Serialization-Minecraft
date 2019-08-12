package drawer

import kotlinx.serialization.*
import kotlinx.serialization.internal.EnumDescriptor
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import net.minecraft.nbt.*

//TODO: test readme (example mod)
//TODO: revisit this claim "Or make myInfo nullable without lateinit if initializing it at first placement is not guaranteed"

//TODO: figure out polymorphic serialization
//TODO: nbt serializer -> Itemstack serializer -> Ingredient serializer -> DefaultedList<...> serializer(add to readme when done)

//TODO: Later version:
//TODO: Text serializer
//TODO: getNullableFrom / readNullableFrom extension methods for all types  + optional key for all types
//TODO: "Identifiable" serializer

internal val TagModule = SerializersModule {
    polymorphic(Tag::class) {
        ByteTag::class with Serializers.ForByteTag
        ShortTag::class with Serializers.ForShortTag
        IntTag::class with Serializers.ForIntTag
        LongTag::class with Serializers.ForLongTag
        FloatTag::class with Serializers.ForFloatTag
        DoubleTag::class with Serializers.ForDoubleTag
        StringTag::class with Serializers.ForStringTag
        EndTag::class with Serializers.ForEndTag
        ByteArrayTag::class with Serializers.ForByteArrayTag
        IntArrayTag::class with Serializers.ForIntArrayTag
        LongArrayTag::class with Serializers.ForLongArrayTag
//            Tag::class with ForTag
//            Tag::class with ForTag
    }
}

/**
 * Keeping this class public for now in case you want to serializer an object directly to tag and vise versa.
 */
class NbtFormat(context: SerialModule = EmptyModule) : AbstractSerialFormat(context + TagModule) {

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


    internal inner class TagEncoder(val compoundTag: CompoundTag = CompoundTag()) : NamedValueEncoder() {
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
        override fun encodeTaggedNotNullMark(tag: String) = compoundTag.putByte(tag + "mark", 1)
        override fun encodeTaggedNull(tag: String) {
            compoundTag.putByte(tag + "mark", 0)
        }

        override fun encodeTaggedShort(tag: String, value: Short) = compoundTag.putShort(tag, value)
        override fun encodeTaggedString(tag: String, value: String) = compoundTag.putString(tag, value)
        override fun encodeTaggedUnit(tag: String) = compoundTag.putByte(tag, 2)

    }

    internal inner class TagDecoder(private val map: CompoundTag) : NamedValueDecoder() {
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
        override fun decodeTaggedNotNullMark(tag: String) = map.getByte(tag + "mark") != 0.toByte()
        override fun decodeTaggedShort(tag: String) = map.getShort(tag)

        override fun decodeTaggedString(tag: String): String = map.getString(tag)
        override fun decodeTaggedUnit(tag: String) = Unit

    }
}







