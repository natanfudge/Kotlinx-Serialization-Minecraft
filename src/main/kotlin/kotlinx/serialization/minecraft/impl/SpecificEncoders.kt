package kotlinx.serialization.minecraft.impl

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.internal.NamedValueDecoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.minecraft.Nbt
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.recipe.Ingredient

internal interface NbtEncoder : ICanEncodeTag, /*ICanEncodeItemStack, */Encoder, CompositeEncoder  {
    fun encodeTaggedTag(key: String, tag: NbtElement)

    companion object {
        val json = Json {
            serializersModule = Nbt.serializersModule
            useArrayPolymorphism = true
        }
    }
}

//@OptIn(InternalSerializationApi::class)
//internal abstract class NamedValueTagEncoder : NamedValueEncoder(), ICanEncodeTag, ICanEncodeItemStack {
//    final override fun encodeTag(tag: NbtElement) = encodeTaggedTag(popTag(), tag)
//    final override fun encodeItemStack(stack: ItemStack) = encodeTaggedTag(popTag(), NbtCompound().also { stack.writeNbt(it) })
//    abstract fun encodeTaggedTag(key: String, tag: NbtElement)
//}

@OptIn(InternalSerializationApi::class)
internal abstract class NamedValueNbtDecoder : NamedValueDecoder(), ICanDecodeTag/*, ICanDecodeItemStack*/ {
    final override fun decodeTag(): NbtElement = decodeTaggedTag(popTag())
//    final override fun decodeItemStack(): ItemStack = ItemStack.fromNbt(decodeTaggedTag(popTag()) as NbtCompound)
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
    fun decodeIngredient(): Ingredient
}

internal interface ICanEncodeItemStack {
    fun encodeItemStack(stack: ItemStack)
}

internal interface ICanDecodeItemStack {
    fun decodeItemStack(): ItemStack
}

