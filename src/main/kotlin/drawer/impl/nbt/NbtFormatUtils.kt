package drawer.impl.nbt

import drawer.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import net.minecraft.nbt.*


internal val TagModule = SerializersModule {
    polymorphic(NbtElement::class) {
        subclass(NbtByte::class, ForNbtByte)
        subclass(NbtShort::class, ForNbtShort)
        subclass(NbtInt::class, ForNbtInt)
        subclass(NbtLong::class, ForNbtLong)
        subclass(NbtFloat::class, ForNbtFloat)
        subclass(NbtDouble::class, ForNbtDouble)
        subclass(NbtString::class, ForNbtString)
        subclass(NbtNull::class, ForNbtNull)
        subclass(NbtByteArray::class, ForNbtByteArray)
        subclass(NbtIntArray::class, ForNbtIntArray)
        subclass(NbtLongArray::class, ForNbtLongArray)
        subclass(NbtList::class, ForNbtList)
        subclass(NbtCompound::class, ForNbtCompound)
    }
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
internal inline fun <T, R1 : T, R2 : T> Nbt.selectMapMode(
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

