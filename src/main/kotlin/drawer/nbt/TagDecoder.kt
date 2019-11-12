//package drawer.nbt
//
//import drawer.NamedValueTagDecoder
//import it.unimi.dsi.fastutil.ints.IntArrayList
//import it.unimi.dsi.fastutil.ints.IntStack
//import kotlinx.serialization.CompositeDecoder
//import kotlinx.serialization.KSerializer
//import kotlinx.serialization.SerialDescriptor
//import kotlinx.serialization.StructureKind
//import kotlinx.serialization.internal.EnumDescriptor
//import kotlinx.serialization.modules.SerialModule
//import net.minecraft.nbt.CompoundTag
//import net.minecraft.nbt.Tag
//
//internal class TagDecoder(private val map: CompoundTag, override val context: SerialModule) : NamedValueTagDecoder() {
//
//    var posStack: IntStack = IntArrayList()
//
//    override fun beginStructure(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
//        posStack.push(0)
//        return this
//    }
//
//    override fun endStructure(desc: SerialDescriptor) {
//        posStack.popInt()
//    }
//
//
//    override fun decodeCollectionSize(desc: SerialDescriptor): Int {
//        return decodeTaggedInt(nested("size"))
//    }
//
//    override fun decodeTaggedBoolean(tag: String) = map.getBoolean(tag)
//    override fun decodeTaggedByte(tag: String) = map.getByte(tag)
//    override fun decodeTaggedChar(tag: String) = map.getString(tag).toCharArray()[0]
//    override fun decodeTaggedDouble(tag: String) = map.getDouble(tag)
//    override fun decodeTaggedEnum(tag: String, enumDescription: EnumDescriptor) =
//        map.getInt(tag)
//
//    override fun decodeTaggedFloat(tag: String) = map.getFloat(tag)
//    override fun decodeTaggedInt(tag: String) = map.getInt(tag)
//
//    override fun decodeTaggedLong(tag: String) = map.getLong(tag)
//    override fun decodeTaggedNull(tag: String): Nothing? = null
//    override fun decodeTaggedNotNullMark(tag: String) = map.getByte(tag.nullMarked()) != NbtFormat.Null
//    override fun decodeTaggedShort(tag: String) = map.getShort(tag)
//    override fun decodeTaggedString(tag: String): String = map.getString(tag)
//    override fun decodeTaggedUnit(tag: String) = Unit
//    override fun decodeTaggedTag(key: String): Tag = map.getTag(key)!!
//
//
//    private fun incrementPos() = posStack.push(posStack.popInt() + 1)
//
//    private fun buildDescriptionParts(): Set<String> {
//        val parts = mutableSetOf<String>()
//        for (key in map.keys) {
//            for (i in key.length - 1 downTo 0) {
//                if (key[i] == '.') parts.add(key.substring(0, i))
//            }
//        }
//        return parts
//    }
//
//    //TODO: hack, replace with a nested compound tag format
//    private val descriptionParts: Set<String> = buildDescriptionParts()
//
//    override fun decodeElementIndex(desc: SerialDescriptor): Int {
//        var position = posStack.topInt()
//        val size = if (desc.kind == StructureKind.LIST) decodeCollectionSize(desc) else desc.elementsCount
//        while (position < size) {
//            incrementPos()
//
//            val name = desc.getTag(position)
//            if (map.containsKey(name) || name in descriptionParts
//                || map.containsKey(name.nullMarked()) || name.nullMarked() in descriptionParts) {
//                return position
//            }
//            position = posStack.topInt()
//
//        }
//        return CompositeDecoder.READ_DONE
//
//
//    }
//
//}