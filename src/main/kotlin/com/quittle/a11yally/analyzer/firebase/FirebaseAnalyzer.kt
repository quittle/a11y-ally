package com.quittle.a11yally.analyzer.firebase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.quittle.a11yally.analyzer.AccessibilityIssue
import com.quittle.a11yally.analyzer.AccessibilityIssueListener
import com.quittle.a11yally.analyzer.AccessibilityItemEventListener
import com.quittle.a11yally.analyzer.IssueType
import com.quittle.a11yally.getDisplayMetrics
import com.quittle.a11yally.isNull
import com.quittle.a11yally.permission.ScreenRecordingActivity
import com.quittle.a11yally.permission.ScreenRecordingActivity.Companion.sScreenRecordingCode
import com.quittle.a11yally.permission.ScreenRecordingActivity.Companion.sScreenRecordingIntent
import com.quittle.a11yally.permission.startScreenRecordingActivity
import com.quittle.a11yally.preferences.PreferenceProvider

/**
 * Analyzes the contents of the screen using Firebase OCR to find and identify properties of text
 * It is an AccessibilityItemEventListener so it knows what text is relevant to scan and what isn't.
 * It also is a privacy issue if the app is scanning every app on the user's device instead of just
 * the one under test.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class FirebaseAnalyzer(context: Context, private val mListener: AccessibilityIssueListener) :
        AccessibilityItemEventListener {
    private val mContext = context.applicationContext
    private var mMinTextHeightSp: Float
    private var mScreenDensity: Int
    private var mScreenHeight: Int
    private var mScreenWidth: Int
    private var mPreviousTinyText: Set<AccessibilityIssue> = HashSet()
    private var mImageReader: ImageReader? = null
    private var mMediaProjection: MediaProjection? = null
    var mIsProcessing = false
    private val mFirebaseTextAnalyzer: FirebaseTextAnalyzer
    private val mPreferenceProvider: PreferenceProvider
    private val mHighlightSmallTextSizeLiveData: LiveData<Int>

    init {
        val displayMetrics = context.getDisplayMetrics()

//        // Source https://material.io/design/typography/the-type-system.html#
//        val minTextSizeSp = 20f
//        mMinTextHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minTextSizeSp, displayMetrics)

        mScreenDensity = displayMetrics.densityDpi
        mScreenHeight = displayMetrics.heightPixels
        mScreenWidth = displayMetrics.widthPixels
        mFirebaseTextAnalyzer = FirebaseTextAnalyzer(mContext)
        mPreferenceProvider = PreferenceProvider(mContext)
        mPreferenceProvider.onResume()
        mHighlightSmallTextSizeLiveData = mPreferenceProvider.getHighlightSmallTextSizeLiveData()
        mHighlightSmallTextSizeLiveData.observeForever { smallTextSizeSp ->
            mMinTextHeightSp = calculateMinTextHeightPx(smallTextSizeSp)
        }
        mMinTextHeightSp = calculateMinTextHeightPx(mPreferenceProvider.getHighlightSmallTextSize())
    }

    private fun calculateMinTextHeightPx(minTextSizeSp: Int): Float {
        return minTextSizeSp.toFloat()
//        val displayMetrics = mContext.getDisplayMetrics()
//        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, minTextSizeSp.toFloat(), displayMetrics)
    }

    override fun onAccessibilityNodeInfo(node: AccessibilityNodeInfo) {}

    override fun onAccessibilityEventStart() {
        // TODO: Possibly start recording if coming back from a non-whitelisted app
    }

    override fun onAccessibilityEventEnd() {}

    override fun onNonWhitelistedApp() {
        stopRecording()
    }

    override fun onPause() {
        stopRecording()
        mPreferenceProvider.onPause()
    }

    private fun stopRecording() {
        mListener.onInvalidateIssues()
        mListener.onIssues(listOf())
        mMediaProjection?.stop()
        mMediaProjection = null
        mImageReader?.close()
        mImageReader = null
        // TODO: Stop the ongoing processing and verify it can be resumed
    }

    companion object {
        private const val TAG = "FirebaseAnalyzer"
    }

    override fun onResume() {
        mPreferenceProvider.onResume()
        if (ScreenRecordingActivity.hasPermission()) {
            startRecording()
            return
        } else {
            // TODO: Suspect that this means nothing will be recorded
            startScreenRecordingActivity(mContext)
        }
    }

    // TODO: Link to StackOverflow source
    private fun getBitmapFromImage(image: Image): Bitmap {
        val cropRect = image.cropRect
        val width = cropRect.width()
        val height = cropRect.height()
        val plane = image.planes[0]
        val buffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * width
        val bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
        // TODO: Try using BitmapFactory.decodeByteBuffer instead
        buffer.rewind()
        bitmap.copyPixelsFromBuffer(buffer)
        return bitmap
    }

    private fun onFirebaseImage(image: Image) {
        // FirebaseVisionImage.fromMediaImage doesn't support bitmap images
        // https://firebase.google.com/docs/reference/android/com/google/firebase/ml/vision/common/FirebaseVisionImage#public-static-firebasevisionimage-frommediaimage-image-image,-int-rotation
        val bitmapImage = getBitmapFromImage(image)
        val firebaseImage = FirebaseVisionImage.fromBitmap(bitmapImage)
        val textRecognizer = FirebaseVision.getInstance().onDeviceTextRecognizer

        textRecognizer.processImage(firebaseImage)
                .addOnSuccessListener { firebaseVisionText ->
                    val tinyTextSet = HashSet<AccessibilityIssue>()
                    firebaseVisionText.textBlocks.forEach { block ->
                        if (block.text != "TT" && block.text != "T" && block.text != "TI" && mFirebaseTextAnalyzer.doesTextBlockContainSmallText(block, mMinTextHeightSp)) {
                            tinyTextSet.add(AccessibilityIssue(IssueType.SmallText, block.boundingBox!!, mapOf("text" to block.text)))
                        }

//                        // TODO: Why not use FirebaseTextAnalyzer and work at the block level?
//                        block.lines.forEach { line ->
//                            line.boundingBox?.let {
//                                if (it.height() < mMinTextHeight) {
//                                    tinyTextSet.add(AccessibilityIssue(IssueType.SmallText, it, mapOf("text" to line.text)))
//                                }
//                            }
//                        }
                    }
                    // TODO: Due to lag, only report if last two scans were identical. otherwise it's often inaccurate
//                    val consistentText = tinyTextSet.intersect(mPreviousTinyText)
//                    if (consistentText.isNotEmpty()) {
//                        mListener.onIssues(consistentText)
//                    } else {
//                        mListener.onInvalidateIssues()
////                        mListener.onIssues(tinyTextSet)
//                    }
                    mListener.onIssues(tinyTextSet)
                    mPreviousTinyText = tinyTextSet

                    Log.i(TAG, "Found ${tinyTextSet}")
                    Log.i(TAG, "Processed successfully and found ${tinyTextSet.size} issues")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed to process", e)
                }
                .addOnCompleteListener {
                    mIsProcessing = false
                }
    }

    private fun startRecording() {
        val mediaProjectionManager = ContextCompat.getSystemService(mContext, MediaProjectionManager::class.java)
        if (mediaProjectionManager.isNull()) {
            Log.e(TAG, "Unable to get MediaProjectionManager service")
            return
        }

        lateinit var projection: MediaProjection
        try {
            projection = mediaProjectionManager.getMediaProjection(sScreenRecordingCode!!, sScreenRecordingIntent!!)
        } catch (e: RuntimeException) {
            Log.e(TAG, "Unable to start recording", e)
            return
        }

        val imageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 1)
        imageReader.setOnImageAvailableListener({ reader ->
            if (mIsProcessing) {
                reader.acquireLatestImage().close()
            } else {
                mIsProcessing = true
                reader.acquireLatestImage()?.use(this::onFirebaseImage)
            }
        }, null)
        val surface = imageReader.surface
        // Hold the reference to ensure it doesn't get garbage collected and abandon the surface
        mImageReader = imageReader

        projection.createVirtualDisplay(
                "A11y Ally",
                mScreenWidth, mScreenHeight,
                mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, // TODO maybe it should be VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY and draw on virtual display
                surface,
                null /*Callbacks*/,
                null /*Handler*/)

        projection.registerCallback(object: MediaProjection.Callback() {
            override fun onStop() {
                super.onStop()
                Log.i(TAG, "Stopping callback")
                this@FirebaseAnalyzer.onPause()
            }
        }, null)

        mMediaProjection = projection
    }
}
