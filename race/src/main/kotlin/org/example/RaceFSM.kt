package org.example

sealed class RaceFSM (val title: String){
    data object Start : RaceFSM("Start")
    data object Spawn : RaceFSM("Spawn")
    data object Moving : RaceFSM("Moving")
    data object Shift : RaceFSM("Shift")
    data object Collided : RaceFSM("Collided")
    data object Reached : RaceFSM("Reached")
    data object GameOver : RaceFSM("Game Over")
}