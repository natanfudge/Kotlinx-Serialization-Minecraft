package drawer.nbt

import drawer.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import net.minecraft.nbt.*


//internal val TagModule = SerializersModule {
//    polymorphic(Tag::class) {
//        subclass(NbtByte::class, ForNbtByte)
//        subclass(NbtShort::class, ForNbtShort)
//        subclass(NbtInt::class, ForNbtInt)
//        subclass(NbtLong::class, ForNbtLong)
//        subclass(NbtFloat::class, ForNbtFloat)
//        subclass(NbtDouble::class, ForNbtDouble)
//        subclass(NbtString::class, ForNbtString)
//        subclass(NbtNull::class, ForNbtNull)
//        subclass(NbtByteArray::class, ForNbtByteArray)
//        subclass(NbtIntArray::class, ForNbtIntArray)
//        subclass(NbtLongArray::class, ForNbtLongArray)
//        subclass(NbtList::class, ForNbtList)
//        subclass(NbtCompound::class, ForNbtCompound)
//    }
//}
internal val TagModule = SerializersModule {
    polymorphic(NbtElement::class) {
        subclass(NbtByte::class, ForNbtByte)
        subclass(NbtShort::class, ForNbtShort)
        subclass(NbtInt::class, ForNbtInt)
        subclass(NbtLong::class, ForNbtLong)
        subclass(NbtFloat::class, ForNbtFloat)
        subclass(NbtDouble::class, ForNbtDouble)
        subclass(NbtString::class, ForNbtString)
        subclass(NbtEnd::class, ForNbtEnd)
        subclass(NbtByteArray::class, ForNbtByteArray)
        subclass(NbtIntArray::class, ForNbtIntArray)
        subclass(NbtLongArray::class, ForNbtLongArray)
        subclass(NbtList::class, ForNbtList)
        subclass(NbtCompound::class, ForNbtCompound)
    }
}


/**
 * Keeping this class public for now in case you want to serializer an object directly to tag and vise versa.
 */
class NbtFormat(context: SerializersModule = EmptySerializersModule()) : SerialFormat {

    /**
     * Converts [obj] into a [NbtCompound] that represents [obj].
     * Later [deserialize] can be called to retrieve an identical instance of [obj] from the [NbtCompound].
     *
     * These functions are not documented because I think they would be confusing.
     * Do you want these to be an official part of the API? Please make an issue.
     */
    fun <T> serialize(serializer: SerializationStrategy<T>, obj: T): NbtElement {
        return writeNbt(obj, serializer)
    }

    fun <T> deserialize(deserializer: DeserializationStrategy<T>, tag: NbtElement): T {
        return readNbt(tag, deserializer)
    }

    override val serializersModule = context + TagModule

}

internal const val NbtFormatNull = 1.toByte()


internal const val ClassDiscriminator = "type"

@OptIn(ExperimentalSerializationApi::class)
internal fun compoundTagInvalidKeyKind(keyDescriptor: SerialDescriptor) = NbtEncodingException(
    "Value of type ${keyDescriptor.serialName} can't be used in a compound tag as map key. " +
            "It should have either primitive or enum kind, but its kind is ${keyDescriptor.kind}."
)

// This is an extension in case we want to have an option to not allow lists
@OptIn(ExperimentalSerializationApi::class)
internal inline fun <T, R1 : T, R2 : T> NbtFormat.selectMapMode(
    mapDescriptor: SerialDescriptor,
    ifMap: () -> R1,
    ifList: () -> R2
): T {
    val keyDescriptor = mapDescriptor.getElementDescriptor(0)
    val keyKind = keyDescriptor.kind
    return if (keyKind is PrimitiveKind || keyKind == SerialKind.ENUM) {
        ifMap()
    } else {
        ifList()
    }
}


