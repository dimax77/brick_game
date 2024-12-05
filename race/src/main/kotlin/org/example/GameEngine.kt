package org.example

class GameEngine {
    private var playerLane = 1 // Полоса игрока (0, 1, 2)
    private val enemies = mutableListOf<EnemyCar>()
    var isGameOver = false
        private set
    var speed = 1

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
}

data class EnemyCar(val lane: Int, var positionY: Int)