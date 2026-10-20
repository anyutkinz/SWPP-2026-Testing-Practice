package com.example.swpp.tododapp.tasks

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * Tests for [dueDateLabel], one per Tip in specs/DueDateUtils.spec.md.
 * Every date is fixed, and every expected label was computed independently of java.time.Period.
 */
class DueDateUtilsTest {

    private fun date(text: String): LocalDate = LocalDate.parse(text)

    // ---------- Success cases ----------

    @Test
    fun dueDateLabel_activeNoDueDate_returnsEmpty() {
        // GIVEN - an active task with no due date
        val today = date("2026-10-20")
        // WHEN
        val label = dueDateLabel(null, today, isCompleted = false)
        // THEN - no label
        assertEquals("", label)
    }

    @Test
    fun dueDateLabel_activeDueIn5Days_returnsDMinus5() {
        // GIVEN - due 2026-10-25, today 2026-10-20
        val dueDate = date("2026-10-25")
        // WHEN
        val label = dueDateLabel(dueDate, date("2026-10-20"), isCompleted = false)
        // THEN
        assertEquals("D-5", label)
    }

    @Test
    fun dueDateLabel_activeDueToday_returnsDDay() {
        // GIVEN - due today
        val today = date("2026-10-20")
        // WHEN
        val label = dueDateLabel(date("2026-10-20"), today, isCompleted = false)
        // THEN
        assertEquals("D-Day", label)
    }

    @Test
    fun dueDateLabel_activeOverdueBy5Days_returnsDPlus5() {
        // GIVEN - due 2026-10-15, today 2026-10-20
        val dueDate = date("2026-10-15")
        // WHEN
        val label = dueDateLabel(dueDate, date("2026-10-20"), isCompleted = false)
        // THEN
        assertEquals("D+5", label)
    }

    @Test
    fun dueDateLabel_completedFutureDueDate_returnsClear() {
        // GIVEN - a completed task due in the future
        val dueDate = date("2026-10-25")
        // WHEN
        val label = dueDateLabel(dueDate, date("2026-10-20"), isCompleted = true)
        // THEN
        assertEquals("CLEAR", label)
    }

    @Test
    fun dueDateLabel_completedOverdueDueDate_returnsClear() {
        // GIVEN - a completed task whose due date has passed
        val dueDate = date("2026-10-15")
        // WHEN
        val label = dueDateLabel(dueDate, date("2026-10-20"), isCompleted = true)
        // THEN
        assertEquals("CLEAR", label)
    }

    @Test
    fun dueDateLabel_completedNoDueDate_returnsClear() {
        // GIVEN - a completed task with no due date
        val today = date("2026-10-20")
        // WHEN
        val label = dueDateLabel(null, today, isCompleted = true)
        // THEN
        assertEquals("CLEAR", label)
    }

    @Test
    fun dueDateLabel_activeDue42DaysAhead_returnsDMinus42() {
        // GIVEN - 2026-10-20 -> 2026-12-01 is 42 days (1 month and 11 days)
        val dueDate = date("2026-12-01")
        // WHEN
        val label = dueDateLabel(dueDate, date("2026-10-20"), isCompleted = false)
        // THEN - all calendar days are counted
        assertEquals("D-42", label)
    }

    @Test
    fun dueDateLabel_activeOverdueBy49Days_returnsDPlus49() {
        // GIVEN - 2026-09-01 -> 2026-10-20 is 49 days (1 month and 19 days)
        val dueDate = date("2026-09-01")
        // WHEN
        val label = dueDateLabel(dueDate, date("2026-10-20"), isCompleted = false)
        // THEN
        assertEquals("D+49", label)
    }

    @Test
    fun dueDateLabel_activeDue52DaysAheadAcrossYearEnd_returnsDMinus52() {
        // GIVEN - 2026-12-20 -> 2027-02-10 is 52 days
        val dueDate = date("2027-02-10")
        // WHEN
        val label = dueDateLabel(dueDate, date("2026-12-20"), isCompleted = false)
        // THEN
        assertEquals("D-52", label)
    }

    @Test
    fun dueDateLabel_activeOverdueBy51DaysAcrossYearEnd_returnsDPlus51() {
        // GIVEN - 2026-11-20 -> 2027-01-10 is 51 days
        val dueDate = date("2026-11-20")
        // WHEN
        val label = dueDateLabel(dueDate, date("2027-01-10"), isCompleted = false)
        // THEN
        assertEquals("D+51", label)
    }

