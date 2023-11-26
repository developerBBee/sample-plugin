//import apps.script.api.AppScriptApi
//import org.jetbrains.kotlin.utils.join

// Top-level build file where you can add configuration options common to all sub-projects/modules.
//plugins {
//    id("com.android.application") version "8.2.0-rc01" apply false
//    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
//    id("apps.script.api.execution") version "1.0" apply false
//}
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

//val resDir = join(listOf(projectDir.absolutePath, "app", "src", "main", "res"), File.separator)
//val executeTask by tasks.registering (AppScriptApi::class) {
//    resDirPath = resDir
//    spreadSheetId = "1234567890qwertyuiop" // spread sheet id
//    spreadSheetRange = "A1:Z100" // data range to retrieve
//}
//
//tasks.register("getArgPathTask") {
//    group = "Sample Group"
//    description = "Print hello dir name"
//    //projectDir = projectDir.absolutePath
//}
