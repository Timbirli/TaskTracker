package com.example.tasktracker.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: Long
)

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val categoryId: Long,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/** Задача вместе с её категорией (JOIN через @Relation). */
data class TaskWithCategory(
    @Embedded val task: Task,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: Category
)

/** Категория с количеством задач (для списка категорий). */
data class CategoryWithCount(
    @Embedded val category: Category,
    val taskCount: Int
)
