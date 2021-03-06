package com.kotlinlibrary.snackbar

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import android.text.Spanned
import android.text.TextUtils
import android.util.TypedValue
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM
import android.widget.RelativeLayout.ALIGN_PARENT_TOP
import android.widget.TextView
import androidx.annotation.ColorInt
import com.kotlinlibrary.R
import com.kotlinlibrary.snackbar.Snackbar.Gravity
import com.kotlinlibrary.snackbar.Snackbar.Gravity.BOTTOM
import com.kotlinlibrary.snackbar.Snackbar.Gravity.TOP
import com.kotlinlibrary.snackbar.Snackbar.ProgressPosition.LEFT
import com.kotlinlibrary.snackbar.Snackbar.ProgressPosition.RIGHT
import com.kotlinlibrary.snackbar.SwipeDismissTouchListener.DismissCallbacks
import com.kotlinlibrary.snackbar.anim.SnackAnimIconBuilder
import com.kotlinlibrary.snackbar.util.convertDpToPx
import com.kotlinlibrary.snackbar.util.getStatusBarHeightInPx
import com.kotlinlibrary.snackbar.view.SbButton
import com.kotlinlibrary.snackbar.view.SbProgress
import com.kotlinlibrary.snackbar.view.ShadowView

internal class SnackbarView(context: Context) : LinearLayout(context) {

    private val TOP_COMPENSATION_MARGIN = resources.getDimension(R.dimen._16sdp).toInt()
    private val BOTTOM_COMPENSATION_MARGIN = resources.getDimension(R.dimen._30sdp).toInt()

    private lateinit var parentSnackbarContainer: SnackbarContainerView
    private lateinit var gravity: Gravity

