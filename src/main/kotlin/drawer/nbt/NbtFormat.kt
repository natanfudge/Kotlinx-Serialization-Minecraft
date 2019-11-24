package drawer.nbt

import drawer.*
import kotlinx.serialization.*
import kotlinx.serialization.json.JsonException
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import net.minecraft.nbt.*

//TODO: put up issues for these

//TODO: Later version:
//TODO: Text serializer
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

    /**
     * Converts [obj] into a [CompoundTag] that represents [obj].
     * Later [deserialize] can be called to retrieve an identical instance of [obj] from the [CompoundTag].
     *
     * These functions are not documented because I think they would be confusing.
     * Do you want these to be an official part of the API? Please make an issue.
     */
    fun <T> serialize(serializer: SerializationStrategy<T>, obj: T): Tag {
        return writeNbt(obj, serializer)
    }

    fun <T> deserialize(deserializer: DeserializationStrategy<T>, tag: Tag): T {
        return readNbt(tag, deserializer)
    }

    internal companion object {
        const val Null = 1.toByte()
    }

}

internal const val ClassDiscriminator = "type"

internal fun compoundTagInvalidKeyKind(keyDescriptor: SerialDescriptor) = JsonException(
    "Value of type ${keyDescriptor.name} can't be used in a compound tag as map key. " +
            "It should have either primitive or enum kind, but its kind is ${keyDescriptor.kind}."
)

// This is an extension in case we want to have an option to not allow lists
internal inline fun <T, R1 : T, R2 : T> NbtFormat.selectMapMode(
    mapDescriptor: SerialDescriptor,
    ifMap: () -> R1,
    ifList: () -> R2
): T {
    val keyDescriptor = mapDescriptor.getElementDescriptor(0)
    val keyKind = keyDescriptor.kind
    return if (keyKind is PrimitiveKind  || keyKind == UnionKind.ENUM_KIND) {
        ifMap()
    } else {
        ifList()
    }
}


