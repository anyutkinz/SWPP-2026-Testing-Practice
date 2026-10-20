package com.example.swpp.tododapp.tasks

import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.example.swpp.tododapp.data.Task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Direct-call tests for [setDueDateLabel], one per Tip in specs/TasksListBindings.spec.md.
 *
 * setDueDateLabel reads LocalDate.now() itself, so fixtures use the device date as the reference
 * date and build due dates as offsets from it. If the device date changes during an attempt
 * (midnight), the attempt is discarded and repeated with fresh fixtures before anything is
 * asserted; assertion failures are never retried.
 */
@MediumTest
@RunWith(AndroidJUnit4::class)
class TasksListBindingsTest {

    /** What the TextView displayed after one call. */
    private data class Shown(val text: String, val visibility: Int) {
        val isNoLabel: Boolean get() = text.isEmpty() || visibility != View.VISIBLE
    }

    private fun TextView.snapshot() = Shown(text.toString(), visibility)

    private fun newTextView() = TextView(InstrumentationRegistry.getInstrumentation().targetContext)

    /**
     * Runs [actions] with the device date as `today`, and returns what they observed only if the
     * device date did not change while they ran. Fixtures must be built inside [actions].
     */
    private fun <T> onStableDeviceDate(testName: String, actions: (today: LocalDate) -> T): T {
        repeat(MAX_DATE_ATTEMPTS) { attempt ->
            val today = LocalDate.now()
            val observed = actions(today)
            val after = LocalDate.now()
            if (after == today) {
                Log.i(TAG, "$testName: referenceDate=$today attempt=${attempt + 1} observed=$observed")
                return observed
            }
            Log.w(TAG, "$testName: device date changed $today -> $after; discarding attempt ${attempt + 1}")
        }
        throw IllegalStateException("$testName: device date kept changing; no stable attempt")
    }

    private fun assertShows(expected: String, shown: Shown, context: String) {
        assertEquals("text ($context)", expected, shown.text)
        assertEquals("visibility ($context)", View.VISIBLE, shown.visibility)
    }

    private fun assertNoLabel(shown: Shown, context: String) {
        assertTrue("expected no label but saw $shown ($context)", shown.isNoLabel)
    }

    /** Binds an active task due `today + offset` to a new TextView. */
    private fun bindActiveDueIn(testName: String, offset: Long): Pair<LocalDate, Shown> =
        onStableDeviceDate(testName) { today ->
            val dueDate = today.plusDays(offset)
            val textView = newTextView()
            setDueDateLabel(textView, Task(title = "Task", isCompleted = false, dueDate = dueDate))
            Log.i(TAG, "$testName: offset=$offset dueDate=$dueDate")
            dueDate to textView.snapshot()
        }

    // ---------- Success cases ----------

    @UiThreadTest
    @Test
    fun setDueDateLabel_activeDueIn3Days_showsDMinus3() {
        // GIVEN an active task due today + 3 / WHEN bound to a new TextView
        val (dueDate, shown) = bindActiveDueIn("activeDueIn3Days", 3)
        // THEN
        assertShows("D-3", shown, "dueDate=$dueDate")
    }

    @UiThreadTest
    @Test
    fun setDueDateLabel_activeDueToday_showsDDay() {
        // GIVEN an active task due today / WHEN bound to a new TextView
        val (dueDate, shown) = bindActiveDueIn("activeDueToday", 0)
        // THEN
        assertShows("D-Day", shown, "dueDate=$dueDate")
    }

    @UiThreadTest
    @Test
    fun setDueDateLabel_activeOverdueBy3Days_showsDPlus3() {
        // GIVEN an active task due today - 3 / WHEN bound to a new TextView
        val (dueDate, shown) = bindActiveDueIn("activeOverdueBy3Days", -3)
        // THEN
        assertShows("D+3", shown, "dueDate=$dueDate")
    }

    @UiThreadTest
    @Test
    fun setDueDateLabel_completedWithDueDate_showsClear() {
        // GIVEN a completed task due today + 3
        val shown = onStableDeviceDate("completedWithDueDate") { today ->
            val textView = newTextView()
            // WHEN
            setDueDateLabel(textView, Task(title = "Task", isCompleted = true, dueDate = today.plusDays(3)))
            textView.snapshot()
        }
        // THEN
        assertShows("CLEAR", shown, "completed, dueDate=today+3")
    }

    @UiThreadTest
    @Test
    fun setDueDateLabel_completedNoDueDate_showsClear() {
        // GIVEN a completed task with no due date
        val textView = newTextView()
        // WHEN
        setDueDateLabel(textView, Task(title = "Task", isCompleted = true, dueDate = null))
        // THEN
        assertShows("CLEAR", textView.snapshot(), "completed, no due date")
    }

    @UiThreadTest
    @Test
    fun setDueDateLabel_activeNoDueDate_showsNoLabel() {
        // GIVEN an active task with no due date
        val textView = newTextView()
        // WHEN
        setDueDateLabel(textView, Task(title = "Task", isCompleted = false, dueDate = null))
        // THEN
        assertNoLabel(textView.snapshot(), "active, no due date")
    }

    @UiThreadTest
    @Test
    fun setDueDateLabel_nullTask_showsNoLabel() {
        // GIVEN a new TextView
        val textView = newTextView()
        // WHEN
        setDueDateLabel(textView, null)
        // THEN
        assertNoLabel(textView.snapshot(), "null task")
    }

