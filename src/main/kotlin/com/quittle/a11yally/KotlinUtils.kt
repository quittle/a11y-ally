package com.quittle.a11yally

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Checks if a value is null, setting up a contract to support smart casting
 * @return true if the value is null, otherwise false
 */
@UseExperimental(ExperimentalContracts::class)
fun Any?.isNull(): Boolean {
    contract {
        returns(false) implies (this@isNull !== null)
    }

    return this === null
}

/**
 * Checks if a value is not null, setting up a contract to support smart casting
 * @return true if the value is nonnull, otherwise false
 */
@UseExperimental(ExperimentalContracts::class)
fun Any?.isNotNull(): Boolean {
    contract {
        returns(true) implies (this@isNotNull !== null)
    }

    return !this.isNull()
}

/**
 * Invokes the [then] function if the value is nonnull
 */
inline fun <T> T?.ifNotNull(then: (T) -> Unit) {
    if (this.isNotNull()) {
        then(this)
    }
}
