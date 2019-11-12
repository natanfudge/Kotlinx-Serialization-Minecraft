package drawer.nbt

import drawer.ForCompoundTag
import drawer.ForEndTag
import drawer.ForListTag
import drawer.util.DrawerLogger
import kotlinx.serialization.*
import kotlinx.serialization.internal.EnumDescriptor
import kotlinx.serialization.json.JsonOutput
import kotlinx.serialization.json.internal.checkKind
import kotlinx.serialization.modules.SerialModule
import mixin.AccessibleListTag
import net.minecraft.nbt.*
import java.lang.reflect.Field

internal fun <T> NbtFormat.writeNbt(value: T, serializer: SerializationStrategy<T>): Tag {
    lateinit var result: Tag
    val encoder = TagEncoder(this) { result = it }
    encoder.encode(serializer, value)
    return result
}

private inline fun <T> AbstractTagEncoder.encodePolymorphically(serializer: SerializationStrategy<T>, value: T, ifPolymorphic: () -> Unit) {
    if (serializer !is PolymorphicSerializer<*>) {
        serializer.serialize(this, value)
        return
    }

    @Suppress("UNCHECKED_CAST")
    val actualSerializer = serializer.findPolymorphicSerializer(this, value as Any) as KSerializer<Any>
//    val kind = actualSerializer.descriptor.kind
//    checkKind(kind)

    ifPolymorphic()
    actualSerializer.serialize(this, value)
}

