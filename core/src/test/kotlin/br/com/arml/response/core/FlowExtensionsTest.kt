package br.com.arml.response.core

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FlowExtensionsTest {

    @Test
    fun `until stops collection when predicate is met and includes last value`() = runTest {
        val flow = flowOf(1, 2, 3, 4, 5)
        val result = flow.until { it == 3 }.toList()
        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun `until collects all values if predicate is never met`() = runTest {
        val flow = flowOf(1, 2, 3)
        val result = flow.until { it == 10 }.toList()
        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun `until handles empty flow`() = runTest {
        val flow = flowOf<Int>()
        val result = flow.until { true }.toList()
        assertEquals(emptyList<Int>(), result)
    }
}
