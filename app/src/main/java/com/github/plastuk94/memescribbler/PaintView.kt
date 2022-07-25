package com.github.plastuk94.memescribbler

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent

class PaintView(context: Context?, attrs: AttributeSet?) :
    androidx.appcompat.widget.AppCompatImageView(
        context!!, attrs
    ) {
    var currentColor: Int = Color.BLACK
    var paintPaint: Paint = Paint()
    var textPaint: Paint = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
    var paintText: String = "Paint Text"
    var paintMode = PaintMode.PAINT
    private var paintList: ArrayList<Paint> = ArrayList<Paint>()
    private var paintModeList : ArrayList<PaintMode> = ArrayList<PaintMode>()
    private var pathList: ArrayList<Path?> = ArrayList<Path?>()

    enum class PaintMode {
        PAINT, PAINT_TEXT, SHAPE, TEXT
    }

    private var canvas: Canvas = Canvas()
    private var isBitmapLoaded = false
    private var paintPath = Path()

    private lateinit var bitmapImg: Bitmap
    fun setBitmap(bitmap: Bitmap) {
        var bitmap = bitmap
        isBitmapLoaded = false
        val aspectRatio = bitmap.width / bitmap.height.toFloat()
        val heightAdjusted = Math.round(width / aspectRatio)
        bitmapImg = Bitmap.createScaledBitmap(bitmap, width, heightAdjusted, true)
        canvas = Canvas(bitmapImg)
        setImageBitmap(bitmapImg)

        // Resize the view to the new bitmap dimensions.
        right = left + bitmap.width
        bottom = top + bitmap.height
        isBitmapLoaded = true
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val eventX = event.x
        val eventY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.i("touch down", String.format("%f, %f", event.x, event.y))
                paintPath.moveTo(eventX, eventY)
            }
            MotionEvent.ACTION_MOVE -> {
                Log.i("touch move", String.format("%f, %f", event.x, event.y))
                paintPath.lineTo(eventX, eventY)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                Log.i(
                    "touch up",
                    String.format("%f, %f, Color: %x", event.x, event.y, currentColor)
                )
                paintList.add(paintPaint)
                pathList.add(paintPath)
                paintModeList.add(paintMode)
                invalidate()
                paintPaint = Paint()
                paintPath = Path()
                initPaint()
            }
        }
        return super.onTouchEvent(event)
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (i in pathList.indices) {
            when (paintModeList[i]) {
                PaintMode.PAINT_TEXT -> canvas.drawTextOnPath(paintText,pathList[i]!!, 0f, 0f, paintList[i])
                else -> canvas.drawPath(pathList[i]!!, paintList[i]);
            }
        }
    }


    init {
        initPaint()
    }

    private fun initPaint() {
        paintPaint.style = Paint.Style.STROKE
        paintPaint.strokeWidth = 5f
        paintPaint.color = currentColor
        paintPaint.textAlign = Paint.Align.LEFT
        paintPaint.flags = Paint.LINEAR_TEXT_FLAG
    }

    fun togglePaintSize(): Float {
        when (paintPaint.strokeWidth) {
            25f -> {
                paintPaint.strokeWidth = 5f
            }
            else -> {
                paintPaint.strokeWidth += 5f
            }
        }
        return paintPaint.strokeWidth
    }
}
