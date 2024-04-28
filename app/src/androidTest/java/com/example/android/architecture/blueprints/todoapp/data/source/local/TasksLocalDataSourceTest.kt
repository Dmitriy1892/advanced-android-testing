package com.example.android.architecture.blueprints.todoapp.data.source.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.succeeded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@MediumTest
@RunWith(AndroidJUnit4::class)
class TasksLocalDataSourceTest {

    private lateinit var localDataSource: TasksLocalDataSource
    private lateinit var database: ToDoDatabase

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ToDoDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localDataSource = TasksLocalDataSource(
            database.taskDao(),
            Dispatchers.Main
        )
    }

    @After
    fun cleanup() {
        database.close()
    }

    @Test
    fun saveTask_retrievesTask() = runTest {
        val newTask = Task("title", "description", false)
        localDataSource.saveTask(newTask)

        val result = localDataSource.getTask(newTask.id)

        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        val data = result.data
        assertThat(data.title, `is`(newTask.title))
        assertThat(data.description, `is`(newTask.description))
        assertThat(data.isCompleted, `is`(newTask.isCompleted))
    }

    @Test
    fun completeTask_retrievedTaskIsComplete() = runTest {
        // 1. Save a new active task in the local data source.
        val newTask = Task("title", "description", false)
        localDataSource.saveTask(newTask)

        // 2. Mark it as complete.
        localDataSource.completeTask(newTask.id)

        // 3. Check that the task can be retrieved from the local data source and is complete.
        val taskFromDb = localDataSource.getTask(newTask.id)
        assertThat(taskFromDb.succeeded, `is`(true))
        taskFromDb as Result.Success
        assertThat(taskFromDb.data.isCompleted, `is`(true))
    }
}