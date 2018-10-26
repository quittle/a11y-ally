package com.quittle.a11yally

import android.os.Bundle
import android.support.annotation.XmlRes
import android.support.v7.preference.PreferenceFragmentCompat

/**
 * Preference fragment backed by a resource
 * @param preferenceXml The fragment resource
 */
public class FixedPreferenceFragment : PreferenceFragmentCompat() {
    companion object {
        private val PREFERENCE_XML_KEY = "preference xml"

        fun newInstance(@XmlRes preferenceXml: Int): FixedPreferenceFragment {
            val fragment = FixedPreferenceFragment()
            val bundle = Bundle(1)
            bundle.putInt(PREFERENCE_XML_KEY, preferenceXml)
            fragment.setArguments(bundle)
            return fragment
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(getArguments()!!.getInt(PREFERENCE_XML_KEY))
    }
}
