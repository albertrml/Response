package br.com.arml.response.test

import br.com.arml.response.core.ErrorReason
import br.com.arml.response.core.Response
import org.junit.Assert.assertEquals
import org.junit.Test

class ResponseAssertionsTest {

    @Test
    fun `assertSuccess should pass and execute action when state is success`() {
        val success = Response.Success("OK")
        var actionCalled = false
        
        val result = success.assertSuccess { data, _ ->
            assertEquals("OK", data)
            actionCalled = true
        }
        
        assertEquals(success, result)
        assertEquals(true, actionCalled)
    }

    @Test(expected = AssertionError::class)
    fun `assertSuccess should fail when state is failure`() {
        val failure = Response.Failure<String>(RuntimeException())
        failure.assertSuccess()
    }

    @Test
    fun `assertFailure should pass and validate reason`() {
        val failure = Response.Failure<String>(RuntimeException(), ErrorReason.Network)
        
        val result = failure.assertFailure(expectedReason = ErrorReason.Network) { _, reason, _, _ ->
            assertEquals(ErrorReason.Network, reason)
        }
        
        assertEquals(failure, result)
    }

    @Test(expected = AssertionError::class)
    fun `assertFailure should fail when reason is different`() {
        val failure = Response.Failure<String>(RuntimeException(), ErrorReason.Network)
        failure.assertFailure(expectedReason = ErrorReason.Server(500))
    }

    @Test(expected = AssertionError::class)
    fun `assertFailure should fail when state is loading`() {
        val loading = Response.Loading<String>()
        loading.assertFailure()
    }

    @Test
    fun `assertLoading should pass and execute action`() {
        val loading = Response.Loading("cache")
        var actionCalled = false
        
        loading.assertLoading { cache, _ ->
            assertEquals("cache", cache)
            actionCalled = true
        }
        
        assertEquals(true, actionCalled)
    }

    @Test(expected = AssertionError::class)
    fun `assertLoading should fail when state is success`() {
        val success = Response.Success("OK")
        success.assertLoading()
    }

    @Test
    fun `mock helpers should create correct instances`() {
        val s = successResponse("data")
        s.assertSuccess()
        
        val f = failureResponse<String>("error", ErrorReason.Client(400))
        f.assertFailure(ErrorReason.Client(400))
        
        val n = networkErrorResponse<String>()
        n.assertFailure(ErrorReason.Network)
    }
}
