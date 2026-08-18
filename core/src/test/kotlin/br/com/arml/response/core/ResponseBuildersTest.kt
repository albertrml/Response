package br.com.arml.response.core

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResponseBuildersTest {

    @Test
    fun `asResponseFlow block builder branch coverage`() = runTest {
        val meta = ResponseMetadata()
        
        // Branch: success path
        val sResults = asResponseFlow(initialMetadata = meta) { "OK" }.toList()
        assertTrue(sResults[1] is Response.Success)
        assertEquals(meta, sResults[1].metadata)

        // Branch: failure path with provided metadata
        val fResults = asResponseFlow<String>(initialMetadata = meta) { throw RuntimeException() }.toList()
        assertTrue(fResults[1] is Response.Failure)
        assertEquals(meta, fResults[1].metadata)
    }

    @Test
    fun `Flow asResponseFlow extension branch coverage`() = runTest {
        // Branch: success path
        val sResults = flowOf("Data").asResponseFlow().toList()
        assertTrue(sResults[1] is Response.Success)
        assertNotNull(sResults[1].metadata)

        // Branch: catch path
        val fResults = flow<String> { throw RuntimeException("Fail") }.asResponseFlow().toList()
        assertTrue(fResults[1] is Response.Failure)
        assertEquals("Fail", (fResults[1] as Response.Failure).error.message)
    }

    @Test
    fun `asResponse oneshot branch coverage`() = runTest {
        // Branch: success with null metadata
        val r1 = asResponse { "D1" }
        assertNotNull(r1.metadata)

        // Branch: failure with explicit metadata
        val meta = ResponseMetadata()
        val r2 = asResponse(metadata = meta) { throw RuntimeException() }
        assertEquals(meta, r2.metadata)
    }

    @Test
    fun `Result asResponse branch coverage`() {
        val meta = ResponseMetadata()
        
        // Success branches
        val s1 = Result.success("OK").asResponse()
        val s2 = Result.success("OK").asResponse(meta)
        assertNotNull(s1.metadata)
        assertEquals(meta, s2.metadata)

        // Failure branch
        val f = Result.failure<String>(RuntimeException()).asResponse(meta)
        assertEquals(meta, f.metadata)
    }

    @Test
    fun `asResponseFlow result flow branch coverage`() = runTest {
        val flow = flowOf(Result.success("OK"))
        val results = flow.asResponseFlow(initialData = "cache").toList()
        
        assertTrue(results[0] is Response.Loading)
        assertEquals("cache", (results[0] as Response.Loading).previousData)
        assertTrue(results[1] is Response.Success)
    }
}
