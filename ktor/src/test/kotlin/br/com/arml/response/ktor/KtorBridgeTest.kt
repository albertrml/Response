package br.com.arml.response.ktor

import br.com.arml.response.core.ErrorReason
import br.com.arml.response.core.Response
import br.com.arml.response.core.ResponseMetadata
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class KtorBridgeTest {

    @Test
    fun `HttpResponse asResponse Success and Metadata`() = runTest {
        val meta = ResponseMetadata()
        val client = HttpClient(MockEngine { respond("OK") })

        val r1 = client.get("/").asResponse<String>()
        val r2 = client.get("/").asResponse<String>(metadata = meta)

        assertTrue(r1 is Response.Success)
        assertEquals(meta, r2.metadata)
    }

    @Test
    fun `HttpResponse asResponse Parsing Error`() = runTest {
        val client = HttpClient(MockEngine { respond("OK") })
        // Conversion from "OK" to Int fails
        val r = client.get("/").asResponse<Int>()
        assertTrue(r is Response.Failure)
        assertEquals(ErrorReason.Unknown, (r as Response.Failure).reason)
    }

    @Test
    fun `Ktor error status codes mapping`() = runTest {
        val client = HttpClient(MockEngine {
            val status = when (it.url.encodedPath) {
                "/400" -> HttpStatusCode.BadRequest
                "/500" -> HttpStatusCode.InternalServerError
                else -> HttpStatusCode.MultipleChoices
            }
            respond("Error", status)
        })

        assertEquals(
            ErrorReason.Client(400),
            (client.get("/400").asResponse<String>() as Response.Failure).reason
        )
        assertEquals(
            ErrorReason.Server(500),
            (client.get("/500").asResponse<String>() as Response.Failure).reason
        )
        assertEquals(
            ErrorReason.Unknown,
            (client.get("/300").asResponse<String>() as Response.Failure).reason
        )
    }

    @Test
    fun `asKtorResponse Exceptions and Metadata`() = runTest {
        val meta = ResponseMetadata()
        val r1 = asKtorResponse<String> { throw IOException() }
        val r2 = asKtorResponse<String>(metadata = meta) { throw IllegalStateException() }

        assertEquals(ErrorReason.Network, (r1 as Response.Failure).reason)
        assertEquals(ErrorReason.Unknown, (r2 as Response.Failure).reason)
        assertEquals(meta, r2.metadata)
    }

    @Test
    fun `asKtorResponseFlow success branch`() = runTest {
        val client = HttpClient(MockEngine { respond("OK") })
        val results = asKtorResponseFlow<String>(initialData = "Cache") { client.get("/") }.toList()

        assertTrue(results[1] is Response.Success)
        assertEquals("Cache", (results[0] as Response.Loading).previousData)
    }

    @Test
    fun `Internal mappings coverage`() {
        assertEquals(ErrorReason.Unknown, mapKtorErrorCode(100))
        assertEquals(ErrorReason.Network, mapKtorException(IOException()))
    }
}
