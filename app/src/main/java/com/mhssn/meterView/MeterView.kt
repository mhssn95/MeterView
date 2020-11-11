package com.mhssn.meterView

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

class MeterView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    companion object {
        private const val START_ANGLE = 135f
        private const val SWEEP_ANGLE = 270f
        private const val SLICE_WIDTH = 5f
    }

    private var textPaint: TextPaint = TextPaint().apply {
        textSize = 18.toDp(context).toFloat()
        color = Color.BLACK
        flags = Paint.ANTI_ALIAS_FLAG
    }

    private var arcCutPaint: Paint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
    private lateinit var arcPaint: Paint

    private var textWidth: Float = 0f
    private var textHeight: Float = 0f

    private var _progress = 0f

    var progress: Float = 0f
        set(value) {
            invalidateTextPaintAndMeasurements()
            animateToNewValue(value)
            field = value
        }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        invalidateTextPaintAndMeasurements()
    }

    private fun invalidateTextPaintAndMeasurements() {
        textPaint.let {
            textWidth = it.measureText(progress.format())
            textHeight = it.fontMetrics.bottom
        }
    }

    private fun animateToNewValue(newValue: Float) {
        val animator = ValueAnimator.ofFloat(_progress, newValue)
        animator.addUpdateListener {
            val currentValue = it.animatedValue as Float
            _progress = currentValue
            invalidateTextPaintAndMeasurements()
            invalidate()
        }
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = 300
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //drawing main arc
        canvas.drawArc(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            START_ANGLE,
            SWEEP_ANGLE * _progress,
            true,
            arcPaint
        )

        //cutting the center of the main arc
        canvas.drawCircle(width / 2f, height / 2f, 50.toDp(context).toFloat(), arcCutPaint)

        //slice arc to small slices
        repeat((SWEEP_ANGLE / SLICE_WIDTH).toInt()) {
            if (it % 2 != 0)
                canvas.drawArc(
                    0f,
                    0f,
                    width.toFloat(),
                    height.toFloat(),
                    START_ANGLE + (it * SLICE_WIDTH),
                    SLICE_WIDTH,
                    true,
                    arcCutPaint
                )
        }

        //drawing progress text
        progress.let {
            canvas.drawText(
                it.format(),
                (width - textWidth) / 2,
                (height + textHeight) / 2,
                textPaint
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 120.toDp(context)
        val desiredHeight = 120.toDp(context)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        var width: Int = desiredWidth
        var height: Int = desiredHeight

        when (widthMode) {
            MeasureSpec.EXACTLY -> width = widthSize
            MeasureSpec.AT_MOST -> width = Math.min(desiredWidth, widthSize)
            MeasureSpec.UNSPECIFIED -> width = desiredWidth
        }
        when (heightMode) {
            MeasureSpec.EXACTLY -> height = heightSize
            MeasureSpec.AT_MOST -> height = Math.min(desiredHeight, heightSize)
            MeasureSpec.UNSPECIFIED -> height = desiredHeight
        }

        //we need viewWidth and viewHeight to init arcPaint
        initArcPaint(width, height)
        setMeasuredDimension(width, height)
    }

    private fun initArcPaint(width: Int, height: Int) {
        arcPaint = Paint().apply {
            flags = Paint.ANTI_ALIAS_FLAG

            val colors = arrayOf(
                R.color.meterRed.getColor(context),
                R.color.meterOrange.getColor(context),
                R.color.meterGreen.getColor(context)
            ).toIntArray()

            val positions = arrayOf(0f, 0.5f, 1f).toFloatArray()

            shader = SweepGradient(
                width / 2f,
                height / 2f,
                colors,
                positions
            ).apply {
                val rotate = START_ANGLE
                val gradientMatrix = Matrix()
                gradientMatrix.preRotate(
                    rotate,
                    width / 2f,
                    height / 2f
                )
                setLocalMatrix(gradientMatrix)
            }
        }
    }
}