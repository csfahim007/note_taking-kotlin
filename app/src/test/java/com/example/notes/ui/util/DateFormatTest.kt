package com.example.notes.ui.util

import org.junit.Assert.assertTrue
import org.junit.Test

class DateFormatTest {

    @Test
    fun toDisplayDateFormatsEpochMilliToNonEmptyString() {
        val millis = 1700000000000L // Nov 14, 2023
        val formatted = millis.toDisplayDate()
        assertTrue(formatted.isNotEmpty())
        assertTrue(formatted.contains("2023"))
    }
}
