package spatialcrafting.util.kotlinwrappers

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld
import net.minecraft.world.World

fun World.getBlock(location: BlockPos): Block = getBlockState(location).block
val World.isServer get() = !isClient
/**
 * Replaces the block in the [pos] with the specified [block], using the default [BlockState].
 */
fun IWorld.setBlock(block: Block, pos: BlockPos): Boolean = world.setBlockState(pos, block.defaultState)

fun World.name() = if (isClient) "Client" else "Server"
fun World.dropItemStack(stack: ItemStack, pos: BlockPos) {
    val itemEntity = ItemEntity(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), stack)
    world!!.spawnEntity(itemEntity)
}