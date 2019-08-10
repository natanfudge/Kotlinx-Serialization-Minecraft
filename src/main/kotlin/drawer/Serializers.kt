package drawer

import kotlinx.serialization.*
import kotlinx.serialization.internal.LongDescriptor
import kotlinx.serialization.internal.SerialClassDescImpl
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.util.registry.Registry
import java.util.*
import kotlin.collections.HashMap

private typealias McBlockPos = net.minecraft.util.math.BlockPos
private typealias McIdentifier = net.minecraft.util.Identifier
private typealias McIngredient = net.minecraft.recipe.Ingredient
private typealias McDefaultedList<T> = net.minecraft.util.DefaultedList<T>
private typealias McItemStack = net.minecraft.item.ItemStack
private typealias McText = net.minecraft.text.Text
private typealias McCompoundTag = net.minecraft.nbt.CompoundTag


object Serializers {
    @Serializer(forClass = net.minecraft.util.math.BlockPos::class)
    object BlockPos : KSerializer<McBlockPos> {
        override val descriptor: SerialDescriptor = LongDescriptor.withName("BlockPos")
        override fun serialize(encoder: Encoder, obj: McBlockPos) = encoder.encodeLong(obj.asLong())
        override fun deserialize(decoder: Decoder): McBlockPos = McBlockPos.fromLong(decoder.decodeLong())
    }


    @Serializer(forClass = McIdentifier::class)
    object Identifier : KSerializer<McIdentifier> {
        override val descriptor: SerialDescriptor = LongDescriptor.withName("Identifier")
        override fun serialize(encoder: Encoder, obj: McIdentifier) = encoder.encodeString(obj.toString())
        override fun deserialize(decoder: Decoder): McIdentifier = McIdentifier(decoder.decodeString())
    }

        @Serializer(forClass = UUID::class)
    object Uuid : KSerializer<UUID> {
        override val descriptor: SerialDescriptor = object : SerialClassDescImpl("Uuid") {
            init {
                addElement("most") // most will have index 0
                addElement("least") // least will have index 1
            }
        }

        private const val MostIndex = 0
        private const val LeastIndex = 1

        override fun serialize(encoder: Encoder, obj: UUID) {
            val compositeOutput = encoder.beginStructure(descriptor)
            compositeOutput.encodeLongElement(descriptor, MostIndex, obj.mostSignificantBits)
            compositeOutput.encodeLongElement(descriptor, LeastIndex, obj.leastSignificantBits)
            compositeOutput.endStructure(descriptor)
        }

        override fun deserialize(decoder: Decoder): UUID {
            val dec: CompositeDecoder = decoder.beginStructure(descriptor)

            assert(dec.decodeElementIndex(descriptor) == CompositeDecoder.READ_ALL)
            { "Only use the UUID serializer for tag compounds and bytebuffs." }

            val most = dec.decodeLongElement(descriptor, MostIndex)
            val least = dec.decodeLongElement(descriptor, LeastIndex)

            dec.endStructure(descriptor)
            return UUID(most, least)
        }


    }

//
//    @Serializer(forClass = McItemStack::class)
//    object ItemStack : KSerializer<McItemStack> {
//        override val descriptor: SerialDescriptor = object : SerialClassDescImpl("ItemStack") {
//            init {
//                addElement("id") //  will have index 0
//                addElement("count") //  will have index 1
//                addElement("tag")
//            }
//        }
//
//        private const val IdIndex = 0
//        private const val CountIndex = 1
//        private const val TagIndex = 2
//
//        override fun serialize(encoder: Encoder, obj: McItemStack) {
//            val compositeOutput = encoder.beginStructure(descriptor)
//            compositeOutput.encodeStringElement(descriptor, IdIndex, Registry.ITEM.getId(obj.item).toString())
//            compositeOutput.encodeByteElement(descriptor, CountIndex, obj.count.toByte())
//            compositeOutput.encodeNullableSerializableElement(descriptor,)
//            compositeOutput.endStructure(descriptor)
//            compoundTag_1.putString("id", )
//            compoundTag_1.putByte("Count", this.count as Byte)
//            if (this.tag != null) {
//                compoundTag_1.put("tag", this.tag)
//            }
//
//            return compoundTag_1
//        }
//
//        override fun deserialize(decoder: Decoder): McItemStack {
//
//        }
//
//
//    }






}

