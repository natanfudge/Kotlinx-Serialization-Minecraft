package drawer.util

import kotlinx.serialization.*

/*
 * Copyright 2017-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

import kotlinx.serialization.CompositeDecoder.Companion.READ_ALL
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule


internal abstract class UnsealedTaggedDecoder<Tag : Any?> : Decoder, CompositeDecoder {
    override val context: SerialModule
        get() = EmptyModule

    override val updateMode: UpdateMode = UpdateMode.UPDATE

    protected abstract fun SerialDescriptor.getTag(index: Int): Tag


    // ---- API ----
    open fun decodeTaggedValue(tag: Tag): Any
            = throw SerializationException("${this::class} can't retrieve untyped values")

    open fun decodeTaggedNotNullMark(tag: Tag): Boolean = true
    open fun decodeTaggedNull(tag: Tag): Nothing? = null

    open fun decodeTaggedUnit(tag: Tag): Unit = decodeTaggedValue(tag) as Unit
    open fun decodeTaggedBoolean(tag: Tag): Boolean = decodeTaggedValue(tag) as Boolean
    open fun decodeTaggedByte(tag: Tag): Byte = decodeTaggedValue(tag) as Byte
    open fun decodeTaggedShort(tag: Tag): Short = decodeTaggedValue(tag) as Short
    open fun decodeTaggedInt(tag: Tag): Int = decodeTaggedValue(tag) as Int
    open fun decodeTaggedLong(tag: Tag): Long = decodeTaggedValue(tag) as Long
    open fun decodeTaggedFloat(tag: Tag): Float = decodeTaggedValue(tag) as Float
    open fun decodeTaggedDouble(tag: Tag): Double = decodeTaggedValue(tag) as Double
    open fun decodeTaggedChar(tag: Tag): Char = decodeTaggedValue(tag) as Char
    open fun decodeTaggedString(tag: Tag): String = decodeTaggedValue(tag) as String
    open fun decodeTaggedEnum(tag: Tag, enumDescription: SerialDescriptor): Int = decodeTaggedValue(tag) as Int


    // ---- Implementation of low-level API ----

    /****************************************************************************************************************/
    /*** Fix to tagStack throwing an "empty" exception when trying to decode a top-level null. Everything else is the same. **/
    final override fun decodeNotNullMark(): Boolean = if(tagStack.isEmpty()) true else decodeTaggedNotNullMark(currentTag)
    /****************************************************************************************************************/

    final override fun decodeNull(): Nothing? = null

    final override fun decodeUnit() = decodeTaggedUnit(popTag())
    final override fun decodeBoolean(): Boolean = decodeTaggedBoolean(popTag())
    final override fun decodeByte(): Byte = decodeTaggedByte(popTag())
    final override fun decodeShort(): Short = decodeTaggedShort(popTag())
    final override fun decodeInt(): Int = decodeTaggedInt(popTag())
    final override fun decodeLong(): Long = decodeTaggedLong(popTag())
    final override fun decodeFloat(): Float = decodeTaggedFloat(popTag())
    final override fun decodeDouble(): Double = decodeTaggedDouble(popTag())
    final override fun decodeChar(): Char = decodeTaggedChar(popTag())
    final override fun decodeString(): String = decodeTaggedString(popTag())

    final override fun decodeEnum(enumDescription: SerialDescriptor): Int = decodeTaggedEnum(popTag(), enumDescription)

    override fun beginStructure(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
        return this
    }

    /**
     * Assumes that all elements go in order by default.
     */
    override fun decodeElementIndex(desc: SerialDescriptor): Int = READ_ALL

    final override fun decodeUnitElement(desc: SerialDescriptor, index: Int) = decodeTaggedUnit(desc.getTag(index))
    final override fun decodeBooleanElement(desc: SerialDescriptor, index: Int): Boolean = decodeTaggedBoolean(desc.getTag(index))
    final override fun decodeByteElement(desc: SerialDescriptor, index: Int): Byte = decodeTaggedByte(desc.getTag(index))
    final override fun decodeShortElement(desc: SerialDescriptor, index: Int): Short = decodeTaggedShort(desc.getTag(index))
    final override fun decodeIntElement(desc: SerialDescriptor, index: Int): Int = decodeTaggedInt(desc.getTag(index))
    final override fun decodeLongElement(desc: SerialDescriptor, index: Int): Long = decodeTaggedLong(desc.getTag(index))
    final override fun decodeFloatElement(desc: SerialDescriptor, index: Int): Float = decodeTaggedFloat(desc.getTag(index))
    final override fun decodeDoubleElement(desc: SerialDescriptor, index: Int): Double = decodeTaggedDouble(desc.getTag(index))
    final override fun decodeCharElement(desc: SerialDescriptor, index: Int): Char = decodeTaggedChar(desc.getTag(index))
    final override fun decodeStringElement(desc: SerialDescriptor, index: Int): String = decodeTaggedString(desc.getTag(index))

    final override fun <T : Any?> decodeSerializableElement(desc: SerialDescriptor, index: Int, deserializer: DeserializationStrategy<T>): T =
        tagBlock(desc.getTag(index)) { decodeSerializableValue(deserializer) }

    final override fun <T : Any> decodeNullableSerializableElement(desc: SerialDescriptor, index: Int, deserializer: DeserializationStrategy<T?>): T? =
        tagBlock(desc.getTag(index)) { decodeNullableSerializableValue(deserializer) }

    override fun <T> updateSerializableElement(desc: SerialDescriptor, index: Int, deserializer: DeserializationStrategy<T>, old: T): T =
        tagBlock(desc.getTag(index)) { updateSerializableValue(deserializer, old) }

    override fun <T : Any> updateNullableSerializableElement(desc: SerialDescriptor, index: Int, deserializer: DeserializationStrategy<T?>, old: T?): T? =
        tagBlock(desc.getTag(index)) { updateNullableSerializableValue(deserializer, old) }

    private fun <E> tagBlock(tag: Tag, block: () -> E): E {
        pushTag(tag)
        val r = block()
        if (!flag) {
            popTag()
        }
        flag = false
        return r
    }

    private val tagStack = arrayListOf<Tag>()
    protected val currentTag: Tag
        get() = tagStack.last()
    protected val currentTagOrNull
        get() = tagStack.lastOrNull()

    protected fun pushTag(name: Tag) {
        tagStack.add(name)
    }

    private var flag = false

    protected fun popTag(): Tag {
        val r = tagStack.removeAt(tagStack.lastIndex)
        flag = true
        return r
    }
}



internal abstract class UnsealedNamedValueDecoder(val rootName: String = "") : UnsealedTaggedDecoder<String>() {
    final override fun SerialDescriptor.getTag(index: Int): String = nested(elementName(this, index))

    protected fun nested(nestedName: String) = composeName(currentTagOrNull ?: rootName, nestedName)
    open fun elementName(desc: SerialDescriptor, index: Int) = desc.getElementName(index)
    open fun composeName(parentName: String, childName: String) = if (parentName.isEmpty()) childName else parentName + "." + childName
}
