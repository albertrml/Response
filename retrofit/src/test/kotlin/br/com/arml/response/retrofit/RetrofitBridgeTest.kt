package br.com.arml.response.retrofit

import br.com.arml.response.core.ErrorReason
import br.com.arml.response.core.Response
import br.com.arml.response.core.ResponseMetadata
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import retrofit2.Response as RetrofitResponse

class RetrofitBridgeTest {

    @Test
    fun `asRetrofitResponse Success paths`() = runTest {
        val meta = ResponseMetadata(extra = mapOf("id" to "explicit"))

        val r1 = asRetrofitResponse { RetrofitResponse.success("D1") }
        val r2 = asRetrofitResponse(metadata = meta) { RetrofitResponse.success("D2") }

        assertTrue(r1 is Response.Success)
        assertEquals(meta, r2.metadata)
    }

    @Test
    fun `asRetrofitResponse should handle Unit type`() = runTest {
        val r = asRetrofitResponse<Unit> { RetrofitResponse.success(null) }
        assertTrue(r is Response.Success)
    }

    @Test
    fun `asRetrofitResponse should fail on null body for non-Unit`() = runTest {
        val cache = "Cache"
        val r = asRetrofitResponse<String>(previousData = cache) { RetrofitResponse.success(null) }
        assertTrue(r is Response.Failure)
        assertEquals(cache, (r as Response.Failure).previousData)
    }

    @Test
    fun `asRetrofitResponse Error codes coverage`() = runTest {
        val body = "".toResponseBody(null)
        val c400 = asRetrofitResponse<String> { RetrofitResponse.error(400, body) }
        val c500 = asRetrofitResponse<String> { RetrofitResponse.error(500, body) }
        val c301 = asRetrofitResponse<String> { RetrofitResponse.error(301, body) }

        assertEquals(ErrorReason.Client(400), (c400 as Response.Failure).reason)
        assertEquals(ErrorReason.Server(500), (c500 as Response.Failure).reason)
        assertEquals(ErrorReason.Unknown, (c301 as Response.Failure).reason)
    }

    @Test
    fun `asRetrofitResponse Exception mapping`() = runTest {
        val io = asRetrofitResponse<String> { throw IOException() }
        val other = asRetrofitResponse<String> { throw IllegalStateException() }

        assertEquals(ErrorReason.Network, (io as Response.Failure).reason)
        assertEquals(ErrorReason.Unknown, (other as Response.Failure).reason)
    }

    @Test
    fun `asRetrofitResponseFlow branches`() = runTest {
        val initial = "Cache"
        val flow = asRetrofitResponseFlow(initialData = initial) {
            RetrofitResponse.success("OK")
        }.toList()

        assertTrue(flow[0] is Response.Loading)
        assertEquals(initial, (flow[0] as Response.Loading).previousData)
        assertEquals("OK", (flow[1] as Response.Success).result)
    }

    @Test
    fun `toResponse mapping edge cases`() {
        val success = RetrofitResponse.success("Data").toResponse(null, null, false)
        assertTrue(success.metadata != null)
    }
}
