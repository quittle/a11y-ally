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
 * Checks if a number is null or zero, setting up a contract to support smart casting
 * @return true if the value is either null or equal to 0
 */
@UseExperimental(ExperimentalContracts::class)
fun Number?.isNullOrZero(): Boolean {
    contract {
        returns(false) implies (this@isNullOrZero !== null)
    }

    return this.isNull() || this.toInt() == 0
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

/**
 * Enables easy looping over items without explicitly creating a collection
 */
fun <T> forEach(vararg args: T, callable: (T) -> Unit) {
    args.forEach(callable)
}

/**
 * Helper method for handling nullable types to provide a default value
 * @param default The value to return if [this] is null
 * @return [this] if not null, otherwise [default]
 */
fun <T> T?.orElse(default: T): T {
    if (this.isNull()) {
        return default
    } else {
        return this
    }
}
