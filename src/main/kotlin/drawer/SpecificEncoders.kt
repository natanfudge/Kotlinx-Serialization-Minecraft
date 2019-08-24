package drawer

import drawer.util.UnsealedNamedValueDecoder
import drawer.util.UnsealedNamedValueEncoder
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.recipe.Ingredient

internal interface ICanEncodeTag : ICanEncodeCompoundTag {
    fun encodeTag(tag: Tag)
    override fun encodeCompoundTag(tag: CompoundTag) = encodeTag(tag)
}

internal interface ICanDecodeTag : ICanDecodeCompoundTag {
    fun decodeTag(): Tag
    override fun decodeCompoundTag(): CompoundTag = decodeTag() as CompoundTag
}

internal interface ICanEncodeCompoundTag {
    fun encodeCompoundTag(tag: CompoundTag)
}

internal interface ICanDecodeCompoundTag {
    fun decodeCompoundTag(): CompoundTag
}

internal abstract class NamedValueTagEncoder : UnsealedNamedValueEncoder(), ICanEncodeTag {
    final override fun encodeTag(tag: Tag) = encodeTaggedTag(popTag(), tag)
    abstract fun encodeTaggedTag(key: String, tag: Tag)
}

internal abstract class NamedValueTagDecoder : UnsealedNamedValueDecoder(), ICanDecodeTag {
    final override fun decodeTag(): Tag = decodeTaggedTag(popTag())
    abstract fun decodeTaggedTag(key: String): Tag
}


internal interface ICanEncodeIngredient{
    fun encodeIngredient(ingredient: Ingredient)
}

interface ICanDecodeIngredient{
    fun decodeIngredient() : Ingredient
}