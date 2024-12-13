package org.race

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameEngine(private var state: RaceFSM = RaceFSM.Idle) {
    private var gameField: List<Array<Boolean>> = List(20) { Array(10) { false } }
    private var score: Int = 0
    private var highScore: Int = 0
    private var level: Int = 1
    private var speed: Int = 1
    var pause: Boolean = false
    private var rivalCars: MutableList<List<IntArray>> = mutableListOf()
    private val playerCar: MutableList<IntArray> = PlayerCar().car
    private var gameLoopJob: Job? = null
    private var levelTimerJob: Job? = null
    private var stageCount = 0
    private var rivalCarsInterval = 15


    fun userInput(action: Action, hold: Boolean) {
        transition(action)
    }

    fun updateCurrentState(): State {
        return State(
            field = gameFieldToList(gameField),
            next = gameFieldToList(gameField),
            score = score, highScore = score, level = level, speed = speed, pause = pause
        )
    }

    fun movePlayerCar(action: Action) {
        println("Action: ${action.name}")
        if (action == Action.Left) {
            if (playerCar[0][1] > 1) {
                playerCar.forEachIndexed { idx, coord ->
                    playerCar[idx] = intArrayOf(coord[0], coord[1] - 1)
                }
                updateGameField()
            }
        }
        if (action == Action.Right) {
            if (playerCar[0][1] < 8) {
                playerCar.forEachIndexed { idx, coord ->
                    playerCar[idx] = intArrayOf(coord[0], coord[1] + 1)
                }
                updateGameField()

            }
        }
        if (action == Action.Up) {
            speed = (speed + 1).coerceAtLeast(1)
        }
        if (action == Action.Down) {
            speed = (speed - 1).coerceAtLeast(1)
        }
    }

    fun startGame() {
        startGameLoop()
        startLevelTimer()
    }

    private fun startGameLoop() {
        gameLoopJob = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                checkAndSpawnRivalCars()
                shiftGameField()
                stageCount++
                score += 100
                if (checkCollision()) {
                    transition(GameEvents.COLLISION)
                    break
                }
                if (speed > 0) {
                    delay((1000L / speed).coerceAtLeast(100L))
                }
            }
        }
    }


    fun stopGame() {
        stopGameLoop()
        stopLevelTimer()

    }

    private fun stopGameLoop() {
        gameLoopJob?.cancel()
    }

    private fun stopLevelTimer() {
        levelTimerJob?.cancel()
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
            createRivalCar()
        }
    }

    private fun shiftGameField() {
        rivalCars.forEachIndexed { idx, car ->
            rivalCars[idx] = car.map { point ->
                intArrayOf(point[0] + 1, point[1])
            }
        }
        updateGameField()
    }

    private fun createRivalCar(): List<IntArray> {
        val currentInstant: java.time.Instant = java.time.Instant.now()
        val currentTimeStamp: Long = currentInstant.toEpochMilli()

        val line = kotlin.math.abs((currentTimeStamp.toInt() % 3))

        val startPosition = intArrayOf(-4, 1 + line * 3)

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
        rivalCars.add(rivalCar)

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

    fun gameOver() {
        stopGame()
        speed = 0
        println("GAME OVER")
        println("SCORE: $score")
        println("HIGH SCORE: $highScore")
        println("LEVEL: $level")
        println("SPEED: $speed")
        CoroutineScope(Dispatchers.Default).launch {
            delay(1000L)
            resetGame()
            transition(RaceFSM.Idle)
        }
    }

    fun resetGame() {
        gameField = List(20) { Array(10) { false } }
        score = 0
        level = 1
        speed = 1
        rivalCarsInterval = 15
        rivalCars.clear()
    }


    private fun removeOutOfBoundsRivalCars() {
        rivalCars.removeAll { car -> car[0][0] >= 20 }
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
            while (true) {
                delay(60_000L) // 2 минуты в миллисекундах
                increaseLevel()
            }
        }
    }

    private fun increaseLevel() {
        level++
        speed += level / 2
        rivalCarsInterval--
        println("Level increased to: $level, Speed: $speed")
    }

    private fun setFSMStateAndProcess(newState: RaceFSM) {
        if (stateTransitions[state::class]?.contains(newState::class) == true) {
            println("Transition: ${state.title} -> ${newState.title}")
            state = newState
            state.handleState(this)
        } else {
            println("Invalid transition: ${state.title} -> ${newState.title}")
        }
    }

    fun transition(event: Any? = null) {
        val nextState = when (val currentState = state) {
            is RaceFSM.Idle -> if (event == Action.Start) RaceFSM.Start else currentState
            is RaceFSM.Start -> RaceFSM.Moving()
            is RaceFSM.Moving -> when (event) {
                is Action -> when {
                    event.isMovement -> RaceFSM.Moving(event)
                    event == Action.Pause -> RaceFSM.Paused
                    else -> currentState
                }

                GameEvents.COLLISION -> RaceFSM.Collided
                else -> currentState
            }

            is RaceFSM.Paused -> when (event) {
                is Action -> when (event) {
                    Action.Pause -> RaceFSM.Start
                    Action.Terminate -> RaceFSM.GameOver
                    else -> currentState
                }

                else -> currentState
            }

            is RaceFSM.Collided -> RaceFSM.GameOver
            is RaceFSM.GameOver -> RaceFSM.Idle
            else -> currentState
        }
        setFSMStateAndProcess(nextState)
    }


}
