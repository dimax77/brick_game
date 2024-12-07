package org.example

class GameEngine {
    private var playerLane = 1 // Полоса игрока (0, 1, 2)
    private val enemies = mutableListOf<EnemyCar>()
    private var gameField: Array<Array<Boolean>> = Array(20) { Array(10) { false } }
    var score: Int = 0
    var highScore: Int = 0
    var level: Int = 0
    var speed: Int = 0
    var pause: Boolean = false
    var isGameOver = false
        private set

    fun movePlayerLeft() {
        if (playerLane > 0) playerLane--
    }

    fun movePlayerRight() {
        if (playerLane < 2) playerLane++
    }

    fun gameFieldToList(field: Array<Array<Boolean>>): List<List<Boolean>> {
        return field.asList().map { it.asList() }
    }

    fun updateGame() {
        // Генерация машин противников
        if ((0..10).random() < 2) {
            enemies.add(EnemyCar((0..2).random(), 0))
        }

        // Обновление позиции машин
        enemies.forEach { it.positionY++ }
        enemies.removeIf { it.positionY > 20 }

        // Проверка на столкновение
        isGameOver = enemies.any { it.lane == playerLane && it.positionY == 19 }
    }

    fun applyPlayerCarOnGameField(
        playerCar: List<List<Boolean>> = PlayerCar().car
    ) {
        for (i in 0..19) {
            for (j in 0..9) {
                if (playerCar[i][j]) gameField[i][j] = true
            }
        }
    }

    fun getGameState(): GameState {
        applyPlayerCarOnGameField()
        return GameState(
            field = gameFieldToList(gameField),
            next = gameFieldToList(gameField),
            score = score, highScore = highScore, level = level, speed = speed, pause = pause
        )
    }

    fun updateCurrentState(action: Action, hold: Boolean) {

    }
}

data class EnemyCar(val lane: Int, var positionY: Int)

data class GameState(
    val field: List<List<Boolean>>,
    val next: List<List<Boolean>>,
    val score: Int,
    val highScore: Int,
    val level: Int,
    val speed: Int,
    val pause: Boolean
)

data class PlayerCar(
    val car: List<List<Boolean>> = listOf(
        listOf(false, false, false, false, false, false, false, false, false, false),
        listOf(false, false, false, false, false, false, false, false, false, false),
        listOf(false, false, false, false, false, false, false, false, false, false),
        listOf(false, false, false, false, false, false, false, false, false, false),
        listOf(false, false, false, false, false, false, false, false, false, false),
        listOf(false, false, false, false, false, false, false, false, false, false),
        listOf(false, false, false, false, false, false, false, false, false, false),
        listOf(false, false, false, false, false, false, false, false, false, false),
        listOf(false, false, false, false, false, false, false, false, false, false),
        listOf(false, false, false, false, false, false, false, false, false, false),
        listOf(false, false, false, false, false, false, false, false, false, false),
        listOf(false, false, false, false, false, false, false, false, false, false),
        listOf(false, false, false, false, false, false, false, false, false, false),
        listOf(false, false, false, false, false, false, false, false, false, false),
        listOf(false, false, false, false, false, false, false, false, false, false),
        listOf(false, false, false, false, false, false, false, false, false, false),
        listOf(false, false, false, false, false, true, false, false, false, false),
        listOf(false, false, false, false, true, true, true, false, false, false),
        listOf(false, false, false, false, false, true, false, false, false, false),
        listOf(false, false, false, false, true, true, true, false, false, false),
    )
)


data class UserAction(
    val actionId: Action,
    val hold: Boolean
)