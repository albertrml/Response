package br.com.arml.response.retrofit

import br.com.arml.response.core.ErrorReason
import br.com.arml.response.core.ResponseMetadata
import br.com.arml.response.test.assertFailure
import br.com.arml.response.test.assertLoading
import br.com.arml.response.test.assertSuccess
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import retrofit2.Response as RetrofitResponse

class RetrofitBridgeTest {

    @Test
    fun `asRetrofitResponse Success paths`() = runTest {
        val meta = ResponseMetadata(extra = mapOf("id" to "explicit"))

        asRetrofitResponse { RetrofitResponse.success("D1") }.assertSuccess()
        asRetrofitResponse(metadata = meta) { RetrofitResponse.success("D2") }.assertSuccess { _, metadata ->
            assertEquals(meta, metadata)
        }
    }

    @Test
    fun `asRetrofitResponse should handle Unit type`() = runTest {
        asRetrofitResponse<Unit> { RetrofitResponse.success(null) }.assertSuccess()
    }

    @Test
    fun `asRetrofitResponse should fail on null body for non-Unit`() = runTest {
        val cache = "RetrofitCache"
        asRetrofitResponse<String>(previousData = cache) { RetrofitResponse.success(null) }.assertFailure { _, _, previous, _ ->
            assertEquals(cache, previous)
        }
    }

    @Test
    fun `asRetrofitResponse Error codes coverage`() = runTest {
        val body = "".toResponseBody(null)
        val errorCodes = hashMapOf(
            400 to ErrorReason.Client(400),
            500 to ErrorReason.Server(500),
            301 to ErrorReason.Unknown
        )

        errorCodes.forEach { (code, reason) ->
            asRetrofitResponse<String> { RetrofitResponse.error(code, body) }
                .assertFailure(reason)
        }
    }

    @Test
    fun `asRetrofitResponse Exception mapping`() = runTest {
        asRetrofitResponse<String> { throw IOException() }.assertFailure(ErrorReason.Network)
        asRetrofitResponse<String> { throw IllegalStateException() }.assertFailure(ErrorReason.Unknown)
    }

    @Test
    fun `asRetrofitResponseFlow branches`() = runTest {
        val initial = "InitialData"
        val flow = asRetrofitResponseFlow(initialData = initial) {
            RetrofitResponse.success("OK")
        }.toList()

        flow[0].assertLoading { cache, _ -> assertEquals(initial, cache) }
        flow[1].assertSuccess { data, _ -> assertEquals("OK", data) }
    }

    @Test
    fun `toResponse mapping edge cases`() {
        RetrofitResponse.success("Data").toResponse(null, null, false).assertSuccess { _, metadata ->
            assertEquals(true, metadata != null)
        }
    }
}
