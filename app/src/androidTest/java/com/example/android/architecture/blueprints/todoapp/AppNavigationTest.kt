package com.example.android.architecture.blueprints.todoapp

import android.app.Activity
import android.view.Gravity
import androidx.appcompat.widget.Toolbar
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.DrawerMatchers.isOpen
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.data.source.local.ToDoDatabase
import com.example.android.architecture.blueprints.todoapp.tasks.TasksActivity
import com.example.android.architecture.blueprints.todoapp.util.DataBindingIdlingResource
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource
import com.example.android.architecture.blueprints.todoapp.util.monitorActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@LargeTest
@RunWith(AndroidJUnit4::class)
class AppNavigationTest {

    private lateinit var tasksRepository: TasksRepository

    private lateinit var database: ToDoDatabase

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        ServiceLocator.setDatabaseFactory { context ->
            database = Room.inMemoryDatabaseBuilder(
                context,
                ToDoDatabase::class.java
            )
                .allowMainThreadQueries()
                .build()

            database
        }
        tasksRepository = ServiceLocator.provideTasksRepository(ApplicationProvider.getApplicationContext())
    }

    @After
    fun reset() {
        ServiceLocator.resetRepository()
        ServiceLocator.resetDatabaseFactory()
        database.close()
    }

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
    fun tasksScreen_clickOnDrawerIcon_OpensNavigation() {
        // Start the Tasks screen.
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // 1. Check that left drawer is closed at startup.
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.START)))

        // 2. Open drawer by clicking drawer icon.
        onView(withContentDescription(activityScenario.getToolbarNavigationContentDescription()))
            .perform(click())

        // 3. Check if drawer is open.
        onView(withId(R.id.drawer_layout)).check(matches(isOpen(Gravity.START)))

        // When using ActivityScenario.launch(), always call close()
        activityScenario.close()
    }

    @Test
    fun taskDetailScreen_doubleUpButton() = runBlocking {
        val task = Task("Up button", "Description")
        tasksRepository.saveTask(task)

        // Start the Tasks screen.
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // 1. Click on the task on the list.
        onView(withText("Up button")).perform(click())

        // 2. Click on the edit task button.
        onView(withId(R.id.edit_task_fab)).perform(click())

        // 3. Confirm that if we click Up button once, we end up back at the task details page.
        onView(withContentDescription(activityScenario.getToolbarNavigationContentDescription()))
            .perform(click())

        onView(withId(R.id.task_detail_title_text)).check(matches(isDisplayed()))

        // 4. Confirm that if we click Up button a second time, we end up back at the home screen.
        onView(withContentDescription(activityScenario.getToolbarNavigationContentDescription()))
            .perform(click())

        onView(withId(R.id.tasks_container_layout)).check(matches(isDisplayed()))

        // When using ActivityScenario.launch(), always call close().
        activityScenario.close()
    }


    @Test
    fun taskDetailScreen_doubleBackButton() = runBlocking {
        val task = Task("Back button", "Description")
        tasksRepository.saveTask(task)

        // Start Tasks screen.
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // 1. Click on the task on the list.
        onView(withText("Back button")).perform(click())

        // 2. Click on the Edit task button.
        onView(withId(R.id.edit_task_fab)).perform(click())

        // 3. Confirm that if we click Back once, we end up back at the task details page.
        pressBack()
        onView(withId(R.id.task_detail_title_text)).check(matches(isDisplayed()))

        // 4. Confirm that if we click Back a second time, we end up back at the home screen.
        pressBack()
        onView(withId(R.id.tasks_container_layout)).check(matches(isDisplayed()))

        // When using ActivityScenario.launch(), always call close()
        activityScenario.close()
    }
}

fun <T : Activity> ActivityScenario<T>.getToolbarNavigationContentDescription()
        : String {
    var description = ""
    onActivity {
        description =
            it.findViewById<Toolbar>(R.id.toolbar).navigationContentDescription as String
    }
    return description
}