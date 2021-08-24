/*
 * Copyright 2017-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("LeakingThis")

package drawer.nbt


import drawer.NamedValueNbtDecoder
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.SerializersModule
import net.minecraft.nbt.*

internal fun <T> NbtFormat.readNbt(element: NbtElement, deserializer: DeserializationStrategy<T>): T {
    val input = when (element) {
        is NbtCompound -> NbtDecoder(this, element)
        is AbstractNbtList<*> -> TagListDecoder(this, element)
        else -> TagPrimitiveDecoder(this, element)
    }
    return input.decodeSerializableValue(deserializer)
}

internal inline fun <reified T : NbtElement> cast(obj: NbtElement): T {
    check(obj is T) { "Expected ${T::class} but found ${obj::class}" }
    return obj
}

private inline fun <reified T> Any.cast() = this as T


@OptIn(ExperimentalSerializationApi::class)
private sealed class AbstractNbtDecoder(val format: NbtFormat, open val map: NbtElement) : NamedValueNbtDecoder() {

    override val serializersModule: SerializersModule
        get() = format.serializersModule


    private fun currentObject() = currentTagOrNull?.let { currentElement(it) } ?: map


    override fun composeName(parentName: String, childName: String): String = childName

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        val currentObject = currentObject()
        return when (descriptor.kind) {
            StructureKind.LIST -> {
                if (descriptor.kind == StructureKind.LIST && descriptor.getElementDescriptor(0).isNullable) NullableListDecoder(
                    format,
                    cast(currentObject)
                )
                else TagListDecoder(format, cast(currentObject))
            }
            is PolymorphicKind -> NbtMapDecoder(format, cast(currentObject))
            StructureKind.MAP -> format.selectMapMode(
                descriptor,
                { NbtMapDecoder(format, cast(currentObject)) },
                { TagListDecoder(format, cast(currentObject)) }
            )
            else -> NbtDecoder(format, cast(currentObject))
        }
    }

    protected open fun getValue(tag: String): NbtElement {
        return currentElementWithTest(tag)
    }

    private fun currentElementWithTest(tag: String): NbtElement {
        val element = currentElement(tag)
        println("Element: $element")
        return element
    }

    protected abstract fun currentElement(tag: String): NbtElement

    override fun decodeTaggedChar(tag: String): Char {
        val o = getValue(tag)
        val str = o.asString()
        return if (str.length == 1) str[0] else throw SerializationException("$o can't be represented as Char")
    }

    override fun decodeTaggedEnum(tag: String, enumDescriptor: SerialDescriptor): Int =
        enumDescriptor.getElementIndex(getValue(tag).asString())

    override fun decodeTaggedNull(tag: String): Nothing? {
        return null
    }

    override fun decodeTaggedNotNullMark(tag: String): Boolean {
        // If we don't do this assigment it fails. I have no clue why. This is a quantum bug, it cannot be debugged.
        val byteValue = (currentElement(tag) as? NbtByte)?.byteValue()
        return byteValue != NbtFormatNull
    }

//    override fun decodeTaggedUnit(tag: String) {
//        return
//    }

    override fun decodeTaggedBoolean(tag: String): Boolean = decodeTaggedByte(tag) == 1.toByte()
    override fun decodeTaggedByte(tag: String): Byte = getNumberValue(tag, { byteValue() }, { toByte() })
    override fun decodeTaggedShort(tag: String) = getNumberValue(tag, { shortValue() }, { toShort() })
    override fun decodeTaggedInt(tag: String): Int = getNumberValue(tag, { intValue() }, { toInt() })

    override fun decodeTaggedLong(tag: String) = getNumberValue(tag, { longValue() }, { toLong() })
    override fun decodeTaggedFloat(tag: String) = getNumberValue(tag, { floatValue() }, { toFloat() })
    override fun decodeTaggedDouble(tag: String) = getNumberValue(tag, { doubleValue() }, { toDouble() })
    override fun decodeTaggedString(tag: String): String = getValue(tag).cast<NbtString>().asString()

    override fun decodeTaggedTag(key: String): NbtElement = getValue(key)

    private inline fun <T> getNumberValue(
        tag: String,
        getter: AbstractNbtNumber.() -> T,
        stringGetter: String.() -> T
    ): T {
        val value = getValue(tag)
        if (value is AbstractNbtNumber) return value.getter()
        else return value.cast<NbtString>().asString().stringGetter()
    }
}


private class TagPrimitiveDecoder(json: NbtFormat, override val map: NbtElement) : AbstractNbtDecoder(json, map) {

    init {
        pushTag(PRIMITIVE_TAG)
    }
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int  = 0

    override fun currentElement(tag: String): NbtElement {
        require(tag === PRIMITIVE_TAG) { "This input can only handle primitives with '$PRIMITIVE_TAG' tag" }
        return map
    }
}

private open class NbtDecoder(json: NbtFormat, override val map: NbtCompound) : AbstractNbtDecoder(json, map) {
    private var position = 0

    @OptIn(ExperimentalSerializationApi::class)
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        while (position < descriptor.elementsCount) {
            val name = descriptor.getTag(position++)
            if (map.contains(name)) {
                return position - 1
            }
        }
        return CompositeDecoder.DECODE_DONE
    }

    override fun currentElement(tag: String): NbtElement = map.get(tag)!!

}

private class NbtMapDecoder(json: NbtFormat, override val map: NbtCompound) : NbtDecoder(json, map) {
    private val keys = map.keys.toList()
    private val size: Int = keys.size * 2
    private var position = -1

    override fun elementName(desc: SerialDescriptor, index: Int): String {
        val i = index / 2
        return keys[i]
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        while (position < size - 1) {
            position++
            return position
        }
        return CompositeDecoder.DECODE_DONE
    }

    override fun currentElement(tag: String): NbtElement {
        return if (position % 2 == 0) NbtString.of(tag) else map.get(tag)!!
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        // do nothing, maps do not have strict keys, so strict mode check is omitted
    }
}

private class NullableListDecoder(json: NbtFormat, override val map: NbtCompound) : NbtDecoder(json, map) {
    private val size: Int = map.size
    private var position = -1

    override fun elementName(desc: SerialDescriptor, index: Int): String = index.toString()


    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        while (position < size - 1) {
            position++
            return position
        }
        return CompositeDecoder.DECODE_DONE
    }

    override fun currentElement(tag: String): NbtElement {
        return map[tag]!!
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        // do nothing, maps do not have strict keys, so strict mode check is omitted
    }
}


private class TagListDecoder(json: NbtFormat, override val map: AbstractNbtList<*>) : AbstractNbtDecoder(json, map) {
    private val size = map.size
    private var currentIndex = -1

    override fun elementName(desc: SerialDescriptor, index: Int): String = (index).toString()

    override fun currentElement(tag: String): NbtElement {
        return map[tag.toInt()]
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        while (currentIndex < size - 1) {
            currentIndex++
            return currentIndex
        }
        return CompositeDecoder.DECODE_DONE
    }
}
