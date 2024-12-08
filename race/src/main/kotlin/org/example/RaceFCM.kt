package org.example

sealed class RaceFCM {
    data object Start : RaceFCM()
    class Spawn(val rivalCar: List<List<Boolean>>) : RaceFCM()
    class Moving(val playerAction: Action) : RaceFCM()
    class Shift(val gameField: List<List<Boolean>>) : RaceFCM()
    class Collided(val gameField: List<List<Boolean>>) : RaceFCM()
    data object Reached : RaceFCM()
    data object GameOver : RaceFCM()
}