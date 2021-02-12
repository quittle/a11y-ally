package com.quittle.a11yally.base

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Checks if a value is null, setting up a contract to support smart casting
 * @return true if the value is null, otherwise false
 */
@OptIn(ExperimentalContracts::class)
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
@OptIn(ExperimentalContracts::class)
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
@OptIn(ExperimentalContracts::class)
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
    return if (this.isNull()) {
        default
    } else {
        this
    }
}

/**
 * Helper method for handling nullable types to provide a default value
 * @param default Generates the return value if [this] is null
 * @return [this] if not null, otherwise the return value of [default]
 */
fun <T> T?.orElse(default: () -> T): T {
    return if (this.isNull()) {
        default()
    } else {
        this
    }
}

/**
 * Helper method for handling nullable types to provide a default value
 * @param default Generates the return value if [this] is null
 * @return [this] if not null, otherwise the return value of [default]
 */
suspend fun <T> T?.orElse(default: suspend () -> T): T {
    return if (this.isNull()) {
        default()
    } else {
        this
    }
}

/**
 * Replacement for the hook statement. Example usage:
 * ```
 * val messageColor: Color = isError.ifElse(Color.Red, Color.Green)
 * ```
 * @param trueCase The return value if true
 * @param falseCase The return value if false
 * @return Either [trueCase] or [falseCase], depending on the value
 */
fun <T> Boolean.ifElse(trueCase: T, falseCase: T): T {
    return if (this) trueCase else falseCase
}

inline fun <I, reified O> Array<I>.mapArray(transform: (I) -> O): Array<O> {
    return Array(this.size) { i -> transform(this[i]) }
}
