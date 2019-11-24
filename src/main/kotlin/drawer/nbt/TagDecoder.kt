/*
 * Copyright 2017-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("LeakingThis")

package drawer.nbt


import drawer.NamedValueTagDecoder
import kotlinx.serialization.*
import kotlinx.serialization.internal.ListLikeDescriptor
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

private inline fun <reified T> Any.cast() = this as T


private sealed class AbstractTagDecoder(val format: NbtFormat, open val map: Tag) : NamedValueTagDecoder() {

    override val context: SerialModule
        get() = format.context


    private fun currentObject() = currentTagOrNull?.let { currentElement(it) } ?: map


    override fun composeName(parentName: String, childName: String): String = childName

    override fun beginStructure(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
        val currentObject = currentObject()
        return when (desc.kind) {
            StructureKind.LIST -> {
                if (desc is ListLikeDescriptor && desc.elementDesc.isNullable) NullableListDecoder(
                    format,
                    cast(currentObject)
                )
                else TagListDecoder(format, cast(currentObject))
            }
            is PolymorphicKind -> TagMapDecoder(format, cast(currentObject))
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

    override fun decodeTaggedEnum(tag: String, enumDescription: SerialDescriptor): Int =
        enumDescription.getElementIndexOrThrow(getValue(tag).asString())

    override fun decodeTaggedNull(tag: String): Nothing? = null

    override fun decodeTaggedNotNullMark(tag: String): Boolean =
        (currentElement(tag) as? ByteTag)?.byte != NbtFormat.Null

    override fun decodeTaggedUnit(tag: String) {
        return
    }

    override fun decodeTaggedBoolean(tag: String): Boolean = decodeTaggedByte(tag) == 1.toByte()
    override fun decodeTaggedByte(tag: String): Byte = getNumberValue(tag, { byte }, { toByte() })
    override fun decodeTaggedShort(tag: String) = getNumberValue(tag, { short }, { toShort() })
    override fun decodeTaggedInt(tag: String): Int = getNumberValue(tag, { int }, { toInt() })

    override fun decodeTaggedLong(tag: String) = getNumberValue(tag, { long }, { toLong() })
    override fun decodeTaggedFloat(tag: String) = getNumberValue(tag, { float }, { toFloat() })
    override fun decodeTaggedDouble(tag: String) = getNumberValue(tag, { double }, { toDouble() })
    override fun decodeTaggedString(tag: String): String = getValue(tag).cast<StringTag>().asString()

    override fun decodeTaggedTag(key: String): Tag = getValue(key)

    private inline fun <T> getNumberValue(
        tag: String,
        getter: AbstractNumberTag.() -> T,
        stringGetter: String.() -> T
    ): T {
        val value = getValue(tag)
        if (value is AbstractNumberTag) return value.getter()
        else return value.cast<StringTag>().asString().stringGetter()
    }
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
            if (map.contains(name)) {
                return position - 1
            }
        }
        return CompositeDecoder.READ_DONE
    }

    override fun currentElement(tag: String): Tag = map.get(tag)!!

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
        return if (position % 2 == 0) StringTag.of(tag) else map.get(tag)!!
    }

    override fun endStructure(desc: SerialDescriptor) {
        // do nothing, maps do not have strict keys, so strict mode check is omitted
    }
}

private class NullableListDecoder(json: NbtFormat, override val map: CompoundTag) : TagDecoder(json, map) {
    private val size: Int = map.size
    private var position = -1

    override fun elementName(desc: SerialDescriptor, index: Int): String = index.toString()


    override fun decodeElementIndex(desc: SerialDescriptor): Int {
        while (position < size - 1) {
            position++
            return position
        }
        return CompositeDecoder.READ_DONE
    }

    override fun currentElement(tag: String): Tag {
        return map[tag]!!
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
