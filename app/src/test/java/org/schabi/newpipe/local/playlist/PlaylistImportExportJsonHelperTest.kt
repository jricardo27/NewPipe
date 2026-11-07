package org.schabi.newpipe.local.playlist

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.schabi.newpipe.database.stream.model.StreamEntity
import org.schabi.newpipe.extractor.stream.StreamType
import org.schabi.newpipe.local.playlist.PlaylistImportExportJsonHelper.PlaylistWithStreams
import org.schabi.newpipe.local.playlist.PlaylistImportExportJsonHelper.StreamExportEntry

class PlaylistImportExportJsonHelperTest {

    private fun stream(url: String, title: String) = StreamEntity(
        uid = 0,
        serviceId = 0,
        url = url,
        title = title,
        streamType = StreamType.VIDEO_STREAM,
        duration = 123,
        uploader = "some uploader",
        uploaderUrl = "https://uploader.example",
        thumbnailUrl = "https://thumbnail.example",
        viewCount = null,
        textualUploadDate = null,
        uploadDate = null,
        isUploadDateApproximation = null
    )

    @Test
    fun writeReadRoundTripPreservesData() {
        val playlists = listOf(
            PlaylistWithStreams(
                "First list",
                listOf(
                    StreamExportEntry(stream("https://v.example/1", "Video one"), 5000),
                    StreamExportEntry(stream("https://v.example/2", "Video two"), 0)
                )
            ),
            PlaylistWithStreams(
                "Empty list",
                emptyList()
            )
        )

        val out = ByteArrayOutputStream()
        PlaylistImportExportJsonHelper.writeTo(playlists, out)
        val read = PlaylistImportExportJsonHelper.readFrom(
            ByteArrayInputStream(out.toByteArray())
        )

        assertEquals(2, read.size)

        assertEquals("First list", read[0].name)
        assertEquals(2, read[0].streams.size)
        assertEquals("https://v.example/1", read[0].streams[0].streamEntity.url)
        assertEquals("Video one", read[0].streams[0].streamEntity.title)
        assertEquals(5000, read[0].streams[0].progressMillis)
        assertEquals(0, read[0].streams[1].progressMillis)

        assertEquals("Empty list", read[1].name)
        assertEquals(0, read[1].streams.size)
    }

    @Test
    fun readFromEmptyPlaylistArray() {
        val json = "{\"app_version\":\"x\",\"app_version_int\":0,\"playlists\":[]}"
        val read = PlaylistImportExportJsonHelper.readFrom(
            ByteArrayInputStream(json.toByteArray())
        )
        assertEquals(0, read.size)
    }

    @Test
    fun readFromMissingProgressDefaultsToZero() {
        // an export without the progress field (older format) must still import
        val json = """
            {"app_version":"x","app_version_int":0,"playlists":[
              {"name":"Old list","items":[
                {"service_id":0,"url":"https://v.example/1","title":"V","stream_type":"VIDEO_STREAM",
                 "duration":10,"uploader":"u","uploader_url":null,"thumbnail_url":null}
              ]}
            ]}
        """.trimIndent()
        val read = PlaylistImportExportJsonHelper.readFrom(
            ByteArrayInputStream(json.toByteArray())
        )
        assertEquals(1, read.size)
        assertEquals(0, read[0].streams[0].progressMillis)
        assertNull(read[0].streams[0].streamEntity.uploadDate)
    }
}
