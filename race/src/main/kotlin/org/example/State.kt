package org.example

data class State(
    val field: List<List<Boolean>>,
    val next: List<List<Boolean>>,
    var score: Int = 0,
    var highScore: Int = 0,
    var level: Int = 0,
    var speed: Int = 0,
    var pause: Boolean = false
)

data class PlayerCar(
    val car: MutableList<IntArray> = mutableListOf(
        intArrayOf(15, 4), intArrayOf(16, 3), intArrayOf(16, 4), intArrayOf(16, 5),
        intArrayOf(17, 4), intArrayOf(18, 3), intArrayOf(18, 4), intArrayOf(18, 5),
        intArrayOf(19, 4)
    )
)
