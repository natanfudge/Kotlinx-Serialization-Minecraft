import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.item.ItemPlacementContext

//package drawer.mc
//

//
//const val ModId = "fabricdrawer"
//
////TODO: remove
//fun init() = ModInit.begin(ModId) {
//    registerBlocksWithItemBlocks {
//        TestBlock withId "test"
//    }
//
//    registerTo(Registry.BLOCK_ENTITY) {
//        BlockEntityType withId "test_entity"
//    }
//}
//
//val BlockEntityType = Builders.blockEntityType(TestBlock) { TestBlockEntity() }
//
class TestBlockEntity : BlockEntity(BlockEntityType) {
    override fun resetBlock() {
        super.resetBlock()
    }
}
//

//
object TestBlock : Block(), BlockEntityProvider {
    override fun canReplace(blockState_1: BlockState?, itemPlacementContext_1: ItemPlacementContext?): Boolean {
        return super.canReplace(blockState_1, itemPlacementContext_1)
    }
    override fun createBlockEntity(var1: BlockView): BlockEntity = TestBlockEntity()
block

}