package eput.android.model

import android.content.Context
import eput.android.R

class WeightUnitHandler(context: Context, default: Int) : UnitHandler(
    listOf(1, 1000, 1000000),
    displayUnitRes.map(context::getString),
    shortDisplayUnitRes.map(context::getString),
    default
) {
    companion object {
        private val displayUnitRes = listOf(
            R.string.unit_weight_mg,
            R.string.unit_weight_g,
            R.string.unit_weight_kg
        )
        private val shortDisplayUnitRes = listOf(
            R.string.unit_short_weight_mg,
            R.string.unit_short_weight_g,
            R.string.unit_short_weight_kg
        )
    }
}