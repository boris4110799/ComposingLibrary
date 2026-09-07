package tw.boris4110799.composing.library.ui.components.pagers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabIndicatorScope
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.hashCode

/**
 * The component combine TabRow and [HorizontalPager].
 * @param state The pager state.
 * @param tabList The data list of tabs.
 */
@Composable
fun <T> BasicTabPager(
    state: PagerState,
    tabList: List<T>,
    modifier: Modifier = Modifier,
    content: @Composable TabPagerScope<T>.() -> Unit
) {
    val scope = remember(state, tabList) { TabPagerScope(state, tabList) }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        scope.content()
    }
}

/**
 * The scope for `TabRow` and [HorizontalPager].
 * @param state The pager state.
 * @param items The data list of tabs.
 */
class TabPagerScope<T>(
    val state: PagerState,
    val items: List<T>
) {
    /**
     * @param onTabChange The callback when the tab is changed. And get the current tab index.
     * @param scrollable Set whether the tab row is scrollable.
     * @param style The style of the [BasicTabPager].
     * @param indicator The indicator of the tab row.
     * @param divider The divider of the tab row.
     * @param content Tab content.
     * @see SecondaryScrollableTabRow
     * @see SecondaryTabRow
     */
    @Composable
    fun TabRow(
        onTabChange: (Int) -> Unit,
        scrollable: Boolean = true,
        style: BasicTabPagerStyle = BasicTabPagerStyle.Default,
        indicator: @Composable TabIndicatorScope.(Int) -> Unit = @Composable { selectedTabIndex ->
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(selectedTabIndex, matchContentSize = false)
            )
        },
        divider: @Composable () -> Unit = @Composable { HorizontalDivider() },
        content: @Composable (index: Int, data: T) -> Unit
    ) {
        var tabIndex by rememberSaveable { mutableIntStateOf(state.currentPage) }

        LaunchedEffect(state) {
            snapshotFlow { state.currentPage }.distinctUntilChanged().collect {
                tabIndex = it
                onTabChange(it)
            }
        }

        if (scrollable) {
            SecondaryScrollableTabRow(
                selectedTabIndex = tabIndex,
                modifier = Modifier.fillMaxWidth().padding(vertical = style.tabRowVerticalPadding),
                containerColor = style.tabRowContainerColor,
                contentColor = style.tabRowContentColor,
                edgePadding = style.tabRowHorizontalPadding,
                indicator = { indicator(tabIndex) },
                divider = divider
            ) {
                items.forEachIndexed { index, data ->
                    content(index, data)
                }
            }
        } else {
            SecondaryTabRow(
                selectedTabIndex = tabIndex,
                modifier = Modifier.fillMaxWidth().padding(
                    horizontal = style.tabRowHorizontalPadding,
                    vertical = style.tabRowVerticalPadding
                ),
                containerColor = style.tabRowContainerColor,
                contentColor = style.tabRowContentColor,
                indicator = { indicator(tabIndex) },
                divider = divider
            ) {
                items.forEachIndexed { index, data ->
                    content(index, data)
                }
            }
        }
    }

    /**
     * @param pageContent Pager content.
     * @see HorizontalPager
     */
    @Composable
    fun Pager(
        pageSize: PageSize = PageSize.Fill,
        beyondViewportPageCount: Int = PagerDefaults.BeyondViewportPageCount,
        pageContent: @Composable PagerScope.(index: Int) -> Unit
    ) {
        HorizontalPager(
            state = state,
            modifier = Modifier.fillMaxSize(),
            pageSize = pageSize,
            beyondViewportPageCount = beyondViewportPageCount,
            key = { index -> items[index].hashCode() },
        ) { pageIndex ->
            pageContent(pageIndex)
        }
    }
}

/**
 * The style of the [BasicTabPager].
 * @param tabRowContainerColor The container color of the tab row.
 * @param tabRowContentColor The content color of the tab row.
 * @param tabRowHorizontalPadding The horizontal padding of the tab row.
 * @param tabRowVerticalPadding The vertical padding of the tab row.
 */
data class BasicTabPagerStyle(
    val tabRowContainerColor: Color,
    val tabRowContentColor: Color,
    val tabRowHorizontalPadding: Dp = 32.dp,
    val tabRowVerticalPadding: Dp = 8.dp,
) {
    companion object {
        val Default: BasicTabPagerStyle
            @Composable get() = BasicTabPagerStyle(
                tabRowContainerColor = TabRowDefaults.primaryContainerColor,
                tabRowContentColor = TabRowDefaults.primaryContentColor
            )
    }
}

@Preview(showBackground = true)
@Composable
private fun BasicTabPagerPreview() {
    val scope = rememberCoroutineScope()
    val list = List(5) { "Tab$it" }
    val state = rememberPagerState { list.size }
    var currentPage by remember { mutableIntStateOf(0) }

    BasicTabPager(state = state, tabList = list) {
        TabRow(onTabChange = {}) { index, data ->
            Tab(
                selected = currentPage == index,
                onClick = { scope.launch { state.scrollToPage(index) } },
                text = { Text(text = data) },
                unselectedContentColor = Color.LightGray
            )
        }

        Pager { index ->
            Text(text = "Page $index")
        }
    }
}
