package com.onlive.trackify.ui.statistics

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.onlive.trackify.R
import kotlin.math.min

class DonutChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = ContextCompat.getColor(context, R.color.md_theme_light_surfaceVariant)
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.md_theme_light_onSurface)
        textAlign = Paint.Align.CENTER
        textSize = 40f
    }

    private val secondaryTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.md_theme_light_onSurfaceVariant)
        textAlign = Paint.Align.CENTER
        textSize = 28f
    }

    private val rect = RectF()
    private var items: List<ChartItem> = emptyList()
    private var centerText: String = ""
    private var centerSubText: String = ""
    private var donutWidth = 60f

    fun setData(items: List<ChartItem>, centerText: String, centerSubText: String) {
        this.items = items
        this.centerText = centerText
        this.centerSubText = centerSubText
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (items.isEmpty()) return

        val totalValue = items.sumOf { it.value }
        if (totalValue <= 0) return

        val width = width.toFloat()
        val height = height.toFloat()
        val minDimension = min(width, height)

        val padding = minDimension * 0.15f
        val centerX = width / 2
        val centerY = height / 2
        val radius = (minDimension / 2) - padding

        rect.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        var startAngle = -90f

        for (item in items) {
            val sweepAngle = (item.value / totalValue * 360).toFloat()
            try {
                paint.color = Color.parseColor(item.color)
            } catch (e: Exception) {
                paint.color = Color.GRAY
            }
            canvas.drawArc(rect, startAngle, sweepAngle, true, paint)
            startAngle += sweepAngle
        }

        paint.color = Color.WHITE
        val innerRadius = radius - donutWidth
        canvas.drawCircle(centerX, centerY, innerRadius, paint)

        textPaint.textSize = innerRadius * 0.4f
        canvas.drawText(centerText, centerX, centerY, textPaint)

        secondaryTextPaint.textSize = innerRadius * 0.25f
        canvas.drawText(centerSubText, centerX, centerY + (textPaint.textSize * 0.8f), secondaryTextPaint)
    }

    data class ChartItem(
        val value: Double,
        val color: String
    )
}