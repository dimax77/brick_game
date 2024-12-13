package org.race

enum class Action(val isMovement: Boolean = false) {
    Start,
    Pause,
    Terminate,
    Left(true),
    Right(true),
    Up(true),
    Down(true),
    DoAction
}
