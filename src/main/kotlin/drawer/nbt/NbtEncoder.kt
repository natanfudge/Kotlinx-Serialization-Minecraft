package drawer.nbt

import drawer.ForNbtCompound
import drawer.ForNbtList
import drawer.NamedValueTagEncoder
import drawer.mixin.AccessibleNbtList
import drawer.util.DrawerLogger
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.SerializersModule
import net.minecraft.nbt.*
import java.lang.reflect.Field

internal fun <T> NbtFormat.writeNbt(value: T, serializer: SerializationStrategy<T>): NbtElement {
    lateinit var result: NbtElement
    val encoder = NbtEncoder(this) { result = it }
    encoder.encodeSerializableValue(serializer, value)
    return result
}
@OptIn(ExperimentalSerializationApi::class)
private sealed class AbstractNbtEncoder(
    val format: NbtFormat,
    val nodeConsumer: (NbtElement) -> Unit
) : NamedValueTagEncoder() {

    final override val serializersModule: SerializersModule
        get() = format.serializersModule


    private var writePolymorphic = false

    override fun composeName(parentName: String, childName: String): String = childName
    abstract fun putElement(key: String, element: NbtElement)
    abstract fun getCurrent(): NbtElement

    override fun encodeTaggedNull(tag: String) = putElement(tag, NbtByte.of(NbtFormatNull))

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


    override fun encodeTaggedTag(key: String, tag: NbtElement) = putElement(key, tag)

    override fun encodeTaggedValue(tag: String, value: Any) {
        putElement(tag, NbtString.of(value.toString()))
    }

    override fun elementName(descriptor: SerialDescriptor, index: Int): String {
        return if (descriptor.kind is PolymorphicKind) index.toString() else super.elementName(descriptor, index)
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        val consumer = if (currentTagOrNull == null) nodeConsumer
        else { node -> putElement(currentTag, node) }

        val encoder = when (descriptor.kind) {
            StructureKind.LIST -> {
                if (descriptor.kind == StructureKind.LIST && descriptor.getElementDescriptor(0).isNullable) NullableListEncoder(format, consumer)
                else NbtListEncoder(format, consumer)
            }
            is PolymorphicKind -> NbtMapEncoder(format, consumer)
            StructureKind.MAP -> format.selectMapMode(descriptor,
                ifMap = { NbtMapEncoder(format, consumer) },
                ifList = { NbtListEncoder(format, consumer) }
            )
            else -> NbtEncoder(format, consumer)
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


internal const val PRIMITIVE_TAG = "primitive" // also used in drawer.nbt.JsonPrimitiveInput

private open class NbtEncoder(format: NbtFormat, nodeConsumer: (NbtElement) -> Unit) :
    AbstractNbtEncoder(format, nodeConsumer) {

    protected val content: NbtCompound = NbtCompound()

    override fun putElement(key: String, element: NbtElement) {
        content.put(key, element)
    }

    override fun getCurrent(): NbtElement = content
}

private class NbtMapEncoder(format: NbtFormat, nodeConsumer: (NbtElement) -> Unit) : NbtEncoder(format, nodeConsumer) {
    private lateinit var key: String

    override fun putElement(key: String, element: NbtElement) {
        val idx = key.toInt()
        // writing key
        when {
            idx % 2 == 0 -> this.key = when (element) {
                is NbtCompound, is AbstractNbtList<*>/*, is NbtNull */-> throw compoundTagInvalidKeyKind(
                    when (element) {
                        is NbtCompound -> ForNbtCompound.descriptor
                        is AbstractNbtList<*> -> ForNbtList.descriptor
                        // is NbtNull -> ForNbtNull.descriptor
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

private class NullableListEncoder(format: NbtFormat, nodeConsumer: (NbtElement) -> Unit) : NbtEncoder(format, nodeConsumer) {
    override fun putElement(key: String, element: NbtElement) {
        content[key] = element
    }

    override fun getCurrent(): NbtElement = content

}


private operator fun NbtCompound.set(key: String, value: NbtElement) = put(key, value)

private const val WrappedListName = "value"
private val wrappedListField: Field by lazy {
    DrawerLogger.warn("Fabric-Drawer is using reflection to access NbtList wrapped list. This should NOT happen normally. If you see this, it's a bug.")
    // using named field here is fine
    NbtList::class.java.getDeclaredField(WrappedListName).apply { isAccessible = true }
}

private fun NbtList.addAnyTag(index: Int, tag: NbtElement) {
    val innerList = if (this is AccessibleNbtList) this.wrappedList
    else wrappedListField.get(this) as MutableList<NbtElement>
    innerList.add(index, tag)
}

private class NbtListEncoder(json: NbtFormat, nodeConsumer: (NbtElement) -> Unit) :
    AbstractNbtEncoder(json, nodeConsumer) {
    private val list: NbtList = NbtList()

    override fun elementName(descriptor: SerialDescriptor, index: Int): String = index.toString()


    override fun putElement(key: String, element: NbtElement) {
        val idx = key.toInt()
        list.addAnyTag(idx, element)
    }

    override fun getCurrent(): NbtElement = list
}

