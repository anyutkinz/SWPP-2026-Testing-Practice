package com.example.swpp.tododapp.tasks

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.example.swpp.tododapp.R
import com.example.swpp.tododapp.ServiceLocator
import com.example.swpp.tododapp.data.Task
import com.example.swpp.tododapp.data.source.FakeAndroidTestRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Supplementary screen-level test for the intent "the label is updated when a task is checked or
 * unchecked". setDueDateLabel is reached through the real list row, data binding and checkbox.
 * Maps to the TasksListBindings.spec.md edge case "same TextView bound again after an active task
 * with no due date is checked and then unchecked".
 */
@MediumTest
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class TasksFragmentDueDateTest {

    private lateinit var repository: FakeAndroidTestRepository

    @Before
    fun initRepository() {
        repository = FakeAndroidTestRepository()
        ServiceLocator.tasksRepository = repository
    }

    @After
    fun cleanupDb() = runTest {
        ServiceLocator.resetRepository()
    }

    /** The first row as read on the main thread: its bound task and what its label shows. */
    private class Row(val boundTask: Task?, val label: TextView, val text: String, val visibility: Int) {
        val isNoLabel: Boolean get() = text.isEmpty() || visibility != View.VISIBLE
    }

    private fun FragmentScenario<TasksFragment>.firstRow(): Row? {
        var row: Row? = null
        onFragment { fragment ->
            val list = fragment.requireView().findViewById<RecyclerView>(R.id.tasks_list)
            val holder = list.findViewHolderForAdapterPosition(0) as? TasksAdapter.ViewHolder
            if (holder != null && !list.hasPendingAdapterUpdates()) {
                val label = holder.binding.dueDateText
                row = Row(holder.binding.task, label, label.text.toString(), label.visibility)
            }
        }
        return row
    }

    /**
     * The list is diffed on a background thread that Espresso does not track, so wait (bounded)
     * until the first row has been re-bound to a task with [completed]. This waits on the data,
     * not on the label, so a wrong label still fails the assertion below instead of timing out.
     */
    private fun FragmentScenario<TasksFragment>.awaitRowBoundTo(completed: Boolean): Row {
        val deadline = SystemClock.uptimeMillis() + TIMEOUT_MS
        while (true) {
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            val row = firstRow()
            if (row?.boundTask?.isCompleted == completed) return row
            check(SystemClock.uptimeMillis() < deadline) {
                "Row was not re-bound to isCompleted=$completed within $TIMEOUT_MS ms"
            }
            SystemClock.sleep(POLL_MS)
        }
    }

    @Test
    fun tasksList_checkThenUncheckTaskWithoutDueDate_showsClearThenNoLabel() = runTest {
        // GIVEN - the task list with one active task that has no due date
        repository.saveTask(Task("TITLE1", "DESCRIPTION1", false, "id1", dueDate = null))
        val scenario = launchFragmentInContainer<TasksFragment>(Bundle(), R.style.AppTheme)
        val initial = scenario.awaitRowBoundTo(completed = false)
        assertTrue("initially no label, saw '${initial.text}'", initial.isNoLabel)

        // WHEN - the checkbox is checked
        onView(withId(R.id.complete_checkbox)).perform(click())
        val checked = scenario.awaitRowBoundTo(completed = true)
        // THEN - the row shows CLEAR
        assertEquals("CLEAR", checked.text)
        assertEquals(View.VISIBLE, checked.visibility)

        // WHEN - the checkbox is unchecked again
        onView(withId(R.id.complete_checkbox)).perform(click())
        val unchecked = scenario.awaitRowBoundTo(completed = false)
        Log.i(
            TAG, "label view reused across check/uncheck: " +
                "${initial.label === checked.label && checked.label === unchecked.label}"
        )
        // THEN - the row shows no label (a visible stale "CLEAR" fails)
        assertTrue(
            "expected no label after uncheck, saw '${unchecked.text}' " +
                "visibility=${unchecked.visibility}",
            unchecked.isNoLabel
        )
    }

    private companion object {
        const val TAG = "DueDateLabelTest"
        const val TIMEOUT_MS = 5_000L
        const val POLL_MS = 20L
    }
}
