package com.example.tasktracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.tasktracker.data.AppDatabase
import com.example.tasktracker.data.AppRepository
import com.example.tasktracker.ui.TaskTrackerApp
import com.example.tasktracker.viewmodel.TaskViewModel
import com.example.tasktracker.viewmodel.TaskViewModelFactory

class MainActivity : ComponentActivity() {

    // Ручная сборка слоёв без DI: БД -> Repository -> Factory -> ViewModel
    private val viewModel: TaskViewModel by viewModels {
        val db = AppDatabase.getInstance(applicationContext)
        val repository = AppRepository(db.categoryDao(), db.taskDao())
        TaskViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
            ) {
                TaskTrackerApp(viewModel)
            }
        }
    }
}
