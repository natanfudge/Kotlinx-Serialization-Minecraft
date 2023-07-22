package kotlinx.serialization.minecraft.impl.nbt

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.getContextualDescriptor
import kotlinx.serialization.minecraft.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import net.minecraft.item.ItemStack
import net.minecraft.nbt.*
import net.minecraft.recipe.Ingredient
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.*


internal val MinecraftModule = SerializersModule {
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
    contextual(BlockPos::class, BlockPosSerializer)
    contextual(Identifier::class, IdentifierSerializer)
    contextual(SoundEvent::class, SoundEventSerializer)
    contextual(ItemStack::class, ItemStackSerializer)
    contextual(Ingredient::class, IngredientSerializer)
    contextual(DefaultedList::class) { args -> DefaultedListSerializer(args[0]) }
    contextual(UUID::class, UUIDSerializer)
    contextual(Vec3d::class, Vec3dSerializer)
}


internal const val NbtFormatNull = 1.toByte()

internal val NbtNull = NbtByte.of(NbtFormatNull)

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

@OptIn(ExperimentalSerializationApi::class)
internal fun SerialDescriptor.carrierDescriptor(module: SerializersModule): SerialDescriptor = when {
    kind == SerialKind.CONTEXTUAL -> module.getContextualDescriptor(this)?.carrierDescriptor(module) ?: this
    isInline -> getElementDescriptor(0).carrierDescriptor(module)
    else -> this
}


//@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
//@Suppress("UNCHECKED_CAST")
//internal inline fun <T> NbtEncoder.encodePolymorphically(
//    serializer: SerializationStrategy<T>,
//    value: T,
//    ifPolymorphic: (String) -> Unit
//) {
//    if (serializer !is AbstractPolymorphicSerializer<*> || json.configuration.useArrayPolymorphism) {
//        serializer.serialize(this, value)
//        return
//    }
//    val casted = serializer as AbstractPolymorphicSerializer<Any>
//    val baseClassDiscriminator = serializer.descriptor.classDiscriminator(json)
//    val actualSerializer = casted.findPolymorphicSerializer(this, value as Any)
//    validateIfSealed(casted, actualSerializer, baseClassDiscriminator)
//    checkKind(actualSerializer.descriptor.kind)
//    ifPolymorphic(baseClassDiscriminator)
//    actualSerializer.serialize(this, value)
//}
//
//@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
//private fun validateIfSealed(
//    serializer: SerializationStrategy<*>,
//    actualSerializer: SerializationStrategy<Any>,
//    classDiscriminator: String
//) {
//    if (serializer !is SealedClassSerializer<*>) return
//    @Suppress("DEPRECATION_ERROR")
//    if (classDiscriminator in actualSerializer.descriptor.jsonCachedSerialNames()) {
//        val baseName = serializer.descriptor.serialName
//        val actualName = actualSerializer.descriptor.serialName
//        error(
//            "Sealed class '$actualName' cannot be serialized as base class '$baseName' because" +
//                    " it has property name that conflicts with JSON class discriminator '$classDiscriminator'. " +
//                    "You can either change class discriminator in JsonConfiguration, " +
//                    "rename property with @SerialName annotation or fall back to array polymorphism"
//        )
//    }
//}
//
//@OptIn(ExperimentalSerializationApi::class)
//internal fun SerialDescriptor.classDiscriminator(json: Json): String {
//    // Plain loop is faster than allocation of Sequence or ArrayList
//    // We can rely on the fact that only one JsonClassDiscriminator is present —
//    // compiler plugin checked that.
//    for (annotation in annotations) {
//        if (annotation is JsonClassDiscriminator) return annotation.discriminator
//    }
//    return json.configuration.classDiscriminator
//}
//
//
//@OptIn(ExperimentalSerializationApi::class)
//internal fun checkKind(kind: SerialKind) {
//    if (kind is SerialKind.ENUM) error("Enums cannot be serialized polymorphically with 'type' parameter. You can use 'JsonBuilder.useArrayPolymorphism' instead")
//    if (kind is PrimitiveKind) error("Primitives cannot be serialized polymorphically with 'type' parameter. You can use 'JsonBuilder.useArrayPolymorphism' instead")
//    if (kind is PolymorphicKind) error("Actual serializer for polymorphic cannot be polymorphic itself")
//}
