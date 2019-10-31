package drawer

import io.netty.buffer.Unpooled
import kotlinx.serialization.*
import kotlinx.serialization.internal.EnumDescriptor
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.plus
import net.minecraft.nbt.CompoundTag
import net.minecraft.recipe.Ingredient
import net.minecraft.util.PacketByteBuf

internal fun bufferedPacket() = PacketByteBuf(Unpooled.buffer())
internal class ByteBufFormat(context: SerialModule = EmptyModule) : AbstractSerialFormat(context + TagModule) {
    inner class ByteBufEncoder(private val buf: PacketByteBuf) : ElementValueEncoder(), ICanEncodeCompoundTag,
        ICanEncodeIngredient {


        override val context: SerialModule = this@ByteBufFormat.context

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

        override fun encodeIngredient(ingredient: Ingredient) {
            ingredient.write(buf)
        }

        override fun encodeCompoundTag(tag: CompoundTag) {
            buf.writeCompoundTag(tag)
        }

    }

    inner class ByteBufDecoder(private val buf: PacketByteBuf) : ElementValueDecoder(), ICanDecodeCompoundTag,
        ICanDecodeIngredient {


        override val context: SerialModule = this@ByteBufFormat.context

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
        override fun decodeString(): String = buf.readString(StringLengthCap)
        override fun decodeEnum(enumDescription: EnumDescriptor): Int = buf.readInt()
        override fun decodeNull(): Nothing? = null
        override fun decodeCompoundTag(): CompoundTag = buf.readCompoundTag()!! /*?: CompoundTag()*/
        override fun decodeIngredient(): Ingredient = Ingredient.fromPacket(buf)

    }


}

private const val StringLengthCap = 32767

