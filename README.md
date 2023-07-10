# Fabric Drawer
[![Discord](https://img.shields.io/discord/219787567262859264?color=blue&label=Discord)](https://discord.gg/CFaCu97)
[![Latest Commit](https://img.shields.io/github/last-commit/natanfudge/fabric-drawer)](https://github.com/natanfudge/Fabric-Drawer/commits/master)

Drawer is a Fabric library mod for Kotlin mods that allows you to easily convert objects back and forth from NBT and PacketBytebuf using kotlinx.serialization, and provides KSerializers for common Minecraft types (for NBT, PacketBytebuf, or even JSON). 

<details><summary><b>Gradle</b></summary>
<p>

Add to dependencies:
```groovy
dependencies {
    // [...]
    modImplementation("io.github.natanfudge:fabric-drawer:<drawer-version>")
    include("io.github.natanfudge:fabric-drawer:<drawer-version>")
}
```
Add the kotlinx.serialization gradle plugin:
```groovy
plugins {
    // [...]
    id ("org.jetbrains.kotlin.plugin.serialization") version <kotlin-version> // Or omit version here and use the new gradle 5.6 plugins block in settings.gradle https://docs.gradle.org/5.6/userguide/plugins.html#sec:plugin_version_management
}
```

</p>
</details>

For other versions of Minecraft, look at different branches. 

<details><summary><b>Basic Usage</b></summary>
<p>

Annotate any class with `@Serializable` to make it serializable. **Make sure that every property has a usable default value when storing data for a block entity.** More information on this farther down.
```kotlin
import kotlinx.serialization.Serializable

@Serializable
data class BlockInfo(var timesClicked: Int = 0, val placementTime: Long = 0, val firstToClick: String? = null)
```

Then you can serialize it back and forth.

### In a block entity

```kotlin
fun fillData() {
    myInfo = BlockInfo(timesClicked = 7, placementTime = 1337, firstToClick = "fudge")
}
// Or make myInfo lateinit if initializing it at first placement is guaranteed
var myInfo: BlockInfo = BlockInfo()
    private set

override fun toNbt(tag: NbtCompound): NbtCompound {
    // Serialize
    BlockInfo.serializer().put(myInfo, inTag = tag)
    return super.toNbt(tag)
}

override fun fromNbt(tag: NbtCompound) {
    super.fromNbt(tag)
    // Deserialize
    myInfo = BlockInfo.serializer().getFrom(tag)
}
```


### In a packet

```kotlin
val data = BlockInfo(timesClicked = 0, placementTime = 420, firstToClick = null)

val packetData = PacketByteBuf(Unpooled.buffer())
// Serialize
BlockInfo.serializer().write(data, toBuf = packetData)
    
for (player in PlayerStream.all(world.server)) {
    ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, Identifier("modid", "packet_id"), packetData)
}
```

```kotlin
ClientSidePacketRegistry.INSTANCE.register(Identifier("modId", "packet_id")) { context, buf ->
    // Deserialize
    val data = BlockInfo.serializer().readFrom(buf)
}
```

Remember that you still need to validate your client to server packets!


An example mod can be seen [here](https://github.com/natanfudge/fabric-drawer-example).

</p>
</details>

<details><summary><b>Putting two objects of the same type in one NbtCompound</b></summary>
<p>

 If you are putting two objects of the same type in one NbtCompound you need to specify a unique key for each one. (Note: You don't need to do this with a `PacketByteBuf`.)
 For example:
```kotlin
val myInfo1 = BlockInfo(timesClicked = 7, placementTime = 1337, firstToClick = "fudge")
val myInfo2 = BlockInfo(timesClicked = 3, placementTime = 9999, firstToClick = "you")

override fun toNbt(tag: NbtCompound): NbtCompound {
    BlockInfo.serializer().put(myInfo1, inTag = tag, key = "myInfo1")
    BlockInfo.serializer().put(myInfo1, inTag = tag, key = "myInfo2")
}

override fun fromNbt(tag: NbtCompound) {
    myInfo1 = BlockInfo.serializer.getFrom(tag, key = "myInfo1")
    myInfo2 = BlockInfo.serializer.getFrom(tag, key = "myInfo2")
}
```
 
This is only true for when YOU are putting 2 instances of the same type. If a class has multiple of the same type that's OK.
```kotlin
// No need for a key
data class MyData(val int1: Int = 0, val int2: Int = 0)
fun toNbt(tag: NbtCompound) {
    MyData.serializer().put(MyData(1, 2))
}
```

```kotlin
// Need a key
data class MyData(val int1: Int = 0, val int2: Int = 0)
fun toNbt(tag: NbtCompound) {
    MyData.serializer().put(MyData(1, 2), key = "first")
    MyData.serializer().put(MyData(3, 4), key = "second")
}
```

</p>
</details>

<details><summary><b>Serializing Java and Minecraft objects</b></summary>

<p>

You can serialize any primitive, and any list of primitives, and any class of your own that is annotated with `@Serializable`, without any extra modification:
```kotlin
// OK
@Serializable
data class MyData(val str: String, val list: List<Double>)
@Serializable
data class Nested(val myData: MyData, val c: Char)
```
However, if you try to put in a `UUID` or a `BlockPos`, for example:
```kotlin
// Error!
@Serializable
data class MyPlayer(val id: UUID)
```

To fix this, put at the very top of the file:
```kotlin
@file:UseSerializers(ForUuid::class, ForBlockPos::class)
```

Serializers for the following classes are available:
- UUID
- BlockPos
- Vec3d
- Identifier
- SoundEvent (note: requires being in a Minecraft context as it accesses the registry)
- All NBT classes
- ItemStack (requires being in a Minecraft context)
- Ingredient (requires being in a Minecraft context)
- DefaultedList<>


If I've missed anything you need please [open an issue](https://github.com/natanfudge/Fabric-Drawer/issues/new).

You can also add your own serializers and more using the kotlinx.serialization API. For more information, [see the README](https://github.com/Kotlin/kotlinx.serialization/blob/master/README.md). 

Note: Primitive serializers don't work right now for `NbtCompound`, so just use the existing `putInt` etc methods. 

</p>
</details>

<details><summary><b>Advanced</b></summary>
<p>

### Why does every property need to have a default value when storing data for a block entity?
There are 2 main reasons:
1. Nbt data is volatile. It can change at any time, via modifying the save file, or by using the `/data` command.
This means you can never trust the information provided to by the NBT to be valid, or the server might crash endlessly on startup trying to deserialize non-existent nbt data.
Having a default value avoids this problem by simply using those default values when the data is invalid.
2. Sometimes you only want to store data on the server, so you don't use `BlockEntityClientSerializable`.
Your data will (usually, see point 1.) be restored on the server just fine.  
However, Minecraft will also call `fromTag` on the client, in an attempt to sync the data to him as well.
You don't send him any of the nbt data required to load your `@Serializable` classes, so if there are no default values, it will simply crash.

Make sure that the default values are **usable**, meaning trying to use them in your mod will never crash!

### Polymorphic serialization
- Read [this](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md) first. 
- In order to do this in drawer you need to add the `SerializersModule` instance whenever you serialize / deserialize using that module. 
If this is cumbersome a simple extension method on `KSerialize<T>` can be used that automatically inserts your module.

</p>
</details>

<details><summary><b>Tips</b></summary>
<p>

- To avoid boilerplate it's recommended to add a `putIn()` / `writeTo()` function to your serializable classes, for example:
```kotlin
@Serializable
data class MyData(val x: Int, val y: String) {
    fun putIn(tag: NbtCompound) = MyData.serializer().put(this, tag)
}
//Usage:
fun toNbt(tag: NbtCompound) {
    val data = MyData(1, "hello")
    tag.putIn(tag) // Instead of MyData.serializer().put(data,tag)
}
```

Please thumbs-up [this issue](https://github.com/Kotlin/kotlinx.serialization/issues/329) so we can have this syntax built-in to the library for all serializable classes! Having a common interface for serializable classes would also enable avoiding boilerplate in other places.

- Serializable classes are also serializable to [Json](https://github.com/Kotlin/kotlinx.serialization/blob/master/README.md), and any other format that kotlinx.serialization and its addons support. 

</p>
</details>