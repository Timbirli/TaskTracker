package com.example.tasktracker.data

import kotlinx.coroutines.flow.Flow

/** Единая точка доступа к данным: вся работа с БД инкапсулирована здесь. */
class AppRepository(
    private val categoryDao: CategoryDao,
    private val taskDao: TaskDao
) {
    // ---- Категории ----
    val categories: Flow<List<Category>> = categoryDao.observeAll()
    val categoriesWithCount: Flow<List<CategoryWithCount>> = categoryDao.observeAllWithCount()

    suspend fun getCategory(id: Long): Category? = categoryDao.getById(id)
    suspend fun addCategory(category: Category): Long = categoryDao.insert(category)
    suspend fun updateCategory(category: Category) = categoryDao.update(category)
    suspend fun deleteCategory(category: Category) = categoryDao.delete(category)

    // ---- Задачи ----
    val tasks: Flow<List<TaskWithCategory>> = taskDao.observeAllWithCategory()
    fun tasksByCategory(categoryId: Long): Flow<List<TaskWithCategory>> =
        taskDao.observeByCategory(categoryId)

    suspend fun getTask(id: Long): Task? = taskDao.getById(id)
    suspend fun addTask(task: Task): Long = taskDao.insert(task)
    suspend fun updateTask(task: Task) = taskDao.update(task)
    suspend fun deleteTask(task: Task) = taskDao.delete(task)
    suspend fun setTaskCompleted(id: Long, completed: Boolean) = taskDao.setCompleted(id, completed)
}
