package eput.android.model

import android.content.Context
import eput.android.R

class TimeUnitHandler(context: Context, default: Int) : UnitHandler(
    listOf(1, 1000, 1000 * 60, 1000 * 60 * 60, 1000 * 60 * 60 * 24),
    displayUnitRes.map(context::getString),
    shortDisplayUnitRes.map(context::getString),
    default
) {
    companion object {
        private val displayUnitRes = listOf(
            R.string.unit_time_ms,
            R.string.unit_time_s,
            R.string.unit_time_m,
            R.string.unit_time_h,
            R.string.unit_time_d
        )
        private val shortDisplayUnitRes = listOf(
            R.string.unit_short_time_ms,
            R.string.unit_short_time_s,
            R.string.unit_short_time_m,
            R.string.unit_short_time_h,
            R.string.unit_short_time_d
        )
    }
}