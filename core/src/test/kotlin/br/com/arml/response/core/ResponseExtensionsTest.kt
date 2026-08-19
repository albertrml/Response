package br.com.arml.response.core

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
            Response.Loading(),
            Response.Failure(Exception())
        ).withCache()
        
        val results = flow.toList()
        
        assertEquals(Response.Success("First", metadata), results[0])
        assertEquals("First", (results[1] as Response.Loading).previousData)
        assertEquals(metadata, results[1].metadata)
        assertEquals("First", (results[2] as Response.Failure).previousData)
        assertEquals(metadata, results[2].metadata)
    }

    @Test
    fun `withCache should not overwrite existing cache or metadata if already present`() = runTest {
        val oldMeta = ResponseMetadata(extra = mapOf("version" to 1))
        val newMeta = ResponseMetadata(extra = mapOf("version" to 2))
        
        val flow = flowOf(
            Response.Success("Old Data", oldMeta),
            Response.Loading(previousData = "New Cache", metadata = newMeta)
        ).withCache()
        
        val results = flow.toList()
        val loading = results[1] as Response.Loading
        assertEquals("New Cache", loading.previousData)
        assertEquals(newMeta, loading.metadata)
    }

    @Test
    fun `zipWith combinations coverage`() {
        val s1 = Response.Success(10)
        val s2 = Response.Success(20)
        val f1 = Response.Failure<Int>(RuntimeException("F1"))
        val l1 = Response.Loading<Int>()

        // Success + Success
        assertEquals(30, (s1.zipWith(s2) { a, b -> a + b } as Response.Success).result)
        
        // Failure + Anything
        assertTrue(f1.zipWith(s2) { a, b -> a + b } is Response.Failure)
        assertTrue(f1.zipWith(l1) { a, b -> a + b } is Response.Failure)
        
        // Success + Failure
        assertTrue(s1.zipWith(f1) { a, b -> a + b } is Response.Failure)
        
        // Loading combinations
        assertTrue(l1.zipWith(s1) { a, b -> a + b } is Response.Loading)
        assertTrue(s1.zipWith(l1) { a, b -> a + b } is Response.Loading)
        assertTrue(l1.zipWith(l1) { a, b -> a + b } is Response.Loading)
    }

    @Test
    fun `zipWith cache logic coverage`() {
        val s1 = Response.Success(10)
        val f1 = Response.Failure(RuntimeException(), previousData = 5)
        val l1 = Response.Loading(previousData = 2)

        // Zip two states with cache
        val result = f1.zipWith(l1) { a, b -> a + b }
        assertEquals(7, (result as Response.Failure).previousData)
        
        // Zip where one is null
        val noCache = Response.Loading<Int>(previousData = null)
        val resultNull = s1.zipWith(noCache) { a, b -> a + b }
        assertEquals(null, (resultNull as Response.Loading).previousData)
    }

    @Test
    fun `combineResponse should delegate to zipWith correctly`() = runTest {
        val f1 = flowOf(Response.Success(1))
        val f2 = flowOf(Response.Success(2))
        val result = f1.combineResponse(f2) { a, b -> a + b }.toList()
        assertEquals(Response.Success(3), result[0])
    }
}
