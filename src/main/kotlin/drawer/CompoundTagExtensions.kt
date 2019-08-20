package drawer

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.internal.CommonEnumSerializer
import kotlinx.serialization.internal.EnumDescriptor
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration


/** See [put] */
fun CompoundTag.putIdentifier(id: Identifier?, key: String? = null) = ForIdentifier.nullable.put(id, this, key)

/** See [getFrom] */
fun CompoundTag.getIdentifier(key: String? = null) = ForIdentifier.getFrom(this, key)

/** * See [put]*/
fun CompoundTag.putBlockPos(id: BlockPos?, key: String? = null) = ForBlockPos.nullable.put(id, this, key)

/** See [getFrom] */
fun CompoundTag.getBlockPos(key: String? = null) = ForBlockPos.getFrom(this, key)
