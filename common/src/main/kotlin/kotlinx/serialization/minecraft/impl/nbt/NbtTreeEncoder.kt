package kotlinx.serialization.minecraft.impl.nbt

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.internal.NamedValueEncoder
import kotlinx.serialization.minecraft.Nbt
import kotlinx.serialization.minecraft.NbtCompoundSerializer
import kotlinx.serialization.minecraft.NbtEndSerializer
import kotlinx.serialization.minecraft.NbtListSerializer
import kotlinx.serialization.minecraft.impl.NbtEncoder
import kotlinx.serialization.modules.SerializersModule
import net.minecraft.nbt.*

internal fun <T> Nbt.writeNbt(value: T, serializer: SerializationStrategy<T>): NbtElement {
    lateinit var result: NbtElement
    val encoder = NbtTreeEncoder(this) { result = it }
    encoder.encodeSerializableValue(serializer, value)
    return result
}

@OptIn(ExperimentalSerializationApi::class)
private val SerialDescriptor.requiresTopLevelTag: Boolean
    get() = kind is PrimitiveKind || kind === SerialKind.ENUM

@OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
private sealed class AbstractNbtEncoder(
    val format: Nbt,
    val nodeConsumer: (NbtElement) -> Unit
) : NamedValueEncoder(), NbtEncoder {

    final override val serializersModule: SerializersModule
        get() = format.serializersModule


    private var writePolymorphic = false

    override fun composeName(parentName: String, childName: String): String = childName
    abstract fun putElement(key: String, element: NbtElement)
    abstract fun getCurrent(): NbtElement

    // has no tag when encoding a nullable element at root level
    override fun encodeNotNullMark() {}

    // has no tag when encoding a nullable element at root level
    override fun encodeNull() {
        val tag = currentTagOrNull ?: return nodeConsumer(NbtNull)
        encodeTaggedNull(tag)
    }

    override fun encodeTaggedNull(tag: String) = putElement(tag, NbtNull)

    override fun encodeTaggedInt(tag: String, value: Int) = putElement(tag, NbtInt.of(value))
    override fun encodeTaggedByte(tag: String, value: Byte) = putElement(tag, NbtByte.of(value))
    override fun encodeTaggedShort(tag: String, value: Short) = putElement(tag, NbtShort.of(value))
    override fun encodeTaggedLong(tag: String, value: Long) = putElement(tag, NbtLong.of(value))
    override fun encodeTaggedFloat(tag: String, value: Float) = putElement(tag, NbtFloat.of(value))
    override fun encodeTaggedDouble(tag: String, value: Double) = putElement(tag, NbtDouble.of(value))

    override fun encodeTaggedBoolean(tag: String, value: Boolean) =
        putElement(tag, NbtByte.of(value))

    override fun encodeTaggedChar(tag: String, value: Char) = putElement(tag, NbtString.of(value.toString()))
    override fun encodeTaggedString(tag: String, value: String) = putElement(tag, NbtString.of(value))

    override fun encodeTaggedEnum(
        tag: String,
        enumDescriptor: SerialDescriptor,
        ordinal: Int
    ) = putElement(tag, NbtString.of(enumDescriptor.getElementName(ordinal)))

    override fun encodeTag(tag: NbtElement) = encodeTaggedTag(popTag(), tag)
//    override fun encodeItemStack(stack: ItemStack) = encodeTaggedTag(popTag(), NbtCompound().also { stack.writeNbt(it) })


    override fun encodeTaggedTag(key: String, tag: NbtElement) = putElement(key, tag)

    override fun encodeTaggedValue(tag: String, value: Any) {
        putElement(tag, NbtString.of(value.toString()))
    }

//    override fun encodeTaggedInline(tag: String, inlineDescriptor: SerialDescriptor): Encoder {
//        return super.encodeTaggedInline(tag, inlineDescriptor)
//    }
//
//    override fun encodeInline(descriptor: SerialDescriptor): Encoder {
//        return super.encodeInline(descriptor)
//    }

    override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
        // Writing non-structured data (i.e. primitives) on top-level (e.g. without any tag) requires special output
        if (currentTagOrNull != null || !serializer.descriptor.carrierDescriptor(serializersModule).requiresTopLevelTag) {
            serializer.serialize(this, value)
        } else NbtPrimitiveEncoder(format, nodeConsumer).apply {
            encodeSerializableValue(serializer, value)
        }
    }

    override fun elementName(descriptor: SerialDescriptor, index: Int): String {
        return if (descriptor.kind is PolymorphicKind) index.toString() else super.elementName(descriptor, index)
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        val consumer = if (currentTagOrNull == null) nodeConsumer
        else { node -> putElement(currentTag, node) }

        val encoder: AbstractNbtEncoder = when (descriptor.kind) {
            StructureKind.LIST -> {
                if (descriptor.kind == StructureKind.LIST && descriptor.getElementDescriptor(0).isNullable) NullableListEncoder(format, consumer)
                else NbtListEncoder(format, consumer)
            }

            is PolymorphicKind -> NbtMapEncoder(format, consumer)
            StructureKind.MAP -> format.selectMapMode(descriptor,
                ifMap = { NbtMapEncoder(format, consumer) },
                ifList = { NbtListEncoder(format, consumer) }
            )

            else -> NbtTreeEncoder(format, consumer)
        }

        if (writePolymorphic) {
            writePolymorphic = false
            encoder.putElement(ClassDiscriminator, NbtString.of(descriptor.serialName))
        }

        return encoder
    }

    override fun endEncode(descriptor: SerialDescriptor) {
        nodeConsumer(getCurrent())
    }
}


