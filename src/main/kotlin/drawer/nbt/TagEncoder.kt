package drawer.nbt

import drawer.ForCompoundTag
import drawer.ForEndTag
import drawer.ForListTag
import drawer.NamedValueTagEncoder
import drawer.mixin.AccessibleListTag
import drawer.util.DrawerLogger
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.SerializersModule
import net.minecraft.nbt.*
import java.lang.reflect.Field

internal fun <T> NbtFormat.writeNbt(value: T, serializer: SerializationStrategy<T>): Tag {
    lateinit var result: Tag
    val encoder = TagEncoder(this) { result = it }
    encoder.encodeSerializableValue(serializer, value)
    return result
}
@OptIn(ExperimentalSerializationApi::class)
private sealed class AbstractTagEncoder(
    val format: NbtFormat,
    val nodeConsumer: (Tag) -> Unit
) : NamedValueTagEncoder() {

    final override val serializersModule: SerializersModule
        get() = format.serializersModule


    private var writePolymorphic = false

    override fun composeName(parentName: String, childName: String): String = childName
    abstract fun putElement(key: String, element: Tag)
    abstract fun getCurrent(): Tag

    override fun encodeTaggedNull(tag: String) = putElement(tag, ByteTag.of(NbtFormat.Null))

    override fun encodeTaggedInt(tag: String, value: Int) = putElement(tag, IntTag.of(value))
    override fun encodeTaggedByte(tag: String, value: Byte) = putElement(tag, ByteTag.of(value))
    override fun encodeTaggedShort(tag: String, value: Short) = putElement(tag, ShortTag.of(value))
    override fun encodeTaggedLong(tag: String, value: Long) = putElement(tag, LongTag.of(value))
    override fun encodeTaggedFloat(tag: String, value: Float) = putElement(tag, FloatTag.of(value))
    override fun encodeTaggedDouble(tag: String, value: Double) = putElement(tag, DoubleTag.of(value))

    override fun encodeTaggedBoolean(tag: String, value: Boolean) =
        putElement(tag, ByteTag.of(value))

    override fun encodeTaggedChar(tag: String, value: Char) = putElement(tag, StringTag.of(value.toString()))
    override fun encodeTaggedString(tag: String, value: String) = putElement(tag, StringTag.of(value))

    override fun encodeTaggedEnum(
        tag: String,
        enumDescriptor: SerialDescriptor,
        ordinal: Int
    ) = putElement(tag, StringTag.of(enumDescriptor.getElementName(ordinal)))


    override fun encodeTaggedTag(key: String, tag: Tag) = putElement(key, tag)

    override fun encodeTaggedValue(tag: String, value: Any) {
        putElement(tag, StringTag.of(value.toString()))
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
                else TagListEncoder(format, consumer)
            }
            is PolymorphicKind -> TagMapEncoder(format, consumer)
            StructureKind.MAP -> format.selectMapMode(descriptor,
                ifMap = { TagMapEncoder(format, consumer) },
                ifList = { TagListEncoder(format, consumer) }
            )
            else -> TagEncoder(format, consumer)
        }

        if (writePolymorphic) {
            writePolymorphic = false
            encoder.putElement(ClassDiscriminator, StringTag.of(descriptor.serialName))
        }

        return encoder
    }

    override fun endEncode(descriptor: SerialDescriptor) {
        nodeConsumer(getCurrent())
    }
}


internal const val PRIMITIVE_TAG = "primitive" // also used in drawer.nbt.JsonPrimitiveInput

private open class TagEncoder(format: NbtFormat, nodeConsumer: (Tag) -> Unit) :
    AbstractTagEncoder(format, nodeConsumer) {

    protected val content: CompoundTag = CompoundTag()

    override fun putElement(key: String, element: Tag) {
        content.put(key, element)
    }

    override fun getCurrent(): Tag = content
}

private class TagMapEncoder(format: NbtFormat, nodeConsumer: (Tag) -> Unit) : TagEncoder(format, nodeConsumer) {
    private lateinit var key: String

    override fun putElement(key: String, element: Tag) {
        val idx = key.toInt()
        // writing key
        when {
            idx % 2 == 0 -> this.key = when (element) {
                is CompoundTag, is AbstractListTag<*>, is EndTag -> throw compoundTagInvalidKeyKind(
                    when (element) {
                        is CompoundTag -> ForCompoundTag.descriptor
                        is AbstractListTag<*> -> ForListTag.descriptor
                        is EndTag -> ForEndTag.descriptor
                        else -> error("impossible")
                    }
                )
                else -> element.asString()
            }
            else -> content[this.key] = element
        }
    }

    override fun getCurrent(): Tag = content

}

private class NullableListEncoder(format: NbtFormat, nodeConsumer: (Tag) -> Unit) : TagEncoder(format, nodeConsumer) {
    override fun putElement(key: String, element: Tag) {
        content[key] = element
    }

    override fun getCurrent(): Tag = content

}


private operator fun CompoundTag.set(key: String, value: Tag) = put(key, value)

private const val WrappedListName = "value"
private val wrappedListField: Field by lazy {
    DrawerLogger.warn("Fabric-Drawer is using reflection to access ListTag wrapped list. This should NOT happen normally. If you see this, it's a bug.")
    // using named field here is fine
    ListTag::class.java.getDeclaredField(WrappedListName).apply { isAccessible = true }
}

private fun ListTag.addAnyTag(index: Int, tag: Tag) {
    val innerList = if (this is AccessibleListTag) this.wrappedList
    else wrappedListField.get(this) as MutableList<Tag>
    innerList.add(index, tag)
}

private class TagListEncoder(json: NbtFormat, nodeConsumer: (Tag) -> Unit) :
    AbstractTagEncoder(json, nodeConsumer) {
    private val list: ListTag = ListTag()

    override fun elementName(descriptor: SerialDescriptor, index: Int): String = index.toString()


    override fun putElement(key: String, element: Tag) {
        val idx = key.toInt()
        list.addAnyTag(idx, element)
    }

    override fun getCurrent(): Tag = list
}

