# Spec: `TasksListBindings.kt`

> Post-conditions are the test oracle: write the expected behavior from intent, not from the code.
> Tag each post-condition [Unit] if its test runs on the JVM; otherwise tag it [Integration].
> End each valid case with `Tested by <test>` if an existing test checks it, or `Tip:` describing the test to write.

## `@BindingAdapter("app:dueDateLabel") fun setDueDateLabel(textView: TextView, task: Task?)`

**Intent**: Show the D-day label of `task` for the device's current date (`D-N`, `D-Day`, `D+N` or `CLEAR`) in the row's due-date `TextView`, and show no label when the task has none. A list row is bound again when its task is checked or unchecked, so each call must fully replace whatever an earlier call showed in the same `TextView`.

**Pre-conditions**
- Called on the main thread with a `TextView` created from a valid `Context` (in the app: `due_date_text` in `task_item.xml`).
- `task` may be `null` (the row has no task bound yet).
- "Today" is the device's current local date (`LocalDate.now()`); a test builds its due dates from that same date and does not run across midnight.
- "Shows no label" means the `TextView` displays no text: its text is `""` or its visibility is not `View.VISIBLE`.

**Post-conditions**
- Success cases
  - Active task due in N days: the `TextView` shows `"D-N"` and is `View.VISIBLE`. `[Integration]`
    - Tip: Given a new TextView and Task(title = "Task", isCompleted = false, dueDate = LocalDate.now().plusDays(3)) / When setDueDateLabel(textView, task) is called / Then textView.text is "D-3" and textView.visibility is View.VISIBLE.
  - Active task due today: the `TextView` shows `"D-Day"` and is visible. `[Integration]`
    - Tip: Given a new TextView and Task(title = "Task", isCompleted = false, dueDate = LocalDate.now()) / When setDueDateLabel(textView, task) is called / Then textView.text is "D-Day" and textView.visibility is View.VISIBLE.
  - Active task overdue by N days: the `TextView` shows `"D+N"` and is visible. `[Integration]`
    - Tip: Given a new TextView and Task(title = "Task", isCompleted = false, dueDate = LocalDate.now().minusDays(3)) / When setDueDateLabel(textView, task) is called / Then textView.text is "D+3" and textView.visibility is View.VISIBLE.
  - Completed task with a due date: the `TextView` shows `"CLEAR"` and is visible. `[Integration]`
    - Tip: Given a new TextView and Task(title = "Task", isCompleted = true, dueDate = LocalDate.now().plusDays(3)) / When setDueDateLabel(textView, task) is called / Then textView.text is "CLEAR" and textView.visibility is View.VISIBLE.
  - Completed task with no due date: the `TextView` shows `"CLEAR"` and is visible. `[Integration]`
    - Tip: Given a new TextView and Task(title = "Task", isCompleted = true, dueDate = null) / When setDueDateLabel(textView, task) is called / Then textView.text is "CLEAR" and textView.visibility is View.VISIBLE.
  - Active task with no due date: the `TextView` shows no label. `[Integration]`
    - Tip: Given a new TextView and Task(title = "Task", isCompleted = false, dueDate = null) / When setDueDateLabel(textView, task) is called / Then textView.text is "" or textView.visibility is not View.VISIBLE.
  - No task (`null`): the `TextView` shows no label. `[Integration]`
    - Tip: Given a new TextView / When setDueDateLabel(textView, null) is called / Then textView.text is "" or textView.visibility is not View.VISIBLE.
  - Active task due more than one month ahead: N counts every calendar day, so the label matches the total number of days. `[Integration]`
    - Tip: Given a new TextView and Task(title = "Task", isCompleted = false, dueDate = LocalDate.now().plusDays(42)) / When setDueDateLabel(textView, task) is called / Then textView.text is "D-42".
- Error cases
  - None. Every `Task`, with or without a due date, and `null` are valid input.
- Edge cases
  - Boundary between overdue, due today and due in the future (one day on each side of today). `[Integration]`
    - Tip: Given a new TextView and Task(title = "Task", isCompleted = false, dueDate = LocalDate.now().minusDays(1)) / When setDueDateLabel(textView, task) is called / Then textView.text is "D+1".
    - Tip: Given a new TextView and Task(title = "Task", isCompleted = false, dueDate = LocalDate.now()) / When setDueDateLabel(textView, task) is called / Then textView.text is "D-Day".
    - Tip: Given a new TextView and Task(title = "Task", isCompleted = false, dueDate = LocalDate.now().plusDays(1)) / When setDueDateLabel(textView, task) is called / Then textView.text is "D-1".
  - The same `TextView` is bound again after an active task with no due date is checked and then unchecked. `[Integration]`
    - Tip: Given one TextView and Task(title = "Task", isCompleted = false, dueDate = null, id = "t1") / When setDueDateLabel is called with that task, then with its copy(isCompleted = true), then with its copy(isCompleted = false) / Then after the second call textView.text is "CLEAR", and after the third call textView.text is "" or textView.visibility is not View.VISIBLE.
  - The same `TextView` is bound again after an active task with a due date is checked and then unchecked. `[Integration]`
    - Tip: Given one TextView and Task(title = "Task", isCompleted = false, dueDate = LocalDate.now().plusDays(3), id = "t1") / When setDueDateLabel is called with that task, then with its copy(isCompleted = true), then with its copy(isCompleted = false) / Then textView.text is "D-3", then "CLEAR", then "D-3", and textView.visibility is View.VISIBLE after the third call.
  - A `TextView` that shows a label is reused for a task that has no label. `[Integration]`
    - Tip: Given one TextView already bound to Task(title = "A", isCompleted = false, dueDate = LocalDate.now().plusDays(3)) so it shows "D-3" / When setDueDateLabel(textView, Task(title = "B", isCompleted = false, dueDate = null)) is called / Then textView.text is "" or textView.visibility is not View.VISIBLE.
  - A `TextView` that shows `"CLEAR"` is bound to `null`. `[Integration]`
    - Tip: Given one TextView already bound to Task(title = "Task", isCompleted = true, dueDate = null) so it shows "CLEAR" / When setDueDateLabel(textView, null) is called / Then textView.text is "" or textView.visibility is not View.VISIBLE.
  - A hidden `TextView` gets a task that has a label. `[Integration]`
    - Tip: Given a new TextView with visibility View.GONE and Task(title = "Task", isCompleted = false, dueDate = LocalDate.now().plusDays(3)) / When setDueDateLabel(textView, task) is called / Then textView.text is "D-3" and textView.visibility is View.VISIBLE.

**Undefined behavior** (optional)
- A call that runs across midnight: `LocalDate.now()` can change between building the test's due date and the call, so the label may be off by one day. Tests must not depend on that moment.
