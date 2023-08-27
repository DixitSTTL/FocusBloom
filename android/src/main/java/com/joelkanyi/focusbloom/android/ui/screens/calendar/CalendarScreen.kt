package com.joelkanyi.focusbloom.android.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.joelkanyi.focusbloom.android.R
import com.joelkanyi.focusbloom.android.component.BloomTopAppBar
import com.joelkanyi.focusbloom.android.domain.model.Task
import com.joelkanyi.focusbloom.android.ui.theme.FocusBloomTheme
import com.joelkanyi.focusbloom.samples.sampleTasks
import com.joelkanyi.horizontalcalendar.HorizontalCalendarView
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

@Destination
@Composable
fun CalendarScreen(
    navigator: DestinationsNavigator,
) {
    CalendarScreenContent()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreenContent() {
    Scaffold(
        topBar = {
            BloomTopAppBar(
                hasBackNavigation = false,
            ) {
                Text(text = stringResource(R.string.schedule))
            }
        },
    ) { paddingValues ->
        var selectedDay by remember {
            mutableStateOf(LocalDate.now())
        }
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(PaddingValues(horizontal = 16.dp)),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HorizontalCalendarView(
                modifier = Modifier.fillMaxWidth(),
                selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                unSelectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedCardColor = MaterialTheme.colorScheme.primary,
                unSelectedCardColor = MaterialTheme.colorScheme.surfaceVariant,
                onDayClick = { day ->
                    selectedDay = day.fullDate.localDate()
                    // Toast.makeText(context, day.toString(), Toast.LENGTH_SHORT).show()
                },
            )

            val todaysTasks =
                sampleTasks.filter { it.start.toLocalDate().dayOfMonth == selectedDay.dayOfMonth }
            if (todaysTasks.isNotEmpty()) {
                Schedule(
                    tasks = todaysTasks.sortedBy { it.start },
                )
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "No tasks for today",
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
    }
}

@Composable
fun BasicTask(
    positionedTask: PositionedTask,
    modifier: Modifier = Modifier,
) {
    val task = positionedTask.task
    val topRadius =
        if (positionedTask.splitType == SplitType.Start || positionedTask.splitType == SplitType.Both) 0.dp else 4.dp
    val bottomRadius =
        if (positionedTask.splitType == SplitType.End || positionedTask.splitType == SplitType.Both) 0.dp else 4.dp
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                top = 2.dp,
                end = 2.dp,
                bottom = if (positionedTask.splitType == SplitType.End) 0.dp else 2.dp,
            )
            .clipToBounds()
            .background(
                task.color,
                shape = RoundedCornerShape(
                    topStart = topRadius,
                    topEnd = topRadius,
                    bottomEnd = bottomRadius,
                    bottomStart = bottomRadius,
                ),
            )
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = "${task.start.format(TaskTimeFormatter)} - ${
                task.end.format(
                    TaskTimeFormatter,
                )
            }",
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )

        Text(
            text = task.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (task.description != null) {
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun BasicDayHeader(
    day: LocalDate,
    modifier: Modifier = Modifier,
) {
    Text(
        text = day.format(DayFormatter),
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
    )
}

@Composable
fun BasicSidebarLabel(
    time: LocalTime,
    modifier: Modifier = Modifier,
) {
    Text(
        text = time.format(HourFormatter),
        modifier = modifier
            .fillMaxHeight()
            .padding(4.dp),
    )
}

@Composable
fun ScheduleSidebar(
    hourHeight: Dp,
    modifier: Modifier = Modifier,
    minTime: LocalTime = LocalTime.MIN,
    maxTime: LocalTime = LocalTime.MAX,
    label: @Composable (time: LocalTime) -> Unit = { BasicSidebarLabel(time = it) },
) {
    val numMinutes = ChronoUnit.MINUTES.between(minTime, maxTime).toInt() + 1
    val numHours = numMinutes / 60
    val firstHour = minTime.truncatedTo(ChronoUnit.HOURS)
    val firstHourOffsetMinutes =
        if (firstHour == minTime) 0 else ChronoUnit.MINUTES.between(minTime, firstHour.plusHours(1))
    val firstHourOffset = hourHeight * (firstHourOffsetMinutes / 60f)
    val startTime = if (firstHour == minTime) firstHour else firstHour.plusHours(1)
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(firstHourOffset))
        repeat(numHours) { i ->
            Box(modifier = Modifier.height(hourHeight)) {
                label(startTime.plusHours(i.toLong()))
            }
        }
    }
}

