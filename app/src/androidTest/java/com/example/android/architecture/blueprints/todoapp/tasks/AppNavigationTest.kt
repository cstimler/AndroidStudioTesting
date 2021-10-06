package com.example.android.architecture.blueprints.todoapp.tasks

import android.app.Activity
import android.view.Gravity
import android.widget.Toolbar
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.DrawerMatchers.isOpen
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.ServiceLocator
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.tasks.util.DataBindingIdlingResource
import com.example.android.architecture.blueprints.todoapp.tasks.util.monitorActivity
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.Espresso.pressBack

@RunWith(AndroidJUnit4::class)
@LargeTest
class AppNavigationTest {

    private lateinit var tasksRepository: TasksRepository

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun init() {
        tasksRepository = ServiceLocator.provideTasksRepository(getApplicationContext())
    }

    @After
    fun reset() {
        ServiceLocator.resetRepository()
    }

    // prepare idling resources by registering them:

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister((EspressoIdlingResource.countingIdlingResource))
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun tasksScreen_clickOnDrawerIcon_OpensNavigation()  {
        // Starts the Tasks screen
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // 1. Check that left drawer is closed at the beginning of test

        onView(withId(R.id.drawer_layout))
            .check(matches(isClosed(Gravity.START))) // Left Drawer is closed.

        // 2. Open the drawer by clicking on its icon

        onView(
            withContentDescription(
                activityScenario
                    .getToolbarNavigationContentDescription()
            )
        ).perform(click())

        // 3. Confirm that the drawer is open

        onView(withId(R.id.drawer_layout))
            .check(matches(isOpen(Gravity.START))) // Left drawer is open.

        // Always close ActivityScenario when done if using launch:
        activityScenario.close()
    }

    @Test
    fun taskDetailScreen_doubleUpButton() = runBlocking {
        val task = Task("Up button", "Description")
        tasksRepository.saveTask(task)

        // Starts the Tasks screen
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // 1. Click on the task in the list.
        onView(withText("Up button")).perform(click())
        // 2. Click on the edit task button.
        onView(withId(R.id.edit_task_fab)).perform(click())
        // 3. Confirm that if we click Up Button once we end up back at the task details page
        onView(
            withContentDescription(
                activityScenario
                    .getToolbarNavigationContentDescription()
            )
        ).perform(click())
        onView(withId(R.id.task_detail_title_text)).check(matches(isDisplayed()))
        // 4. Confirm that if we click the Up Button once more we then end up at the home screen
        onView(
            withContentDescription(
                activityScenario
                    .getToolbarNavigationContentDescription()
            )
        ).perform(click())
        onView(withId(R.id.tasks_container_layout)).check(matches(isDisplayed()))
        // remember to close the Activity Scenario:
        activityScenario.close()
    }

    @Test
    fun taskDetailScreen_doubleBackButton() = runBlocking {
        val task = Task("Back button", "Description")
        tasksRepository.saveTask(task)

        // Start the Tasks screen
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // 1. Click on the task in the list.
        onView(withText("Back button")).perform(click())
        // 2. Click on the edit task button.
        onView(withId(R.id.edit_task_fab)).perform(click())
        // 3. Confirm that if we click Back Button once we end up back at the task details page
        pressBack()
        onView(withId(R.id.task_detail_title_text)).check(matches(isDisplayed()))
        // 4. Confirm that if we click the Back Button once more we then end up at the home screen
        pressBack()
        onView(withId(R.id.tasks_container_layout)).check(matches(isDisplayed()))
        // remember to close the Activity Scenario:
        activityScenario.close()


    }
}

fun <T : Activity> ActivityScenario<T>.getToolbarNavigationContentDescription()
        : String {
    var description = ""
    onActivity {
        description =
            it.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).navigationContentDescription as String
    }
    return description
}


/*
fun <T : Activity> ActivityScenario<T>.getToolbarNavigationContentDescription()
        : String {
    var description = ""
    onActivity {
        description =
            it.findViewById<Toolbar>(R.id.toolbar).navigationContentDescription as String
    }
    return description
}
*/
