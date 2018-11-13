package com.quittle.a11yally

import com.quittle.a11yally.view.FixedContentActivity

/**
 * Non-accessibility friendly demo activity
 */
class UnfriendlyActivity : FixedContentActivity() {
    override val layoutId = R.layout.unfriendly_activity
}
