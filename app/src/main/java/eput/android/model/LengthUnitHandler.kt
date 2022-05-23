package eput.android.model

import android.content.Context
import eput.android.R

class LengthUnitHandler(context: Context, default: Int) : UnitHandler(
    listOf(1, 10, 100, 10000, 10000000),
    displayUnitRes.map(context::getString),
    shortDisplayUnitRes.map(context::getString),
    default
) {
    companion object {
        private val displayUnitRes = listOf(
            R.string.unit_length_mm,
            R.string.unit_length_cm,
            R.string.unit_length_dm,
            R.string.unit_length_m,
            R.string.unit_length_km
        )
        private val shortDisplayUnitRes = listOf(
            R.string.unit_short_length_mm,
            R.string.unit_short_length_cm,
            R.string.unit_short_length_dm,
            R.string.unit_short_length_m,
            R.string.unit_short_length_km
        )
    }
}