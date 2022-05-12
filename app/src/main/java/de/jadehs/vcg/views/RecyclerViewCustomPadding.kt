package de.jadehs.vcg.views

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.doOnLayout
import androidx.recyclerview.widget.RecyclerView
import de.jadehs.vcg.R

class RecyclerViewCustomPadding(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs) {
    private var paddingLeftPercent: Float
    private var paddingRightPercent: Float
    private var paddingTopPercent: Float
    private var paddingBottomPercent: Float

    private var showFadingEdge: Boolean

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.RecyclerViewCustomPadding, 0, 0).apply {
            try {
                var default = 0.0F;
                default = validPercentOrDefault(getFloat(R.styleable.RecyclerViewCustomPadding_paddingPercentage, default), default)
                paddingLeftPercent = validPercentOrDefault(getFloat(R.styleable.RecyclerViewCustomPadding_paddingPercentageLeft, default), default)
                paddingRightPercent = validPercentOrDefault(getFloat(R.styleable.RecyclerViewCustomPadding_paddingPercentageRight, default), default)
                paddingTopPercent = validPercentOrDefault(getFloat(R.styleable.RecyclerViewCustomPadding_paddingPercentageTop, default), default)
                paddingBottomPercent = validPercentOrDefault(getFloat(R.styleable.RecyclerViewCustomPadding_paddingPercentageBottom, default), default)
                showFadingEdge = getBoolean(R.styleable.RecyclerViewCustomPadding_showFadingEdge, false)
            } finally {
                recycle()
            }
        }
        this.doOnLayout {
            val width = it.width
            val height = it.height
            it.setPadding((width * paddingLeftPercent).toInt(), (height * paddingTopPercent).toInt(), (width * paddingRightPercent).toInt(), (height * paddingBottomPercent).toInt())

        }
    }


    private fun validPercentOrDefault(value: Float, default: Float): Float {
        return if (value < 0 || value > 1) default else value

    }


    override fun isPaddingOffsetRequired(): Boolean {
        return !clipToPadding
    }

    override fun getLeftPaddingOffset(): Int {
        return if (clipToPadding) 0 else -paddingLeft
    }

    override fun getTopPaddingOffset(): Int {
        return if (clipToPadding) 0 else -paddingTop
    }

    override fun getRightPaddingOffset(): Int {
        return if (clipToPadding) 0 else paddingRight
    }

    override fun getBottomPaddingOffset(): Int {
        return if (clipToPadding) 0 else paddingBottom
    }


}