@Composable
fun Schedule(
    tasks: List<Task>,
    modifier: Modifier = Modifier,
    taskContent: @Composable (positionedTask: PositionedTask) -> Unit = {
        BasicTask(
            positionedTask = it,
        )
    },
    dayHeader: @Composable (day: LocalDate) -> Unit = { BasicDayHeader(day = it) },
    timeLabel: @Composable (time: LocalTime) -> Unit = { BasicSidebarLabel(time = it) },
    minDate: LocalDate = tasks.minByOrNull(Task::start)?.start?.toLocalDate() ?: LocalDate.now(),
    maxDate: LocalDate = tasks.maxByOrNull(Task::end)?.end?.toLocalDate() ?: LocalDate.now(),
    minTime: LocalTime = LocalTime.MIN,
    maxTime: LocalTime = LocalTime.MAX,
    daySize: ScheduleSize = ScheduleSize.FixedSize(300.dp),
    hourSize: ScheduleSize = ScheduleSize.FixedSize(64.dp),
) {
    val numDays = 0 + 1
    val numMinutes = ChronoUnit.MINUTES.between(minTime, maxTime).toInt() + 1
    val numHours = numMinutes.toFloat() / 60f
    val verticalScrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    /**
     * Scroll to the closest task
     * */
    // LaunchedEffect(key1 = tasks, block = {
    when (hourSize) {
        is ScheduleSize.Adaptive -> {
            val task = tasks.minByOrNull { it.start }
            if (task != null) {
                val taskStartMinutes = ChronoUnit.MINUTES.between(minTime, task.start)
                val taskStartHours = taskStartMinutes / 60f
                val taskStartOffset = taskStartHours * hourSize.minSize.dpToPx()
                LaunchedEffect(key1 = tasks, block = {
                    scope.launch {
                        verticalScrollState.animateScrollTo(taskStartOffset.roundToInt())
                    }
                })
            }
        }

        is ScheduleSize.FixedCount -> {
            val task = tasks.minByOrNull { it.start }
            if (task != null) {
                val taskStartMinutes = ChronoUnit.MINUTES.between(minTime, task.start)
                val taskStartHours = taskStartMinutes / 60f
                val taskStartOffset = taskStartHours * hourSize.count.dp
                LaunchedEffect(key1 = tasks, block = {
                    scope.launch {
                        verticalScrollState.animateScrollTo(taskStartOffset.value.roundToInt())
                    }
                })
            }
        }
        is ScheduleSize.FixedSize -> {
            // Scroll to the closest task
            val task = tasks.minByOrNull { it.start }
            if (task != null) {
                val taskStartMinutes = ChronoUnit.MINUTES.between(minTime, task.start)
                val taskStartHours = taskStartMinutes / 60f
                val taskStartOffset = taskStartHours * hourSize.size.dpToPx()
                LaunchedEffect(key1 = tasks, block = {
                    scope.launch {
                        verticalScrollState.animateScrollTo(taskStartOffset.roundToInt())
                    }
                })
            }
        }
    }
    // })

    val horizontalScrollState = rememberScrollState()
    var sidebarWidth by remember { mutableStateOf(0) }
    // var headerHeight by remember { mutableStateOf(0) }
    BoxWithConstraints(modifier = modifier) {
        val dayWidth: Dp = when (daySize) {
            is ScheduleSize.FixedSize -> daySize.size
            is ScheduleSize.FixedCount -> with(LocalDensity.current) { ((constraints.maxWidth - sidebarWidth) / daySize.count).toDp() }
            is ScheduleSize.Adaptive -> with(LocalDensity.current) {
                maxOf(
                    ((constraints.maxWidth - sidebarWidth) / numDays).toDp(),
                    daySize.minSize,
                )
            }
        }
        val hourHeight: Dp = when (hourSize) {
            is ScheduleSize.FixedSize -> hourSize.size
            is ScheduleSize.FixedCount -> with(LocalDensity.current) { ((constraints.maxHeight) / hourSize.count).toDp() }
            is ScheduleSize.Adaptive -> with(LocalDensity.current) {
                maxOf(
                    ((constraints.maxHeight) / numHours).toDp(),
                    hourSize.minSize,
                )
            }
        }
        Column(modifier = modifier) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.Start),
            ) {
                ScheduleSidebar(
                    hourHeight = hourHeight,
                    minTime = minTime,
                    maxTime = maxTime,
                    label = timeLabel,
                    modifier = Modifier
                        .verticalScroll(verticalScrollState)
                        .onGloballyPositioned { sidebarWidth = it.size.width },
                )
                BasicSchedule(
                    tasks = tasks,
                    taskContent = taskContent,
                    minDate = minDate,
                    maxDate = maxDate,
                    minTime = minTime,
                    maxTime = maxTime,
                    dayWidth = dayWidth,
                    hourHeight = hourHeight,
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(verticalScrollState)
                        .horizontalScroll(horizontalScrollState),
                )
            }
        }
    }
}

