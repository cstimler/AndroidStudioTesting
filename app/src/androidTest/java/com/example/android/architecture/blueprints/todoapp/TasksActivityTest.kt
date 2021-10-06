package com.example.android.architecture.blueprints.todoapp

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.tasks.TasksActivity
import com.example.android.architecture.blueprints.todoapp.tasks.util.DataBindingIdlingResource
import com.example.android.architecture.blueprints.todoapp.tasks.util.monitorActivity
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class TasksActivityTest {

    private lateinit var repository: TasksRepository

    @Before
    fun init() {
   repository = ServiceLocator.provideTasksRepository(getApplicationContext())
       runBlocking {
           repository.deleteAllTasks()
       }
}

    @After
    fun reset() {
        ServiceLocator.resetRepository()
    }

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun editTask() = runBlocking {
        // Set the initial state:
        repository.saveTask(Task("TITLE1", "DESCRIPTION"))

        // Start up Tasks screen:
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        // Espresso code:

        // click on the first task in list and confirm the right information appears:

        onView(withText("TITLE1")).perform(click())
        onView(withId(R.id.task_detail_title_text)).check(matches(withText("TITLE1")))
        onView(withId(R.id.task_detail_description_text)).check(matches(withText("DESCRIPTION")))
        onView(withId(R.id.task_detail_complete_checkbox)).check(matches(not(isChecked())))

        //  click on the edit button, edit, and save:

        onView(withId(R.id.edit_task_fab)).perform(click())
        onView(withId(R.id.add_task_title_edit_text)).perform(replaceText("NEW TITLE"))
        onView(withId(R.id.add_task_description_edit_text)).perform(replaceText("NEW DESCRIPTION"))
        onView(withId(R.id.save_task_fab)).perform(click())

        // Verify task is displayed on screen in the task list.
        onView(withText("NEW TITLE")).check(matches(isDisplayed()))
        //Verify previous task is not displayed
        onView(withText("TITLE1")).check(doesNotExist())
        // Must close activityScenario before doing any database reset:

        activityScenario.close()
    }

    @Test
    fun createOneTask_deleteTask() {

        // 1. Start TasksActivity.
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        // 2. Add an active task by clicking on the FAB and saving a new task.
       // val newTask = Task("NEWTASK", "EASY")
        onView(withId(R.id.add_task_fab)).perform(click())
        onView(withId(R.id.add_task_title_edit_text)).perform(replaceText("NEWTASK"))
        onView(withId(R.id.add_task_description_edit_text)).perform(replaceText("EASY"))
        onView(withId(R.id.save_task_fab)).perform(click())
        // 3. Open the new task in a details view.
        // Let's first check to be sure that the new task is displayed:
        onView(withText("NEWTASK")).check(matches(isDisplayed()))
        onView(withText("NEWTASK")).perform(click())
        // Let's check to see if it is displayed in the task details menu
        onView(withText("NEWTASK")).check(matches(isDisplayed()))
        onView(withText("EASY")).check(matches(isDisplayed()))

        // 4. Click delete task in menu.
        onView(withId(R.id.task_detail_complete_checkbox)).perform(click())
        onView(withId(R.id.task_detail_complete_checkbox)).check(matches(isChecked()))
        // 5. Verify it was deleted.
        onView(withId(R.id.menu_delete)).perform(click())
        onView(withText("NEWTASK")).check(doesNotExist())
        /*
        onView(withId(R.id.edit_task_fab)).perform(click())
        onView(withId(R.id.save_task_fab)).perform(click())
        onView(withId(R.id.task_detail_complete_checkbox)).check(matches(isChecked()))
        */
        // 6. Make sure the activity is closed.
        activityScenario.close()
    }

}