private sealed class AbstractTagEncoder(
    val format: NbtFormat,
    val nodeConsumer: (Tag) -> Unit
) : NamedValueEncoder() {

    final override val context: SerialModule
        get() = format.context


    private var writePolymorphic = false

    override fun composeName(parentName: String, childName: String): String = childName
    abstract fun putElement(key: String, element: Tag)
    abstract fun getCurrent(): Tag

    //    override fun encodeTaggedNull(tag: String) {
//        compoundTag.putByte(tag.nullMarked(), NbtFormat.Null)
//    }

    //    //TODO: check which type of list it is somehow, and then put the right tag for that list.
    override fun encodeTaggedNull(tag: String) = putElement(tag, ByteTag(NbtFormat.Null))
//    override fun encodeTaggedNotNullMark(tag: String) {
//        super.encodeTaggedNotNullMark(tag)
//    }
//

    override fun encodeTaggedInt(tag: String, value: Int) = putElement(tag, IntTag(value))
    override fun encodeTaggedByte(tag: String, value: Byte) = putElement(tag, ByteTag(value))
    override fun encodeTaggedShort(tag: String, value: Short) = putElement(tag, ShortTag(value))
    override fun encodeTaggedLong(tag: String, value: Long) = putElement(tag, LongTag(value))
    override fun encodeTaggedFloat(tag: String, value: Float) = putElement(tag, FloatTag(value))
    override fun encodeTaggedDouble(tag: String, value: Double) = putElement(tag, DoubleTag(value))

    override fun encodeTaggedBoolean(tag: String, value: Boolean) =
        putElement(tag, ByteTag(if (value) 1.toByte() else 0.toByte()))

    override fun encodeTaggedChar(tag: String, value: Char) = putElement(tag, StringTag(value.toString()))
    override fun encodeTaggedString(tag: String, value: String) = putElement(tag, StringTag(value))
    override fun encodeTaggedEnum(
        tag: String,
        enumDescription: EnumDescriptor,
        ordinal: Int
    ) = putElement(tag, StringTag(enumDescription.getElementName(ordinal)))



//    override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
////        super.encodeSerializableValue(serializer, value)
//
//        // Writing non-structured data (i.e. primitives) on top-level (e.g. without any tag) requires special output
//        if (currentTagOrNull != null || serializer.descriptor.kind !is PrimitiveKind && serializer.descriptor.kind !== UnionKind.ENUM_KIND) {
//            encodePolymorphically(serializer, value) { writePolymorphic = true }
//        } else TagPrimitiveEncoder(format, nodeConsumer).apply {
//            encodeSerializableValue(serializer, value)
//            endEncode(serializer.descriptor)
//        }
//    }


    override fun encodeTaggedValue(tag: String, value: Any) {
        putElement(tag, StringTag(value.toString()))
    }

    override fun beginStructure(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeEncoder {
        val consumer = if (currentTagOrNull == null) nodeConsumer
        else { node -> putElement(currentTag, node) }

        val encoder = when (desc.kind) {
            StructureKind.LIST, UnionKind.POLYMORPHIC -> TagListEncoder(format, consumer)
            StructureKind.MAP -> format.selectMapMode(desc,
                ifMap = { TagMapEncoder(format, consumer) },
                ifList = { TagListEncoder(format, consumer) })
            else -> TagEncoder(format, consumer)
        }

        if (writePolymorphic) {
            writePolymorphic = false
            encoder.putElement(ClassDiscriminator, StringTag(desc.name))
        }

        return encoder
    }

    override fun endEncode(desc: SerialDescriptor) {
        nodeConsumer(getCurrent())
    }
}


internal const val PRIMITIVE_TAG = "primitive" // also used in drawer.nbt.JsonPrimitiveInput

private class TagPrimitiveEncoder(json: NbtFormat, nodeConsumer: (Tag) -> Unit) :
    AbstractTagEncoder(json, nodeConsumer) {
    private var content: Tag? = null

    init {
        pushTag(PRIMITIVE_TAG)
    }

    override fun putElement(key: String, element: Tag) {
        require(key === PRIMITIVE_TAG) { "This output can only consume primitives with '$PRIMITIVE_TAG' tag" }
        require(content == null) { "Primitive element was already recorded. Does call to .encodeXxx happen more than once?" }
        content = element
    }

    override fun getCurrent(): Tag =
        requireNotNull(content) { "Primitive element has not been recorded. Is call to .encodeXxx is missing in serializer?" }
}

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
        val idx = key.toIntOrNull()
        if (idx != null && idx % 2 == 0) { // writing key
            this.key = when (element) {
                is CompoundTag, is AbstractListTag<*>, is EndTag -> throw CompoundTagInvalidKeyKind(
                    when (element) {
                        is CompoundTag -> ForCompoundTag.descriptor
                        is AbstractListTag<*> -> ForListTag.descriptor
                        is EndTag -> ForEndTag.descriptor
                        else -> error("impossible")
                    }
                )
                else -> element.asString()
            }
        } else {
            content[this.key] = element
        }
    }

    override fun getCurrent(): Tag = content

    override fun shouldWriteElement(desc: SerialDescriptor, tag: String, index: Int): Boolean = true
}

private operator fun CompoundTag.set(key: String, value: Tag) = put(key, value)

private const val WrappedListName = "value"
private val wrappedListField: Field by lazy {
    DrawerLogger.warn("Using reflection to access ListTag wrapped list. This should NOT happen normally. If you see this, it's a bug.")
    // using named field here is fine
    ListTag::class.java.getDeclaredField(WrappedListName).apply { isAccessible = true }
}

private fun ListTag.addAnyTag(index : Int, tag: Tag) {
    val innerList = if (this is AccessibleListTag) this.wrappedList
    else wrappedListField.get(this) as MutableList<Tag>
    innerList.add(index,tag)
}

private class TagListEncoder(json: NbtFormat, nodeConsumer: (Tag) -> Unit) :
    AbstractTagEncoder(json, nodeConsumer) {
    private val list: ListTag = ListTag()

    override fun elementName(desc: SerialDescriptor, index: Int): String = index.toString()

//    override fun encodeTaggedNull(tag: String) {
//        list.addAnyTag(ByteTag(NbtFormat.Null))
//    }

    override fun shouldWriteElement(desc: SerialDescriptor, tag: String, index: Int): Boolean = true

    override fun putElement(key: String, element: Tag) {
        val idx = key.toInt()
        list.addAnyTag(idx, element)
    }

    override fun getCurrent(): Tag = list
}

