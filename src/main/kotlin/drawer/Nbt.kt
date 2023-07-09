package drawer

import drawer.impl.nbt.TagModule
import drawer.impl.nbt.readNbt
import drawer.impl.nbt.writeNbt
import kotlinx.serialization.*
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement


public open class Nbt(context: SerializersModule) : SerialFormat {

    public companion object Default : Nbt(EmptySerializersModule())

    /**
     * Converts [obj] into a [NbtCompound] that represents [obj].
     * Later [decodeFromNbt] can be called to retrieve an identical instance of [obj] from the [NbtCompound].
     */
    public fun <T> encodeToNbt(serializer: SerializationStrategy<T>, obj: T): NbtElement {
        return writeNbt(obj, serializer)
    }

    public fun <T> decodeFromNbt(deserializer: DeserializationStrategy<T>, tag: NbtElement): T {
        return readNbt(tag, deserializer)
    }

    override val serializersModule: SerializersModule = context + TagModule
}

public inline fun <reified T> Nbt.encodeToNbt(obj: T): NbtElement = encodeToNbt(serializersModule.serializer(), obj)
public inline fun <reified T> Nbt.decodeFromNbt(nbt: NbtElement): T = decodeFromNbt(serializersModule.serializer(), nbt)


/**
 * Puts [obj] into the [NbtCompound] instance of [inTag].
 * Later [getFrom] can be called to retrieve an identical instance of [obj] from the [NbtCompound].
 * For nullable values you SHOULD NOT use the .nullable serializer. It is not needed and does not work.
 *
 * @param key If you are serializing two objects of the same type, you MUST  specify a key.
 * The same key must be used in [getFrom].
 * @param context Used for polymorphic serialization, see [Here](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md).
 */
@OptIn(ExperimentalSerializationApi::class)
public fun <T> SerializationStrategy<T>.put(
    obj: T?,
    inTag: NbtCompound,
    key: String? = null,
    context: SerializersModule = EmptySerializersModule()
) {
    val usedKey = key ?: this.descriptor.serialName
    require(!inTag.contains(usedKey)) {
        """A '${this.descriptor.serialName}' appears twice in the NbtCompound.
            |If you are serializing two objects of the same type, you MUST specify a key, see kdoc.
        |Also make sure you didn't use the same key twice.
    """.trimMargin()
    }
    if (obj != null) inTag.put(usedKey, Nbt(context).encodeToNbt(this, obj))
}

/**
 * Retrieves the object the tag that was stored in [tag] with [put] and converts it into the original object.
 * For nullable values use the .nullable extension on the serializer.
 *
 * @param key If you are serializing two objects of the same type, you MUST specify a key.
 * The same key must be used in [put].
 * @param context Used for polymorphic serialization, see [Here](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md).
 */
@OptIn(ExperimentalSerializationApi::class)
public fun <T> DeserializationStrategy<T>.getFrom(
    tag: NbtCompound,
    key: String? = null,
    context: SerializersModule = EmptySerializersModule()
): T {
    val deserializedTag =
        tag.get(key ?: this.descriptor.serialName) ?: if (descriptor.isNullable) return null as T else NbtCompound()
    return Nbt(context).decodeFromNbt(this, deserializedTag)
}
