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