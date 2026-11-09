package com.example.tasktracker.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tasktracker.data.Category
import com.example.tasktracker.data.CategoryWithCount

/** Палитра для выбора цвета категории. */
val CategoryColors: List<Long> = listOf(
    0xFFE53935, 0xFFFB8C00, 0xFFFDD835, 0xFF43A047,
    0xFF1E88E5, 0xFF8E24AA, 0xFF00ACC1, 0xFF6D4C41
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    categories: List<CategoryWithCount>,
    isLoading: Boolean,
    onAddCategory: (String, Long) -> Unit,
    onDeleteCategory: (Category) -> Unit,
    onOpenCategory: (Long?) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<CategoryWithCount?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Категории") },
                actions = {
                    TextButton(onClick = { onOpenCategory(null) }) { Text("Все задачи") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить категорию")
            }
        }
    ) { padding ->
        when {
            isLoading -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }

            categories.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Text(
                    "Категорий пока нет.\nНажмите «+», чтобы создать первую.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories, key = { it.category.id }) { item ->
                    CategoryRow(
                        item = item,
                        onClick = { onOpenCategory(item.category.id) },
                        onDelete = { categoryToDelete = item }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddCategoryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, color ->
                onAddCategory(name, color)
                showAddDialog = false
            }
        )
    }

    categoryToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Удалить категорию?") },
            text = {
                Text(
                    "Категория «${item.category.name}» содержит задач: ${item.taskCount}. " +
                        "При удалении категории ВСЕ её задачи будут удалены безвозвратно (каскадное удаление)."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteCategory(item.category)
                    categoryToDelete = null
                }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun CategoryRow(item: CategoryWithCount, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(24.dp)
                    .background(Color(item.category.colorHex), CircleShape)
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(item.category.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Задач: ${item.taskCount}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить категорию")
            }
        }
    }
}

@Composable
private fun AddCategoryDialog(onDismiss: () -> Unit, onConfirm: (String, Long) -> Unit) {
    var name by remember { mutableStateOf("") }
    var color by remember { mutableLongStateOf(CategoryColors.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая категория") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    singleLine = true
                )
                Text("Цвет", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryColors.take(4).forEach { c -> ColorDot(c, c == color) { color = c } }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryColors.drop(4).forEach { c -> ColorDot(c, c == color) { color = c } }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, color) },
                enabled = name.isNotBlank()
            ) { Text("Создать") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@Composable
private fun ColorDot(color: Long, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(36.dp)
            .background(Color(color), CircleShape)
            .border(
                BorderStroke(if (selected) 3.dp else 0.dp, MaterialTheme.colorScheme.onSurface),
                CircleShape
            )
            .clickable(onClick = onClick)
    )
}
