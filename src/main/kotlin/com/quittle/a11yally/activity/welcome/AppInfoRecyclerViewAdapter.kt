package com.quittle.a11yally.activity.welcome

import android.app.Activity
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.quittle.a11yally.R
import com.quittle.a11yally.base.RefreshableWeakReference
import com.quittle.a11yally.base.ifElse
import com.quittle.a11yally.view.CheckableConstraintLayout
import kotlinx.parcelize.Parcelize

/**
 * A parcelable equivalent of [AppInfo]. The main difference being the lack of the icon
 */
@Parcelize
data class ParcelableAppInfo(
    val label: String,
    val packageName: String,
    val flags: Int
) : Parcelable

/**
 * A parcelable equivalent of [CheckableAppInfo]. The main difference being the lack of the icon
 */
@Parcelize
data class ParcelableCheckableAppInfo(
    val appInfo: ParcelableAppInfo,
    var isChecked: Boolean
) : Parcelable

/**
 * Holds the relevant information for an application.
 */
data class AppInfo(
    val label: String,
    val packageName: String,
    val flags: Int,
    val icon: RefreshableWeakReference<Drawable>
)

/**
 * Holds the all the relevant information for maintaining state about an application.
 */
data class CheckableAppInfo(val appInfo: AppInfo, var isChecked: Boolean)

/**
 * Holds references to each entry's contents.
 */
class AppInfoRecyclerViewAdapterViewHolder(itemView: CheckableConstraintLayout) :
    RecyclerView.ViewHolder(itemView) {
    val wrapper: CheckableConstraintLayout = itemView
    val icon: ImageView = itemView.findViewById(R.id.icon)
    val title: TextView = itemView.findViewById(R.id.title)
    val subTitle: TextView = itemView.findViewById(R.id.subtitle)
    val divider: View = itemView.findViewById(R.id.divider)
}

/**
 * RecyclerView adapter for displaying rows of selectable apps.
 */
class AppInfoRecyclerViewAdapter(
    private val mActivity: Activity,
    private val mApps: List<CheckableAppInfo>,
    private val mOnCheckedChange: (
        apps: List<CheckableAppInfo>,
        index: Int,
        isChecked: Boolean
    ) -> Unit = { _, _, _ -> },
) :
    RecyclerView.Adapter<AppInfoRecyclerViewAdapterViewHolder>() {
    private val mCheckboxDrawable = RefreshableWeakReference {
        ContextCompat.getDrawable(mActivity, android.R.drawable.checkbox_on_background)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
        AppInfoRecyclerViewAdapterViewHolder {
            val layout = mActivity.layoutInflater.inflate(
                R.layout.welcome2_activity_app_entry, parent, false
            )
            return AppInfoRecyclerViewAdapterViewHolder(layout as CheckableConstraintLayout)
        }

    override fun onBindViewHolder(holder: AppInfoRecyclerViewAdapterViewHolder, position: Int) {
        val checkableAppInfo = mApps[position]
        val appInfo = checkableAppInfo.appInfo

        // Set checked status based on apps
        holder.wrapper.apply {
            setOnCheckedChangedListener(null)
            isChecked = checkableAppInfo.isChecked
            setOnCheckedChangedListener { _, isChecked ->
                checkableAppInfo.isChecked = isChecked
                mOnCheckedChange(mApps, position, isChecked)
            }
        }

        holder.icon.apply {
            val d = StateListDrawable()
            d.addState(intArrayOf(android.R.attr.state_checked), mCheckboxDrawable.get())
            d.addState(intArrayOf(), appInfo.icon.get())
            this.setImageDrawable(d)
        }
        val label = appInfo.label
        val packageName = appInfo.packageName
        holder.title.text = label

        // If the app label is the same as the package, it means there almost definitely wasn't a
        // real app name. There's no point in showing the package name again so just show nothing
        holder.subTitle.text = if (packageName != label) {
            packageName
        } else {
            null
        }

        holder.divider.visibility = (position < mApps.size - 1).ifElse(View.VISIBLE, View.GONE)
    }

    override fun getItemCount(): Int {
        return mApps.size
    }
}
