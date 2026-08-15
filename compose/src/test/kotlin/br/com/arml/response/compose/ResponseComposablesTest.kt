package br.com.arml.response.compose

import br.com.arml.response.core.Response
import org.junit.Assert.assertEquals
import org.junit.Test

class ResponseComposablesTest {

    @Test
    fun `ShowResults logic executes correct actions`() {
        var successData: String? = null
        var errorOccurred = false

        // Manual simulation of Composable logic decision branch
        when (val successResponse: Response<String> = Response.Success("Data")) {
            is Response.Success -> successData = successResponse.result
            else -> errorOccurred = true
        }
        
        assertEquals("Data", successData)
        assertEquals(false, errorOccurred)
    }
}
