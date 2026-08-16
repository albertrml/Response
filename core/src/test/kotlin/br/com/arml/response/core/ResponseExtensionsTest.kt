package br.com.arml.response.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ResponseExtensionsTest {

    @Test
    fun `mapSuccess transforms only success state`() = runTest {
        val loading: Response<Int> = Response.Loading(10)
        val success: Response<Int> = Response.Success(10)
        val failure: Response<Int> = Response.Failure(Exception(), ErrorReason.Unknown, 10)
        
        val source: Flow<Response<Int>> = flowOf(loading, success, failure)
        val results = source.mapSuccess { it * 2 }.toList()
        
        assertEquals(20, (results[0] as Response.Loading).previousData)
        assertEquals(Response.Success(20), results[1])
        assertEquals(20, (results[2] as Response.Failure).previousData)
    }

    @Test
    fun `update modifies state flow correctly`() {
        val state = MutableStateFlow("initial")
        val response = Response.Success("new")
        
        response.update(state) { _, res -> res.getOrNull() ?: "" }
        assertEquals("new", state.value)
    }

    @Test
    fun `withCache should persist last success data into subsequent loading and failure`() = runTest {
        val flow = flowOf(
            Response.Success("First"),
            Response.Loading<String>(),
            Response.Failure<String>(Exception())
        ).withCache()
        
        val results = flow.toList()
        
        assertEquals(Response.Success("First"), results[0])
        assertEquals("First", (results[1] as Response.Loading).previousData)
        assertEquals("First", (results[2] as Response.Failure).previousData)
    }

    @Test
    fun `withCache should not overwrite existing cache if already present`() = runTest {
        val flow = flowOf(
            Response.Success("Old"),
            Response.Loading(previousData = "New")
        ).withCache()
        
        val results = flow.toList()
        assertEquals("New", (results[1] as Response.Loading).previousData)
    }
}
