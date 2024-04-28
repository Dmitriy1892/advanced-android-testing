package com.example.android.architecture.blueprints.todoapp

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import com.example.android.architecture.blueprints.todoapp.data.source.DefaultTasksRepository
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksLocalDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.local.ToDoDatabase
import com.example.android.architecture.blueprints.todoapp.data.source.remote.TasksRemoteDataSource
import kotlinx.coroutines.runBlocking

object ServiceLocator {

    private val lock = Any()

    private var database: ToDoDatabase? = null

    private var databaseFactory: (Context) -> ToDoDatabase = ::createDataBase

    @Volatile
    var tasksRepository: TasksRepository? = null
        @VisibleForTesting set

    @VisibleForTesting
    fun resetRepository() {
        synchronized(lock) {
            runBlocking {
                TasksRemoteDataSource.deleteAllTasks()
            }
            // Clear all data to avoid test pollution.
            database?.apply {
                clearAllTables()
                close()
            }
            database = null
            tasksRepository = null
        }
    }

    fun setDatabaseFactory(factory: (Context) -> ToDoDatabase) {
        databaseFactory = factory
    }

    fun resetDatabaseFactory() {
        databaseFactory = ::createDataBase
    }

    fun provideTasksRepository(context: Context): TasksRepository {
        return tasksRepository ?: createTaskRepository(context)
    }

    @Synchronized
    private fun createTaskRepository(context: Context): TasksRepository {
        return tasksRepository ?: run {
            val newRepo = DefaultTasksRepository(
                TasksRemoteDataSource,
                createTaskLocalDataSource(context)
            )
            tasksRepository = newRepo

            newRepo
        }
    }

    private fun createTaskLocalDataSource(context: Context): TasksDataSource {
        val database = database ?: databaseFactory(context)
        return TasksLocalDataSource(database.taskDao())
    }

    private fun createDataBase(context: Context): ToDoDatabase {
        val result = Room.databaseBuilder(
            context.applicationContext,
            ToDoDatabase::class.java, "Tasks.db"
        ).build()
        database = result
        return result
    }
}