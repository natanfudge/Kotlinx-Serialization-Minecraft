package drawer

import kotlinx.serialization.*
import kotlinx.serialization.internal.EnumDescriptor
import net.minecraft.util.PacketByteBuf


/**
 * This demo shows how user can define his own custom binary format
 *
 * In most cases, it is sufficient to define how all primitive types are getting serialized.
 * If you want precise control over the fields and maybe record their names, you can use
 * `writeElement` methods, see CustomKeyValueDemo.kt
 */

class ByteBufEncoder(val buf: PacketByteBuf) : ElementValueEncoder() {
    override fun beginCollection(
        desc: SerialDescriptor,
        collectionSize: Int,
        vararg typeParams: KSerializer<*>
    ): CompositeEncoder {
        return super.beginCollection(desc, collectionSize, *typeParams).also {
            buf.writeInt(collectionSize)
        }
    }

    override fun encodeNull() {
        buf.writeByte(0)
    }
    override fun encodeNotNullMark() {
        buf.writeByte(1)
    }
    override fun encodeBoolean(value: Boolean) {
        buf.writeByte(if (value) 1 else 0)
    }
    override fun encodeByte(value: Byte) {
        buf.writeByte(value.toInt())
    }
    override fun encodeShort(value: Short) {
        buf.writeShort(value.toInt())
    }
    override fun encodeInt(value: Int) {
        buf.writeInt(value)
    }
    override fun encodeLong(value: Long) {
        buf.writeLong(value)
    }
    override fun encodeFloat(value: Float) {
        buf.writeFloat(value)
    }
    override fun encodeDouble(value: Double) {
        buf.writeDouble(value)
    }
    override fun encodeChar(value: Char) {
        buf.writeChar(value.toInt())
    }
    override fun encodeString(value: String) {
         buf.writeString(value)
    }
    override fun encodeEnum(enumDescription: EnumDescriptor, ordinal: Int) {
         buf.writeInt(ordinal)
    }
}

class ByteBufDecoder(private val buf: PacketByteBuf) : ElementValueDecoder() {
    override fun decodeCollectionSize(desc: SerialDescriptor): Int = buf.readInt()
    override fun decodeNotNullMark(): Boolean = buf.readByte() != 0.toByte()
    override fun decodeBoolean(): Boolean = buf.readByte().toInt() != 0
    override fun decodeByte(): Byte = buf.readByte()
    override fun decodeShort(): Short = buf.readShort()
    override fun decodeInt(): Int = buf.readInt()
    override fun decodeLong(): Long = buf.readLong()
    override fun decodeFloat(): Float = buf.readFloat()
    override fun decodeDouble(): Double = buf.readDouble()
    override fun decodeChar(): Char = buf.readChar()
    override fun decodeString(): String = buf.readString()
    override fun decodeEnum(enumDescription: EnumDescriptor): Int = buf.readInt()
    override fun decodeNull(): Nothing? = null
}

