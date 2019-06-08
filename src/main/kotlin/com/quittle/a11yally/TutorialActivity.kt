package com.quittle.a11yally

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import com.quittle.a11yally.view.FixedContentActivity
import androidx.viewpager.widget.ViewPager
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.quittle.a11yally.preferences.PreferenceProvider
import com.quittle.a11yally.preferences.withPreferenceProvider

class TutorialActivity : FixedContentActivity() {
    override val layoutId = R.layout.tutorial_activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        class TutorialPagerAdapter(val mContainer: ViewGroup) : PagerAdapter() {

            override fun instantiateItem(collection: ViewGroup, position: Int): Any {
                return mContainer.getChildAt(position)
            }

            override fun getCount(): Int {
                return mContainer.childCount - 1
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                // Do nothing
            }

            override fun isViewFromObject(arg0: View, arg1: Any): Boolean {
                return arg0 === arg1 as View
            }
        }


        // Set the ViewPager adapter
        findViewById<ViewPager>(R.id.pager).run {
            adapter = TutorialPagerAdapter(this)
        }

        findViewById<View>(R.id.lets_go).setOnClickListener {
            withPreferenceProvider(this) {
                setShowTutorial(false)
            }
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
