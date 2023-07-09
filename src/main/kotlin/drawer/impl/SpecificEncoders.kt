package drawer.impl

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.internal.NamedValueDecoder
import kotlinx.serialization.internal.NamedValueEncoder
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.recipe.Ingredient

@OptIn(InternalSerializationApi::class)
internal abstract class NamedValueTagEncoder : NamedValueEncoder(), ICanEncodeTag {
    final override fun encodeTag(tag: NbtElement) = encodeTaggedTag(popTag(), tag)
    abstract fun encodeTaggedTag(key: String, tag: NbtElement)
}

@OptIn(InternalSerializationApi::class)
internal abstract class NamedValueNbtDecoder : NamedValueDecoder(), ICanDecodeTag {
    final override fun decodeTag(): NbtElement = decodeTaggedTag(popTag())
    abstract fun decodeTaggedTag(key: String): NbtElement
}

internal interface ICanEncodeTag : ICanEncodeNbtCompound {
    fun encodeTag(tag: NbtElement)
    override fun encodeNbtCompound(tag: NbtCompound) = encodeTag(tag)
}

internal interface ICanDecodeTag : ICanDecodeNbtCompound {
    fun decodeTag(): NbtElement
    override fun decodeNbtCompound(): NbtCompound = decodeTag() as NbtCompound
}

internal interface ICanEncodeNbtCompound {
    fun encodeNbtCompound(tag: NbtCompound)
}

internal interface ICanDecodeNbtCompound {
    fun decodeNbtCompound(): NbtCompound
}

internal interface ICanEncodeIngredient {
    fun encodeIngredient(ingredient: Ingredient)
}

internal interface ICanDecodeIngredient {
     fun decodeIngredient() : Ingredient
}