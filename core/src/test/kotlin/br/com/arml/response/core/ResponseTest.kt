package br.com.arml.response.core

import org.junit.Assert.assertEquals
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
    fun `Response mapTo should transform data and preserve multiple policies`() {
        val pagination = PagePaginationPolicy(currentPage = 2, hasMore = false)
        val metadata = ResponseMetadata(policies = listOf(pagination), extra = mapOf("key" to "value"))
        
        // Test Success
        val success = Response.Success(10, metadata)
        val mappedSuccess = success.mapTo { it * 2 }
        assertEquals(20, (mappedSuccess as Response.Success).result)
        assertEquals(metadata, mappedSuccess.metadata)

        // Test Failure with cache
        val failureWithCache = Response.Failure(RuntimeException(), ErrorReason.Unknown, 5, metadata)
        val mappedFailure = failureWithCache.mapTo { it * 2 }
        assertEquals(10, (mappedFailure as Response.Failure).previousData)
        assertEquals(metadata, mappedFailure.metadata)

        // Test Loading with cache
        val loadingWithCache = Response.Loading(2, metadata)
        val mappedLoading = loadingWithCache.mapTo { it * 2 }
        assertEquals(4, (mappedLoading as Response.Loading).previousData)
        assertEquals(metadata, mappedLoading.metadata)
    }

    @Test
    fun `mapTo should handle null previousData correctly`() {
        val failure = Response.Failure<Int>(RuntimeException())
        val loading = Response.Loading<Int>()
        
        val mappedFailure = failure.mapTo { it * 2 }
        val mappedLoading = loading.mapTo { it * 2 }
        
        assertEquals(null, (mappedFailure as Response.Failure).previousData)
        assertEquals(null, (mappedLoading as Response.Loading).previousData)
    }

    @Test
    fun `getOrNull should return result or cache from any state`() {
        assertEquals("data", Response.Success("data").getOrNull())
        assertEquals("cache", Response.Failure(RuntimeException(), ErrorReason.Unknown, "cache").getOrNull())
        assertEquals("cache", Response.Loading("cache").getOrNull())
    }

    @Test
    fun `exceptionOrNull should return the error on Failure or null otherwise`() {
        val error = RuntimeException("Fail")
        val failure = Response.Failure<Int>(error)
        val success = Response.Success(10)
        val loading = Response.Loading<Int>()

        assertEquals(error, failure.exceptionOrNull())
        assertEquals(null, success.exceptionOrNull())
        assertEquals(null, loading.exceptionOrNull())
    }

    @Test
    fun `side-effect operators should execute only on correct states`() {
        var called = false
        
        // onSuccess
        Response.Success("ok").onSuccess { called = true }
        assertTrue(called); called = false
        Response.Failure<String>(RuntimeException()).onSuccess { called = true }
        assertTrue(!called)

        // onFailure
        Response.Failure<String>(RuntimeException()).onFailure { _, _, _ -> called = true }
        assertTrue(called); called = false
        Response.Success("ok").onFailure { _, _, _ -> called = true }
        assertTrue(!called)

        // onLoading
        Response.Loading<String>().onLoading { called = true }
        assertTrue(called); called = false
        Response.Success("ok").onLoading { called = true }
        assertTrue(!called)
    }

    @Test
    fun `onRecover without params coverage`() {
        val cache = "cached"
        val failureWithCache = Response.Failure(RuntimeException(), ErrorReason.Unknown, cache)
        val failureNoCache = Response.Failure<String>(RuntimeException())
        val success = Response.Success("ok")

        assertEquals(cache, (failureWithCache.onRecover() as Response.Success).result)
        assertTrue(failureNoCache.onRecover() is Response.Failure)
        assertEquals(success, success.onRecover())
    }

    @Test
    fun `onRecover with lambda coverage`() {
        val failure = Response.Failure(RuntimeException("err"), ErrorReason.Unknown, "old")
        val success = Response.Success("ok")

        val recovered = failure.onRecover { e, c -> "rec-$c-${e.message}" }
        assertEquals("rec-old-err", (recovered as Response.Success).result)
        
        // Non-failure states should return this
        assertEquals(success, success.onRecover { _, _ -> "fail" })
    }

    @Test
    fun `mapError branch coverage`() {
        val error = RuntimeException("orig")
        val failure = Response.Failure<String>(error)
        val success = Response.Success("data")
        val loading = Response.Loading<String>()

        val mapped = failure.mapError { RuntimeException("mapped") }
        assertEquals("mapped", (mapped as Response.Failure).error.message)
        
        // Ensure success/loading are untouched
        assertEquals(success, success.mapError { it })
        assertEquals(loading, loading.mapError { it })
    }
}
