package utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import kotlin.reflect.KFunction

data class TestResult(
    val obj: Any, // original object
    val res: Any, // resulting object
    val ext: Any  // serialized (external) representation
)

data class Case<T : Any>(
    val obj: T,
    val serializer: KSerializer<T>,
    val context: SerialModule = EmptyModule,
    val name: String = obj.javaClass.simpleName,
    val hasNulls: Boolean = false
)
val testCases: List<Case<*>> = listOf(
    Case(CityData(1, "New York"), CityData.serializer()),
    Case(StreetData(2, "Broadway", CityData(1, "New York")), StreetData.serializer()),
    Case(StreetData2(2, "Broadway", CityData(1, "New York")), StreetData2.serializer()),
    Case(StreetData2(2, "Broadway", null), StreetData2.serializer(), hasNulls = true),
    Case(CountyData("US", listOf(CityData(1, "New York"), CityData(2, "Chicago"))), CountyData.serializer()),
    Case(zoo, Zoo.serializer(), hasNulls = true),
    Case(shop, Shop.serializer()),
    Case(otherFormats, OtherFormats.serializer()),
    Case(zeroNumbers, VariousNumbers.serializer()),
    Case(nullableZeroNumbers, VariousNullableNumbers.serializer()),
    Case(tags, Tags.serializer()),
    Case(intArrayTagWrapper, IntArrayTagWrapper.serializer()),
    Case(abstractTags, AbstractTags.serializer()),
    Case(message, MessageWrapper.serializer(), messageModule),
    Case(listTags, ListTags.serializer()),
    Case(compoundTags,CompoundTags.serializer())

)

@Suppress("UNCHECKED_CAST")
fun <T : Any> testCase(
    serializer: KSerializer<T>,
    obj: T,
    context: SerialModule,
    method: (KSerializer<Any>, Any, SerialModule) -> TestResult,
    verbose: Boolean = true

): Boolean {
    if (verbose) println("Start with $obj")
    val result = method(serializer as KSerializer<Any>, obj, context)

    if (verbose) {
        println("Loaded obj ${result.res}")
        println("    equals=${obj == result.res}, sameRef=${obj === result.res}")
        println("Saved form ${result.ext}")
    }
    return obj == result.res
}

fun testCase(
    case: Case<Any>,
    method: (KSerializer<Any>, Any, SerialModule) -> TestResult,
    verbose: Boolean = true
): Boolean {
    println("Test case ${case.name}")
    try {
        return testCase(case.serializer, case.obj, case.context, method, verbose)
    } catch (e: java.lang.Exception) {
        println("Error executing test case '$case':\n")
        throw e
    }

}

class TestException(message: String) : java.lang.Exception(message)

@Suppress("UNCHECKED_CAST")
fun testMethod(
    method: (KSerializer<Any>, Any, SerialModule) -> TestResult,
    supportsNull: Boolean = true,
    verbose: Boolean = true
): Pair<Int, Int> {
    if (verbose) println("==============================================")
    println("Running with ${(method as KFunction<*>).name}")
    var totalCount = 0
    var failCount = 0
    testCases.forEach { case ->
        if (!supportsNull && case.hasNulls) return@forEach
        if (verbose) println()
        if (!testCase(case as Case<Any>, method, verbose))
            failCount++
        totalCount++
    }
    if (verbose) {
        println("==============================================")
        println("Done with ${method.name}")
        if (failCount > 0)
            println("!!! FAILED $failCount TEST CASES OUT OF $totalCount TEST CASES !!!")
        else
            println("Passed $totalCount test cases")
    }
    if (failCount > 0) throw Exception("Not all tests passed!")
    return Pair(failCount, totalCount)
}
