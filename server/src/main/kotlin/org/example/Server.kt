// BrickGameServerClass.kt
package org.example

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.defaultForFile
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.LocalFileContent
import io.ktor.server.http.content.default
import io.ktor.server.http.content.files
import io.ktor.server.http.content.resources
import io.ktor.server.http.content.static
import io.ktor.server.http.content.staticRootFolder
//import io.ktor.server.http.content.staticFiles
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.IgnoreTrailingSlash
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.util.combineSafe
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.race.Action
import org.race.GameEngine
import org.race.State
import java.io.File

fun main() {
    embeddedServer(Netty, host = "localhost", port = 8080, module = Application::module).start(wait = true)
}

fun Route.filesWithDefaultIndex(dir: File) {
    val combinedDir = staticRootFolder?.resolve(dir) ?: dir
    println("Combine dir: $combinedDir")
    get("/") {
//        val relativePath = call.parameters
//            .getAll("/")
//            ?.joinToString(File.separator) ?: return@get
//        val file = combinedDir.combineSafe(relativePath)
//        val fallbackFile = file.combineSafe("index.html")
//
//        val localFile = when {
//            file.isFile -> file
//            file.isDirectory && fallbackFile.isFile -> fallbackFile
//            else -> return@get
//        }
        val file = combinedDir.resolve("index.html")
        if (file.exists() && file.isFile) {
            call.respond(LocalFileContent(file, ContentType.defaultForFile(file)))
        } else {
            call.respond(HttpStatusCode.NotFound, "Index file not found")
        }


//        call.respond(LocalFileContent(localFile, ContentType.defaultForFile(localFile)))
    }
    static {
        files(dir)
    }
}
fun Application.module() {
    val race = GameEngine()

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }


    install(IgnoreTrailingSlash)
    routing {

//        println("Static folder exists: " + File("C:\\Users\\ebox\\school\\brick_game\\web_gui").exists())

        //mac
//        staticFiles("/", File("web_gui"))

        val staticData = File("web_gui")
        filesWithDefaultIndex(staticData)



//        static("/") {
//            files("web_gui")
//            default("index.html")
//        }
        val folder = File("web_gui/index.html")
        println("Does folder exist? ${folder.exists()}")
        println("Folder absolute path: ${folder.absolutePath}")

        //win
//        staticFiles("/", File("C:\\Users\\ebox\\school\\brick_game\\web_gui"))


        // Получение списка доступных игр
        get("/api/games") {
            val gamesList = GamesList(
                games = listOf(
                    GameInfo(1, "Race"),
//                    GameInfo(2, "Tetris")
                )
            )
            println("Received gameId: $gamesList")
            call.respond(HttpStatusCode.OK, gamesList)
        }

        // Выбор и запуск игры
        post("/api/games/{gameId}") {
            val gameId = call.parameters["gameId"]?.toIntOrNull()
            println("Received gameId: $gameId")
            when {
                gameId == null -> call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorMessage("Invalid gameId")
                )

                gameId !in 1..1 -> call.respond(
                    HttpStatusCode.NotFound,
                    ErrorMessage("Game not found")
                )

                else -> call.respond(HttpStatusCode.OK, "Game $gameId started")
            }
        }

        // Выполнить команду игрока
        post("/api/actions") {
            println("Trying process action request..")
            val rawBody = call.receiveText()
            println("Raw Body: $rawBody")
            val code = rawBody.substring(rawBody.indexOf(":")+1, rawBody.indexOf(",")).toInt()
            val action = when (code) {
                1 -> Action.Start
                2 -> Action.Pause
                3 -> Action.Terminate
                4 -> Action.Left
                5 -> Action.Right
                6 -> Action.Up
                7 -> Action.Down
                8 -> Action.Action
                else -> {
                    Action.Pause}
            }
                race.userInput(action, false)
                call.respond(HttpStatusCode.OK, "Action executed")
        }

        // Получение текущего состояния игры
        get("/api/state") {
            println("State request accepted")
//            println("Request body (raw): $call")
//            val rawBody = call.request.headers
//            println("Raw Body: $rawBody")
            val g = race.updateCurrentState().toSerializableState()
            val gameState = Json.encodeToString(g)
//            println(gameState)
            call.respond(HttpStatusCode.OK, gameState)
        }
    }
}

@Serializable
data class GamesList(
    val games: List<GameInfo>
)

@Serializable
data class GameInfo(
    val id: Int,
    val name: String
)

@Serializable
data class UserAction(
    val actionId: Action,
    val hold: Boolean
)

fun State.toSerializableState(): SerializableState {
    return SerializableState(
        field = this.field,
        next = this.next,
        score = this.score,
        highScore = this.highScore,
        level = this.level,
        speed = this.speed,
        pause = this.pause
    )
}

@Serializable
data class SerializableState(
    val field: List<List<Boolean>>,
    val next: List<List<Boolean>>,
    val score: Int,
    val highScore: Int,
    val level: Int,
    val speed: Int,
    val pause: Boolean
)

@Serializable
data class ErrorMessage(
    val message: String
)

enum class Action {
    Start,
    Pause,
    Terminate,
    Left,
    Right,
    Up,
    Down,
    Action
}
