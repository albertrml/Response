package br.com.arml.response.core

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResponseBuildersTest {

    @Test
    fun `asResponse emits Loading and then Success`() = runTest {
        val result = asResponse { "Data" }.toList()
        assertEquals(2, result.size)
        assertTrue(result[0] is Response.Loading)
        assertEquals(Response.Success("Data"), result[1])
    }

    @Test
    fun `asResponse catches exception and emits Failure`() = runTest {
        val exception = RuntimeException("Error")
        val result = asResponse { throw exception }.toList()
        assertEquals(2, result.size)
        assertTrue(result[0] is Response.Loading)
        assertEquals(Response.Failure(exception), result[1])
    }

    @Test
    fun `runAsResponse returns Success on data`() = runTest {
        val result = runAsResponse { "Data" }
        assertEquals(Response.Success("Data"), result)
    }

    @Test
    fun `runAsResponse returns Failure on exception`() = runTest {
        val exception = RuntimeException("Error")
        val result = runAsResponse { throw exception }
        assertEquals(Response.Failure(exception), result)
    }
}
