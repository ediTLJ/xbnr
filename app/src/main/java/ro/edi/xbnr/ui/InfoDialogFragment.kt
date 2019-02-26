package ro.edi.xbnr

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import ro.edi.util.getAppVersionName


class InfoDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (!isAdded) return super.onCreateDialog(savedInstanceState)

        val dialog = AlertDialog.Builder(requireActivity())

        dialog.setTitle(R.string.app_name)
        dialog.setMessage(getString(R.string.info, getAppVersionName(requireActivity())))

        dialog.setPositiveButton(R.string.btn_ok, null)
        dialog.setNegativeButton(R.string.btn_rate) { _, _ ->
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("market://details?id=ro.edi.xbnr")
            startActivity(intent)
        }
        dialog.setNeutralButton(R.string.btn_other_apps) { _, _ ->
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("market://search?q=pub:Eduard%20Scarlat")
            startActivity(intent)
        }

        return dialog.create()
    }
}