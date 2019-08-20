package drawer

import drawer.util.UnsealedNamedValueDecoder
import kotlinx.serialization.*
import kotlinx.serialization.internal.EnumDescriptor
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerialModule
import net.minecraft.nbt.*

@Suppress("USELESS_CAST") // Contracts does not work in K/N
internal inline fun <reified T : Tag> cast(obj: Tag): T {
    check(obj is T) { "Expected ${T::class} but found ${obj::class}" }
    return obj as T
}

internal sealed class AbstractTagDecoder( val format: NbtFormat, open val obj: Tag)
    : UnsealedNamedValueDecoder() {

    override val context: SerialModule
        get() = format.context


    private fun currentObject() = currentTagOrNull?.let { currentElement(it) } ?: obj


//    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
//        return decodeSerializableValuePolymorphic(deserializer)
//    }

    override fun composeName(parentName: String, childName: String): String = childName

    override fun beginStructure(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
        val currentObject = currentObject()
        return when (desc.kind) {
            StructureKind.LIST -> ListTagDecoder(format, cast(currentObject))
            StructureKind.MAP -> MapTagDecoder(format, cast(currentObject))
            else -> TagDecoder(format, cast(currentObject))
        }
    }

    protected open fun getValue(tag: String): Tag {
        val currentElement = currentElement(tag)
        return currentElement as? Tag ?: throw JsonElementTypeMismatchException("$currentElement at $tag", "JsonPrimitive")
    }

    protected abstract fun currentElement(tag: String): Tag

    override fun decodeTaggedChar(tag: String): Char  = decodeTaggedString(tag).toCharArray()[0]
//    {
//        val o = getValue(tag)
//        return if (o.content.length == 1) o.content[0] else throw SerializationException("$o can't be represented as Char")
//    }

    override fun decodeTaggedEnum(tag: String, enumDescription: EnumDescriptor): Int =
        enumDescription.getElementIndexOrThrow(getValue(tag).asString())

    override fun decodeTaggedNull(tag: String): Nothing? = null

    override fun decodeTaggedNotNullMark(tag: String): Boolean = (currentElement(tag + "mark") as ByteTag).byte != 0.toByte()

    override fun decodeTaggedUnit(tag: String) {
        return
    }

    override fun decodeTaggedBoolean(tag: String): Boolean = (getValue(tag) as ByteTag).byte !=0.toByte()
    override fun decodeTaggedByte(tag: String): Byte = (getValue(tag)as ByteTag ).byte
    override fun decodeTaggedShort(tag: String) = (getValue(tag)as ShortTag).short
    override fun decodeTaggedInt(tag: String) = (getValue(tag)as IntTag).int
    override fun decodeTaggedLong(tag: String) = (getValue(tag)as LongTag).long
    override fun decodeTaggedFloat(tag: String) = (getValue(tag) as FloatTag).float
    override fun decodeTaggedDouble(tag: String) = (getValue(tag) as DoubleTag).double
    override fun decodeTaggedString(tag: String) : String = getValue(tag).asString()
}



internal open class TagDecoder(format: NbtFormat, override val obj: CompoundTag) : AbstractTagDecoder(format, obj) {
    private var position = 0

    override fun decodeElementIndex(desc: SerialDescriptor): Int {
        while (position < desc.elementsCount) {
            val name = desc.getTag(position++)
            if (name in obj.keys) {
                return position - 1
            }
        }
        return CompositeDecoder.READ_DONE
    }

    override fun currentElement(tag: String): Tag = obj.getTag(tag)!!

    override fun endStructure(desc: SerialDescriptor) {
        //TODO: maybe remove
//
//        // Validate keys
//        val names = HashSet<String>(desc.elementsCount)
//        for (i in 0 until desc.elementsCount) {
//            names += desc.getElementName(i)
//        }
//
//        for (key in obj.keys) {
//            if (key !in names) throw JsonUnknownKeyException("Encountered an unknown key '$key'")
//        }
    }
}