internal const val PRIMITIVE_TAG = "primitive"

private open class NbtTreeEncoder(format: Nbt, nodeConsumer: (NbtElement) -> Unit) :
    AbstractNbtEncoder(format, nodeConsumer) {

    protected val content: NbtCompound = NbtCompound()

    override fun putElement(key: String, element: NbtElement) {
        content.put(key, element)
    }

    override fun getCurrent(): NbtElement = content
}

private class NbtMapEncoder(format: Nbt, nodeConsumer: (NbtElement) -> Unit) : NbtTreeEncoder(format, nodeConsumer) {
    private lateinit var key: String

    override fun putElement(key: String, element: NbtElement) {
        val idx = key.toInt()
        // writing key
        when {
            idx % 2 == 0 -> this.key = when (element) {
                is NbtCompound, is AbstractNbtList<*>, is NbtEnd -> throw compoundTagInvalidKeyKind(
                    when (element) {
                        is NbtCompound -> NbtCompoundSerializer.descriptor
                        is AbstractNbtList<*> -> NbtListSerializer.descriptor
                        is NbtEnd -> NbtEndSerializer.descriptor
                        else -> error("impossible")
                    }
                )

                else -> element.asString()
            }

            else -> content[this.key] = element
        }
    }

    override fun getCurrent(): NbtElement = content

}

private class NullableListEncoder(format: Nbt, nodeConsumer: (NbtElement) -> Unit) : NbtTreeEncoder(format, nodeConsumer) {
    override fun putElement(key: String, element: NbtElement) {
        content[key] = element
    }

    override fun getCurrent(): NbtElement = content

}


private class NbtPrimitiveEncoder(
    nbt: Nbt,
    nodeConsumer: (NbtElement) -> Unit
) : AbstractNbtEncoder(nbt, nodeConsumer) {
    private var content: NbtElement? = null

    init {
        pushTag(PRIMITIVE_TAG)
    }

    override fun putElement(key: String, element: NbtElement) {
        require(key === PRIMITIVE_TAG) { "This output can only consume primitives with '${PRIMITIVE_TAG}' tag" }
        require(content == null) { "Primitive element was already recorded. Does call to .encodeXxx happen more than once?" }
        content = element
        nodeConsumer(element)
    }

    override fun getCurrent(): NbtElement =
        requireNotNull(content) { "Primitive element has not been recorded. Is call to .encodeXxx is missing in serializer?" }
}


private operator fun NbtCompound.set(key: String, value: NbtElement) = put(key, value)


private fun NbtList.addAnyTag(index: Int, tag: NbtElement) {
    value.add(index, tag)
}

private class NbtListEncoder(json: Nbt, nodeConsumer: (NbtElement) -> Unit) :
    AbstractNbtEncoder(json, nodeConsumer) {
    private val list: NbtList = NbtList()

    override fun elementName(descriptor: SerialDescriptor, index: Int): String = index.toString()


    override fun putElement(key: String, element: NbtElement) {
        val idx = key.toInt()
        list.addAnyTag(idx, element)
    }

    override fun getCurrent(): NbtElement = list
}

