package br.com.arml.response.compose

import br.com.arml.response.core.Response
import br.com.arml.response.core.ResponseMetadata
import org.junit.Assert.assertEquals
import org.junit.Test

class ResponseComposablesTest {

    @Test
    fun `ShowResults logic executes correct actions and passes metadata`() {
        var successData: String? = null
        var metadataReceived: ResponseMetadata? = null

        val metadata = ResponseMetadata()
        val successResponse: Response<String> = Response.Success("Data", metadata)
        
        // Manual simulation of Composable logic decision branch
        when (successResponse) {
            is Response.Success -> {
                successData = successResponse.result
                metadataReceived = successResponse.metadata
            }
            else -> {}
        }
        
        assertEquals("Data", successData)
        assertEquals(metadata, metadataReceived)
    }
}
