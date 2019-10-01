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

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import ro.edi.util.getAppVersionName
import ro.edi.xbnr.R
import ro.edi.xbnr.ui.util.Helper

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        val prefTheme = preferenceScreen.findPreference<ListPreference>("key_theme")
        prefTheme?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, theme ->
                Helper.setTheme(theme as String?)
                true
            }

        val prefVersion = preferenceScreen.findPreference<Preference>("key_version")
        prefVersion?.apply {
            summary = getAppVersionName(context)
        }
    }
}