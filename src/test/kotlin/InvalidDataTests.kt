@file:UseSerializers(ForBlockPos::class, ForUuid::class, ForIdentifier::class)

import drawer.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import org.junit.jupiter.api.Test
import utils.CityData
import utils.OtherFormats
import utils.Zoo
import utils.testTagSerializer
import java.util.*
import kotlin.test.assertEquals

@Serializable
data class DefaultedData(
    val uuid: UUID? = null, val blockPos: BlockPos = BlockPos(1, 2, 3),
    val identifier: Identifier = Identifier("minecraft", "carrot")
)

@Serializable
data class NoDefaultIntData(val int: Int = 2)

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
            innerTag.remove(innerTag.keys.first())
            val result = deserialize()

            assertEquals(null, result.uuid)
            assertEquals(1, result.blockPos.x)
            assertEquals(2, result.blockPos.y)
            assertEquals(3, result.blockPos.z)
            assertEquals("minecraft", result.identifier.namespace)
            assertEquals("carrot", result.identifier.path)
        }




}

@Serializable
data class Defaulted(val int: Int = 2)