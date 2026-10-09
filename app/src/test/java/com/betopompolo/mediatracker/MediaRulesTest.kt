package com.betopompolo.mediatracker

import com.betopompolo.mediatracker.model.MediaDraft
import com.betopompolo.mediatracker.model.MediaItem
import com.betopompolo.mediatracker.model.MediaKind
import com.betopompolo.mediatracker.model.MediaValidator
import org.junit.Assert.*
import org.junit.Test

class MediaRulesTest {
    @Test fun emptyFieldsAreRejected() {
        val errors = MediaValidator.validate(MediaDraft())
        assertNotNull(errors.title)
        assertNotNull(errors.totalUnits)
        assertNull(errors.year)
    }
    @Test fun optionalValuesAreConvertedToNull() {
        val item = MediaValidator.toItem("one", MediaDraft(title = "  Meu livro  ", totalUnits = "200"))
        assertEquals("Meu livro", item.title)
        assertNull(item.creator)
        assertNull(item.year)
        assertEquals(0, item.currentUnits)
    }
    @Test fun limitsAndIntegerOverflowAreRejected() {
        for (total in listOf("0", "-1", "100001", "9999999999999", "1.5", "texto")) {
            assertNotNull(total, MediaValidator.validate(MediaDraft("Livro", totalUnits = total)).totalUnits)
        }
        assertNotNull(MediaValidator.validate(MediaDraft("A", totalUnits = "1")).title)
        assertNotNull(MediaValidator.validate(MediaDraft("Livro", totalUnits = "1", year = "1499")).year)
    }
    @Test fun progressUsesPagesOrMinutesWithoutDivisionByZero() {
        val item = MediaItem("one", "Meu filme", MediaKind.MOVIE, totalUnits = 100, currentUnits = 25)
        assertEquals(25, item.progressPercent)
        assertEquals("Em andamento", item.status)
        assertEquals("Concluído", item.copy(currentUnits = 100).status)
        assertEquals("Quero começar", item.copy(currentUnits = 0).status)
    }
    @Test(expected = IllegalArgumentException::class) fun zeroTotalIsNotAValidModel() {
        MediaItem("one", "Livro", MediaKind.BOOK, totalUnits = 0)
    }
    @Test(expected = IllegalArgumentException::class) fun progressAboveTotalIsNotAValidModel() {
        MediaItem("one", "Livro", MediaKind.BOOK, totalUnits = 10, currentUnits = 11)
    }
}
