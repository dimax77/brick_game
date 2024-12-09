package org.example

class GameEngine(private var state: RaceFSM = RaceFSM.Start) {
    private var gameField: List<Array<Boolean>> = List(20) { Array(10) { false } }
    var score: Int = 0
    var highScore: Int = 0
    var level: Int = 0
    var speed: Int = 0
    var pause: Boolean = false
    private var rivalCars: MutableList<List<IntArray>> = mutableListOf()
    private val playerCar: MutableList<IntArray> = PlayerCar().car

    var stageCount = 0

    private fun gameFieldToList(field: List<Array<Boolean>>): List<List<Boolean>> {
        return field.map { it.asList() }
    }

    private fun movePlayerCar(action: Action) {
        if (action == Action.Left) {
            if (playerCar[0][1] > 1) {
                playerCar.forEachIndexed { idx, coord ->
                    playerCar[idx] = intArrayOf(coord[0], coord[1] - 1)
                }
            }
        }
        if (action == Action.Right) {
            if (playerCar[0][1] < 8) {
                playerCar.forEachIndexed { idx, coord ->
                    playerCar[idx] = intArrayOf(coord[0], coord[1] + 1)
                }
            }
        }
    }

    private fun applyPlayerCarOnGameField(
    ) {
        playerCar.forEach {
            gameField[it[0]][it[1]] = true
        }
    }

    private fun applyRivalCarOnGameField(
    ) {
        rivalCars.forEachIndexed { idx, rivalCar ->
            if (rivalCar[0][0] == 19) {
                rivalCars.removeAt(idx)
            } else {
                rivalCar.forEach {
                    if (it[0] in 0..19 && it[1] in 0..9) {
                        gameField[it[0]][it[1]] = true
                    }
                }
            }
        }
    }


    fun updateCurrentState(): State {
        if (state == RaceFSM.Shift) {
            gameField.clear()
            rivalCars.forEachIndexed { idx, it ->
                val currentPositionY = it[idx][0] + 1
                val currentPositionX = it[idx][1]
                val startPosition = intArrayOf(currentPositionY, currentPositionX)
                val rivalCar = listOf(
                    startPosition,
                    intArrayOf(startPosition[0] + 1, startPosition[1] - 1),
                    intArrayOf(startPosition[0] + 1, startPosition[1]),
                    intArrayOf(startPosition[0] + 1, startPosition[1] + 1),
                    intArrayOf(startPosition[0] + 2, startPosition[1]),
                    intArrayOf(startPosition[0] + 3, startPosition[1] - 1),
                    intArrayOf(startPosition[0] + 3, startPosition[1]),
                    intArrayOf(startPosition[0] + 3, startPosition[1] + 1)
                )
                rivalCars[idx] = rivalCar
            }
            applyRivalCarOnGameField()
            applyPlayerCarOnGameField()
            println("Rival cars count after shift: ${rivalCars.size}")
        }
        stageCount++
        if (stageCount == 15) {
            state = RaceFSM.Spawn
            stageCount = 0
        }
        if (state == RaceFSM.Spawn) {
            createRivalCar()
            state = RaceFSM.Shift
        }



        return State(
            field = gameFieldToList(gameField),
            next = gameFieldToList(gameField),
            score = score, highScore = highScore, level = level, speed = speed, pause = pause
        )
    }

    private fun createRivalCar(): List<IntArray> {
        val currentInstant: java.time.Instant = java.time.Instant.now()
        val currentTimeStamp: Long = currentInstant.toEpochMilli()

        val line = kotlin.math.abs((currentTimeStamp.toInt() % 3))

        val startPosition = intArrayOf(0, 1 + line * 3)
        println("-------------------------------------")
        println("     RIVAL CAR LANE: $line")
        println("     CAR CREATED AT POSITION: ${startPosition[0]}:${startPosition[1]}")
        println("-------------------------------------")

        val rivalCar = listOf(
            startPosition,
            intArrayOf(startPosition[0] + 1, startPosition[1] - 1),
            intArrayOf(startPosition[0] + 1, startPosition[1]),
            intArrayOf(startPosition[0] + 1, startPosition[1] + 1),
            intArrayOf(startPosition[0] + 2, startPosition[1]),
            intArrayOf(startPosition[0] + 3, startPosition[1] - 1),
            intArrayOf(startPosition[0] + 3, startPosition[1]),
            intArrayOf(startPosition[0] + 3, startPosition[1] + 1)
        )
        println("Rival Cars size: ${rivalCars.size}")
        rivalCars.add(rivalCar)
        println("Rival Cars size: ${rivalCars.size}")

        return rivalCar
    }

    fun userAction(action: Action, hold: Boolean) {
        if (action == Action.Start && state == RaceFSM.Start) {
            state = RaceFSM.Spawn
        } else if (action == Action.Terminate) {
            state = RaceFSM.Start
        } else if (action == Action.Left || action == Action.Right && state == RaceFSM.Shift
            || state == RaceFSM.Spawn
        ) {
            state = RaceFSM.Moving
            movePlayerCar(action)
            state = RaceFSM.Shift
        }
    }
}

private fun List<Array<Boolean>>.clear() {
    this.forEach { line ->
        for (i in 0..9)
            line[i] = false
    }
}

data class EnemyCar(val lane: Int, var positionY: Int)


data class UserAction(
    val actionId: Action,
    val hold: Boolean
)



// Scratches
private var playerLane = 1 // Полоса игрока (0, 1, 2)
private val enemies = mutableListOf<EnemyCar>()
private var isGameOver = false

fun movePlayerLeft() {
    if (playerLane > 0) playerLane--
}

fun movePlayerRight() {
    if (playerLane < 2) playerLane++
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