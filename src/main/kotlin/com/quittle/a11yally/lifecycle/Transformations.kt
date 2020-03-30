package com.quittle.a11yally.lifecycle

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations

fun negateLiveData(source: LiveData<Boolean>): LiveData<Boolean> {
    return Transformations.map(source, Boolean::not)
}
