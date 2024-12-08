package org.example

data class State(
    val field: List<List<Boolean>> = PlayerCar().car,
    val next: List<List<Boolean>> = PlayerCar().car,
    val score: Int = 0,
    val highScore: Int = 0,
    var level: Int = 0,
    var speed: Int = 0,
    val pause: Boolean = false
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