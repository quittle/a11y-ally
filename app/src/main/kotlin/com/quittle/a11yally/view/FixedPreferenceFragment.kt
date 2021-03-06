package com.quittle.a11yally.view

import android.os.Bundle
import androidx.annotation.XmlRes
import androidx.preference.PreferenceFragmentCompat

/**
 * Preference fragment backed by a fixed resource
 */
class FixedPreferenceFragment : PreferenceFragmentCompat() {
    companion object {
        private const val PREFERENCE_XML_KEY = "preference xml"

        fun newInstance(@XmlRes preferenceXml: Int): FixedPreferenceFragment {
            val fragment = FixedPreferenceFragment()
            val bundle = Bundle(1)
            bundle.putInt(PREFERENCE_XML_KEY, preferenceXml)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(requireArguments().getInt(PREFERENCE_XML_KEY))
    }
}
