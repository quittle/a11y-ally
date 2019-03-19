package com.quittle.a11yally.preferences

import android.content.Context
import android.content.SharedPreferences

internal class PreferenceProviderIntMember(context: Context, prefKeyId: Int) :
        PreferenceProviderMember<Int>(context, prefKeyId, 0,
                SharedPreferences::getInt, SharedPreferences.Editor::putInt)
