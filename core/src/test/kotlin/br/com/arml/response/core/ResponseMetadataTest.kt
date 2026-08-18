package br.com.arml.response.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResponseMetadataTest {
    @Test
    fun `ResponseMetadata should allow retrieving PagePaginationPolicy by type`() {
        val pagination = PagePaginationPolicy(currentPage = 1, hasMore = true)
        val metadata = ResponseMetadata(policies = listOf(pagination))

        val foundPolicy = metadata.findPolicy<PagePaginationPolicy>()
        assertEquals(pagination, foundPolicy)
        assertEquals(1, foundPolicy?.currentPage)
    }

    @Test
    fun `ResponseMetadata should allow retrieving CursorPaginationPolicy by type`() {
        val cursor = CursorPaginationPolicy(nextCursor = "abc", hasMore = true)
        val metadata = ResponseMetadata(policies = listOf(cursor))

        val foundPolicy = metadata.findPolicy<CursorPaginationPolicy>()
        assertEquals(cursor, foundPolicy)
        assertEquals("abc", foundPolicy?.nextCursor)
    }

    @Test
    fun `ResponseMetadata should allow retrieving SyncPolicy by type`() {
        val sync = SyncPolicy(lastVersion = 100L, isDelta = true)
        val metadata = ResponseMetadata(policies = listOf(sync))

        val foundPolicy = metadata.findPolicy<SyncPolicy>()
        assertEquals(sync, foundPolicy)
        assertEquals(100L, foundPolicy?.lastVersion)
    }

    @Test
    fun `ResponseMetadata should allow multiple policies and retrieve them correctly`() {
        val pagination = PagePaginationPolicy(currentPage = 1, hasMore = true)
        val sync = SyncPolicy(lastVersion = 50L, isDelta = false)
        val metadata = ResponseMetadata(policies = listOf(pagination, sync))

        assertEquals(pagination, metadata.findPolicy<PagePaginationPolicy>())
        assertEquals(sync, metadata.findPolicy<SyncPolicy>())
    }

    @Test
    fun `findPolicy should return null if policy type is not present even with other policies`() {
        val sync = SyncPolicy(1, false)
        val metadata = ResponseMetadata(policies = listOf(sync))
        assertEquals(null, metadata.findPolicy<PagePaginationPolicy>())
    }

    @Test
    fun `ResponseMetadata default values should be sane`() {
        val metadata = ResponseMetadata()
        assertTrue(metadata.timestamp > 0)
        assertTrue(metadata.policies.isEmpty())
        assertTrue(metadata.extra.isEmpty())
    }

    @Test
    fun `ResponseMetadata should allow full manual initialization`() {
        val time = 123456L
        val policies = listOf(PagePaginationPolicy(1, true))
        val extra = mapOf("meta" to "data")
        val metadata = ResponseMetadata(timestamp = time, policies = policies, extra = extra)
        
        assertEquals(time, metadata.timestamp)
        assertEquals(policies, metadata.policies)
        assertEquals(extra, metadata.extra)
    }
}
