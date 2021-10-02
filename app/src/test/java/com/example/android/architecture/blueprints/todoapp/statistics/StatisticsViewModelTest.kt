package com.example.android.architecture.blueprints.todoapp.statistics

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.android.architecture.blueprints.todoapp.MainCoroutineRule
import com.example.android.architecture.blueprints.todoapp.data.source.FakeTestRepository
import com.example.android.architecture.blueprints.todoapp.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.robolectric.util.Logger.error

@ExperimentalCoroutinesApi
class StatisticsViewModelTest {
    // executes each task synchronously using Architecture components
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // subject under test
    private lateinit var statisticsViewModel: StatisticsViewModel

    // use fake repository
    private lateinit var tasksRepository: FakeTestRepository

    @Before
    fun setupStatisticsViewModel() {
        // initialize the repository with no tasks
        tasksRepository = FakeTestRepository()

        statisticsViewModel = StatisticsViewModel(tasksRepository)
    }

    @Test
    fun loadTasks_loading() {
        mainCoroutineRule.pauseDispatcher()
        statisticsViewModel.refresh()

        assertThat(statisticsViewModel.dataLoading.getOrAwaitValue(), `is` (true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(statisticsViewModel.dataLoading.getOrAwaitValue(), `is` (false))
    }

    @Test
    fun loadStatisticsWhenTasksAreUnavailable_callErrorToDisplay() {
        // Make repository return errors
        tasksRepository.setReturnError(true)
        statisticsViewModel.refresh()
        // Then an error message is shown
        assertThat(statisticsViewModel.empty.getOrAwaitValue(), `is` (true))
        assertThat(statisticsViewModel.error.getOrAwaitValue(), `is` (true))
    }
}