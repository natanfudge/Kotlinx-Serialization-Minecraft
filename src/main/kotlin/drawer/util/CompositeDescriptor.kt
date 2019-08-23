package drawer.util

import kotlinx.serialization.internal.SerialClassDescImpl

class CompositeDescriptor(name: String, vararg elements : String) : SerialClassDescImpl(name){
    init {
        for(element in elements) addElement(element)
    }
}