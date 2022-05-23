package eput.android.model

import java.math.BigInteger

abstract class UnitHandler(
    private val factors: List<Long>,
    val displayUnits: List<String>,
    val shortDisplayUnits: List<String>,
    var selectedUnit: Int
) {


    fun toUnit(value: BigInteger): BigInteger {
        val factor = factors[selectedUnit]
        return value.divide(BigInteger.valueOf(factor))
    }

    fun fromUnit(value: BigInteger): BigInteger {
        val factor = factors[selectedUnit]
        return value.times(BigInteger.valueOf(factor))
    }

    fun toUnit(value: Float): Float {
        val factor = factors[selectedUnit]
        return value / factor
    }

    fun fromUnit(value: Float): Float {
        val factor = factors[selectedUnit]
        return value * factor
    }

    fun toUnit(value: Double): Double {
        val factor = factors[selectedUnit]
        return value / factor
    }

    fun fromUnit(value: Double): Double {
        val factor = factors[selectedUnit]
        return value * factor
    }
}