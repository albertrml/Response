package br.com.arml.response.core

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ResponseExtensionsTest {

    @Test
    fun `mapSuccess transforms only success state and preserves metadata`() = runTest {
        val metadata = ResponseMetadata(extra = mapOf("test" to true))
        val loading: Response<Int> = Response.Loading(10, metadata)
        val success: Response<Int> = Response.Success(10, metadata)
        val failure: Response<Int> = Response.Failure(Exception(), ErrorReason.Unknown, 10, metadata)
        
        val source: Flow<Response<Int>> = flowOf(loading, success, failure)
        val results = source.mapSuccess { it * 2 }.toList()
        
        assertEquals(20, (results[0] as Response.Loading).previousData)
        assertEquals(metadata, results[0].metadata)

        assertEquals(Response.Success(20, metadata), results[1])
        assertEquals(metadata, results[1].metadata)

        assertEquals(20, (results[2] as Response.Failure).previousData)
        assertEquals(metadata, results[2].metadata)
    }

    @Test
    fun `update modifies state flow correctly`() {
        val state = MutableStateFlow("initial")
        val response = Response.Success("new")
        
        response.update(state) { _, res -> res.getOrNull() ?: "" }
        assertEquals("new", state.value)
    }

    @Test
    fun `withCache should persist last success data and metadata`() = runTest {
        val metadata = ResponseMetadata(extra = mapOf("id" to "123"))
        val flow = flowOf(
            Response.Success("First", metadata),
            Response.Loading<String>(),
            Response.Failure<String>(Exception())
        ).withCache()
        
        val results = flow.toList()
        
        assertEquals(Response.Success("First", metadata), results[0])
        assertEquals("First", (results[1] as Response.Loading).previousData)
        assertEquals(metadata, results[1].metadata)
        assertEquals("First", (results[2] as Response.Failure).previousData)
        assertEquals(metadata, results[2].metadata)
    }
}
