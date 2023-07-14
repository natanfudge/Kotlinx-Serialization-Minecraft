package kotlinx.serialization.minecraft.mixin;

import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * Internal
 */
@Mixin(NbtList.class)
public interface AccessibleNbtList {
    /**
     * Internal
     * @return The private list
     */
    @Accessor("value")
    @NotNull
    List<@NotNull NbtElement> getWrappedList();
}
