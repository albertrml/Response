package br.com.arml.response.core

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResponseBuildersTest {

    @Test
    fun `asResponseFlow emits Loading and then Success`() = runTest {
        val result = asResponseFlow { "Data" }.toList()
        assertEquals(2, result.size)
        assertTrue(result[0] is Response.Loading)
        assertEquals(Response.Success("Data"), result[1])
    }

    @Test
    fun `asResponseFlow catches exception and emits Failure`() = runTest {
        val exception = RuntimeException("Error")
        val result = asResponseFlow { throw exception }.toList()
        assertEquals(2, result.size)
        assertTrue(result[0] is Response.Loading)
        val failure = result[1] as Response.Failure
        assertEquals(exception, failure.error)
        assertEquals(ErrorReason.Unknown, failure.reason)
    }

    @Test
    fun `asResponse returns Success on data`() = runTest {
        val result = asResponse { "Data" }
        assertEquals(Response.Success("Data"), result)
    }

    @Test
    fun `asResponse returns Failure on exception`() = runTest {
        val exception = RuntimeException("Error")
        val result = asResponse { throw exception }
        assertTrue(result is Response.Failure)
        assertEquals(exception, (result as Response.Failure).error)
    }

    @Test
    fun `asResponseFlow should emit Loading and Failure with previousData`() = runTest {
        val cache = "Old Data"
        val exception = RuntimeException("Error")
        val result = asResponseFlow(initialData = cache) { throw exception }.toList()
        
        assertEquals(2, result.size)
        assertEquals(Response.Loading(cache), result[0])
        assertEquals(Response.Failure(exception, previousData = cache), result[1])
    }

    @Test
    fun `Flow asResponseFlow converts success flow correctly`() = runTest {
        val results = flowOf("Data").asResponseFlow().toList()
        assertEquals(2, results.size)
        assertTrue(results[0] is Response.Loading)
        assertEquals(Response.Success("Data"), results[1])
    }

    @Test
    fun `Flow asResponseFlow handles flow errors`() = runTest {
        val exception = RuntimeException("Flow Error")
        val results = flow<String> { throw exception }.asResponseFlow().toList()
        assertEquals(2, results.size)
        assertTrue(results[0] is Response.Loading)
        assertEquals(Response.Failure<String>(exception), results[1])
    }

    @Test
    fun `Flow asResponseFlow converts flow with cache correctly`() = runTest {
        val cache = "Old Data"
        val results = flowOf("New Data").asResponseFlow(initialData = cache).toList()
        
        assertEquals(2, results.size)
        assertEquals(Response.Loading(cache), results[0])
        assertEquals(Response.Success("New Data"), results[1])
    }
}