    @Test
    fun dueDateLabel_activeDueExactlyOneYearAhead_returnsDMinus365() {
        // GIVEN - 2026-10-20 -> 2027-10-20 is 365 days (no Feb 29 in between)
        val dueDate = date("2027-10-20")
        // WHEN
        val label = dueDateLabel(dueDate, date("2026-10-20"), isCompleted = false)
        // THEN
        assertEquals("D-365", label)
    }

    // ---------- Edge cases: overdue / today / future ----------

    @Test
    fun dueDateLabel_activeDueYesterday_returnsDPlus1() {
        // GIVEN - due one day before today
        val dueDate = date("2026-10-19")
        // WHEN
        val label = dueDateLabel(dueDate, date("2026-10-20"), isCompleted = false)
        // THEN
        assertEquals("D+1", label)
    }

    @Test
    fun dueDateLabel_activeDueOnTodayBoundary_returnsDDay() {
        // GIVEN - due exactly today (the boundary itself)
        val dueDate = date("2026-10-20")
        // WHEN
        val label = dueDateLabel(dueDate, date("2026-10-20"), isCompleted = false)
        // THEN
        assertEquals("D-Day", label)
    }

    @Test
    fun dueDateLabel_activeDueTomorrow_returnsDMinus1() {
        // GIVEN - due one day after today
        val dueDate = date("2026-10-21")
        // WHEN
        val label = dueDateLabel(dueDate, date("2026-10-20"), isCompleted = false)
        // THEN
        assertEquals("D-1", label)
    }

    // ---------- Edge cases: one calendar month ahead ----------

    @Test
    fun dueDateLabel_activeDue30DaysAhead_returnsDMinus30() {
        // GIVEN - 2026-10-20 -> 2026-11-19
        val dueDate = date("2026-11-19")
        // WHEN
        val label = dueDateLabel(dueDate, date("2026-10-20"), isCompleted = false)
        // THEN
        assertEquals("D-30", label)
    }

    @Test
    fun dueDateLabel_activeDue31DaysAhead_returnsDMinus31() {
        // GIVEN - 2026-10-20 -> 2026-11-20, exactly one calendar month
        val dueDate = date("2026-11-20")
        // WHEN
        val label = dueDateLabel(dueDate, date("2026-10-20"), isCompleted = false)
        // THEN
        assertEquals("D-31", label)
    }

    @Test
    fun dueDateLabel_activeDue32DaysAhead_returnsDMinus32() {
        // GIVEN - 2026-10-20 -> 2026-11-21
        val dueDate = date("2026-11-21")
        // WHEN
        val label = dueDateLabel(dueDate, date("2026-10-20"), isCompleted = false)
        // THEN
        assertEquals("D-32", label)
    }

    // ---------- Edge cases: one calendar month overdue ----------

    @Test
    fun dueDateLabel_activeOverdueBy29Days_returnsDPlus29() {
        // GIVEN - 2026-09-21 -> 2026-10-20
        val dueDate = date("2026-09-21")
        // WHEN
        val label = dueDateLabel(dueDate, date("2026-10-20"), isCompleted = false)
        // THEN
        assertEquals("D+29", label)
    }

    @Test
    fun dueDateLabel_activeOverdueBy30Days_returnsDPlus30() {
        // GIVEN - 2026-09-20 -> 2026-10-20, exactly one calendar month
        val dueDate = date("2026-09-20")
        // WHEN
        val label = dueDateLabel(dueDate, date("2026-10-20"), isCompleted = false)
        // THEN
        assertEquals("D+30", label)
    }

    @Test
    fun dueDateLabel_activeOverdueBy31Days_returnsDPlus31() {
        // GIVEN - 2026-09-19 -> 2026-10-20
        val dueDate = date("2026-09-19")
        // WHEN
        val label = dueDateLabel(dueDate, date("2026-10-20"), isCompleted = false)
        // THEN
        assertEquals("D+31", label)
    }

    // ---------- Edge cases: month end ----------

    @Test
    fun dueDateLabel_dueDayBeforeMonthEnd_returnsDPlus1() {
        // GIVEN - today is the last day of October
        val dueDate = date("2026-10-30")
        // WHEN
        val label = dueDateLabel(dueDate, date("2026-10-31"), isCompleted = false)
        // THEN
        assertEquals("D+1", label)
    }

