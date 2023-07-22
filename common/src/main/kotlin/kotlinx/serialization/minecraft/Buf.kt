@file:OptIn(ExperimentalSerializationApi::class)

package kotlinx.serialization.minecraft

import kotlinx.serialization.minecraft.impl.ByteBufDecoder
import kotlinx.serialization.minecraft.impl.ByteBufEncoder
import kotlinx.serialization.minecraft.impl.nbt.MinecraftModule
import kotlinx.serialization.*
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import net.minecraft.network.PacketByteBuf


public open class Buf(context: SerializersModule) : SerialFormat {

    public companion object Default : Buf(EmptySerializersModule())

    public fun <T> encodeToByteBuf(serializer: SerializationStrategy<T>, obj: T, toBuf: PacketByteBuf){
        ByteBufEncoder(toBuf, this).apply {
            if (obj != null) {
                encodeNotNullMark()
                encodeSerializableValue(serializer, obj)
            } else encodeNull()
        }
    }

    public fun <T> decodeFromByteBuf(deserializer: DeserializationStrategy<T>, buf: PacketByteBuf): T {
        val decoder = ByteBufDecoder(buf, this)
        return when {
            decoder.decodeNotNullMark() -> decoder.decodeSerializableValue(deserializer)
            deserializer.descriptor.isNullable -> null as T
            else -> throw SerializationException("You need to use a nullable serializer to be able to read a nullable value. Use the .nullable extension property.")
        }
    }

    override val serializersModule: SerializersModule = context + MinecraftModule
}

public inline fun <reified T> Buf.encodeToByteBuf(obj: T, buf: PacketByteBuf): Unit = encodeToByteBuf(serializersModule.serializer(), obj, buf)
public inline fun <reified T> Buf.decodeFromByteBuf(buf: PacketByteBuf): T = decodeFromByteBuf(serializersModule.serializer(), buf)


/**
 * Writes [obj] into [toBuf], to later be retrieved with [readFrom].
 * @param context Used for polymorphic serialization, see [Here](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md).
 */
public fun <T> SerializationStrategy<T>.write(obj: T, toBuf: PacketByteBuf, context: SerializersModule = EmptySerializersModule()) {
    Buf(context).encodeToByteBuf(this, obj, toBuf)
}


/**
 * Retrieves the object that was stored in the [buf] previously with [write]. For nullable values use .nullable extension on the serializer.
 *  @param context Used for polymorphic serialization, see [Here](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md).
 */
public fun <T> DeserializationStrategy<T>.readFrom(buf: PacketByteBuf, context: SerializersModule = EmptySerializersModule()): T {
    return Buf(context).decodeFromByteBuf(this, buf)
}