private class ListTagDecoder(format: NbtFormat, override val obj: ListTag) : AbstractTagDecoder(format, obj) {
    private val size = obj.size
    private var currentIndex = -1

    override fun elementName(desc: SerialDescriptor, index: Int): String = (index).toString()

    override fun currentElement(tag: String): Tag {
        return obj[tag.toInt()]
    }

    override fun decodeElementIndex(desc: SerialDescriptor): Int {
        while (currentIndex < size - 1) {
            currentIndex++
            return currentIndex
        }
        return CompositeDecoder.READ_DONE
    }
}
internal const val PRIMITIVE_TAG = "primitive" // also used in JsonPrimitiveInput

private class PrimitiveTagDecoder(format: NbtFormat, override val obj: Tag) : AbstractTagDecoder(format, obj) {

    init {
        pushTag(PRIMITIVE_TAG)
    }

    override fun currentElement(tag: String): Tag {
        require(tag === PRIMITIVE_TAG) { "This input can only handle primitives with '$PRIMITIVE_TAG' tag" }
        return obj
    }
}

private class MapTagDecoder(format:NbtFormat, override val obj: CompoundTag) : AbstractTagDecoder(format, obj) {
    private val keys = obj.keys.toList()
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
        return if (position % 2 == 0) StringTag(tag) else obj.getTag(tag)!!
    }

    override fun endStructure(desc: SerialDescriptor) {
        // do nothing, maps do not have strict keys, so strict mode check is omitted
    }
}





//    internal abstract inner class AbstractTagDecoder(private val map: CompoundTag, open val obj: Tag) :
//        NamedValueTagDecoder() {
//        private fun currentObject(): Tag = currentTagOrNull?.let { currentElement(it) } ?: obj
//
//        protected open fun getValue(tag: String): JsonPrimitive {
//            val currentElement = currentElement(tag)
//            return currentElement as? JsonPrimitive ?: throw JsonElementTypeMismatchException(
//                "$currentElement at $tag",
//                "JsonPrimitive"
//            )
//        }
//
//        abstract fun currentElement(tag: String): Tag
//
//        private var position = 0
//        override val context: SerialModule = this@NbtFormat.context
//        override fun decodeCollectionSize(desc: SerialDescriptor): Int {
//            return decodeTaggedInt(nested("size"))
//        }
//
//        override fun decodeTaggedBoolean(tag: String) = map.getBoolean(tag)
//        override fun decodeTaggedByte(tag: String) = map.getByte(tag)
//        override fun decodeTaggedChar(tag: String) = map.getString(tag).toCharArray()[0]
//        override fun decodeTaggedDouble(tag: String) = map.getDouble(tag)
//        override fun decodeTaggedEnum(tag: String, enumDescription: EnumDescriptor) =
//            map.getInt(tag)
//
//        override fun decodeTaggedFloat(tag: String) = map.getFloat(tag)
//        override fun decodeTaggedInt(tag: String) = map.getInt(tag)
//
//        override fun decodeTaggedLong(tag: String) = map.getLong(tag)
//        override fun decodeTaggedNotNullMark(tag: String) = map.getByte(tag + "mark") != 0.toByte()
//        override fun decodeTaggedShort(tag: String) = map.getShort(tag)
//        override fun decodeTaggedString(tag: String): String = map.getString(tag)
//        override fun decodeTaggedUnit(tag: String) = Unit
//        override fun decodeTaggedTag(key: String): Tag = map.getTag(key)!!
//
//
//        override fun decodeElementIndex(desc: SerialDescriptor): Int {
//            while (position < desc.elementsCount) {
//                val name = desc.getTag(position++)
//                if (name in map.keys) {
//                    return position - 1
//                }
//            }
//            return CompositeDecoder.READ_DONE
//        }
//    }
//
//    internal inner class TagDecoder(map: CompoundTag) : AbstractTagDecoder(map) {
//
//    }

    //TODO: support default values