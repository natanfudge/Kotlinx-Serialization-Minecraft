package drawer.mc.util

import net.fabricmc.fabric.api.client.render.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.fabricmc.fabric.api.network.PacketContext
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import net.minecraft.util.registry.Registry

object ModInit {
    /**
     * Should be called at the init method of the mod. Do all of your registry here.
     */
    inline fun begin(modId: String, group: ItemGroup? = null, init: ModInitializationContext.() -> Unit) =
            ModInitializationContext(modId, group).init()
}

class ModInitializationContext(private val modId: String, private val group: ItemGroup?) {
    fun <T> registerTo(registry: Registry<T>, dsl: NamespacedRegistryDsl<T>.() -> Unit) =
            dsl(NamespacedRegistryDsl(modId, registry))

    fun registerBlocksWithItemBlocks(dsl: BlockWithItemRegistryDsl.() -> Unit) =
            dsl(BlockWithItemRegistryDsl(modId, group))

    inline fun <reified T : BlockEntity> register(renderer: BlockEntityRenderer<T>) = BlockEntityRendererRegistry.INSTANCE.register(T::class.java, renderer)

    fun registerServerToClientPacket(packetId: String, packetConsumer: (PacketContext, PacketByteBuf) -> Unit) =
            ClientSidePacketRegistry.INSTANCE.register(Identifier(modId, packetId), packetConsumer)


}



open class NamespacedRegistryDsl<T>(private val namespace: String, private val registry: Registry<T>) {
    open infix fun T.withId(name: String): T = Registry.register(registry, Identifier(namespace, name), this)
}

class BlockWithItemRegistryDsl(private val namespace: String, private val group: ItemGroup?) {
    infix fun Block.withId(name: String): Block {
        Registry.register(Registry.BLOCK, Identifier(namespace, name), this)
        Registry.register(Registry.ITEM, Identifier(namespace, name), BlockItem(this, Item.Settings().group(group ?: ItemGroup.MISC)))
        return this
    }
}