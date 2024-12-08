package org.example

class GameEngine(var state: RaceFCM = RaceFCM.Start) {
    private var playerLane = 1 // Полоса игрока (0, 1, 2)
    private val enemies = mutableListOf<EnemyCar>()
    private var gameField: Array<Array<Boolean>> = Array(20) { Array(10) { false } }
    var score: Int = 0
    var highScore: Int = 0
    var level: Int = 0
    var speed: Int = 0
    var pause: Boolean = false
    val gameState: State = State()

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

    fun getGameState(): State {
        applyPlayerCarOnGameField()
        return State(
            field = gameFieldToList(gameField),
            next = gameFieldToList(gameField),
            score = score, highScore = highScore, level = level, speed = speed, pause = pause
        )
    }

    fun updateCurrentState(): State {
        return getGameState()
    }

    fun createRivalCar(): List<IntArray> {
        val currentInstant: java.time.Instant = java.time.Instant.now()
        val currentTimeStamp: Long = currentInstant.toEpochMilli()
        val line = (currentTimeStamp / 3).toInt()
        println("Line: $line")
        val line2 = (currentTimeStamp / 3).toInt()
        println("Line: $line2")
        val line3 = (currentTimeStamp / 3).toInt()
        println("Line: $line3")
        val rivalCar = listOf(
            intArrayOf(0, 1), intArrayOf(1, 0), intArrayOf(1, 1), intArrayOf(1, 2),
            intArrayOf(2, 1), intArrayOf(3, 0), intArrayOf(3, 1), intArrayOf(3, 2)
        )
        return rivalCar
    }

    fun userAction(action: Action, hold: Boolean) {
        if (action == Action.Start && state == RaceFCM.Start) {
            gameState.level = 1
            gameState.speed = 1
//            state = RaceFCM.Spawn()
            val currentInstant: java.time.Instant = java.time.Instant.now()
            val currentTimeStamp: Long = currentInstant.toEpochMilli()
            val line = (currentTimeStamp / 3).toInt()
            println("Line: $line")
            val line2 = (currentTimeStamp / 3).toInt()
            println("Line: $line2")
            val line3 = (currentTimeStamp / 3).toInt()
            println("Line: $line3")
        }
    }
}

data class EnemyCar(val lane: Int, var positionY: Int)


data class UserAction(
    val actionId: Action,
    val hold: Boolean
)