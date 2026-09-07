package tw.boris4110799.composing.library.ui.components.gestures

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.yearMonth
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

/**
 * Create a picker state.
 * @param initialIndex The initial index of picker.
 */
@Composable
fun rememberPickerState(initialIndex: Int = 0) = remember { PickerState(initialIndex) }

/**
 * Picker state.
 * @param initialIndex The initial index of picker.
 */
class PickerState(initialIndex: Int) {
    private var _selectedIndex by mutableIntStateOf(initialIndex)
    private var _selectedItem by mutableStateOf("")

    /**
     * The current selected index of picker.
     */
    val selectedIndex
        get() = _selectedIndex

    /**
     * The current selected item of picker.
     */
    val selectedItem
        get() = _selectedItem

    /**
     * Picker.
     *
     * Note: A picker state can only be used by one picker at a time. Multiple picker share the same state will cause the incorrect display.
     * @param items Item list.
     * @param visibleItemsCount Visible item count.
     * @param indicatorColor The color of the indicator.
     * @param textModifier The Modifier of the item text.
     * @param textSelectedColor The text color of the selected item.
     * @param textUnSelectedColor The text color of the unselected item.
     * @param textStyle The text style of the item text.
     * @sample PickerSample
     */
    @Composable
    fun Picker(
        modifier: Modifier = Modifier,
        items: List<String>,
        visibleItemsCount: Int = 5,
        indicatorColor: Color = MaterialTheme.colorScheme.primaryContainer,
        textModifier: Modifier = Modifier,
        textSelectedColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
        textUnSelectedColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
        textStyle: TextStyle = MaterialTheme.typography.headlineLarge
    ) {
        val density = LocalDensity.current
        val listState = rememberLazyListState()
        val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

        /** The item count for padding at the top and bottom of the list. */
        val paddingCount = (visibleItemsCount - 1) / 2

        /** The total number of items in the list. */
        val listScrollCount = items.size + 2 * paddingCount

        /** Item height. */
        val itemHeightDp = with(density) { textStyle.lineHeight.toDp() }
        val lazyColumnPadding = if (visibleItemsCount % 2 == 0) itemHeightDp * 0.5f else 0.dp

        LaunchedEffect(items) {
            // If the selected item is in the list, scroll to it. Instead of tracking the original index.
            if (_selectedItem.isNotEmpty() && items.contains(_selectedItem)) {
                _selectedIndex = items.indexOf(_selectedItem)
                listState.requestScrollToItem(_selectedIndex)
            }

            snapshotFlow { listState.firstVisibleItemIndex }.distinctUntilChanged()
                .collectLatest { index ->
                    _selectedIndex = index
                    _selectedItem = items[index]
                }
        }

        LaunchedEffect(_selectedIndex) {
            listState.scrollToItem(_selectedIndex)
        }

        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier.fillMaxWidth().height(itemHeightDp).background(indicatorColor)
            )

            // -1.dp for making 'firstVisibleItemIndex' work properly.
            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(itemHeightDp * visibleItemsCount - 1.dp),
                state = listState,
                contentPadding = PaddingValues(vertical = lazyColumnPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                flingBehavior = flingBehavior
            ) {
                items(listScrollCount, contentType = { String }) { index ->
                    val realIndex = index - paddingCount

                    val text = if (realIndex in items.indices) {
                        items[realIndex]
                    } else {
                        ""
                    }

                    val color = if (realIndex == _selectedIndex) {
                        textSelectedColor
                    } else {
                        textUnSelectedColor
                    }

                    Text(
                        text = text,
                        modifier = Modifier.requiredHeight(itemHeightDp) then textModifier,
                        color = color,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = textStyle
                    )
                }
            }
        }
    }
}

/**
 * Year range.
 */
sealed class YearRange {
    /** The range from chosen [year] to today. */
    data class PastYearToNow(val year: Int) : YearRange()

    /** The range from past [range] of year to today. */
    data class PastRangeToNow(val range: Int) : YearRange()

    /** The range from chosen [startYear] to chosen [endYear]. */
    data class YearToYear(
        val startYear: Int,
        val endYear: Int
    ) : YearRange()

    /** The range of today to chosen [year]. */
    data class NowToFutureYear(val year: Int) : YearRange()

    /** The range of today to future [range] of year. */
    data class NowToFutureRange(val range: Int) : YearRange()
}

/**
 * Date Picker.
 * @param yearRange The year range of the picker.
 * @param onSelected The callback when the user selects a date.
 * @sample DatePickerSample
 */
