### 1.1.1
Add `SoundEvent` Serializer
### 1.1.2
Add `SoundCategory` and `SoundInstance.AttenuationType` Serializers
# 2.0.0
Add a fully custom ItemStack serializer that allows ItemStacks that store more than 64 of a kind.
This is a breaking change, all previously stored ItemStacks will be incompatible.
### 2.0.1 
- Built-in serializers will now log a warning instead of crash when fields are missing. You should still provide default values.
- Kotlinx.serialization updated to 0.13.0
# 3.0.0
NBT is now stored in a completely different format.   
To illustrate, take the following class:
```kotlin
@Serializable class MyData(val ids: List<UUID>, val someNum: Int)
```
Previously it would be stored in a format that looks like this (this is NBT, not JSON):
```json
{
  "ids.0.least": 1234,
  "ids.0.most": 5678,
  "ids.1.least": 121212,
  "ids.1.most": 3232323,
  "someNum": 2 
}
```
Now objects are properly nested using `ListTag` and `CompoundTag`:
```json
{
  "ids": [
            {
                "least": 1234,
                "most": 5678
            },
            {
                "least" : 121212,
                "most": 3232323
            }
         ],
  "someNum": 2 
}
```
This improves speed, and reduces memory usage and space taken. And most importantly, it makes the data much easier to debug.  
  
Unsurprisingly, <span style="font-size:larger;"> __**THIS UPDATE IS VERY SUPER BREAKING**__</span>. Any NBT data stored with Drawer 2.0.1 and earlier will not be usable.

- Also, removed the .nullable extension property, since it's now provided by kotlinx.serialization itself! You just need to change the import.

## 3.1.0
Updated kotlinx.serialization to 0.14.0. This release allows you to serialize `object`s and sealed classes without `SerialModule`!

# 3.1.0-1.15-pre1
Now works in 1.15 (not compatible with 1.14.4).
### 3.1.1
Fixed an unnecessary cast causing an exception. 
## 3.2.0 
Fixed NBT serialization being completely broken with nullable lists and polymorphic classes.