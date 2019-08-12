package drawer

import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerialKind
import kotlinx.serialization.StructureKind

internal abstract class UnsealedListLikeDescriptor(val elementDesc: SerialDescriptor) : SerialDescriptor {
    override val kind: SerialKind get() = StructureKind.LIST
    override val elementsCount: Int = 1
    override fun getElementName(index: Int): String = index.toString()
    override fun getElementIndex(name: String): Int =
        name.toIntOrNull() ?: throw IllegalArgumentException("$name is not a valid list index")

    override fun getElementDescriptor(index: Int): SerialDescriptor = elementDesc

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnsealedListLikeDescriptor) return false

        if (elementDesc == other.elementDesc && name == other.name) return true

        return false
    }

    override fun hashCode(): Int {
        return elementDesc.hashCode() * 31 + name.hashCode()
    }
}

internal class UnsealedListLikeDescriptorImpl(elementDesc: SerialDescriptor,override val name : String) : UnsealedListLikeDescriptor(elementDesc)