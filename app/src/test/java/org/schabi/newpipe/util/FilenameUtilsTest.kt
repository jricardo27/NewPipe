package org.schabi.newpipe.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FilenameUtilsTest {

    @Test
    fun sanitizeRemovesIllegalCharacters() {
        assertEquals(
            "a_b_c_d_e_f_g_h_i",
            FilenameUtils.sanitizeFilename("a\\b/c:d*e?f\"g<h>i")
        )
    }

    @Test
    fun sanitizeRemovesPipe() {
        assertEquals("a_b", FilenameUtils.sanitizeFilename("a|b"))
    }

    @Test
    fun sanitizeKeepsNormalNames() {
        assertEquals("My Playlist 2024", FilenameUtils.sanitizeFilename("My Playlist 2024"))
    }

    @Test
    fun sanitizeKeepsDots() {
        assertEquals("playlist.json", FilenameUtils.sanitizeFilename("playlist.json"))
    }

    @Test
    fun sanitizeEmptyString() {
        assertEquals("", FilenameUtils.sanitizeFilename(""))
    }
}