    @UiThreadTest
    @Test
    fun setDueDateLabel_activeDue42DaysAhead_showsDMinus42() {
        // GIVEN an active task due today + 42 (always more than one calendar month)
        val (dueDate, shown) = bindActiveDueIn("activeDue42DaysAhead", 42)
        // THEN
        assertShows("D-42", shown, "dueDate=$dueDate")
    }

    // ---------- Edge cases: overdue / today / future ----------

    @UiThreadTest
    @Test
    fun setDueDateLabel_activeDueYesterday_showsDPlus1() {
        // GIVEN an active task due today - 1 / WHEN bound
        val (dueDate, shown) = bindActiveDueIn("activeDueYesterday", -1)
        // THEN
        assertShows("D+1", shown, "dueDate=$dueDate")
    }

    @UiThreadTest
    @Test
    fun setDueDateLabel_activeDueOnTodayBoundary_showsDDay() {
        // GIVEN an active task due today (the boundary itself) / WHEN bound
        val (dueDate, shown) = bindActiveDueIn("activeDueOnTodayBoundary", 0)
        // THEN
        assertShows("D-Day", shown, "dueDate=$dueDate")
    }

    @UiThreadTest
    @Test
    fun setDueDateLabel_activeDueTomorrow_showsDMinus1() {
        // GIVEN an active task due today + 1 / WHEN bound
        val (dueDate, shown) = bindActiveDueIn("activeDueTomorrow", 1)
        // THEN
        assertShows("D-1", shown, "dueDate=$dueDate")
    }

    // ---------- Edge cases: the same TextView bound again ----------

    @UiThreadTest
    @Test
    fun setDueDateLabel_noDueDateTaskCheckedThenUnchecked_showsClearThenNoLabel() {
        // GIVEN one TextView and an active task with no due date
        val textView = newTextView()
        val task = Task(title = "Task", isCompleted = false, id = "t1", dueDate = null)
        // WHEN the task is bound, checked, then unchecked on the same TextView
        setDueDateLabel(textView, task)
        setDueDateLabel(textView, task.copy(isCompleted = true))
        val afterCheck = textView.snapshot()
        setDueDateLabel(textView, task.copy(isCompleted = false))
        val afterUncheck = textView.snapshot()
        // THEN "CLEAR" while completed, and no label once active again
        assertShows("CLEAR", afterCheck, "after check")
        assertNoLabel(afterUncheck, "after uncheck")
    }

    @UiThreadTest
    @Test
    fun setDueDateLabel_dueDateTaskCheckedThenUnchecked_showsDMinus3ThenClearThenDMinus3() {
        // GIVEN one TextView and an active task due today + 3
        val shown = onStableDeviceDate("dueDateTaskCheckedThenUnchecked") { today ->
            val textView = newTextView()
            val task = Task(title = "Task", isCompleted = false, id = "t1", dueDate = today.plusDays(3))
            // WHEN the task is bound, checked, then unchecked on the same TextView
            setDueDateLabel(textView, task)
            val first = textView.snapshot()
            setDueDateLabel(textView, task.copy(isCompleted = true))
            val second = textView.snapshot()
            setDueDateLabel(textView, task.copy(isCompleted = false))
            listOf(first, second, textView.snapshot())
        }
        // THEN
        assertShows("D-3", shown[0], "first bind")
        assertShows("CLEAR", shown[1], "after check")
        assertShows("D-3", shown[2], "after uncheck")
    }

    @UiThreadTest
    @Test
    fun setDueDateLabel_labelledViewReusedForTaskWithoutLabel_showsNoLabel() {
        // GIVEN one TextView already showing "D-3" for task A
        val shown = onStableDeviceDate("labelledViewReusedForTaskWithoutLabel") { today ->
            val textView = newTextView()
            setDueDateLabel(textView, Task(title = "A", isCompleted = false, dueDate = today.plusDays(3)))
            val before = textView.snapshot()
            // WHEN it is bound to task B, which is active with no due date
            setDueDateLabel(textView, Task(title = "B", isCompleted = false, dueDate = null))
            before to textView.snapshot()
        }
        // THEN
        assertShows("D-3", shown.first, "precondition: bound to A")
        assertNoLabel(shown.second, "after binding B")
    }

    @UiThreadTest
    @Test
    fun setDueDateLabel_clearViewBoundToNull_showsNoLabel() {
        // GIVEN one TextView already showing "CLEAR"
        val textView = newTextView()
        setDueDateLabel(textView, Task(title = "Task", isCompleted = true, dueDate = null))
        assertShows("CLEAR", textView.snapshot(), "precondition: completed task bound")
        // WHEN it is bound to no task
        setDueDateLabel(textView, null)
        // THEN
        assertNoLabel(textView.snapshot(), "after binding null")
    }

    @UiThreadTest
    @Test
    fun setDueDateLabel_goneViewWithLabel_showsDMinus3Visible() {
        // GIVEN a hidden TextView and an active task due today + 3
        val shown = onStableDeviceDate("goneViewWithLabel") { today ->
            val textView = newTextView().apply { visibility = View.GONE }
            // WHEN
            setDueDateLabel(textView, Task(title = "Task", isCompleted = false, dueDate = today.plusDays(3)))
            textView.snapshot()
        }
        // THEN
        assertShows("D-3", shown, "view started GONE")
    }

    private companion object {
        const val TAG = "DueDateLabelTest"
        const val MAX_DATE_ATTEMPTS = 3
    }
}
