package drawer

import TagDecoder
import TagEncoder
import kotlinx.serialization.*
import kotlinx.serialization.internal.LongDescriptor
import kotlinx.serialization.internal.SerialClassDescImpl
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.PacketByteBuf
import java.util.*

object Serializers {
    @Serializer(forClass = net.minecraft.util.math.BlockPos::class)
    object BlockPos : KSerializer<net.minecraft.util.math.BlockPos> {
        override val descriptor: SerialDescriptor
            get() = LongDescriptor.withName("BlockPos")

        override fun serialize(encoder: Encoder, obj: net.minecraft.util.math.BlockPos) {
            encoder.encodeLong(obj.asLong())
        }

        override fun deserialize(decoder: Decoder): net.minecraft.util.math.BlockPos {
            return net.minecraft.util.math.BlockPos.fromLong(decoder.decodeLong())
        }
    }

    @Serializer(forClass = UUID::class)
    object Uuid : KSerializer<UUID> {
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

            assert(dec.decodeElementIndex(descriptor) == CompositeDecoder.READ_ALL)
            { "Only use the UUID serializer for tag compounds and bytebuffs." }

            val most = dec.decodeLongElement(descriptor, MostIndex)
            val least = dec.decodeLongElement(descriptor, LeastIndex)

            dec.endStructure(descriptor)
            return UUID(most, least)
        }


    }


}


/**
 * Converts [obj] into a [CompoundTag] that represents [obj].
 * Later [fromTag] can be called to retrieve an identical instance of [obj] from the [CompoundTag].
 *
 * These functions are not part of the api because I think they would be confusing.
 * Do you want these internal functions as part of the API? Please make an issue.
 */
//TODO: check if this should be internal and document if not

 fun < T > KSerializer<T>.convertToTag(obj: T) = TagEncoder().also { it.encode(this, obj) }.compoundTag
//fun <T> convertToTag(serializer: KSerializer<T>, obj: T) =
//    TagEncoder().also { it.encode(serializer, obj) }.compoundTag

fun <T> KSerializer<T>.fromTag(tag: CompoundTag) = TagDecoder(tag).decode(this)


/**
 * Puts [obj] into the [CompoundTag] instance of [inTag].
 * Later [getFrom] can be called to retrieve an identical instance of [obj] from the [CompoundTag].
 *
 * @param key If you are serializing two objects of the same type, you MUST  specify a key.
 * The same key must be used in [getFrom].
 */
fun <T> KSerializer<T>.put(obj: T, inTag: CompoundTag, key: String? = null) {
    val usedKey = key ?: this.descriptor.name
    require(!inTag.containsKey(usedKey)) {
        """If you are serializing two objects of the same type, you MUST specify a key, see kdoc.
        |Also make sure you didn't use the same key twice.
    """.trimMargin()
    }
    inTag.put(usedKey, convertToTag(obj))
}


/**
 * Retrieves the object the tag that was stored in [tag] with [put] and converts it into the original object.
 *
 * @param key If you are serializing two objects of the same type, you MUST specify a key.
 * The same key must be used in [put].
 */
fun <T> KSerializer<T>.getFrom(tag: CompoundTag, key: String? = null): T {
    return fromTag(tag.getTag(key ?: this.descriptor.name) as CompoundTag)
}

/**
 * Writes [obj] into [toBuf], to later be retrieved with [readFrom].
 */
fun <T> KSerializer<T>.write(obj: T, toBuf: PacketByteBuf) = ByteBufEncoder(toBuf).encode(this, obj)

/**
 * Retrieves the object that was stored in the [buf] previously with [write].
 */
fun <T> KSerializer<T>.readFrom(buf: PacketByteBuf): T = ByteBufDecoder(buf).decode(this)