@Composable
fun BasicSchedule(
    tasks: List<Task>,
    modifier: Modifier = Modifier,
    taskContent: @Composable (positionedTask: PositionedTask) -> Unit = {
        BasicTask(
            positionedTask = it,
        )
    },
    minDate: LocalDate = tasks.minByOrNull(Task::start)?.start?.toLocalDate() ?: LocalDate.now(),
    maxDate: LocalDate = tasks.maxByOrNull(Task::end)?.end?.toLocalDate() ?: LocalDate.now(),
    minTime: LocalTime = LocalTime.MIN,
    maxTime: LocalTime = LocalTime.MAX,
    dayWidth: Dp,
    hourHeight: Dp,
) {
    val numDays = ChronoUnit.DAYS.between(minDate, maxDate).toInt() + 1
    val numMinutes = ChronoUnit.MINUTES.between(minTime, maxTime).toInt() + 1
    val numHours = numMinutes / 60
    val dividerColor =
        if (androidx.compose.material.MaterialTheme.colors.isLight) Color.LightGray else Color.DarkGray
    val positionedTasks =
        remember(tasks) { arrangeTasks(splitTasks(tasks.sortedBy(Task::start))).filter { it.end > minTime && it.start < maxTime } }
    Layout(
        content = {
            positionedTasks.forEach { positionedTask ->
                Box(modifier = Modifier.taskData(positionedTask)) {
                    taskContent(positionedTask)
                }
            }
        },
        modifier = modifier
            .offset(y = (16).dp)
            .drawBehind {
                repeat(23) {
                    drawLine(
                        dividerColor,
                        start = Offset(0f, (it + 1) * hourHeight.toPx()),
                        end = Offset(size.width, (it + 1) * hourHeight.toPx()),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
            },
    ) { measureables, constraints ->
        val height = (hourHeight.toPx() * (numMinutes / 60f)).roundToInt()
        val width = dayWidth.roundToPx() * numDays
        val placeablesWithTasks = measureables.map { measurable ->
            val splitTask = measurable.parentData as PositionedTask
            val taskDurationMinutes =
                ChronoUnit.MINUTES.between(splitTask.start, minOf(splitTask.end, maxTime))
            val taskHeight = ((taskDurationMinutes / 60f) * hourHeight.toPx()).roundToInt()
            val taskWidth =
                ((splitTask.colSpan.toFloat() / splitTask.colTotal.toFloat()) * dayWidth.toPx()).roundToInt()
            val placeable = measurable.measure(
                constraints.copy(
                    minWidth = taskWidth,
                    maxWidth = taskWidth,
                    minHeight = taskHeight,
                    maxHeight = taskHeight,
                ),
            )
            Pair(placeable, splitTask)
        }
        layout(width, height) {
            placeablesWithTasks.forEach { (placeable, splitTask) ->
                val taskOffsetMinutes = if (splitTask.start > minTime) {
                    ChronoUnit.MINUTES.between(
                        minTime,
                        splitTask.start,
                    )
                } else {
                    0
                }
                val taskY = ((taskOffsetMinutes / 60f) * hourHeight.toPx()).roundToInt()
                val taskX =
                    (splitTask.col * (dayWidth.toPx() / splitTask.colTotal.toFloat())).roundToInt()
                placeable.place(taskX, taskY)
            }
        }
    }
}

@Preview
@Composable
fun CalendarTimeBandPreview() {
    FocusBloomTheme {
        Column(
            modifier = Modifier.background(MaterialTheme.colorScheme.onPrimary),
        ) {
        }
    }
}

@Preview
@Composable
fun CalendarScreenContentPreview() {
    FocusBloomTheme {
        CalendarScreenContent()
    }
}

inline class SplitType private constructor(val value: Int) {
    companion object {
        val None = SplitType(0)
        val Start = SplitType(1)
        val End = SplitType(2)
        val Both = SplitType(3)
    }
}

data class PositionedTask(
    val task: Task,
    val splitType: SplitType,
    val date: LocalDate,
    val start: LocalTime,
    val end: LocalTime,
    val col: Int = 0,
    val colSpan: Int = 1,
    val colTotal: Int = 1,
)

val TaskTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")
val DayFormatter = DateTimeFormatter.ofPattern("EE, MMM d")

sealed class ScheduleSize {
    class FixedSize(val size: Dp) : ScheduleSize()
    class FixedCount(val count: Float) : ScheduleSize() {
        constructor(count: Int) : this(count.toFloat())
    }

    class Adaptive(val minSize: Dp) : ScheduleSize()
}

private class TaskDataModifier(
    val positionedTask: PositionedTask,
) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?) = positionedTask
}

