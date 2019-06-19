package com.quittle.a11yally.preferences

import android.content.Context
import android.content.SharedPreferences

internal class PreferenceProviderStringSetMember(context: Context, prefKeyId: Int) :
        PreferenceProviderMember<Set<String>>(context, prefKeyId, emptySet(),
                SharedPreferences::getStringSet, SharedPreferences.Editor::putStringSet)
