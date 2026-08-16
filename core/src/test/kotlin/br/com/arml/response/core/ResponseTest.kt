package br.com.arml.response.core

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResponseTest {

    @Test
    fun `Response Success holds result correctly`() {
        val data = "Test Data"
        val response = Response.Success(data)
        assertEquals(data, response.result)
    }

    @Test
    fun `Response Failure holds error, reason and cache correctly`() {
        val error = RuntimeException("Test Exception")
        val cache = "Old Data"
        val response = Response.Failure(error, ErrorReason.Network, cache)
        
        assertEquals(error, response.error)
        assertEquals(ErrorReason.Network, response.reason)
        assertEquals(cache, response.previousData)
    }

    @Test
    fun `Response Loading can hold previous data`() {
        val cache = "Cached Data"
        val loading = Response.Loading(cache)
        assertEquals(cache, loading.previousData)
    }

    @Test
    fun `Response fold should execute correct block with new parameters`() {
        val success = Response.Success("data")
        val failure = Response.Failure(RuntimeException("error"), ErrorReason.Network, "cache")
        val loading = Response.Loading("cache")

        assertEquals("data", success.fold({ "loading" }, { it }, { _, _, _ -> "error" }))
        assertEquals("cache-network", failure.fold({ "loading" }, { "success" }, { _, reason, cache -> "$cache-${reason.javaClass.simpleName.lowercase()}" }))
        assertEquals("cache-cache", loading.fold({ "cache-$it" }, { "success" }, { _, _, _ -> "error" }))
    }

    @Test
    fun `Response mapTo should transform Success and all Cache states`() {
        val success = Response.Success(10)
        val failure = Response.Failure(RuntimeException(), ErrorReason.Unknown, 5)
        val loading = Response.Loading(2)

        val transform: (Int) -> Int = { it * 2 }

        assertEquals(20, (success.mapTo(transform) as Response.Success).result)
        assertEquals(10, (failure.mapTo(transform) as Response.Failure).previousData)
        assertEquals(4, (loading.mapTo(transform) as Response.Loading).previousData)
    }

    @Test
    fun `getOrNull should return result or cache from any state`() {
        assertEquals("data", Response.Success("data").getOrNull())
        assertEquals("cache", Response.Failure(RuntimeException(), ErrorReason.Unknown, "cache").getOrNull())
        assertEquals("cache", Response.Loading("cache").getOrNull())
    }

    @Test
    fun `onSuccess should execute block only on Success state`() {
        var called = false
        Response.Success("data").onSuccess { called = true }
        assertTrue(called)

        called = false
        Response.Loading("cache").onSuccess { called = true }
        assertTrue(!called)
    }

    @Test
    fun `onFailure should execute block only on Failure state`() {
        var called = false
        Response.Failure<String>(RuntimeException()).onFailure { _, _, _ -> called = true }
        assertTrue(called)

        called = false
        Response.Success("data").onFailure { _, _, _ -> called = true }
        assertTrue(!called)
    }

    @Test
    fun `onLoading should execute block only on Loading state`() {
        var called = false
        Response.Loading("cache").onLoading { called = true }
        assertTrue(called)

        called = false
        Response.Success("data").onLoading { called = true }
        assertTrue(!called)
    }

    @Test
    fun `onRecover without params should transform Failure with cache to Success`() {
        val cache = "cached data"
        val failure = Response.Failure(RuntimeException(), ErrorReason.Unknown, cache)
        val recovered = failure.onRecover()
        
        assertTrue(recovered is Response.Success)
        assertEquals(cache, (recovered as Response.Success).result)
    }

    @Test
    fun `onRecover without params should stay Failure if cache is null`() {
        val failure = Response.Failure<String>(RuntimeException())
        val result = failure.onRecover()
        
        assertTrue(result is Response.Failure)
    }

    @Test
    fun `onRecover with lambda should allow custom recovery using cache`() {
        val failure = Response.Failure(RuntimeException("error"), ErrorReason.Unknown, "old")
        val recovered = failure.onRecover { e, cache -> "Recovered $cache after ${e.message}" }
        
        assertEquals("Recovered old after error", (recovered as Response.Success).result)
    }

    @Test
    fun `onRecover should do nothing on Success`() {
        val success = Response.Success("data")
        val result = success.onRecover { _, _ -> "recovered" }
        
        assertEquals(success, result)
    }

    @Test
    fun `mapError should transform Failure error`() {
        val error = RuntimeException("original")
        val failure = Response.Failure<String>(error)
        val mapped = failure.mapError { RuntimeException("mapped") }
        
        assertEquals("mapped", (mapped as Response.Failure).error.message)
    }

    @Test
    fun `mapError should do nothing on Success`() {
        val success = Response.Success("data")
        val result = success.mapError { RuntimeException("mapped") }
        assertEquals(success, result)
    }

    @Test
    fun `asResponseFlow emits Loading then Success when operation is successful`() = runTest {
        val expectedData = "Test Data"
        val operation: suspend () -> String = { expectedData }

        val flow = asResponseFlow(block = operation)
        val results = flow.toList()

        assertEquals(2, results.size)
        assertTrue(results[0] is Response.Loading)
        assertTrue(results[1] is Response.Success)
        assertEquals(expectedData, (results[1] as Response.Success).result)
    }
}
