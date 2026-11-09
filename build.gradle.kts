// Версии должны совпадать с теми, что уже есть в вашем проекте (gradle/libs.versions.toml).
// Если используете шаблон Android Studio — лучше НЕ заменять этот файл, а добавить в него только ksp.
// KSP для AGP 9 (встроенный Kotlin) должен быть 2.3.x и новее; Kotlin — 2.3.x.
plugins {
    id("com.android.application") version "9.1.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.10" apply false
    id("com.google.devtools.ksp") version "2.3.6" apply false
}
