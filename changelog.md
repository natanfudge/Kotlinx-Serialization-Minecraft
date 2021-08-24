# 4.1.0
- Updated to Minecraft 1.17.1
- Updated kotlinx.serialization to 1.2.2
- Updated kotlin to 1.5.21
# 4.0.0
- No longer includes and exposes kotlinx.serialization - this is now part of Fabric Language Kotlin.
- Updated to kotlinx.serialization-json 1.0.1
## 3.2.0
Fixed NBT serialization being completely broken with nullable lists and polymorphic classes.
### 3.1.1
Fixed an unnecessary cast causing an exception.
# 3.1.0-1.15-pre1
Now works in 1.15 (not compatible with 1.14.4).
## 3.1.0
Updated kotlinx.serialization to 0.14.0. This release allows you to serialize `object`s and sealed classes without `SerializersModule`!
# 3.0.0
NBT is now stored in a completely different format.
### 2.0.1
- Built-in serializers will now log a warning instead of crash when fields are missing. You should still provide default values.
- Kotlinx.serialization updated to 0.13.0
- # 2.0.0
Add a fully custom ItemStack serializer that allows ItemStacks that store more than 64 of a kind.
This is a breaking change, all previously stored ItemStacks will be incompatible.
### 1.1.2
Add `SoundCategory` and `SoundInstance.AttenuationType` Serializers
### 1.1.1
Add `SoundEvent` Serializer



