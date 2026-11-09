package com.example.tasktracker.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tasktracker.viewmodel.TaskViewModel
import com.example.tasktracker.viewmodel.UiEvent

private const val ROUTE_CATEGORIES = "categories"
private const val ROUTE_TASKS = "tasks"
private const val ROUTE_EDIT = "edit/{taskId}"
private const val NEW_TASK_ID = -1L

@Composable
fun TaskTrackerApp(viewModel: TaskViewModel) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Одноразовые события из SharedFlow -> Snackbar
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                when (event) {
                    is UiEvent.Message -> snackbarHostState.showSnackbar(event.text)
                }
            }
        }
    }

    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = ROUTE_CATEGORIES) {

            composable(ROUTE_CATEGORIES) {
                CategoriesScreen(
                    categories = categories,
                    isLoading = isLoading,
                    onAddCategory = viewModel::addCategory,
                    onDeleteCategory = viewModel::deleteCategory,
                    onOpenCategory = { id ->
                        viewModel.selectCategory(id)
                        navController.navigate(ROUTE_TASKS)
                    }
                )
            }

            composable(ROUTE_TASKS) {
                TasksScreen(
                    tasks = tasks,
                    categories = categories.map { it.category },
                    selectedCategoryId = selectedCategoryId,
                    onSelectCategory = viewModel::selectCategory,
                    onToggle = viewModel::toggleCompleted,
                    onDelete = viewModel::deleteTask,
                    onAdd = { navController.navigate("edit/$NEW_TASK_ID") },
                    onEdit = { task -> navController.navigate("edit/${task.id}") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = ROUTE_EDIT,
                arguments = listOf(navArgument("taskId") { type = NavType.LongType })
            ) { entry ->
                val taskId = entry.arguments?.getLong("taskId") ?: NEW_TASK_ID
                TaskEditScreen(
                    taskId = taskId,
                    categories = categories.map { it.category },
                    preselectedCategoryId = selectedCategoryId,
                    loadTask = viewModel::getTask,
                    onSave = { task ->
                        viewModel.saveTask(task)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
