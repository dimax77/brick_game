// BrickGameServerClass.kt
package org.example

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import io.ktor.server.http.content.*
import java.io.File

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    routing {

        staticFiles("/", File("web_gui"))

        // Получение списка доступных игр
        get("/api/games") {
            val gamesList = GamesList(
                games = listOf(
                    GameInfo(1, "Race"),
                    GameInfo(2, "Tetris")
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
                gameId == null -> call.respond(HttpStatusCode.BadRequest, ErrorMessage("Invalid gameId"))
                gameId !in 1..2 -> call.respond(HttpStatusCode.NotFound, ErrorMessage("Game not found"))
                else -> call.respond(HttpStatusCode.OK, "Game $gameId started")
            }
        }

        // Выполнить команду игрока
        post("/api/actions") {
            val action = try {
                call.receive<UserAction>()
            } catch (e: Exception) {
                null
            }
            if (action == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorMessage("Invalid action format"))
            } else {
                call.respond(HttpStatusCode.OK, "Action ${action.actionId} executed")
            }
        }

        // Получение текущего состояния игры
        get("/api/state") {
            val gameState = GameState(
                field = List(20) { List(10) { false } },
                next = List(4) { List(4) { false } },
                score = 1200,
                highScore = 5000,
                level = 3,
                speed = 5,
                pause = false
            )
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
    val actionId: Int,
    val hold: Boolean
)

@Serializable
data class GameState(
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