@OptIn(FlowPreview::class)
@Composable
fun DatePicker(
    modifier: Modifier = Modifier,
    yearRange: YearRange = YearRange.PastRangeToNow(100),
    onSelected: (year: String, month: String, day: String) -> Unit
) {
    val currentDate =
        remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }

    val currentYearIndex = when (yearRange) {
        is YearRange.PastYearToNow    -> currentDate.year - yearRange.year
        is YearRange.PastRangeToNow   -> yearRange.range
        is YearRange.YearToYear       -> currentDate.year - yearRange.startYear
        is YearRange.NowToFutureYear  -> 0
        is YearRange.NowToFutureRange -> 0
    }
    val currentMonthIndex = when (yearRange) {
        is YearRange.PastYearToNow    -> currentDate.month.number - 1
        is YearRange.PastRangeToNow   -> currentDate.month.number - 1
        is YearRange.YearToYear       -> currentDate.month.number - 1
        is YearRange.NowToFutureYear  -> 0
        is YearRange.NowToFutureRange -> 0
    }
    val currentDayIndex = when (yearRange) {
        is YearRange.PastYearToNow    -> currentDate.day - 1
        is YearRange.PastRangeToNow   -> currentDate.day - 1
        is YearRange.YearToYear       -> currentDate.day - 1
        is YearRange.NowToFutureYear  -> 0
        is YearRange.NowToFutureRange -> 0
    }

    val yearState = rememberPickerState(initialIndex = currentYearIndex)
    val monthState = rememberPickerState(initialIndex = currentMonthIndex)
    val dayState = rememberPickerState(initialIndex = currentDayIndex)

    val year = remember {
        when (yearRange) {
            is YearRange.PastYearToNow    -> yearRange.year..currentDate.year
            is YearRange.PastRangeToNow   -> currentDate.year - yearRange.range..currentDate.year
            is YearRange.YearToYear       -> yearRange.startYear..yearRange.endYear
            is YearRange.NowToFutureYear  -> currentDate.year..yearRange.year
            is YearRange.NowToFutureRange -> currentDate.year..currentDate.year + yearRange.range
        }
    }
    var startMonth by remember { mutableIntStateOf(1) }
    var endMonth by remember { mutableIntStateOf(12) }
    var startDay by remember { mutableIntStateOf(1) }
    var endDay by remember { mutableIntStateOf(31) }

    LaunchedEffect(Unit) {
        snapshotFlow { yearState.selectedItem to monthState.selectedItem }.debounce(100L.milliseconds)
            .filter { it.first.isNotEmpty() && it.second.isNotEmpty() }
            .map { it.first.toInt() to it.second.toInt() }
            .distinctUntilChanged()
            .collect { (y, m) ->
                startMonth = when (y) {
                    year.first -> when (yearRange) {
                        is YearRange.PastRangeToNow   -> YearMonth(
                            y, currentDate.month
                        ).month.number

                        is YearRange.NowToFutureYear  -> currentDate.month.number
                        is YearRange.NowToFutureRange -> currentDate.month.number
                        else                          -> 1
                    }

                    else       -> 1
                }
                endMonth = when (y) {
                    year.last -> when (yearRange) {
                        is YearRange.PastYearToNow    -> currentDate.month.number
                        is YearRange.PastRangeToNow   -> currentDate.month.number
                        is YearRange.NowToFutureRange -> YearMonth(
                            y, currentDate.month
                        ).month.number

                        else                          -> 12
                    }

                    else      -> 12
                }
                startDay = when {
                    y == year.first && m == currentDate.month.number -> when (yearRange) {
                        is YearRange.PastRangeToNow   -> LocalDate(y, m, currentDate.day).day
                        is YearRange.NowToFutureYear  -> currentDate.day
                        is YearRange.NowToFutureRange -> currentDate.day
                        else                          -> 1
                    }

                    else                                             -> 1
                }
                endDay = when {
                    y == year.last && m == currentDate.month.number -> when (yearRange) {
                        is YearRange.PastYearToNow    -> currentDate.day
                        is YearRange.PastRangeToNow   -> currentDate.day
                        is YearRange.NowToFutureRange -> LocalDate(y, m, currentDate.day).day
                        else                          -> LocalDate(
                            y, m, currentDate.day
                        ).yearMonth.numberOfDays
                    }

                    else                                            -> LocalDate(
                        y, m, currentDate.day
                    ).yearMonth.numberOfDays
                }
            }
    }

    LaunchedEffect(yearState.selectedItem, monthState.selectedItem, dayState.selectedItem) {
        onSelected(yearState.selectedItem, monthState.selectedItem, dayState.selectedItem)
    }

    Row(modifier = modifier) {
        yearState.Picker(
            modifier = Modifier.weight(1f),
            items = year.toList().map { it.toString() },
        )

        monthState.Picker(
            modifier = Modifier.weight(1f),
            items = (startMonth..endMonth).toList().map { it.toString() },
        )

        dayState.Picker(
            modifier = Modifier.weight(1f),
            items = (startDay..endDay).toList().map { it.toString() },
        )
    }
}

@Composable
private fun PickerSample() {
    val pickerState = rememberPickerState()
    val items = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")

    Box(modifier = Modifier.fillMaxWidth()) {
        pickerState.Picker(items = items)
    }
}

@Composable
private fun DatePickerSample() {
    Box(modifier = Modifier.fillMaxWidth()) {
        DatePicker(modifier = Modifier.fillMaxWidth()) { year, month, day ->
            // Handle the callback.
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PickerPreview() {
    PickerSample()
}

@Preview(showBackground = true)
@Composable
private fun DatePickerPreview() {
    DatePickerSample()
}
