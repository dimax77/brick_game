package org.race

sealed class RaceFSM(val title: String) {
    abstract fun handleState(gameEngine: GameEngine)

    data object Idle : RaceFSM("Idle") {
        override fun handleState(gameEngine: GameEngine) {
            println("Game not started")
            gameEngine.resetGame()
        }
    }

    data object Start : RaceFSM("Start") {
        override fun handleState(gameEngine: GameEngine) {
            println("Game started")
            gameEngine.startGame()
            gameEngine.transition()
        }
    }

    data class Moving(val move: Action? = null) : RaceFSM("Moving") {
        override fun handleState(gameEngine: GameEngine) {
            println(this.title)
            move?.let { println(move.name) }
            if (move != null) {
                gameEngine.movePlayerCar(move)
            }
        }
    }

    data object Collided : RaceFSM("Collided") {
        override fun handleState(gameEngine: GameEngine) {
            println("Collision occurred")
            gameEngine.transition(GameOver)
        }
    }

    data object Reached : RaceFSM("Reached") {
        override fun handleState(gameEngine: GameEngine) {
            println(this.title)
        }
    }

    data object GameOver : RaceFSM("Game Over") {
        override fun handleState(gameEngine: GameEngine) {
            println("Game Over")
            gameEngine.gameOver()
        }
    }

    data object Paused : RaceFSM("Paused") {
        override fun handleState(gameEngine: GameEngine) {
            println(this.title)
            gameEngine.pause = true
            gameEngine.stopGame()
        }
    }
}

val stateTransitions = mapOf(
    RaceFSM.Idle::class to listOf(RaceFSM.Start::class),
    RaceFSM.Start::class to listOf(RaceFSM.Moving::class),
    RaceFSM.Moving::class to listOf(RaceFSM.Reached::class, RaceFSM.Paused::class, RaceFSM.GameOver::class, RaceFSM.Collided::class, RaceFSM.Moving::class),
    RaceFSM.Paused::class to listOf(RaceFSM.GameOver::class, RaceFSM.Start::class),
    RaceFSM.Collided::class to listOf(RaceFSM.GameOver::class),
    RaceFSM.GameOver::class to listOf(RaceFSM.Idle::class)
)