private fun Modifier.taskData(positionedTask: PositionedTask) =
    this.then(TaskDataModifier(positionedTask))

private fun splitTasks(tasks: List<Task>): List<PositionedTask> {
    return tasks
        .map { task ->
            val startDate = task.start.toLocalDate()
            val endDate = task.end.toLocalDate()
            if (startDate == endDate) {
                listOf(
                    PositionedTask(
                        task,
                        SplitType.None,
                        task.start.toLocalDate(),
                        task.start.toLocalTime(),
                        task.end.toLocalTime(),
                    ),
                )
            } else {
                val days = ChronoUnit.DAYS.between(startDate, endDate)
                val splitTasks = mutableListOf<PositionedTask>()
                for (i in 0..days) {
                    val date = startDate.plusDays(i)
                    splitTasks += PositionedTask(
                        task,
                        splitType = if (date == startDate) SplitType.End else if (date == endDate) SplitType.Start else SplitType.Both,
                        date = date,
                        start = if (date == startDate) task.start.toLocalTime() else LocalTime.MIN,
                        end = if (date == endDate) task.end.toLocalTime() else LocalTime.MAX,
                    )
                }
                splitTasks
            }
        }
        .flatten()
}

private fun PositionedTask.overlapsWith(other: PositionedTask): Boolean {
    return date == other.date && start < other.end && end > other.start
}

private fun List<PositionedTask>.timesOverlapWith(task: PositionedTask): Boolean {
    return any { it.overlapsWith(task) }
}

private fun arrangeTasks(tasks: List<PositionedTask>): List<PositionedTask> {
    val positionedTasks = mutableListOf<PositionedTask>()
    val groupTasks: MutableList<MutableList<PositionedTask>> = mutableListOf()

    fun resetGroup() {
        groupTasks.forEachIndexed { colIndex, col ->
            col.forEach { e ->
                positionedTasks.add(e.copy(col = colIndex, colTotal = groupTasks.size))
            }
        }
        groupTasks.clear()
    }

    tasks.forEach { task ->
        var firstFreeCol = -1
        var numFreeCol = 0
        for (i in 0 until groupTasks.size) {
            val col = groupTasks[i]
            if (col.timesOverlapWith(task)) {
                if (firstFreeCol < 0) continue else break
            }
            if (firstFreeCol < 0) firstFreeCol = i
            numFreeCol++
        }

        when {
            // Overlaps with all, add a new column
            firstFreeCol < 0 -> {
                groupTasks += mutableListOf(task)
                // Expand anything that spans into the previous column and doesn't overlap with this task
                for (ci in 0 until groupTasks.size - 1) {
                    val col = groupTasks[ci]
                    col.forEachIndexed { ei, e ->
                        if (ci + e.colSpan == groupTasks.size - 1 && !e.overlapsWith(task)) {
                            col[ei] = e.copy(colSpan = e.colSpan + 1)
                        }
                    }
                }
            }
            // No overlap with any, start a new group
            numFreeCol == groupTasks.size -> {
                resetGroup()
                groupTasks += mutableListOf(task)
            }
            // At least one column free, add to first free column and expand to as many as possible
            else -> {
                groupTasks[firstFreeCol] += task.copy(colSpan = numFreeCol)
            }
        }
    }
    resetGroup()
    return positionedTasks
}

private val HourFormatter = DateTimeFormatter.ofPattern("h a")

fun String.localDate(): LocalDate {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    return LocalDate.parse(this, formatter)
}

@Composable
fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx() }

@Composable
fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }
