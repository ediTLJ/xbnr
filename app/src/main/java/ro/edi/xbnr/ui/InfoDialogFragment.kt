/*
* Copyright 2019 Eduard Scarlat
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package ro.edi.xbnr.ui

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ro.edi.util.getAppVersionName
import ro.edi.xbnr.R

class InfoDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (!isAdded) return super.onCreateDialog(savedInstanceState)

        val dialog = MaterialAlertDialogBuilder(requireActivity())

        dialog.setTitle(R.string.app_name)
        dialog.setMessage(getString(R.string.info, getAppVersionName(requireActivity())))

        dialog.setPositiveButton(R.string.btn_ok, null)
        dialog.setNegativeButton(R.string.btn_rate) { _, _ ->
            val i = Intent(Intent.ACTION_VIEW)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            i.data = Uri.parse("market://details?id=ro.edi.xbnr")
            startActivity(i)
        }
//        dialog.setNeutralButton(R.string.btn_other_apps) { _, _ ->
//            val i = Intent(Intent.ACTION_VIEW)
//            i.data = Uri.parse("market://search?q=pub:Eduard%20Scarlat")
//            startActivity(i)
//        }

        return dialog.create()
    }
}