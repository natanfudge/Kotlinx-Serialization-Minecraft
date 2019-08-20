//package drawer
//
//
///*
// * Copyright 2017-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
// */
//
//
//import kotlinx.serialization.*
//import kotlinx.serialization.internal.EnumDescriptor
//import kotlinx.serialization.json.*
//import kotlinx.serialization.modules.SerialModule
//import net.minecraft.nbt.*
//import kotlin.collections.set
//
////internal fun <T> Json.writeJson(value: T, serializer: SerializationStrategy<T>): Tag {
////    lateinit var result: Tag
////    val encoder = TagEncoder(this) { result = it }
////    encoder.encode(serializer, value)
////    return result
////}
//
//private sealed class AbstractTagEncoder(
//    val format: NbtFormat,
//    val nodeConsumer: (Tag) -> Unit
//) : NamedValueTagEncoder() {
//
//    final override val context: SerialModule
//        get() = format.context
//
//    private var writePolymorphic = false
//
//
//    override fun composeName(parentName: String, childName: String): String = childName
//    abstract fun putElement(key: String, element: Tag)
//    abstract fun getCurrent(): Tag
//
////            override fun encodeTaggedNotNullMark(tag: String) = compoundTag.putByte(tag + "mark", 1)
////    override fun encodeTaggedNull(tag: String) {
////        compoundTag.putByte(tag + "mark", 0)
////    }
//    override fun encodeTaggedNull(tag: String) = putElement(tag + "mark", ByteTag(0))
//override fun encodeTaggedNotNullMark(tag: String) = putElement(tag + "mark", ByteTag(1))
//
//    override fun encodeTaggedInt(tag: String, value: Int) = putElement(tag, IntTag(value))
//    override fun encodeTaggedByte(tag: String, value: Byte) = putElement(tag, ByteTag(value))
//    override fun encodeTaggedShort(tag: String, value: Short) = putElement(tag, ShortTag(value))
//    override fun encodeTaggedLong(tag: String, value: Long) = putElement(tag, LongTag(value))
//
//    override fun encodeTaggedFloat(tag: String, value: Float) = putElement(tag, FloatTag(value))
//
//
////    override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
////        // Writing non-structured data (i.e. primitives) on top-level (e.g. without any tag) requires special output
////        if (currentTagOrNull != null || serializer.descriptor.kind !is PrimitiveKind && serializer.descriptor.kind !== UnionKind.ENUM_KIND) {
////            encodePolymorphically(serializer, value) { writePolymorphic = true }
////        } else JsonPrimitiveOutput(format, nodeConsumer).apply {
////            encodeSerializableValue(serializer, value)
////            endEncode(serializer.descriptor)
////        }
////    }
//
//    override fun encodeTaggedDouble(tag: String, value: Double) = putElement(tag, DoubleTag(value))
//
//    override fun encodeTaggedBoolean(tag: String, value: Boolean) = putElement(tag, ByteTag(value))
//    override fun encodeTaggedChar(tag: String, value: Char) = putElement(tag, JsonLiteral(value.toString()))
//    override fun encodeTaggedString(tag: String, value: String) = putElement(tag, JsonLiteral(value))
//    override fun encodeTaggedEnum(
//        tag: String,
//        enumDescription: EnumDescriptor,
//        ordinal: Int
//    ) = putElement(tag, JsonLiteral(enumDescription.getElementName(ordinal)))
//
//    override fun encodeTaggedValue(tag: String, value: Any) {
//        putElement(tag, JsonLiteral(value.toString()))
//    }
//
//    override fun beginStructure(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeEncoder {
//        val consumer =
//            if (currentTagOrNull == null) nodeConsumer
//            else { node -> putElement(currentTag, node) }
//
//        val encoder = when (desc.kind) {
//            StructureKind.LIST, UnionKind.POLYMORPHIC -> JsonTreeListOutput(format, consumer)
//            StructureKind.MAP -> JsonTreeMapOutput(format, consumer)
//            else -> TagEncoder(format, consumer)
//        }
//
//        if (writePolymorphic) {
//            writePolymorphic = false
//            encoder.putElement(configuration.classDiscriminator, JsonPrimitive(desc.name))
//        }
//
//        return encoder
//    }
//
//    override fun endEncode(desc: SerialDescriptor) {
//        nodeConsumer(getCurrent())
//    }
//}
//
//internal const val PRIMITIVE_TAG = "primitive" // also used in JsonPrimitiveInput
//
//private class JsonPrimitiveOutput(json: Json, nodeConsumer: (Tag) -> Unit) :
//    AbstractTagEncoder(json, nodeConsumer) {
//    private var content: Tag? = null
//
//    init {
//        pushTag(PRIMITIVE_TAG)
//    }
//
//    override fun putElement(key: String, element: Tag) {
//        require(key === PRIMITIVE_TAG) { "This output can only consume primitives with '$PRIMITIVE_TAG' tag" }
//        require(content == null) { "Primitive element was already recorded. Does call to .encodeXxx happen more than once?" }
//        content = element
//    }
//
//    override fun getCurrent(): Tag =
//        requireNotNull(content) { "Primitive element has not been recorded. Is call to .encodeXxx is missing in serializer?" }
//}
//
//private open class TagEncoder(json: Json, nodeConsumer: (Tag) -> Unit) :
//    AbstractTagEncoder(json, nodeConsumer) {
//
//    protected val content: MutableMap<String, Tag> = linkedMapOf()
//
//    override fun putElement(key: String, element: Tag) {
//        content[key] = element
//    }
//
//    override fun getCurrent(): Tag = JsonObject(content)
//}
//
//private class JsonTreeMapOutput(json: Json, nodeConsumer: (Tag) -> Unit) : TagEncoder(json, nodeConsumer) {
//    private lateinit var tag: String
//
//    override fun putElement(key: String, element: Tag) {
//        val idx = key.toInt()
//        if (idx % 2 == 0) { // writing key
//            check(element is JsonLiteral) { "Expected JsonLiteral, but has $element" }
//            tag = element.content
//        } else {
//            content[tag] = element
//        }
//    }
//
//    override fun getCurrent(): Tag {
//        return JsonObject(content)
//    }
//
//    override fun shouldWriteElement(desc: SerialDescriptor, tag: String, index: Int): Boolean = true
//}
//
//private class JsonTreeListOutput(json: Json, nodeConsumer: (Tag) -> Unit) :
//    AbstractTagEncoder(json, nodeConsumer) {
//    private val array: ArrayList<Tag> = arrayListOf()
//
//    override fun shouldWriteElement(desc: SerialDescriptor, tag: String, index: Int): Boolean = true
//
//    override fun putElement(key: String, element: Tag) {
//        val idx = key.toInt()
//        array.add(idx, element)
//    }
//
//    override fun getCurrent(): Tag = JsonArray(array)
//}
//
