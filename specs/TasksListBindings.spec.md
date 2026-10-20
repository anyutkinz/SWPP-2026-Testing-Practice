# Spec: `TasksListBindings.kt`

> Post-conditions are the test oracle: write the expected behavior from intent, not from the code.
> Tag each post-condition [Unit] if its test runs on the JVM; otherwise tag it [Integration].
> End each valid case with `Tested by <test>` if an existing test checks it, or `Tip:` describing the test to write.

## `@BindingAdapter("app:dueDateLabel") fun setDueDateLabel(textView: TextView, task: Task?)`

**Intent**: Show the D-day label of `task` for the device's current date (`D-N`, `D-Day`, `D+N` or `CLEAR`) in the row's due-date `TextView`, and show no label when the task has none. A list row is bound again when its task is checked or unchecked, so each call must fully replace whatever an earlier call showed in the same `TextView`.

**Pre-conditions**
- Called on the main thread with a `TextView` created from a valid `Context` (in the app: `due_date_text` in `task_item.xml`).
- `task` may be `null` (the row has no task bound yet).
- The reference date ("today") is the Android device's current local date, which the function reads itself with `LocalDate.now()`; it cannot be fixed without changing production code. Each Tip therefore builds its due date with the recipe `today = LocalDate.now()` (read on the device under test, not on the host) and `dueDate = today.plusDays(k)` for a stated offset `k`. The expected label depends only on `k`, so the literal expected string is exact on every run date. Examples for reference date 2026-10-20: k = +3 → dueDate 2026-10-23 → `"D-3"`; k = +42 → dueDate 2026-12-01 → `"D-42"`; k = −3 → dueDate 2026-10-17 → `"D+3"`.
- The test's reference date equals the date the function reads. A test reads the device date before building its fixture and again after the last call; if the two differ (the device clock passed midnight), that attempt is a test-environment race: it is discarded and repeated with regenerated fixtures, and its observations are never asserted.

**Post-conditions**
- Success cases
  - Active task due in N days: the `TextView` shows the label, i.e. `textView.text.toString()` is exactly `"D-N"` and `textView.visibility` is `View.VISIBLE`. Every "shows X" below means exactly this: text equal to X and `View.VISIBLE`. `[Integration]`
    - Tip: Given a new TextView and Task(title = "Task", isCompleted = false, dueDate = today.plusDays(3)) (e.g. 2026-10-20 → 2026-10-23) / When setDueDateLabel(textView, task) is called / Then textView.text is "D-3" and textView.visibility is View.VISIBLE.
  - Active task due today: the `TextView` shows `"D-Day"`. `[Integration]`
    - Tip: Given a new TextView and Task(title = "Task", isCompleted = false, dueDate = today.plusDays(0)) (e.g. 2026-10-20 → 2026-10-20) / When setDueDateLabel(textView, task) is called / Then textView.text is "D-Day" and textView.visibility is View.VISIBLE.
  - Active task overdue by N days: the `TextView` shows `"D+N"`. `[Integration]`
    - Tip: Given a new TextView and Task(title = "Task", isCompleted = false, dueDate = today.plusDays(-3)) (e.g. 2026-10-20 → 2026-10-17) / When setDueDateLabel(textView, task) is called / Then textView.text is "D+3" and textView.visibility is View.VISIBLE.
  - Completed task with a due date: the `TextView` shows `"CLEAR"`. `[Integration]`
    - Tip: Given a new TextView and Task(title = "Task", isCompleted = true, dueDate = today.plusDays(3)) (e.g. 2026-10-20 → 2026-10-23) / When setDueDateLabel(textView, task) is called / Then textView.text is "CLEAR" and textView.visibility is View.VISIBLE.
  - Completed task with no due date: the `TextView` shows `"CLEAR"`. `[Integration]`
    - Tip: Given a new TextView and Task(title = "Task", isCompleted = true, dueDate = null) / When setDueDateLabel(textView, task) is called / Then textView.text is "CLEAR" and textView.visibility is View.VISIBLE.
  - Active task with no due date: the `TextView` shows no label, i.e. `textView.text.toString().isEmpty() || textView.visibility != View.VISIBLE` is true. Every "shows no label" below means exactly this; a visible non-empty label such as a stale `"CLEAR"` is not "no label". `[Integration]`
    - Tip: Given a new TextView and Task(title = "Task", isCompleted = false, dueDate = null) / When setDueDateLabel(textView, task) is called / Then textView.text.toString().isEmpty() || textView.visibility != View.VISIBLE is true.
  - No task (`null`): the `TextView` shows no label. `[Integration]`
    - Tip: Given a new TextView / When setDueDateLabel(textView, null) is called / Then textView.text.toString().isEmpty() || textView.visibility != View.VISIBLE is true.
  - Active task due more than one month ahead: N counts every calendar day, so the label matches the total number of days. 42 days always span more than one calendar month, so `"D-42"` separates the total day count from a day-of-month difference on every run date. `[Integration]`
    - Tip: Given a new TextView and Task(title = "Task", isCompleted = false, dueDate = today.plusDays(42)) (e.g. 2026-10-20 → 2026-12-01) / When setDueDateLabel(textView, task) is called / Then textView.text is "D-42" and textView.visibility is View.VISIBLE.
