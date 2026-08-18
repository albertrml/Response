package br.com.arml.response.core

import org.junit.Assert.assertEquals
import org.junit.Test

class ErrorReasonTest {
    @Test
    fun `ErrorReason subclasses should hold data correctly`() {
        assertEquals(null, ErrorReason.Server().code)
        assertEquals(500, ErrorReason.Server(500).code)
        
        assertEquals(null, ErrorReason.Client().code)
        assertEquals(404, ErrorReason.Client(404).code)
        
        assertEquals(null, ErrorReason.Business().code)
        assertEquals("USER_EXISTS", ErrorReason.Business("USER_EXISTS").code)
        
        assertEquals(ErrorReason.Network, ErrorReason.Network)
        assertEquals(ErrorReason.Unknown, ErrorReason.Unknown)
    }
}
