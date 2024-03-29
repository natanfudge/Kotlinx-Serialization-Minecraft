/*
 * Copyright 2017-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("LeakingThis")

package kotlinx.serialization.minecraft.impl.nbt


import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.minecraft.Nbt
import kotlinx.serialization.minecraft.impl.NamedValueNbtDecoder
import kotlinx.serialization.minecraft.impl.NbtEncoder
import kotlinx.serialization.modules.SerializersModule
import net.minecraft.nbt.*

internal fun <T> Nbt.readNbt(element: NbtElement, deserializer: DeserializationStrategy<T>): T {
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
private sealed class AbstractNbtDecoder(val format: Nbt, open val map: NbtElement) : NamedValueNbtDecoder() {

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

            is PolymorphicKind, StructureKind.MAP -> NbtMapDecoder(format, cast(currentObject))

//            StructureKind.MAP -> NbtMapDecoder(format, cast(currentObject))
            else -> {
                when (currentObject) {
                    is NbtCompound -> NbtDecoder(format, cast(currentObject))
                    // In map keys, objects may be encoded as strings.
                    is NbtString -> TODO()
                    else -> error("Objects should be encoded as NbtCompound or NbtString")
                }
            }
        }
    }

    protected open fun getValue(tag: String): NbtElement {
        return currentElement(tag)
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

    override fun decodeNotNullMark(): Boolean = currentObject() != NbtNull


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

    private fun <T> getNumberValue(
        tag: String,
        getter: AbstractNbtNumber.() -> T,
        stringGetter: String.() -> T
    ): T {
        val value = getValue(tag)
        if (value is AbstractNbtNumber) return value.getter()
        if (value is NbtCompound) {
            TODO()
        }
        // Map keys are encoded as NbtString
        else return value.cast<NbtString>().asString().stringGetter()
    }
}


private class TagPrimitiveDecoder(nbt: Nbt, override val map: NbtElement) : AbstractNbtDecoder(nbt, map) {

    init {
        pushTag(PRIMITIVE_TAG)
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int = 0

    override fun currentElement(tag: String): NbtElement {
        require(tag === PRIMITIVE_TAG) { "This input can only handle primitives with '$PRIMITIVE_TAG' tag" }
        return map
    }
}

private open class NbtDecoder(nbt: Nbt, override val map: NbtCompound) : AbstractNbtDecoder(nbt, map) {
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

private class NbtMapDecoder(nbt: Nbt, override val map: NbtCompound) : NbtDecoder(nbt, map) {
    private val keys = map.keys.toList()
    private val size: Int = keys.size * 2
    private var position = -1

    override fun elementName(descriptor: SerialDescriptor, index: Int): String {
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

    @OptIn(ExperimentalSerializationApi::class)
    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
        if (deserializer.descriptor.kind !is PrimitiveKind) {
            val current = currentElement(currentTag)
            if (current is NbtString) {
                // If we have a string but descriptor expects something complex, it means we serialized it as json.
                return NbtEncoder.json.decodeFromString(deserializer, current.asString())
            }
        }
//        if(current is NbtString)
        return super.decodeSerializableValue(deserializer)
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        // do nothing, maps do not have strict keys, so strict mode check is omitted
    }
}

private class NullableListDecoder(nbt: Nbt, override val map: NbtCompound) : NbtDecoder(nbt, map) {
    private val size: Int = map.size
    private var position = -1

    override fun elementName(descriptor: SerialDescriptor, index: Int): String = index.toString()


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


private class TagListDecoder(nbt: Nbt, override val map: AbstractNbtList<*>) : AbstractNbtDecoder(nbt, map) {
    private val size = map.size
    private var currentIndex = -1

    override fun elementName(descriptor: SerialDescriptor, index: Int): String = (index).toString()

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