    @Test
    fun dueDateLabel_dueOnMonthEnd_returnsDDay() {
        // GIVEN - due on the last day of October, which is today
        val dueDate = date("2026-10-31")
        // WHEN
        val label = dueDateLabel(dueDate, date("2026-10-31"), isCompleted = false)
        // THEN
        assertEquals("D-Day", label)
    }

    @Test
    fun dueDateLabel_dueDayAfterMonthEnd_returnsDMinus1() {
        // GIVEN - due on the first day of the next month
        val dueDate = date("2026-11-01")
        // WHEN
        val label = dueDateLabel(dueDate, date("2026-10-31"), isCompleted = false)
        // THEN
        assertEquals("D-1", label)
    }

    // ---------- Edge cases: year end ----------

    @Test
    fun dueDateLabel_dueDayBeforeYearEnd_returnsDPlus1() {
        // GIVEN - today is 2026-12-31, due the day before
        val dueDate = date("2026-12-30")
        // WHEN
        val label = dueDateLabel(dueDate, date("2026-12-31"), isCompleted = false)
        // THEN
        assertEquals("D+1", label)
    }

    @Test
    fun dueDateLabel_dueOnYearEnd_returnsDDay() {
        // GIVEN - due on 2026-12-31, which is today
        val dueDate = date("2026-12-31")
        // WHEN
        val label = dueDateLabel(dueDate, date("2026-12-31"), isCompleted = false)
        // THEN
        assertEquals("D-Day", label)
    }

    @Test
    fun dueDateLabel_dueNewYearsDayFromYearEnd_returnsDMinus1() {
        // GIVEN - today 2026-12-31, due 2027-01-01
        val dueDate = date("2027-01-01")
        // WHEN
        val label = dueDateLabel(dueDate, date("2026-12-31"), isCompleted = false)
        // THEN
        assertEquals("D-1", label)
    }

    @Test
    fun dueDateLabel_dueYearEndFromNewYearsDay_returnsDPlus1() {
        // GIVEN - today 2027-01-01, due 2026-12-31
        val dueDate = date("2026-12-31")
        // WHEN
        val label = dueDateLabel(dueDate, date("2027-01-01"), isCompleted = false)
        // THEN
        assertEquals("D+1", label)
    }

    // ---------- Edge cases: end of February ----------

    @Test
    fun dueDateLabel_dueLeapDayFromFeb28_returnsDMinus1() {
        // GIVEN - 2028 is a leap year
        val dueDate = date("2028-02-29")
        // WHEN
        val label = dueDateLabel(dueDate, date("2028-02-28"), isCompleted = false)
        // THEN
        assertEquals("D-1", label)
    }

    @Test
    fun dueDateLabel_dueMarch1FromFeb28InLeapYear_returnsDMinus2() {
        // GIVEN - 2028-02-28 -> 2028-03-01 passes Feb 29
        val dueDate = date("2028-03-01")
        // WHEN
        val label = dueDateLabel(dueDate, date("2028-02-28"), isCompleted = false)
        // THEN
        assertEquals("D-2", label)
    }

    @Test
    fun dueDateLabel_dueMarch1FromFeb28InCommonYear_returnsDMinus1() {
        // GIVEN - 2027 is a common year
        val dueDate = date("2027-03-01")
        // WHEN
        val label = dueDateLabel(dueDate, date("2027-02-28"), isCompleted = false)
        // THEN
        assertEquals("D-1", label)
    }

    @Test
    fun dueDateLabel_wholeFebruaryInLeapYear_returnsDMinus29() {
        // GIVEN - 2028-02-01 -> 2028-03-01 is 29 days
        val dueDate = date("2028-03-01")
        // WHEN
        val label = dueDateLabel(dueDate, date("2028-02-01"), isCompleted = false)
        // THEN
        assertEquals("D-29", label)
    }

    @Test
    fun dueDateLabel_wholeFebruaryInCommonYear_returnsDMinus28() {
        // GIVEN - 2027-02-01 -> 2027-03-01 is 28 days
        val dueDate = date("2027-03-01")
        // WHEN
        val label = dueDateLabel(dueDate, date("2027-02-01"), isCompleted = false)
        // THEN
        assertEquals("D-28", label)
    }

    // ---------- Edge cases: completion flag ----------

    @Test
    fun dueDateLabel_completedDueToday_returnsClear() {
        // GIVEN - a completed task due today (active would be "D-Day")
        val dueDate = date("2026-10-20")
        // WHEN
        val label = dueDateLabel(dueDate, date("2026-10-20"), isCompleted = true)
        // THEN - completion wins
        assertEquals("CLEAR", label)
    }
}
