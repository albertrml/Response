package br.com.arml.response.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResponseExtensionsTest {

    @Test
    fun `toResponseFlow converts success flow correctly`() = runTest {
        val results = flowOf("Data").toResponseFlow().toList()
        assertEquals(2, results.size)
        assertTrue(results[0] is Response.Loading)
        assertEquals(Response.Success("Data"), results[1])
    }

    @Test
    fun `toResponseFlow handles flow errors`() = runTest {
        val exception = RuntimeException("Flow Error")
        val results = flow<String> { throw exception }.toResponseFlow().toList()
        assertEquals(2, results.size)
        assertTrue(results[0] is Response.Loading)
        assertEquals(Response.Failure(exception), results[1])
    }

    @Test
    fun `mapSuccess transforms only success state`() = runTest {
        val source = flowOf(Response.Loading, Response.Success(10), Response.Failure(Exception()))
        val results = source.mapSuccess { it * 2 }.toList()
        
        assertTrue(results[0] is Response.Loading)
        assertEquals(Response.Success(20), results[1])
        assertTrue(results[2] is Response.Failure)
    }

    @Test
    fun `update modifies state flow correctly`() {
        val state = MutableStateFlow("initial")
        val response = Response.Success("new")
        
        response.update(state) { _, res -> res.getOrNull() ?: "" }
        assertEquals("new", state.value)
    }
}
