package com.example.tasktracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tasktracker.data.AppRepository
import com.example.tasktracker.data.Category
import com.example.tasktracker.data.CategoryWithCount
import com.example.tasktracker.data.Task
import com.example.tasktracker.data.TaskWithCategory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Одноразовые события UI (Snackbar). */
sealed interface UiEvent {
    data class Message(val text: String) : UiEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModel(private val repository: AppRepository) : ViewModel() {

    // ---------- StateFlow: состояние UI ----------

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    val categories: StateFlow<List<CategoryWithCount>> = repository.categoriesWithCount
        .onEach { _isLoading.value = false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskWithCategory>> = _selectedCategoryId
        .flatMapLatest { id ->
            if (id == null) repository.tasks else repository.tasksByCategory(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---------- SharedFlow: одноразовые события ----------

    private val _events = MutableSharedFlow<UiEvent>(replay = 0, extraBufferCapacity = 8)
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    // ---------- Действия ----------

    fun selectCategory(id: Long?) {
        _selectedCategoryId.value = id
    }

    fun addCategory(name: String, colorHex: Long) = launchIO("Категория «$name» создана") {
        repository.addCategory(Category(name = name.trim(), colorHex = colorHex))
    }

    fun deleteCategory(category: Category) = launchIO("Категория «${category.name}» удалена вместе с задачами") {
        if (_selectedCategoryId.value == category.id) _selectedCategoryId.value = null
        repository.deleteCategory(category)
    }

    fun saveTask(task: Task) = launchIO(
        if (task.id == 0L) "Задача создана" else "Задача обновлена"
    ) {
        if (task.id == 0L) repository.addTask(task) else repository.updateTask(task)
    }

    fun toggleCompleted(task: Task) = launchIO {
        repository.setTaskCompleted(task.id, !task.isCompleted)
    }

    fun deleteTask(task: Task) = launchIO("Задача удалена") {
        repository.deleteTask(task)
    }

    suspend fun getTask(id: Long): Task? = withContext(Dispatchers.IO) {
        repository.getTask(id)
    }

    private fun launchIO(successMessage: String? = null, block: suspend () -> Unit) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
                successMessage?.let { _events.emit(UiEvent.Message(it)) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.emit(UiEvent.Message("Ошибка: ${e.message ?: "неизвестная"}"))
            }
        }
}

/** Фабрика создаёт ViewModel с репозиторием вручную (без DI). */
class TaskViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return TaskViewModel(repository) as T
    }
}
