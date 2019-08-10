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
//class TestBlockEntity : BlockEntity(BlockEntityType) {

//}
//

//
//object TestBlock : DefaultBlock(), BlockEntityProvider {
//    override fun createBlockEntity(var1: BlockView): BlockEntity = TestBlockEntity()

//
//}