package br.com.arml.response.core

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResponseTest {

    @Test
    fun `Response Success holds result and metadata correctly`() {
        val data = "Test Data"
        val metadata = ResponseMetadata()
        val response = Response.Success(data, metadata)
        assertEquals(data, response.result)
        assertEquals(metadata, response.metadata)
    }

    @Test
    fun `Response Failure holds error, reason, cache and metadata correctly`() {
        val error = RuntimeException("Test Exception")
        val cache = "Old Data"
        val metadata = ResponseMetadata()
        val response = Response.Failure(error, ErrorReason.Network, cache, metadata)
        
        assertEquals(error, response.error)
        assertEquals(ErrorReason.Network, response.reason)
        assertEquals(cache, response.previousData)
        assertEquals(metadata, response.metadata)
    }

    @Test
    fun `Response Loading can hold previous data and metadata`() {
        val cache = "Cached Data"
        val metadata = ResponseMetadata()
        val loading = Response.Loading(cache, metadata)
        assertEquals(cache, loading.previousData)
        assertEquals(metadata, loading.metadata)
    }

    @Test
    fun `Response fold should execute correct block with metadata`() {
        val success = Response.Success("data")
        val failure = Response.Failure(RuntimeException("error"), ErrorReason.Network, "cache")
        val loading = Response.Loading("cache")

        assertEquals("data", success.fold({ _, _ -> "loading" }, { data, _ -> data }, { _, _, _, _ -> "error" }))
        assertEquals("cache-network", failure.fold({ _, _ -> "loading" }, { _, _ -> "success" }, { _, reason, cache, _ -> "$cache-${reason.javaClass.simpleName.lowercase()}" }))
        assertEquals("cache-cache", loading.fold({ cache, _ -> "cache-$cache" }, { _, _ -> "success" }, { _, _, _, _ -> "error" }))
    }

    @Test
    fun `ResponseMetadata should allow retrieving policies by type`() {
        val pagination = PagePaginationPolicy(currentPage = 1, hasMore = true)
        val metadata = ResponseMetadata(policies = listOf(pagination))
        
        val foundPolicy = metadata.findPolicy<PagePaginationPolicy>()
        assertEquals(pagination, foundPolicy)
        assertEquals(1, foundPolicy?.currentPage)
    }

    @Test
    fun `Response mapTo should transform data and preserve multiple policies`() {
        val pagination = PagePaginationPolicy(currentPage = 2, hasMore = false)
        val metadata = ResponseMetadata(policies = listOf(pagination), extra = mapOf("key" to "value"))
        val success = Response.Success(10, metadata)
        val failure = Response.Failure(RuntimeException(), ErrorReason.Unknown, 5, metadata)
        val loading = Response.Loading(2, metadata)

        val transform: (Int) -> Int = { it * 2 }

        val mappedSuccess = success.mapTo(transform)
        assertEquals(20, (mappedSuccess as Response.Success).result)
        assertEquals(metadata, mappedSuccess.metadata)

        val mappedFailure = failure.mapTo(transform)
        assertEquals(10, (mappedFailure as Response.Failure).previousData)
        assertEquals(metadata, mappedFailure.metadata)

        val mappedLoading = loading.mapTo(transform)
        assertEquals(4, (mappedLoading as Response.Loading).previousData)
        assertEquals(metadata, mappedLoading.metadata)
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
    fun `asResponseFlow emits Loading then Success with automatic metadata`() = runTest {
        val expectedData = "Test Data"
        val operation: suspend () -> String = { expectedData }

        val flow = asResponseFlow(block = operation)
        val results = flow.toList()

        assertEquals(2, results.size)
        assertTrue(results[0] is Response.Loading)
        assertTrue(results[1] is Response.Success)
        assertEquals(expectedData, (results[1] as Response.Success).result)
        assertNotNull(results[1].metadata)
    }
}