    private var isMarginCompensationApplied: Boolean = false

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (!isMarginCompensationApplied) {
            isMarginCompensationApplied = true

            val params = layoutParams as ViewGroup.MarginLayoutParams
            when (gravity) {
                TOP -> params.topMargin = -TOP_COMPENSATION_MARGIN
                BOTTOM -> params.bottomMargin = -BOTTOM_COMPENSATION_MARGIN
            }
            requestLayout()
        }
    }

    internal fun init(
            gravity: Gravity,
            castShadow: Boolean,
            shadowStrength: Int) {
        this.gravity = gravity
        this.orientation = VERTICAL

        // If the bar appears with the bottom, then the shadow needs to added to the top of it,
        // Thus, before the inflation of the bar
        if (castShadow && gravity == BOTTOM) {
            castShadow(ShadowView.ShadowType.TOP, shadowStrength)
        }

        inflate(context, R.layout.snack_bar_view, this)

        // If the bar appears with the top, then the shadow needs to added to the bottom of it,
        // Thus, after the inflation of the bar
        if (castShadow && gravity == TOP) {
            castShadow(ShadowView.ShadowType.BOTTOM, shadowStrength)
        }
    }

    internal fun adjustWitPositionAndOrientation(activity: Activity,
                                                 gravity: Gravity) {
        val snackbarViewLp = RelativeLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        val statusBarHeight = activity.getStatusBarHeightInPx()

        val snackbarViewContentLp =
            findViewById<LinearLayout>(R.id.fbContent).layoutParams as LinearLayout.LayoutParams

        when (gravity) {
            TOP -> {
                snackbarViewContentLp.topMargin = statusBarHeight.plus(TOP_COMPENSATION_MARGIN / 2)
                snackbarViewLp.addRule(ALIGN_PARENT_TOP)
            }
            BOTTOM -> {
                snackbarViewContentLp.bottomMargin = BOTTOM_COMPENSATION_MARGIN
                snackbarViewLp.addRule(ALIGN_PARENT_BOTTOM)
            }
        }
        findViewById<LinearLayout>(R.id.fbContent).layoutParams = snackbarViewContentLp
        layoutParams = snackbarViewLp
    }

    internal fun addParent(snackbarContainerView: SnackbarContainerView) {
        this.parentSnackbarContainer = snackbarContainerView
    }

    internal fun setBarBackgroundDrawable(drawable: Drawable?) {
        if (drawable == null) return

        if (SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            findViewById<LinearLayout>(R.id.fbContent).background = drawable
        } else {
            findViewById<LinearLayout>(R.id.fbContent).setBackgroundDrawable(drawable)
        }
    }

    internal fun setBarBackgroundColor(@ColorInt color: Int?) {
        if (color == null) return
        findViewById<LinearLayout>(R.id.fbContent).setBackgroundColor(color)
    }

    internal fun setBarTapListener(listener: Snackbar.OnTapListener?) {
        if (listener == null) return

        findViewById<LinearLayout>(R.id.fbContent).setOnClickListener {
            listener.onTap(parentSnackbarContainer.parentSnackbar)
        }
    }

    internal fun setTitle(title: String?) {
        if (TextUtils.isEmpty(title)) return

        findViewById<TextView>(R.id.fbTitle).text = title
        findViewById<TextView>(R.id.fbTitle).visibility = VISIBLE
    }

    internal fun setTitleSpanned(title: Spanned?) {
        if (title == null) return

        findViewById<TextView>(R.id.fbTitle).text = title
        findViewById<TextView>(R.id.fbTitle).visibility = VISIBLE
    }

    internal fun setTitleTypeface(typeface: Typeface?) {
        if (typeface == null) return
        findViewById<TextView>(R.id.fbTitle).typeface = typeface
    }

    internal fun setTitleSizeInPx(size: Float?) {
        if (size == null) return
        findViewById<TextView>(R.id.fbTitle).setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
    }

    internal fun setTitleSizeInSp(size: Float?) {
        if (size == null) return
        findViewById<TextView>(R.id.fbTitle).setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
    }

    internal fun setTitleColor(color: Int?) {
        if (color == null) return
        findViewById<TextView>(R.id.fbTitle).setTextColor(color)
    }

    internal fun setTitleAppearance(titleAppearance: Int?) {
        if (titleAppearance == null) return

        if (SDK_INT >= M) {
            findViewById<TextView>(R.id.fbTitle).setTextAppearance(titleAppearance)
        } else {
            findViewById<TextView>(R.id.fbTitle).setTextAppearance(findViewById<TextView>(R.id.fbTitle).context, titleAppearance)
        }
    }

    internal fun setMessage(message: String?) {
        if (TextUtils.isEmpty(message)) return

        findViewById<TextView>(R.id.fbMessage).text = message
        findViewById<TextView>(R.id.fbMessage).visibility = VISIBLE
    }

    internal fun setMessageSpanned(message: Spanned?) {
        if (message == null) return

        findViewById<TextView>(R.id.fbMessage).text = message
        findViewById<TextView>(R.id.fbMessage).visibility = VISIBLE
    }

    internal fun setMessageTypeface(typeface: Typeface?) {
        if (typeface == null) return
        findViewById<TextView>(R.id.fbMessage).typeface = typeface
    }

    internal fun setMessageSizeInPx(size: Float?) {
        if (size == null) return
        findViewById<TextView>(R.id.fbMessage).setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
    }

    internal fun setMessageSizeInSp(size: Float?) {
        if (size == null) return
        findViewById<TextView>(R.id.fbMessage).setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
    }

    internal fun setMessageColor(color: Int?) {
        if (color == null) return
        findViewById<TextView>(R.id.fbMessage).setTextColor(color)
    }

    internal fun setMessageAppearance(messageAppearance: Int?) {
        if (messageAppearance == null) return

        if (SDK_INT >= M) {
            findViewById<TextView>(R.id.fbMessage).setTextAppearance(messageAppearance)
        } else {
            findViewById<TextView>(R.id.fbMessage).setTextAppearance(findViewById<TextView>(R.id.fbMessage).context, messageAppearance)
        }
    }

    internal fun setPrimaryActionText(text: String?) {
        if (TextUtils.isEmpty(text)) return

        findViewById<SbButton>(R.id.fbPrimaryAction).text = text
        findViewById<SbButton>(R.id.fbPrimaryAction).visibility = VISIBLE
    }

    internal fun setPrimaryActionTextSpanned(text: Spanned?) {
        if (text == null) return

        findViewById<SbButton>(R.id.fbPrimaryAction).text = text
        findViewById<SbButton>(R.id.fbPrimaryAction).visibility = VISIBLE
    }

    internal fun setPrimaryActionTextTypeface(typeface: Typeface?) {
        if (typeface == null) return
        findViewById<SbButton>(R.id.fbPrimaryAction).typeface = typeface
    }

    internal fun setPrimaryActionTextSizeInPx(size: Float?) {
        if (size == null) return
        findViewById<SbButton>(R.id.fbPrimaryAction).setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
    }

    internal fun setPrimaryActionTextSizeInSp(size: Float?) {
        if (size == null) return
        findViewById<SbButton>(R.id.fbPrimaryAction).setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
    }

    internal fun setPrimaryActionTextColor(color: Int?) {
        if (color == null) return
        findViewById<SbButton>(R.id.fbPrimaryAction).setTextColor(color)
    }

    internal fun setPrimaryActionTextAppearance(messageAppearance: Int?) {
        if (messageAppearance == null) return

        if (SDK_INT >= M) {
            findViewById<SbButton>(R.id.fbPrimaryAction).setTextAppearance(messageAppearance)
        } else {
            findViewById<SbButton>(R.id.fbPrimaryAction).setTextAppearance(findViewById<SbButton>(R.id.fbPrimaryAction).context, messageAppearance)
        }
    }

    internal fun setPrimaryActionTapListener(listener: Snackbar.OnActionTapListener?) {
        if (listener == null) return

        findViewById<SbButton>(R.id.fbPrimaryAction).setOnClickListener {
            listener.onActionTapped(parentSnackbarContainer.parentSnackbar)
        }
    }

    internal fun setPositiveActionText(text: String?) {
        if (TextUtils.isEmpty(text)) return

        findViewById<LinearLayout>(R.id.fbSecondaryActionContainer).visibility = VISIBLE
        findViewById<SbButton>(R.id.fbPositiveAction).text = text
        findViewById<SbButton>(R.id.fbPositiveAction).visibility = VISIBLE
    }

    internal fun setPositiveActionTextSpanned(text: Spanned?) {
        if (text == null) return

        findViewById<LinearLayout>(R.id.fbSecondaryActionContainer).visibility = VISIBLE
        findViewById<SbButton>(R.id.fbPositiveAction).text = text
        findViewById<SbButton>(R.id.fbPositiveAction).visibility = VISIBLE
    }

    internal fun setPositiveActionTextTypeface(typeface: Typeface?) {
        if (typeface == null) return
        findViewById<SbButton>(R.id.fbPositiveAction).typeface = typeface
    }

    internal fun setPositiveActionTextSizeInPx(size: Float?) {
        if (size == null) return
        findViewById<SbButton>(R.id.fbPositiveAction).setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
    }

    internal fun setPositiveActionTextSizeInSp(size: Float?) {
        if (size == null) return
        findViewById<SbButton>(R.id.fbPositiveAction).setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
    }

    internal fun setPositiveActionTextColor(color: Int?) {
        if (color == null) return
        findViewById<SbButton>(R.id.fbPositiveAction).setTextColor(color)
    }

    internal fun setPositiveActionTextAppearance(messageAppearance: Int?) {
        if (messageAppearance == null) return

        if (SDK_INT >= M) {
            findViewById<SbButton>(R.id.fbPositiveAction).setTextAppearance(messageAppearance)
        } else {
            findViewById<SbButton>(R.id.fbPositiveAction).setTextAppearance(findViewById<SbButton>(R.id.fbPositiveAction).context, messageAppearance)
        }
    }

    internal fun setPositiveActionTapListener(listener: Snackbar.OnActionTapListener?) {
        if (listener == null) return

        findViewById<SbButton>(R.id.fbPositiveAction).setOnClickListener {
            listener.onActionTapped(parentSnackbarContainer.parentSnackbar)
        }
    }

    internal fun setNegativeActionText(text: String?) {
        if (TextUtils.isEmpty(text)) return

        findViewById<LinearLayout>(R.id.fbSecondaryActionContainer).visibility = VISIBLE
        findViewById<SbButton>(R.id.fbNegativeAction).text = text
        findViewById<SbButton>(R.id.fbNegativeAction).visibility = VISIBLE
    }

    internal fun setNegativeActionTextSpanned(text: Spanned?) {
        if (text == null) return

        findViewById<LinearLayout>(R.id.fbSecondaryActionContainer).visibility = VISIBLE
        findViewById<SbButton>(R.id.fbNegativeAction).text = text
        findViewById<SbButton>(R.id.fbNegativeAction).visibility = VISIBLE
    }

    internal fun setNegativeActionTextTypeface(typeface: Typeface?) {
        if (typeface == null) return
        findViewById<SbButton>(R.id.fbNegativeAction).typeface = typeface
    }

    internal fun setNegativeActionTextSizeInPx(size: Float?) {
        if (size == null) return
        findViewById<SbButton>(R.id.fbNegativeAction).setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
    }

    internal fun setNegativeActionTextSizeInSp(size: Float?) {
        if (size == null) return
        findViewById<SbButton>(R.id.fbNegativeAction).setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
    }

    internal fun setNegativeActionTextColor(color: Int?) {
        if (color == null) return
        findViewById<SbButton>(R.id.fbNegativeAction).setTextColor(color)
    }

    internal fun setNegativeActionTextAppearance(messageAppearance: Int?) {
        if (messageAppearance == null) return

        if (SDK_INT >= M) {
            findViewById<SbButton>(R.id.fbNegativeAction).setTextAppearance(messageAppearance)
        } else {
            findViewById<SbButton>(R.id.fbNegativeAction).setTextAppearance(findViewById<SbButton>(R.id.fbNegativeAction).context, messageAppearance)
        }
    }

    internal fun setNegativeActionTapListener(listener: Snackbar.OnActionTapListener?) {
        if (listener == null) return

        findViewById<SbButton>(R.id.fbNegativeAction).setOnClickListener {
            listener.onActionTapped(parentSnackbarContainer.parentSnackbar)
        }
    }

    internal fun showIcon(showIcon: Boolean) {
        findViewById<ImageView>(R.id.fbIcon).visibility = if (showIcon) VISIBLE else GONE
    }

    internal fun showIconScale(scale: Float, scaleType: ImageView.ScaleType?) {
        findViewById<ImageView>(R.id.fbIcon).scaleX = scale
        findViewById<ImageView>(R.id.fbIcon).scaleY = scale
        findViewById<ImageView>(R.id.fbIcon).scaleType = scaleType
    }

    internal fun setIconDrawable(icon: Drawable?) {
        if (icon == null) return
        findViewById<ImageView>(R.id.fbIcon).setImageDrawable(icon)
    }

    internal fun setIconBitmap(bitmap: Bitmap?) {
        if (bitmap == null) return
        findViewById<ImageView>(R.id.fbIcon).setImageBitmap(bitmap)
    }

    internal fun setIconColorFilter(colorFilter: Int?, filterMode: PorterDuff.Mode?) {
        if (colorFilter == null) return
        if (filterMode == null) {
            findViewById<ImageView>(R.id.fbIcon).setColorFilter(colorFilter)
        } else {
            findViewById<ImageView>(R.id.fbIcon).setColorFilter(colorFilter, filterMode)
        }
    }

    internal fun startIconAnimation(animator: SnackAnimIconBuilder?) {
        animator?.withView(findViewById<ImageView>(R.id.fbIcon))?.build()?.start()
    }

    internal fun stopIconAnimation() {
        findViewById<ImageView>(R.id.fbIcon).clearAnimation()
    }

    internal fun enableSwipeToDismiss(enable: Boolean, callbacks: DismissCallbacks) {
        if (enable) {
            findViewById<LinearLayout>(R.id.fbRoot).setOnTouchListener(SwipeDismissTouchListener(this, callbacks))
        }
    }

    internal fun setProgressPosition(position: Snackbar.ProgressPosition?) {
        if (position == null) return
        when (position) {
            LEFT -> {
                findViewById<SbProgress>(R.id.fbLeftProgress).visibility = VISIBLE
                findViewById<SbProgress>(R.id.fbRightProgress).visibility = GONE
            }
            RIGHT -> {
                findViewById<SbProgress>(R.id.fbLeftProgress).visibility = GONE
                findViewById<SbProgress>(R.id.fbRightProgress).visibility = VISIBLE
            }
        }
    }

    internal fun setProgressTint(progressTint: Int?,
                                 position: Snackbar.ProgressPosition?) {
        if (position == null || progressTint == null) return

        val progressBar = when (position) {
            LEFT -> findViewById(R.id.fbLeftProgress)
            RIGHT -> findViewById<SbProgress>(R.id.fbRightProgress)
        }

        progressBar.setBarColor(progressTint)
    }

    private fun castShadow(shadowType: ShadowView.ShadowType, strength: Int) {
        val params = RelativeLayout.LayoutParams(MATCH_PARENT, context.convertDpToPx(strength))
        val shadow = ShadowView(context)
        shadow.applyShadow(shadowType)
        addView(shadow, params)
    }
}