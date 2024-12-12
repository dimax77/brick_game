package org.race

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameEngine(private var state: RaceFSM = RaceFSM.Start) {
    private var gameField: List<Array<Boolean>> = List(20) { Array(10) { false } }
    private var score: Int = 0
    private var highScore: Int = 0
    private var level: Int = 1
    private var speed: Int = 1
    private var pause: Boolean = false
    private var rivalCars: MutableList<List<IntArray>> = mutableListOf()
    private val playerCar: MutableList<IntArray> = PlayerCar().car
    private var gameLoopJob: Job? = null
    private var levelTimerJob: Job? = null
    private var stageCount = 0
    private var rivalCarsInterval = 15


    fun userInput(action: Action, hold: Boolean) {
        when (action) {
            Action.Left -> if (state == RaceFSM.Moving) movePlayerCar(Action.Left)
            Action.Right -> if (state == RaceFSM.Moving) movePlayerCar(Action.Right)
            Action.Up -> if (state == RaceFSM.Moving) speed++
            Action.Down -> if (state == RaceFSM.Moving) speed--

            Action.Start -> if (state == RaceFSM.Start) {
                if (speed == 0) resetGame()
                state = RaceFSM.Moving
                startGameLoop()
                startLevelTimer()
            }

            Action.Pause -> if (state == RaceFSM.Moving || state == RaceFSM.Paused) {
                togglePause()
            }

            Action.Terminate -> {
                state = RaceFSM.GameOver
                gameOver()
            }

            else -> {}
        }
    }

    fun updateCurrentState(): State {
        return State(
            field = gameFieldToList(gameField),
            next = gameFieldToList(gameField),
            score = score, highScore = score, level = level, speed = speed, pause = pause
        )
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
        updateGameField()
    }

    private fun startGameLoop() {
        gameLoopJob = CoroutineScope(Dispatchers.Default).launch {
            while (state != RaceFSM.GameOver) {
                if (state == RaceFSM.Moving) {
                    checkAndSpawnRivalCars()
                    state = RaceFSM.Shift
                    shiftGameField()
                    state = RaceFSM.Moving
                    stageCount++
                    score += 100
                    if (checkCollision()) {
                        state = RaceFSM.Collided
                        gameOver()
                        break
                    }
                }

                if (speed > 0) {
                    delay((1000L / speed).coerceAtLeast(100L))
                }
            }
        }
    }


    private fun togglePause() {
        pause = !pause
        if (pause) {
            state = RaceFSM.Paused
            stopGameLoop()
        } else {
            state = RaceFSM.Moving
            startGameLoop()
        }
    }

    private fun stopGameLoop() {
        gameLoopJob?.cancel()
    }

    private fun updateGameField() {
        gameField = List(20) { Array(10) { false } }
        removeOutOfBoundsRivalCars()
        applyRivalCarOnGameField()
        applyPlayerCarOnGameField()
    }


    private fun checkAndSpawnRivalCars() {
        if (rivalCars.size == 0 || stageCount == rivalCarsInterval) {
            stageCount = 0
            state = RaceFSM.Spawn
            createRivalCar()
            state = RaceFSM.Moving
        }
    }

    private fun shiftGameField() {
        rivalCars.forEachIndexed { idx, car ->
            rivalCars[idx] = car.map { point ->
                intArrayOf(point[0] + 1, point[1])
            }
        }
        updateGameField()
        println("Rival cars count after shift: ${rivalCars.size}")
    }

    private fun createRivalCar(): List<IntArray> {
        val currentInstant: java.time.Instant = java.time.Instant.now()
        val currentTimeStamp: Long = currentInstant.toEpochMilli()

        val line = kotlin.math.abs((currentTimeStamp.toInt() % 3))

        val startPosition = intArrayOf(-4, 1 + line * 3)
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

    private fun checkCollision(): Boolean {
        // Проходим по каждой машине соперника
        rivalCars.forEach { rivalCar ->
            // Проверяем, есть ли хоть одна точка пересечения с машиной игрока
            if (rivalCar.any { rivalPoint ->
                    playerCar.any { playerPoint ->
                        rivalPoint[0] == playerPoint[0] && rivalPoint[1] == playerPoint[1]
                    }
                }) {
                return true // Столкновение обнаружено
            }
        }
        return false // Столкновений нет
    }

    private fun gameOver() {
        stopGameLoop()
        levelTimerJob?.cancel()
        speed = 0
        state = RaceFSM.Start
        println("GAME OVER")
        println("SCORE: $score")
        println("HIGH SCORE: $highScore")
        println("LEVEL: $level")
        println("SPEED: $speed")
    }

    private fun resetGame() {
        gameField = List(20) { Array(10) { false } }
        score = 0
        level = 1
        speed = 1
        rivalCarsInterval = 15
        rivalCars.clear()
    }


    private fun removeOutOfBoundsRivalCars() {
        rivalCars.removeAll { car -> car.any { it[0] >= 20 } }
    }

    private fun applyRivalCarOnGameField(
    ) {
        rivalCars.forEach { rivalCar ->
            rivalCar.forEach {
                if (it[0] in 0..19 && it[1] in 0..9) {
                    gameField[it[0]][it[1]] = true
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

    private fun gameFieldToList(field: List<Array<Boolean>>): List<List<Boolean>> {
        return field.map { it.asList() }
    }

    private fun startLevelTimer() {
        levelTimerJob = CoroutineScope(Dispatchers.Default).launch {
            while (state != RaceFSM.GameOver) {
                delay(60_000L) // 2 минуты в миллисекундах
                if (state == RaceFSM.Moving) {
                    increaseLevel()
                }
            }
        }
    }

    private fun increaseLevel() {
        level++
        speed += level / 2
        rivalCarsInterval--
        println("Level increased to: $level, Speed: $speed")
    }


}
