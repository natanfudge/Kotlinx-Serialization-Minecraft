package drawer.mc.util

import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.block.MaterialColor
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.piston.PistonBehavior


object Builders {
    /**
     * Creates a new [BlockEntityType]
     * @param blocks The blocks that will have the [BlockEntity].
     * @param blockEntitySupplier Pass a function that simply returns a new [BlockEntity] instance.
     */
    fun <T : BlockEntity> blockEntityType(vararg blocks: Block, blockEntitySupplier: () -> T): BlockEntityType<T> =
            BlockEntityType.Builder.create(blockEntitySupplier, blocks).build(null)

    /**
     * Creates a new [BlockEntityType]
     * @param blocks The blocks that will have the [BlockEntity].
     * @param blockEntitySupplier Pass a function that simply returns a new [BlockEntity] instance.
     */
    fun <T : BlockEntity> blockEntityType(blocks: List<Block>, blockEntitySupplier: () -> T): BlockEntityType<T> =
            BlockEntityType.Builder.create(blockEntitySupplier, blocks.toTypedArray()).build(null)

    /**
     * Creates a new [Material]
     */
    fun material(materialColor: MaterialColor, isLiquid: Boolean = false, isSolid: Boolean = true,
                 blocksMovement: Boolean = true, blocksLight: Boolean = true, requiresTool: Boolean = false,
                 burnable: Boolean = false, replaceable: Boolean = false,
                 pistonBehavior: PistonBehavior = PistonBehavior.NORMAL
    ): Material = Material(
            materialColor,
            isLiquid,
            isSolid,
            blocksMovement,
            blocksLight,
            !requiresTool,
            burnable,
            replaceable,
            pistonBehavior
    )


    /**
     * Creates a new [Block.Settings] with an existing material
     */
    fun blockSettings(material: Material, collidable: Boolean = true, slipperiness: Float = 0.6F,
                      hardness: Float = 0.0f, resistance: Float = 0.0f, dropsLike: Block? = null): Block.Settings =
            Block.Settings.of(material).apply {
                if (!collidable) noCollision()
                slipperiness(slipperiness)
                strength(hardness, resistance)
                if (dropsLike != null) dropsLike(dropsLike)
            }

    /**
     * Creates a new [Block.Settings] by building its material on the spot.
     */
    fun blockSettings(materialColor: MaterialColor,
                      collidable: Boolean = true,
                      slipperiness: Float = 0.6F,
                      hardness: Float = 0.0f,
                      resistance: Float = 0.0f,
                      dropsLike: Block? = null,
                      isLiquid: Boolean = false,
                      isSolid: Boolean = true,
                      blocksMovement: Boolean = true,
                      blocksLight: Boolean = true,
                      requiresTool: Boolean = false,
                      burnable: Boolean = false,
                      replaceable: Boolean = false,
                      pistonBehavior: PistonBehavior = PistonBehavior.NORMAL
    ): Block.Settings = Block.Settings.of(Material(
            materialColor,
            isLiquid,
            isSolid,
            blocksMovement,
            blocksLight,
            !requiresTool,
            burnable,
            replaceable,
            pistonBehavior
    )
    ).apply {
        if (!collidable) noCollision()
        slipperiness(slipperiness)
        strength(hardness, resistance)
        if (dropsLike != null) dropsLike(dropsLike)
    }


}