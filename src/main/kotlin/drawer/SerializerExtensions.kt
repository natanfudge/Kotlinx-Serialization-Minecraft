package drawer

import TagDecoder
import TagEncoder
import kotlinx.serialization.*
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.PacketByteBuf


/**
 * Converts [obj] into a [CompoundTag] that represents [obj].
 * Later [fromTag] can be called to retrieve an identical instance of [obj] from the [CompoundTag].
 *
 * These functions are not documented because I think they would be confusing.
 * Do you want these to be an official part of the API? Please make an issue.
 */

fun <T> SerializationStrategy<T>.convertToTag(obj: T) = TagEncoder().also { it.encode(this, obj) }.compoundTag

fun <T> DeserializationStrategy<T>.fromTag(tag: CompoundTag) = TagDecoder(tag).decode(this)


/**
 * Puts [obj] into the [CompoundTag] instance of [inTag].
 * Later [getFrom] can be called to retrieve an identical instance of [obj] from the [CompoundTag].
 *
 * @param key If you are serializing two objects of the same type, you MUST  specify a key.
 * The same key must be used in [getFrom].
 */
fun <T> SerializationStrategy<T>.put(obj: T?, inTag: CompoundTag, key: String? = null) {
    val usedKey = key ?: this.descriptor.name
    require(!inTag.containsKey(usedKey)) {
        """A '${this.descriptor.name}' appears twice in the CompoundTag.
            |If you are serializing two objects of the same type, you MUST specify a key, see kdoc.
        |Also make sure you didn't use the same key twice.
    """.trimMargin()
    }
    if(obj != null) inTag.put(usedKey, convertToTag(obj))
}


/**
 * Retrieves the object the tag that was stored in [tag] with [put] and converts it into the original object.
 * That object can be null. If you know it's not nullable use [getFrom] instead.
 *
 * @param key If you are serializing two objects of the same type, you MUST specify a key.
 * The same key must be used in [put].
 */
fun <T> DeserializationStrategy<T>.getNullableFrom(tag: CompoundTag, key: String? = null): T? {
    val deserializedTag = tag.getTag(key ?: this.descriptor.name) ?: return null
    return fromTag(deserializedTag as CompoundTag)
}

/**
 * Retrieves the object the tag that was stored in [tag] with [put] and converts it into the original object.
 * That object cannot be null. If you need it to be nullable use [getNullableFrom] instead.
 *
 * @param key If you are serializing two objects of the same type, you MUST specify a key.
 * The same key must be used in [put].
 */
fun <T> DeserializationStrategy<T>.getFrom(tag: CompoundTag, key: String? = null): T = getNullableFrom(tag,key) ?:
 throw SerializationException("getFrom cannot be used on a nullable value. Use getNullableFrom instead.")



/**
 * Writes [obj] into [toBuf], to later be retrieved with [readFrom].
 */
fun <T> SerializationStrategy<T>.write(obj: T?, toBuf: PacketByteBuf) {
    ByteBufEncoder(toBuf).apply {
        if(obj != null) {
            encodeNotNullMark()
            encode(this@write, obj)
        } else encodeNull()
    }
}

/**
 * Retrieves the object that was stored in the [buf] previously with [write].
 * Must be used on non-null values only. For nullable values use [readNullableFrom].
 */
fun <T> DeserializationStrategy<T>.readFrom(buf: PacketByteBuf): T = readNullableFrom(buf) ?:
throw SerializationException("readFrom cannot be used on a nullable value. Use readNullableFrom instead.")
/**
 * Retrieves the object that was stored in the [buf] previously with [write].
 * For non-null values use [readFrom].
 */
fun <T> DeserializationStrategy<T>.readNullableFrom(buf: PacketByteBuf): T? {
    val decoder = ByteBufDecoder(buf)
    return if(decoder.decodeNotNullMark()){
        decoder.decode(this)
    }else null
}

