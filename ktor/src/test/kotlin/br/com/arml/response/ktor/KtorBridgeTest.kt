package br.com.arml.response.ktor

import br.com.arml.response.core.ErrorReason
import br.com.arml.response.core.ResponseMetadata
import br.com.arml.response.test.assertFailure
import br.com.arml.response.test.assertLoading
import br.com.arml.response.test.assertSuccess
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class KtorBridgeTest {

    @Test
    fun `HttpResponse asResponse Success and Metadata`() = runTest {
        val meta = ResponseMetadata()
        val client = HttpClient(MockEngine { respond("OK") })

        with(client){
            get("/").apply {
                asResponse<String>().assertSuccess()
                asResponse<String>(metadata = meta).assertSuccess { _, metadata ->
                    assertEquals(meta, metadata)
                }
            }
        }
    }

    @Test
    fun `HttpResponse asResponse Parsing Error`() = runTest {
        val client = HttpClient(MockEngine { respond("OK") })
        client
            .get("/")
            .asResponse<Int>()
            .assertFailure(ErrorReason.Unknown)
    }

    @Test
    fun `HttpResponse asResponse Error branches with cache`() = runTest {
        val cache = "KtorCacheValue"
        val client = HttpClient(MockEngine { respond("Error", HttpStatusCode.BadRequest) })

        val r = client.get("/").asResponse<String>(previousData = cache)
        r.assertFailure(ErrorReason.Client(400)) { _, _, previous, _ ->
            assertEquals(cache, previous)
        }
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

        val failureCodes = hashMapOf(
            "/400" to ErrorReason.Client(400),
            "/300" to ErrorReason.Unknown,
            "/500" to ErrorReason.Server(500),
        )

        failureCodes.forEach { (path, reason) ->
            client.get(path).asResponse<String>().assertFailure(reason)
        }
    }

    @Test
    fun `asKtorResponse Exceptions and Metadata`() = runTest {
        val meta = ResponseMetadata()
        asKtorResponse<String> { throw IOException() }.assertFailure(ErrorReason.Network)
        asKtorResponse<String>(metadata = meta) { throw IllegalStateException() }.assertFailure(
            ErrorReason.Unknown
        ) { _, _, _, m ->
            assertEquals(meta, m)
        }
    }

    @Test
    fun `asKtorResponseFlow success branch`() = runTest {
        val client = HttpClient(MockEngine { respond("OK") })
        val results = asKtorResponseFlow<String>(initialData = "KtorFlowCache") { client.get("/") }.toList()

        results[0].assertLoading { cache, _ -> assertEquals("KtorFlowCache", cache) }
        results[1].assertSuccess { data, _ -> assertEquals("OK", data) }
    }

    @Test
    fun `Internal mappings coverage`() {
        assertEquals(ErrorReason.Unknown, mapKtorErrorCode(100))
        assertEquals(ErrorReason.Network, mapKtorException(IOException()))
    }
}
