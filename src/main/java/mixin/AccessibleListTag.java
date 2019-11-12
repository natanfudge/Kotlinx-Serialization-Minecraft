package mixin;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.ArrayList;
import java.util.List;

@Mixin(ListTag.class)
public interface AccessibleListTag {
    @Accessor("value")
    @NotNull
    List<@NotNull Tag> getWrappedList();
}
