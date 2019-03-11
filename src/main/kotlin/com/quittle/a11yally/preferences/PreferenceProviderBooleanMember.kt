package com.quittle.a11yally.preferences

import android.content.Context
import android.content.SharedPreferences

internal class PreferenceProviderBooleanMember(context: Context, prefKeyId: Int) :
        PreferenceProviderMember<Boolean>(context, prefKeyId, false, SharedPreferences::getBoolean)