- Error cases
  - None. Every `Task`, with or without a due date, and `null` are valid input.
- Edge cases
  - Boundary between overdue, due today and due in the future (k = −1, 0 and +1). `[Integration]`
    - Tip: Given a new TextView and Task(title = "Task", isCompleted = false, dueDate = today.plusDays(-1)) (e.g. 2026-10-20 → 2026-10-19) / When setDueDateLabel(textView, task) is called / Then textView.text is "D+1" and textView.visibility is View.VISIBLE.
    - Tip: Given a new TextView and Task(title = "Task", isCompleted = false, dueDate = today.plusDays(0)) (e.g. 2026-10-20 → 2026-10-20) / When setDueDateLabel(textView, task) is called / Then textView.text is "D-Day" and textView.visibility is View.VISIBLE.
    - Tip: Given a new TextView and Task(title = "Task", isCompleted = false, dueDate = today.plusDays(1)) (e.g. 2026-10-20 → 2026-10-21) / When setDueDateLabel(textView, task) is called / Then textView.text is "D-1" and textView.visibility is View.VISIBLE.
  - The same `TextView` is bound again after an active task with no due date is checked and then unchecked: the last call shows no label. `[Integration]`
    - Tip: Given one TextView and Task(title = "Task", isCompleted = false, dueDate = null, id = "t1") / When setDueDateLabel is called with that task, then with its copy(isCompleted = true), then with its copy(isCompleted = false) / Then after the second call textView.text is "CLEAR" and textView.visibility is View.VISIBLE, and after the third call textView.text.toString().isEmpty() || textView.visibility != View.VISIBLE is true.
  - The same `TextView` is bound again after an active task with a due date is checked and then unchecked: the last call shows the D-day label again. `[Integration]`
    - Tip: Given one TextView and Task(title = "Task", isCompleted = false, dueDate = today.plusDays(3), id = "t1") (e.g. 2026-10-20 → 2026-10-23) / When setDueDateLabel is called with that task, then with its copy(isCompleted = true), then with its copy(isCompleted = false) / Then textView.text is "D-3", then "CLEAR", then "D-3", and textView.visibility is View.VISIBLE after each call.
  - A `TextView` that shows a label is reused for a task that has no label: it shows no label. `[Integration]`
    - Tip: Given one TextView already bound to Task(title = "A", isCompleted = false, dueDate = today.plusDays(3)) (e.g. 2026-10-20 → 2026-10-23) so it shows "D-3" / When setDueDateLabel(textView, Task(title = "B", isCompleted = false, dueDate = null)) is called / Then textView.text.toString().isEmpty() || textView.visibility != View.VISIBLE is true.
  - A `TextView` that shows `"CLEAR"` is bound to `null`: it shows no label. `[Integration]`
    - Tip: Given one TextView already bound to Task(title = "Task", isCompleted = true, dueDate = null) so it shows "CLEAR" / When setDueDateLabel(textView, null) is called / Then textView.text.toString().isEmpty() || textView.visibility != View.VISIBLE is true.
  - A hidden `TextView` gets a task that has a label: it shows the label. `[Integration]`
    - Tip: Given a new TextView with visibility View.GONE and Task(title = "Task", isCompleted = false, dueDate = today.plusDays(3)) (e.g. 2026-10-20 → 2026-10-23) / When setDueDateLabel(textView, task) is called / Then textView.text is "D-3" and textView.visibility is View.VISIBLE.

**Undefined behavior** (optional)
- None. Every input has a defined result; a device date change during a test is handled as a test-environment race (see Pre-conditions), not as application behavior.
