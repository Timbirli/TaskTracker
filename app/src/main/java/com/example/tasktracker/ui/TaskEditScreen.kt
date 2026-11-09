package com.example.tasktracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tasktracker.data.Category
import com.example.tasktracker.data.Task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditScreen(
    taskId: Long,                         // -1 = новая задача
    categories: List<Category>,
    preselectedCategoryId: Long?,
    loadTask: suspend (Long) -> Task?,
    onSave: (Task) -> Unit,
    onBack: () -> Unit
) {
    val isNew = taskId == -1L
    var loaded by remember { mutableStateOf(isNew) }
    var existing by remember { mutableStateOf<Task?>(null) }
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var categoryId by rememberSaveable { mutableStateOf(preselectedCategoryId) }
    var expanded by remember { mutableStateOf(false) }

    // Загрузка редактируемой задачи
    LaunchedEffect(taskId) {
        if (!isNew) {
            loadTask(taskId)?.let { t ->
                existing = t
                title = t.title
                description = t.description
                categoryId = t.categoryId
            }
            loaded = true
        }
    }
    // Категория по умолчанию — первая в списке
    LaunchedEffect(categories) {
        if (categoryId == null) categoryId = categories.firstOrNull()?.id
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "Новая задача" else "Редактирование") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        if (!loaded) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Заголовок") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            val selected = categories.find { it.id == categoryId }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selected?.name ?: "Выберите категорию",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Категория") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c.name) },
                            leadingIcon = {
                                Box(Modifier.size(12.dp).background(Color(c.colorHex), CircleShape))
                            },
                            onClick = {
                                categoryId = c.id
                                expanded = false
                            }
                        )
                    }
                }
            }

            if (categories.isEmpty()) {
                Text("Нет категорий. Создайте хотя бы одну категорию.")
            }

            Button(
                onClick = {
                    val cat = categoryId ?: return@Button
                    val base = existing
                    onSave(
                        base?.copy(title = title.trim(), description = description.trim(), categoryId = cat)
                            ?: Task(title = title.trim(), description = description.trim(), categoryId = cat)
                    )
                },
                enabled = title.isNotBlank() && categoryId != null,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Сохранить") }
        }
    }
}
