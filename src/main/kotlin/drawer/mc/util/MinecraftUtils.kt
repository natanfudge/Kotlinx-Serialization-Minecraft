@file:Suppress("NOTHING_TO_INLINE")

package drawer.mc.util

import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.Material
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.text.LiteralText
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.roundToInt
import kotlin.math.sqrt
import net.minecraft.block.Block.Settings

val BlockPos.xz get() = "($x,$z)"

fun World.play(soundEvent: SoundEvent,at: BlockPos,
                    ofCategory: SoundCategory,toPlayer: PlayerEntity? = null,  volumeMultiplier: Float = 1.0f, pitchMultiplier: Float = 1.0f) : Unit
        = playSound(toPlayer, at,soundEvent, ofCategory, volumeMultiplier, pitchMultiplier)

fun LivingEntity?.sendMessage(message: String) {
    if (this == null) return
    this.sendMessage(LiteralText(message))
}

fun Vec3d.toBlockPos() = BlockPos(x.roundToInt(), y.roundToInt(), z.roundToInt())
inline fun Ingredient.matches(itemStack: ItemStack) = method_8093(itemStack)


fun DrawableHelper.drawCenteredStringWithoutShadow(textRenderer_1: TextRenderer, string_1: String?, int_1: Int, int_2: Int, int_3: Int) {
    textRenderer_1.draw(string_1, (int_1 - textRenderer_1.getStringWidth(string_1) / 2).toFloat(), int_2.toFloat(), int_3)
}

fun BlockPos.distanceFrom(otherPos :Vec3d) =
        sqrt((otherPos.x - this.x).squared() + (otherPos.y - this.y).squared() + (otherPos.z - this.z).squared())

val  PlayerEntity.itemsInInventoryAndOffhand get() = inventory.main + inventory.offHand

open class DefaultBlock : Block(Settings.of(Material.STONE))