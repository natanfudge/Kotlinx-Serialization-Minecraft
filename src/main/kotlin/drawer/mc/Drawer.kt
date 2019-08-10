//package drawer.mc
//
//import drawer.convertToTag
//import drawer.mc.util.Builders
//import drawer.mc.util.DefaultBlock
//import drawer.mc.util.ModInit
//import kotlinx.serialization.KSerializer
//import kotlinx.serialization.serializer
//import net.minecraft.block.BlockEntityProvider
//import net.minecraft.block.BlockState
//import net.minecraft.block.entity.BlockEntity
//import net.minecraft.block.entity.BlockEntityType
//import net.minecraft.entity.player.PlayerEntity
//import net.minecraft.nbt.CompoundTag
//import net.minecraft.util.Hand
//import net.minecraft.util.hit.BlockHitResult
//import net.minecraft.util.math.BlockPos
//import net.minecraft.util.registry.Registry
//import net.minecraft.world.BlockView
//import net.minecraft.world.World
//import kotlin.reflect.KProperty
//
//class Bar<T : Any?>(val value: T)
//
//fun <T : Any?> foo(x: T) {
//
//}
//
//fun main() {
//    val x = Bar(1)
//    foo(x.value)
//}
//
//
////TODO: key
//data class MemorizedValue<T>(val value: T, val serializer: KSerializer<T>)
//
//abstract class AutoSerializable(type: BlockEntityType<*>) : BlockEntity(type) {
//    val values = mutableMapOf<String, Any?>()
//    private val serializers = mutableMapOf<String, KSerializer<Any?>>()
//
//    override fun toTag(tag: CompoundTag): CompoundTag {
//        val data = CompoundTag().apply {
//            for ((k, v) in values) {
//                put(k, serializers.getValue(k).convertToTag(v))
//            }
//        }
//
//        tag.put("key", data)
//
//        return super.toTag(tag)
//    }
//
//    override fun fromTag(tag: CompoundTag) {
//        super.fromTag(tag)
//        val data = tag.getTag("key") as CompoundTag
//        for (key in data.keys){
//            values.put(key,data.)
//        }
//    }
//
//    operator fun < T> KSerializer<T>.getValue(thisRef: Any?, property: KProperty<*>): T? {
//        return values[property.name] as? T
//    }
//
//    operator fun <T> KSerializer<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T?): T? {
//        values[property.name] = value
//        serializers[property.name] = this as KSerializer<Any?>
//        return value
//    }
//
//}
//
//
//fun SerializerDelegate() {
//
//}
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
//class TestBlockEntity : AutoSerializable(BlockEntityType) {
//    var num: Int? by Int.serializer()
////    override fun toTag(compoundTag_1: CompoundTag?): CompoundTag {
////        return super.toTag(compoundTag_1)
////    }
////
////    override fun fromTag(compoundTag_1: CompoundTag?) {
////        super.fromTag(compoundTag_1)
////    }
//}
//
////private operator fun <T> KSerializer<T>.setValue(testBlockEntity: TestBlockEntity, property: KProperty<*>, t: T?) {
////        TODO("not implemented")
////}
////
////private operator fun <T> KSerializer<T>.getValue(testBlockEntity: TestBlockEntity, property: KProperty<*>): T {
////    TODO("not implemented")
////}
//
//object TestBlock : DefaultBlock(), BlockEntityProvider {
//    override fun createBlockEntity(var1: BlockView): BlockEntity = TestBlockEntity()
//
//
//    override fun activate(
//        blockState_1: BlockState?,
//        world: World,
//        pos: BlockPos,
//        playerEntity_1: PlayerEntity?,
//        hand_1: Hand?,
//        blockHitResult_1: BlockHitResult?
//    ): Boolean {
//        (world.getBlockEntity(pos) as TestBlockEntity).num++
//        return super.activate(blockState_1, world, pos, playerEntity_1, hand_1, blockHitResult_1)
//    }
//
//
//}