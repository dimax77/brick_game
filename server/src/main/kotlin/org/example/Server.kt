// BrickGameServerClass.kt
package org.example

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    val race = GameEngine()

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
    routing {

//        println("Static folder exists: " + File("C:\\Users\\ebox\\school\\brick_game\\web_gui").exists())

        //mac
        staticFiles("/", File("web_gui"))

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
                else -> {Action.Pause}
            }
                race.userAction(action, false)
                call.respond(HttpStatusCode.OK, "Action executed")
        }

        // Получение текущего состояния игры
        get("/api/state") {
            println("State request accepted")
//            println("Request body (raw): $call")
//            val rawBody = call.request.headers
//            println("Raw Body: $rawBody")
            val g = race.updateCurrentState()
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

@Serializable
data class State(
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
