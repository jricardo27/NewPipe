package org.schabi.newpipe.error

import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.schabi.newpipe.R
import org.schabi.newpipe.extractor.exceptions.AgeRestrictedContentException
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.GeographicRestrictionException
import org.schabi.newpipe.extractor.exceptions.PaidContentException
import org.schabi.newpipe.player.mediasource.FailedMediaSource

class ErrorInfoTest {

    private fun resId(message: ErrorInfo.Companion.ErrorMessage): Int {
        val field = ErrorInfo.Companion.ErrorMessage::class.java.getDeclaredField("stringRes")
        field.isAccessible = true
        return field.getInt(message)
    }

    // ///////////////////////////
    // getMessage nested unwrapping
    // ///////////////////////////

    @Test
    fun messageIsUnwrappedThroughIOExceptionChain() {
        val error = IOException(Exception(AgeRestrictedContentException("nope")))
        assertEquals(
            R.string.restricted_video_no_stream,
            resId(ErrorInfo.getMessage(error, UserAction.PLAY_STREAM, null))
        )
    }

    @Test
    fun messageIsUnwrappedThroughGenericException() {
        val error = Exception(IOException(GeographicRestrictionException("nope")))
        assertEquals(
            R.string.georestricted_content,
            resId(ErrorInfo.getMessage(error, UserAction.PLAY_STREAM, null))
        )
    }

    @Test
    fun messageForDirectContentNotAvailable() {
        val error = PaidContentException("paid")
        assertEquals(
            R.string.paid_content,
            resId(ErrorInfo.getMessage(error, UserAction.PLAY_STREAM, null))
        )
    }

    @Test
    fun messageForFailedMediaSourceExceptionUnwrapsCause() {
        val error = FailedMediaSource.StreamInfoLoadException(
            AgeRestrictedContentException("nope")
        )
        assertEquals(
            R.string.restricted_video_no_stream,
            resId(ErrorInfo.getMessage(error, UserAction.PLAY_STREAM, null))
        )
    }

    @Test
    fun messageForPlainExtractionExceptionDoesNotRecurseOnItself() {
        // findNestedExtractionException must skip the throwable itself
        val error = ExtractionException("extraction failed")
        assertEquals(
            R.string.parsing_error,
            resId(ErrorInfo.getMessage(error, UserAction.PLAY_STREAM, null))
        )
    }

    // ///////////////////////////
    // isReportable nested unwrapping
    // ///////////////////////////

    @Test
    fun surelyNotAvailableContentIsNotReportableWhenWrapped() {
        val wrapped = IOException(Exception(AgeRestrictedContentException("nope")))
        assertFalse(ErrorInfo.isReportable(wrapped))
    }

    @Test
    fun surelyNotAvailableContentIsNotReportableWhenWrappedTwice() {
        val wrapped = RuntimeException(IOException(GeographicRestrictionException("nope")))
        assertFalse(ErrorInfo.isReportable(wrapped))
    }

    @Test
    fun genericExceptionIsReportable() {
        assertTrue(ErrorInfo.isReportable(RuntimeException("boom")))
    }

    @Test
    fun genericExtractionExceptionIsReportable() {
        assertTrue(ErrorInfo.isReportable(ExtractionException("boom")))
    }

    // ///////////////////////////
    // isRetryable nested unwrapping
    // ///////////////////////////

    @Test
    fun surelyNotAvailableContentIsNotRetryableWhenWrapped() {
        val wrapped = IOException(Exception(AgeRestrictedContentException("nope")))
        assertFalse(ErrorInfo.isRetryable(wrapped))
    }

    @Test
    fun paidContentIsNotRetryableWhenWrapped() {
        val wrapped = Exception(PaidContentException("paid"))
        assertFalse(ErrorInfo.isRetryable(wrapped))
    }

    @Test
    fun genericExceptionIsRetryable() {
        assertTrue(ErrorInfo.isRetryable(RuntimeException("boom")))
    }
}
