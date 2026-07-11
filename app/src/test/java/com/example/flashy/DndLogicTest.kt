package com.dsb.flashy

import com.dsb.flashy.advance.isWithinDND
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

/**
 * Unit tests for the DND schedule logic in isWithinDND().
 * These run on the JVM — no device or emulator required.
 *
 * Two windows are tested:
 *   Same-day   : start < end  (e.g. 09:00 → 18:00)
 *   Overnight  : start > end  (e.g. 22:00 → 07:00)
 */
class DndLogicTest {

    // ── Same-day window: 09:00 → 18:00 ─────────────────────────────────────

    @Test
    fun sameDayWindow_currentAtNoon_isInsideRange() {
        assertTrue(
            isWithinDND(
                current = LocalTime.of(12, 0),
                start   = LocalTime.of(9, 0),
                end     = LocalTime.of(18, 0)
            )
        )
    }

    @Test
    fun sameDayWindow_currentJustAfterStart_isInsideRange() {
        assertTrue(
            isWithinDND(
                current = LocalTime.of(9, 1),
                start   = LocalTime.of(9, 0),
                end     = LocalTime.of(18, 0)
            )
        )
    }

    @Test
    fun sameDayWindow_currentBeforeStart_isOutsideRange() {
        assertFalse(
            isWithinDND(
                current = LocalTime.of(8, 59),
                start   = LocalTime.of(9, 0),
                end     = LocalTime.of(18, 0)
            )
        )
    }

    @Test
    fun sameDayWindow_currentAfterEnd_isOutsideRange() {
        assertFalse(
            isWithinDND(
                current = LocalTime.of(18, 1),
                start   = LocalTime.of(9, 0),
                end     = LocalTime.of(18, 0)
            )
        )
    }

    @Test
    fun sameDayWindow_currentExactlyAtStart_isOutsideRange() {
        // isAfter() is exclusive — the boundary itself is not inside the window.
        assertFalse(
            isWithinDND(
                current = LocalTime.of(9, 0),
                start   = LocalTime.of(9, 0),
                end     = LocalTime.of(18, 0)
            )
        )
    }

    @Test
    fun sameDayWindow_currentExactlyAtEnd_isOutsideRange() {
        assertFalse(
            isWithinDND(
                current = LocalTime.of(18, 0),
                start   = LocalTime.of(9, 0),
                end     = LocalTime.of(18, 0)
            )
        )
    }

    // ── Overnight window: 22:00 → 07:00 ────────────────────────────────────

    @Test
    fun overnightWindow_currentAfterMidnight_isInsideRange() {
        // 02:00 is between 22:00 (prev day) and 07:00
        assertTrue(
            isWithinDND(
                current = LocalTime.of(2, 0),
                start   = LocalTime.of(22, 0),
                end     = LocalTime.of(7, 0)
            )
        )
    }

    @Test
    fun overnightWindow_currentBeforeMidnight_isInsideRange() {
        // 23:30 is after 22:00, still in the window
        assertTrue(
            isWithinDND(
                current = LocalTime.of(23, 30),
                start   = LocalTime.of(22, 0),
                end     = LocalTime.of(7, 0)
            )
        )
    }

    @Test
    fun overnightWindow_currentAtNoon_isOutsideRange() {
        assertFalse(
            isWithinDND(
                current = LocalTime.of(12, 0),
                start   = LocalTime.of(22, 0),
                end     = LocalTime.of(7, 0)
            )
        )
    }

    @Test
    fun overnightWindow_currentAt8am_isOutsideRange() {
        // 08:00 is after the 07:00 end time
        assertFalse(
            isWithinDND(
                current = LocalTime.of(8, 0),
                start   = LocalTime.of(22, 0),
                end     = LocalTime.of(7, 0)
            )
        )
    }

    @Test
    fun overnightWindow_currentExactlyAtStart_isOutsideRange() {
        assertFalse(
            isWithinDND(
                current = LocalTime.of(22, 0),
                start   = LocalTime.of(22, 0),
                end     = LocalTime.of(7, 0)
            )
        )
    }

    @Test
    fun overnightWindow_currentExactlyAtEnd_isOutsideRange() {
        assertFalse(
            isWithinDND(
                current = LocalTime.of(7, 0),
                start   = LocalTime.of(22, 0),
                end     = LocalTime.of(7, 0)
            )
        )
    }

    // ── Edge cases ──────────────────────────────────────────────────────────

    @Test
    fun overnightWindow_currentAtMidnight_isInsideRange() {
        // 00:00 is between 22:00 and 07:00
        assertTrue(
            isWithinDND(
                current = LocalTime.MIDNIGHT,
                start   = LocalTime.of(22, 0),
                end     = LocalTime.of(7, 0)
            )
        )
    }

    @Test
    fun defaultSchedule_22to07_noonIsNotInRange() {
        // Validates the app's default DND window doesn't block midday flashes
        assertFalse(
            isWithinDND(
                current = LocalTime.of(14, 0),
                start   = LocalTime.parse("22:00"),
                end     = LocalTime.parse("07:00")
            )
        )
    }
}
