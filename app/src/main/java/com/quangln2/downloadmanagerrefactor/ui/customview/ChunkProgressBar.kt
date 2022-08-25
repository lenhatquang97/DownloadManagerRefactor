package com.quangln2.downloadmanagerrefactor.ui.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class ChunkProgressBar
@JvmOverloads constructor
    (
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val chunkSize = 5
    private val padding = 16f
    var percentArr = listOf(0.0, 0.0, 0.0, 0.0, 0.0)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(width, 30)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        val w = width.toFloat()
        val h = height.toFloat()

        if (percentArr.size == 1) {
            drawLinePart(1, canvas, w, h, percentArr[0].toFloat())
        } else {
            for (i in 1..chunkSize) {
                drawLinePart(i, canvas, w, h, percentArr[i - 1].toFloat())
            }
        }

        postInvalidate()
        super.onDraw(canvas)
    }

    private fun drawLinePart(i: Int, canvas: Canvas?, width: Float, height: Float, percent: Float) {
        val left = 0f

        val rectf = RectF(
            left + (width / chunkSize * (i - 1)),
            0f + padding,
            width / chunkSize * (i - 1 + percent),
            height
        )
        paint.color = if (i % 2 == 0) Color.BLACK else Color.RED
        canvas?.drawRoundRect(rectf, 0f, 0f, paint)

    }

}