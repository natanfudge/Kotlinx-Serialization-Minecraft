@file:UseSerializers(BlockPosSerializer::class, UUIDSerializer::class, IdentifierSerializer::class, Vec3dSerializer::class)

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.minecraft.BlockPosSerializer
import kotlinx.serialization.minecraft.IdentifierSerializer
import kotlinx.serialization.minecraft.UUIDSerializer
import kotlinx.serialization.minecraft.Vec3dSerializer
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import kotlin.test.Test
import utils.testTagSerializer
import java.util.*
import kotlin.test.assertEquals

@Serializable
data class DefaultedData(
    val uuid: UUID? = null, val blockPos: BlockPos = BlockPos(1, 2, 3),
    val identifier: Identifier = Identifier("minecraft", "carrot")
)


@Serializable
data class DefaultedOtherFormats(
    val uuid: UUID = UUID(1,2),
    val uuidList: List<UUID> = listOf(UUID(2,3)),
    val blockPos: BlockPos = BlockPos(-1,2,33),
    val blockPosList: List<BlockPos> = listOf(),
    val id: Identifier = Identifier("asdf:6sss"),
    val vec3d : Vec3d = Vec3d(777.0,123.0,-3.3)
)

class InvalidDataTests {


    @Test
    fun `If you have defaulted data you know the values are always valid`() =
        testTagSerializer(DefaultedData.serializer()) {
            val originalValue = DefaultedData(
                UUID(1, 2), BlockPos(6, 7, 8), Identifier("holy", "gradle")
            )

            serialize(originalValue)
            innerTag.remove(innerTag.keys.first())
            innerTag.remove(innerTag.keys.first())
            innerTag.remove(innerTag.keys.first())
            val result = deserialize()

            assertEquals(null, result.uuid)
            assertEquals(1, result.blockPos.x)
            assertEquals(2, result.blockPos.y)
            assertEquals(3, result.blockPos.z)
            assertEquals("minecraft", result.identifier.namespace)
            assertEquals("carrot", result.identifier.path)
        }


    @Test
    fun `If the tag is empty you will get the default value instead`() =
        testTagSerializer(DefaultedData.serializer()) {
            val result = deserialize()

            assertEquals(result,DefaultedData())

        }


    @Test
    fun `Custom serializers properly default the values`() =
        testTagSerializer(DefaultedOtherFormats.serializer()) {
            val result = deserialize()

            assertEquals(result,DefaultedOtherFormats())

        }




}

