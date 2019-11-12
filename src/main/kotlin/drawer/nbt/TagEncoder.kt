//package drawer.nbt
//
//import drawer.NamedValueTagEncoder
//import kotlinx.serialization.CompositeEncoder
//import kotlinx.serialization.KSerializer
//import kotlinx.serialization.SerialDescriptor
//import kotlinx.serialization.SerializationException
//import kotlinx.serialization.internal.EnumDescriptor
//import kotlinx.serialization.modules.SerialModule
//
//internal class TagEncoder(val compoundTag: CompoundTag = CompoundTag(),override val context: SerialModule) : NamedValueTagEncoder() {
//
//    override fun encodeTaggedBoolean(tag: String, value: Boolean) = compoundTag.putBoolean(tag, value)
//    override fun beginCollection(desc: SerialDescriptor, collectionSize: Int, vararg typeParams: KSerializer<*>):
//            CompositeEncoder {
//        encodeTaggedInt(nested("size"), collectionSize)
//
//        return this
//    }
//
//
//    override fun encodeTaggedByte(tag: String, value: Byte) = compoundTag.putByte(tag, value)
//    override fun encodeTaggedChar(tag: String, value: Char) = compoundTag.putString(tag, value.toString())
//    override fun encodeTaggedDouble(tag: String, value: Double) = compoundTag.putDouble(tag, value)
//    override fun encodeTaggedEnum(tag: String, enumDescription: EnumDescriptor, ordinal: Int) =
//        compoundTag.putInt(tag, ordinal)
//
//    override fun encodeTaggedFloat(tag: String, value: Float) = compoundTag.putFloat(tag, value)
//    override fun encodeTaggedInt(tag: String, value: Int) = compoundTag.putInt(tag, value)
//    override fun encodeTaggedLong(tag: String, value: Long) = compoundTag.putLong(tag, value)
//    //        override fun encodeTaggedNotNullMark(tag: String) = compoundTag.putByte(tag.nullMarked(), 1)
//    override fun encodeTaggedNull(tag: String) {
//        compoundTag.putByte(tag.nullMarked(), NbtFormat.Null)
//    }
//
//    override fun encodeTaggedShort(tag: String, value: Short) = compoundTag.putShort(tag, value)
//    override fun encodeTaggedString(tag: String, value: String) = compoundTag.putString(tag, value)
//    override fun encodeTaggedUnit(tag: String) = compoundTag.putByte(tag, 2)
//    override fun encodeTaggedValue(tag: String, value: Any) {
//        if (value is Tag) {
//            compoundTag.put(tag, value)
//        } else {
//            throw SerializationException("Non-serializable ${value::class} is not supported by ${this::class} encoder")
//        }
//
//    }
//
//    override fun encodeTaggedTag(key: String, tag: Tag) {
//        compoundTag.put(key, tag)
//    }
//
//
//}