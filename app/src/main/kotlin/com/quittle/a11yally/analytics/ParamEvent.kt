package com.quittle.a11yally.analytics

enum class ParamEvent {
    UX,
    PREFERENCE
}

const val EVENT_UX_ACTION = "ux_action"
const val EVENT_PREFERENCE_CHANGE = "preference_change"

enum class EventUxAction {
    CLICK
}

enum class EventPreferenceChange {
    SET,
    REMOVE
}
