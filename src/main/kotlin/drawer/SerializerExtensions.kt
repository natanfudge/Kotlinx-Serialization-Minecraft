package drawer

import drawer.nbt.NbtFormat
import kotlinx.serialization.*
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf


/**
 * Puts [obj] into the [CompoundTag] instance of [inTag].
 * Later [getFrom] can be called to retrieve an identical instance of [obj] from the [CompoundTag].
 * For nullable values you SHOULD NOT use the .nullable serializer. It is not needed and does not work.
 *
 * @param key If you are serializing two objects of the same type, you MUST  specify a key.
 * The same key must be used in [getFrom].
 * @param context Used for polymorphic serialization, see [Here](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md).
 */
@OptIn(ExperimentalSerializationApi::class)
fun <T> SerializationStrategy<T>.put(
    obj: T?,
    inTag: CompoundTag,
    key: String? = null,
    context: SerializersModule = EmptySerializersModule
) {
    val usedKey = key ?: this.descriptor.serialName
    require(!inTag.contains(usedKey)) {
        """A '${this.descriptor.serialName}' appears twice in the CompoundTag.
            |If you are serializing two objects of the same type, you MUST specify a key, see kdoc.
        |Also make sure you didn't use the same key twice.
    """.trimMargin()
    }
    if (obj != null) inTag.put(usedKey, NbtFormat(context).serialize(this, obj))
}

/**
 * Retrieves the object the tag that was stored in [tag] with [put] and converts it into the original object.
 * For nullable values use the .nullable extension on the serializer.
 *
 * @param key If you are serializing two objects of the same type, you MUST specify a key.
 * The same key must be used in [put].
 * @param context Used for polymorphic serialization, see [Here](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md).
 */
@OptIn(ExperimentalSerializationApi::class)
fun <T> DeserializationStrategy<T>.getFrom(
    tag: CompoundTag,
    key: String? = null,
    context: SerializersModule = EmptySerializersModule
): T {
    val deserializedTag =
        tag.get(key ?: this.descriptor.serialName) ?: if (descriptor.isNullable) return null as T else CompoundTag()
    return NbtFormat(context).deserialize(this, deserializedTag)
}


/**
 * Writes [obj] into [toBuf], to later be retrieved with [readFrom].
 * @param context Used for polymorphic serialization, see [Here](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md).
 */
@OptIn(ExperimentalSerializationApi::class)
fun <T> SerializationStrategy<T>.write(obj: T?, toBuf: PacketByteBuf, context: SerializersModule = EmptySerializersModule) {
    ByteBufFormat(context).ByteBufEncoder(toBuf).apply {
        if (obj != null) {
            encodeNotNullMark()
            encodeSerializableValue(this@write, obj)
        } else encodeNull()
    }
}


/**
 * Retrieves the object that was stored in the [buf] previously with [write]. For nullable values use .nullable extension on the serializer.
 *  @param context Used for polymorphic serialization, see [Here](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md).
 */
@OptIn(ExperimentalSerializationApi::class)
fun <T> DeserializationStrategy<T>.readFrom(buf: PacketByteBuf, context: SerializersModule = EmptySerializersModule): T {
    val decoder = ByteBufFormat(context).ByteBufDecoder(buf)
    return when {
        decoder.decodeNotNullMark() -> decoder.decodeSerializableValue(this)
        descriptor.isNullable -> null as T
        else -> throw SerializationException("You need to use a nullable serializer to be able to read a nullable value. Use the .nullable extension property.")
    }
}


