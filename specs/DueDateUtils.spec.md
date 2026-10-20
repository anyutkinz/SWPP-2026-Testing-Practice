# Spec: `DueDateUtils.kt`

> Post-conditions are the test oracle: write the expected behavior from intent, not from the code.
> Tag each post-condition [Unit] if its test runs on the JVM; otherwise tag it [Integration].
> End each valid case with `Tested by <test>` if an existing test checks it, or `Tip:` describing the test to write.

## `internal fun dueDateLabel(dueDate: LocalDate?, today: LocalDate, isCompleted: Boolean): String`

**Intent**: Build the D-day label shown next to a task in the task list. A completed task gets `"CLEAR"` with or without a due date; an active task with no due date gets no label (`""`); an active task with a due date gets `"D-N"` when it is due in N days, `"D-Day"` when it is due today and `"D+N"` when it is overdue by N days, where N is the total number of calendar days between `today` and `dueDate`.

**Pre-conditions**
- `today` is any `LocalDate`; it is the reference date the label is computed against. The app passes the device's current date, but the function never reads the clock itself, so any fixed date (for example 2028-02-28) is valid input.
- `dueDate` is the task's due date, or `null` when the task has no due date.
- `isCompleted` is `true` for a completed task and `false` for an active task.

**Post-conditions**
- Success cases
  - Active task with no due date: returns `""` (no label). `[Unit]`
    - Tip: Given dueDate = null, today = 2026-10-20, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "".
  - Active task due in N days: returns `"D-N"`, with N written as a plain decimal number (no padding, no sign other than the `-`). `[Unit]`
    - Tip: Given dueDate = 2026-10-25, today = 2026-10-20, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D-5".
  - Active task due today: returns `"D-Day"`. `[Unit]`
    - Tip: Given dueDate = 2026-10-20, today = 2026-10-20, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D-Day".
  - Active task overdue by N days: returns `"D+N"`. `[Unit]`
    - Tip: Given dueDate = 2026-10-15, today = 2026-10-20, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D+5".
  - Completed task with a future due date: returns `"CLEAR"`, not `"D-N"`. `[Unit]`
    - Tip: Given dueDate = 2026-10-25, today = 2026-10-20, isCompleted = true / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "CLEAR".
  - Completed task with an overdue due date: returns `"CLEAR"`, not `"D+N"`. `[Unit]`
    - Tip: Given dueDate = 2026-10-15, today = 2026-10-20, isCompleted = true / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "CLEAR".
  - Completed task with no due date: returns `"CLEAR"`. `[Unit]`
    - Tip: Given dueDate = null, today = 2026-10-20, isCompleted = true / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "CLEAR".
  - Active task due more than one month ahead: N counts every calendar day in between, not only the difference of the day-of-month. `[Unit]`
    - Tip: Given dueDate = 2026-12-01, today = 2026-10-20, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D-42".
  - Active task overdue by more than one month: N counts every calendar day in between. `[Unit]`
    - Tip: Given dueDate = 2026-09-01, today = 2026-10-20, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D+49".
  - Active task due across a year end and more than one month ahead. `[Unit]`
    - Tip: Given dueDate = 2027-02-10, today = 2026-12-20, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D-52".
  - Active task overdue across a year end by more than one month: N counts every calendar day in between. `[Unit]`
    - Tip: Given dueDate = 2026-11-20, today = 2027-01-10, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D+51".
  - Active task due exactly one year ahead. `[Unit]`
    - Tip: Given dueDate = 2027-10-20, today = 2026-10-20, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D-365".
- Error cases
  - None. Every combination of `dueDate` (including `null`), `today` and `isCompleted` is valid input and has a label defined above.
- Edge cases
  - Boundary between overdue, due today and due in the future (dueDate − today = −1, 0 and +1 day). `[Unit]`
    - Tip: Given dueDate = 2026-10-19, today = 2026-10-20, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D+1".
    - Tip: Given dueDate = 2026-10-20, today = 2026-10-20, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D-Day".
    - Tip: Given dueDate = 2026-10-21, today = 2026-10-20, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D-1".
  - Boundary of one calendar month ahead, where the day-of-month difference wraps around (30, 31 and 32 days ahead). `[Unit]`
    - Tip: Given dueDate = 2026-11-19, today = 2026-10-20, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D-30".
    - Tip: Given dueDate = 2026-11-20, today = 2026-10-20, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D-31".
    - Tip: Given dueDate = 2026-11-21, today = 2026-10-20, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D-32".
  - Boundary of one calendar month overdue (29, 30 and 31 days overdue). `[Unit]`
    - Tip: Given dueDate = 2026-09-21, today = 2026-10-20, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D+29".
    - Tip: Given dueDate = 2026-09-20, today = 2026-10-20, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D+30".
    - Tip: Given dueDate = 2026-09-19, today = 2026-10-20, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D+31".
  - Month end, one day on each side of the last day of the month. `[Unit]`
    - Tip: Given dueDate = 2026-10-30, today = 2026-10-31, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D+1".
    - Tip: Given dueDate = 2026-10-31, today = 2026-10-31, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D-Day".
    - Tip: Given dueDate = 2026-11-01, today = 2026-10-31, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D-1".
  - Year end: `today` is the last day of the year and the due date is one day before it, on it and one day after it; plus the reverse direction from the first day of the next year. `[Unit]`
    - Tip: Given dueDate = 2026-12-30, today = 2026-12-31, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D+1".
    - Tip: Given dueDate = 2026-12-31, today = 2026-12-31, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D-Day".
    - Tip: Given dueDate = 2027-01-01, today = 2026-12-31, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D-1".
    - Tip: Given dueDate = 2026-12-31, today = 2027-01-01, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D+1".
  - End of February in a leap year and in a common year. `[Unit]`
    - Tip: Given dueDate = 2028-02-29, today = 2028-02-28, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D-1".
    - Tip: Given dueDate = 2028-03-01, today = 2028-02-28, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D-2".
    - Tip: Given dueDate = 2027-03-01, today = 2027-02-28, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D-1".
  - Whole month of February in a leap year and in a common year. `[Unit]`
    - Tip: Given dueDate = 2028-03-01, today = 2028-02-01, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D-29".
    - Tip: Given dueDate = 2027-03-01, today = 2027-02-01, isCompleted = false / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "D-28".
  - Completion flag for a task due today (active and completed with the same dates). `[Unit]`
    - Tip: Given dueDate = 2026-10-20, today = 2026-10-20, isCompleted = true / When dueDateLabel(dueDate, today, isCompleted) is called / Then it returns "CLEAR".

**Undefined behavior** (optional)
- None. Every input has a defined label.
