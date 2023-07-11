package kotlinx.serialization.minecraft.impl.nbt

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.minecraft.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import net.minecraft.nbt.*


internal val TagModule = SerializersModule {
    polymorphic(NbtElement::class) {
        subclass(NbtByte::class, NBtByteSerializer)
        subclass(NbtShort::class, NbtShortSerializer)
        subclass(NbtInt::class, NbtIntSerializer)
        subclass(NbtLong::class, NbtLongSerializer)
        subclass(NbtFloat::class, NbtFloatSerializer)
        subclass(NbtDouble::class, NbtDoubleSerializer)
        subclass(NbtString::class, NbtStringSerializer)
        subclass(NbtEnd::class, NbtEndSerializer)
        subclass(NbtByteArray::class, NbtByteArraySerializer)
        subclass(NbtIntArray::class, NbtIntArraySerializer)
        subclass(NbtLongArray::class, NbtLongArraySerializer)
        subclass(NbtList::class, NbtListSerializer)
        subclass(NbtCompound::class, NbtCompoundSerializer)
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

