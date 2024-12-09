package org.example

private var rivalCars: MutableList<List<IntArray>> = mutableListOf()


fun createRivalCar(): List<IntArray> {
    val currentInstant: java.time.Instant = java.time.Instant.now()
    val currentTimeStamp: Long = currentInstant.toEpochMilli()

    val line = kotlin.math.abs((currentTimeStamp.toInt() % 3))

    var startPosition = intArrayOf(0, 1 + line * 3)
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

fun applyRivalCarOnGameField(
    gameField: List<Array<Boolean>> = List(20) { Array(10) { false } }

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