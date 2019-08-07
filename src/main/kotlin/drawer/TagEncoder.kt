import kotlinx.serialization.*
import kotlinx.serialization.internal.EnumDescriptor
import net.minecraft.nbt.CompoundTag

//TODO: bytebuf encoder
//TODO: document usage
//TODO: upload to bintray and curseforge

/**
 * This demo shows another approach to serialization:
 * Instead of writing fields and their values separately in two steps, as ElementValueOutput does,
 * in NamedValueOutput they got merged into one call.
 *
 * NamedValue is a subclass of TaggedValue, which allows you to associate any custom tag with object's field,
 * see TaggedDemo.kt.
 *
 * Here, the tag is field's name. Functionality of these classes is similar to kotlinx.serialization.Mapper
 * Note that null values are not supported here.
 */

internal class TagEncoder(val compoundTag: CompoundTag = CompoundTag()) : NamedValueEncoder() {
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

internal class TagDecoder(private val map: CompoundTag) : NamedValueDecoder() {
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


