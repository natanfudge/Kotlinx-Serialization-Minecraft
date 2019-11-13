/*
 * Copyright 2017-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("LeakingThis")

package drawer.nbt


import drawer.NamedValueTagDecoder
import kotlinx.serialization.*
import kotlinx.serialization.internal.EnumDescriptor
import kotlinx.serialization.modules.SerialModule
import net.minecraft.nbt.*

internal fun <T> NbtFormat.readNbt(element: Tag, deserializer: DeserializationStrategy<T>): T {
    val input = when (element) {
        is CompoundTag -> TagDecoder(this, element)
        is AbstractListTag<*> -> TagListDecoder(this, element)
        else -> TagPrimitiveDecoder(this, element)
    }
    return input.decode(deserializer)
}

internal inline fun <reified T : Tag> cast(obj: Tag): T {
    check(obj is T) { "Expected ${T::class} but found ${obj::class}" }
    return obj
}


private sealed class AbstractTagDecoder(val format: NbtFormat, open val map: Tag) : NamedValueTagDecoder() {

    override val context: SerialModule
        get() = format.context


    private fun currentObject() = currentTagOrNull?.let { currentElement(it) } ?: map


    override fun composeName(parentName: String, childName: String): String = childName

    override fun beginStructure(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
        val currentObject = currentObject()
        return when (desc.kind) {
            StructureKind.LIST, UnionKind.POLYMORPHIC -> TagListDecoder(format, cast(currentObject))
            StructureKind.MAP -> format.selectMapMode(
                desc,
                { TagMapDecoder(format, cast(currentObject)) },
                { TagListDecoder(format, cast(currentObject)) }
            )
            else -> TagDecoder(format, cast(currentObject))
        }
    }

    protected open fun getValue(tag: String): Tag = currentElement(tag)

    protected abstract fun currentElement(tag: String): Tag

    override fun decodeTaggedChar(tag: String): Char {
        val o = getValue(tag)
        val str = o.asString()
        return if (str.length == 1) str[0] else throw SerializationException("$o can't be represented as Char")
    }

    override fun decodeTaggedEnum(tag: String, enumDescription: EnumDescriptor): Int =
        enumDescription.getElementIndexOrThrow(getValue(tag).asString())

    override fun decodeTaggedNull(tag: String): Nothing? = null

    override fun decodeTaggedNotNullMark(tag: String): Boolean = (currentElement(tag) as? ByteTag)?.byte != NbtFormat.Null

    override fun decodeTaggedUnit(tag: String) {
        return
    }

    override fun decodeTaggedBoolean(tag: String): Boolean = getValue(tag).cast<ByteTag>().byte == 1.toByte()
    override fun decodeTaggedByte(tag: String): Byte = getValue(tag).cast<ByteTag>().byte
    override fun decodeTaggedShort(tag: String) = getValue(tag).cast<ShortTag>().short
    override fun decodeTaggedInt(tag: String) = getValue(tag).cast<IntTag>().int
    override fun decodeTaggedLong(tag: String) = getValue(tag).cast<LongTag>().long
    override fun decodeTaggedFloat(tag: String) = getValue(tag).cast<FloatTag>().float
    override fun decodeTaggedDouble(tag: String) = getValue(tag).cast<DoubleTag>().double
    override fun decodeTaggedString(tag: String): String = getValue(tag).cast<StringTag>().asString()

    override fun decodeTaggedTag(key: String): Tag = getValue(key)

    private inline fun<reified T> Any.cast() = this as T
}

private class TagPrimitiveDecoder(json: NbtFormat, override val map: Tag) : AbstractTagDecoder(json, map) {

    init {
        pushTag(PRIMITIVE_TAG)
    }

    override fun currentElement(tag: String): Tag {
        require(tag === PRIMITIVE_TAG) { "This input can only handle primitives with '$PRIMITIVE_TAG' tag" }
        return map
    }
}

private open class TagDecoder(json: NbtFormat, override val map: CompoundTag) : AbstractTagDecoder(json, map) {
    private var position = 0

    override fun decodeElementIndex(desc: SerialDescriptor): Int {
        while (position < desc.elementsCount) {
            val name = desc.getTag(position++)
            if (map.containsKey(name)) {
                return position - 1
            }
        }
        return CompositeDecoder.READ_DONE
    }

    override fun currentElement(tag: String): Tag = map.getTag(tag)!!

}

private class TagMapDecoder(json: NbtFormat, override val map: CompoundTag) : TagDecoder(json, map) {
    private val keys = map.keys.toList()
    private val size: Int = keys.size * 2
    private var position = -1

    override fun elementName(desc: SerialDescriptor, index: Int): String {
        val i = index / 2
        return keys[i]
    }

    override fun decodeElementIndex(desc: SerialDescriptor): Int {
        while (position < size - 1) {
            position++
            return position
        }
        return CompositeDecoder.READ_DONE
    }

    override fun currentElement(tag: String): Tag {
        return if (position % 2 == 0) StringTag(tag) else map.getTag(tag)!!
    }

    override fun endStructure(desc: SerialDescriptor) {
        // do nothing, maps do not have strict keys, so strict mode check is omitted
    }
}

private class TagListDecoder(json: NbtFormat, override val map: AbstractListTag<*>) : AbstractTagDecoder(json, map) {
    private val size = map.size
    private var currentIndex = -1

    override fun elementName(desc: SerialDescriptor, index: Int): String = (index).toString()

    override fun currentElement(tag: String): Tag {
        return map[tag.toInt()]
    }

    override fun decodeElementIndex(desc: SerialDescriptor): Int {
        while (currentIndex < size - 1) {
            currentIndex++
            return currentIndex
        }
        return CompositeDecoder.READ_DONE
    }
}
