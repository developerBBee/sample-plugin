import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.nio.file.Path
import java.nio.file.Paths

class ReadSpreadSheetPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val resDir = Paths.get(project.projectDir.absolutePath, "src", "main", "res")
        val sheetId = project.properties["SHEET_ID"] as String?
        println(sheetId ?: "sheetId is nothing")
        val range = project.properties["RANGE"] as String?
        println(sheetId ?: "sheetId is nothing")

        project.tasks.register<CustomTask>("CustomTask") {
            spreadSheetId.set(sheetId)
            spreadSheetRange.set(range ?: "A3:A1000")
            resourceDir.set(resDir)
        }
    }
}

abstract class CustomTask : DefaultTask() {
    @get:Input
    abstract val spreadSheetId: Property<String>

    @get:Input
    abstract val spreadSheetRange: Property<String>

    @get:Input
    abstract val resourceDir: Property<Path>

    private val APPLICATION_NAME = "Google API Kotlin Quickstart"

    private val JSON_FACTORY = GsonFactory.getDefaultInstance()

    private val TOKENS_DIRECTORY_PATH = "tokens"

    private val SCOPE_SPREAD_SHEET = listOf(SheetsScopes.SPREADSHEETS_READONLY)

    private val CREDENTIALS_FILE_PATH = "/credentials.json"

    private val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()


    @TaskAction
    fun execute() {
        println("spread-sheet-reader start")
//        println("spreadSheetId: $spreadSheetId")
//        println("spreadSheetRange: $spreadSheetRange")
        println()

//        executeAppScript()
        executeSpreadSheet(
            spreadSheetId = spreadSheetId.get(),
            range = spreadSheetRange.get() ?: run {
                println("The argument \"spreadSheetRange\" is not set, so it will be set to the default value.")
                println("The default value is \"A3:A1000\".")
                "A3:A1000"
            }
        )
    }

    private fun executeSpreadSheet(spreadSheetId: String, range: String) {
        val service = Sheets.Builder(
            HTTP_TRANSPORT,
            JSON_FACTORY,
            getCredentials(HTTP_TRANSPORT, SCOPE_SPREAD_SHEET)
        )
            .setApplicationName(APPLICATION_NAME)
            .build()

        val spreadSheet = service.Spreadsheets().get(spreadSheetId).execute()
        val sheetTitleList = mutableListOf<String>()
        spreadSheet.sheets.forEach { sheet ->
            println("Sheet: ${sheet.properties.title}")
            sheetTitleList.add(sheet.properties.title)
        }
        sheetTitleList.forEach {
            val values = service.Spreadsheets().Values().get(spreadSheetId, "$it!$range").execute()
            println("$it values: $values")
        }
    }

    private fun getCredentials(httpTransport: NetHttpTransport, scopes: List<String>): Credential {
        // Load client secrets.
        val inputStream = ReadSpreadSheetPlugin::class.java.getResourceAsStream(CREDENTIALS_FILE_PATH)
            ?: throw FileNotFoundException("Resource not found: $CREDENTIALS_FILE_PATH")
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(inputStream))

        // Build flow and trigger user authorization request.
        val flow = GoogleAuthorizationCodeFlow.Builder(
            httpTransport, JSON_FACTORY, clientSecrets, scopes
        )
            .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build()

        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }
}