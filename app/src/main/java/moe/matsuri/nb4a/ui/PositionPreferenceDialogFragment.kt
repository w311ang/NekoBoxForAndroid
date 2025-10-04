package moe.matsuri.nb4a.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.preference.PreferenceDialogFragmentCompat
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.database.DataStore

class PositionPreferenceDialogFragment : PreferenceDialogFragmentCompat() {

    private lateinit var xEditText: EditText
    private lateinit var yEditText: EditText

    companion object {
        fun newInstance(key: String): PositionPreferenceDialogFragment {
            val fragment = PositionPreferenceDialogFragment()
            val args = Bundle(1)
            args.putString(ARG_KEY, key)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        xEditText = view.findViewById(R.id.edit_x)
        yEditText = view.findViewById(R.id.edit_y)

        xEditText.setText(DataStore.floatingPixelX.toString())
        yEditText.setText(DataStore.floatingPixelY.toString())
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            val x = xEditText.text.toString().toIntOrNull() ?: 0
            val y = yEditText.text.toString().toIntOrNull() ?: 0

            DataStore.floatingPixelX = x
            DataStore.floatingPixelY = y

            // Notify service to update position
            val intent = Intent("io.nekohasekai.sagernet.action.UPDATE_FLOATING_PIXEL_POSITION")
            context?.sendBroadcast(intent)
        }
    }
}