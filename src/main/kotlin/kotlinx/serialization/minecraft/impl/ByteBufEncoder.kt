@file:OptIn(ExperimentalSerializationApi::class)

package kotlinx.serialization.minecraft.impl

import kotlinx.serialization.minecraft.Buf
import io.netty.buffer.Unpooled
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.SerializersModule
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.Ingredient


internal fun bufferedPacket() = PacketByteBuf(Unpooled.buffer())


internal class ByteBufEncoder(private val buf: PacketByteBuf, private val format: Buf) : AbstractEncoder(), ICanEncodeNbtCompound,
    ICanEncodeIngredient, ICanEncodeItemStack {

    override val serializersModule: SerializersModule = format.serializersModule

    private inline fun debug(msg: () -> String) {
        if (DEBUG) println("ENCODE: " + msg())
    }

    private val DEBUG = false

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        debug { "COLLECTION" }
        return super.beginCollection(descriptor, collectionSize).also {
            buf.writeInt(collectionSize)
        }
    }

    override fun encodeNull() {
        debug { "NULL" }
        buf.writeByte(0)
    }

    override fun encodeNotNullMark() {
        debug { "NOTNULL" }
        buf.writeByte(1)
    }

    override fun encodeBoolean(value: Boolean) {
        debug { "BOOLEAN" }
        buf.writeByte(if (value) 1 else 0)
    }

    override fun encodeByte(value: Byte) {
        debug { "BYTE" }
        buf.writeByte(value.toInt())
    }

    override fun encodeShort(value: Short) {
        debug { "SHORT" }
        buf.writeShort(value.toInt())
    }

    override fun encodeInt(value: Int) {
        debug { "INT" }
        buf.writeInt(value)
    }

    override fun encodeLong(value: Long) {
        debug { "LONG" }
        buf.writeLong(value)
    }

    override fun encodeFloat(value: Float) {
        debug { "FLOAT" }
        buf.writeFloat(value)
    }

    override fun encodeDouble(value: Double) {
        debug { "DOUBLE" }
        buf.writeDouble(value)
    }

    override fun encodeChar(value: Char) {
        debug { "CHAR" }
        buf.writeChar(value.code)
    }

    override fun encodeString(value: String) {
        debug { "STRING: $value" }
        buf.writeString(value)
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        debug { "STRUCTURE END" }
        //No need to do anything
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        debug { "ENUM" }
        buf.writeInt(index)
    }

    override fun encodeIngredient(ingredient: Ingredient) {
        debug { "INGREDIENT" }
        ingredient.write(buf)
    }

    override fun encodeNbtCompound(tag: NbtCompound) {
        debug { "COMPOUNDTAG" }
        buf.writeNbt(tag)
    }

    override fun encodeItemStack(stack: ItemStack) {
        buf.writeItemStack(stack)
    }

}

internal class ByteBufDecoder(private val buf: PacketByteBuf, private val format: Buf) : AbstractDecoder(), ICanDecodeNbtCompound,
    ICanDecodeIngredient, ICanDecodeItemStack {

    override fun decodeSequentially(): Boolean {
        return true
    }


    private inline fun debug(msg: () -> String) {
        if (DEBUG) println("DECODE: " + msg())
    }

    private val DEBUG = false

    override val serializersModule: SerializersModule = format.serializersModule

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
        debug { "COLLECTION" }
        return buf.readInt()
    }

    override fun decodeNotNullMark(): Boolean {
        debug { "NOTNULL" }
        return buf.readByte() != 0.toByte()
    }

    override fun decodeBoolean(): Boolean {
        debug { "BOOLEAN" }
        return buf.readByte().toInt() != 0
    }

    override fun decodeByte(): Byte {
        debug { "BYTE" }
        return buf.readByte()
    }

    override fun decodeShort(): Short {
        debug { "SHORT" }
        return buf.readShort()
    }

    override fun decodeInt(): Int {
        debug { "INT" }
        return buf.readInt()
    }

    override fun decodeLong(): Long {
        debug { "LONG" }
        return buf.readLong()
    }

    override fun decodeFloat(): Float {
        debug { "FLOAT" }
        return buf.readFloat()
    }

    override fun decodeDouble(): Double {
        debug { "DOUBLE" }
        return buf.readDouble()
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        debug { "INDEX" }
        return 0
//            buf.siz
//            if (elementIndex == descriptor.elementsCount) return CompositeDecoder.DECODE_DONE
//            return elementIndex++
    }

    override fun decodeChar(): Char {
        debug { "CHAR" }
        return buf.readChar()
    }

    override fun decodeString(): String {
        debug { "STRING" }
        return buf.readString(StringLengthCap)
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        debug { "ENUM" }
        return buf.readInt()
    }

    override fun decodeNull(): Nothing? {
        debug { "NULL" }
        return null
    }

    override fun decodeNbtCompound(): NbtCompound {
        debug { "COMPOUNDTAG" }
        return buf.readNbt()!!
    }

    override fun decodeIngredient(): Ingredient {
        debug { "INGREDIENT" }
        return Ingredient.fromPacket(buf)
    }

    override fun decodeItemStack(): ItemStack {
        return buf.readItemStack()
    }

}




private const val StringLengthCap = 32767

