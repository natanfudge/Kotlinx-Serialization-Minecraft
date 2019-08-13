package drawer

import drawer.util.UnsealedNamedValueDecoder
import drawer.util.UnsealedNamedValueEncoder
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag

interface ICanEncodeTag : ICanEncodeCompoundTag {
    fun encodeTag(tag: Tag)
    override fun encodeCompoundTag(tag: CompoundTag) = encodeTag(tag)
}

interface ICanDecodeTag : ICanDecodeCompoundTag {
    fun decodeTag(): Tag
    override fun decodeCompoundTag(): CompoundTag = decodeTag() as CompoundTag
}

interface ICanEncodeCompoundTag {
    fun encodeCompoundTag(tag: CompoundTag)
}

interface ICanDecodeCompoundTag {
    fun decodeCompoundTag(): CompoundTag
}

abstract class NamedValueTagEncoder : UnsealedNamedValueEncoder(), ICanEncodeTag {
    final override fun encodeTag(tag: Tag) = encodeTaggedTag(popTag(), tag)
    abstract fun encodeTaggedTag(key: String, tag: Tag)
}

abstract class NamedValueTagDecoder : UnsealedNamedValueDecoder(), ICanDecodeTag {
    final override fun decodeTag(): Tag = decodeTaggedTag(popTag())
    abstract fun decodeTaggedTag(key: String): Tag
}
