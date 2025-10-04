package moe.matsuri.nb4a.ui

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference
import io.nekohasekai.sagernet.R

class PositionPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.preference.R.attr.dialogPreferenceStyle,
    defStyleRes: Int = 0
) : DialogPreference(context, attrs, defStyleAttr, defStyleRes) {

    init {
        dialogLayoutResource = R.layout.dialog_position_preference
        setPositiveButtonText(android.R.string.ok)
        setNegativeButtonText(android.R.string.cancel)
    }
}