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
    var paintText: String = "Text"
    var paintMode = PaintMode.PAINT
    private var paintList: ArrayList<Paint> = ArrayList<Paint>()
    private var paintModeList : ArrayList<PaintMode> = ArrayList<PaintMode>()
    private var pathList: ArrayList<Path?> = ArrayList<Path?>()
    private var xVals: ArrayList<Float> = ArrayList<Float>()
    private var yVals: ArrayList<Float> = ArrayList<Float>()
    private var textStringList: ArrayList<String?> = ArrayList<String?>()

    enum class PaintMode {
        PAINT, PAINT_TEXT, TEXT, INSERT_IMAGE, OPEN_IMAGE
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

                when (paintMode) {
                    PaintMode.PAINT, PaintMode.PAINT_TEXT -> {
                        pathList.add(paintPath)
                    }
                    PaintMode.TEXT -> {
                        pathList.add(null)
                    }
                    else -> {}
                }
                textStringList.add(paintText)
                xVals.add(eventX)
                yVals.add(eventY)
                paintList.add(paintPaint)
                paintModeList.add(paintMode)

                invalidate()
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
                PaintMode.PAINT_TEXT -> canvas.drawTextOnPath(textStringList[i]!!,pathList[i]!!, 0f, 0f, paintList[i])
                PaintMode.TEXT -> canvas.drawText(textStringList[i]!!, xVals[i], yVals[i], paintList[i])
                PaintMode.PAINT -> canvas.drawPath(pathList[i]!!, paintList[i]);
                else -> {}
            }
        }
    }


    init {
        initPaint()
    }

    fun initPaint() {
        paintPaint.color = currentColor
        when (paintMode) {
            PaintMode.PAINT, PaintMode.PAINT_TEXT -> {
                paintPaint = Paint()
                paintPaint.style = Paint.Style.STROKE
                paintPaint.strokeWidth = 5f
                paintPaint.textAlign = Paint.Align.LEFT
                paintPaint.flags = Paint.LINEAR_TEXT_FLAG
                paintPaint.textSize = 50f
            }
            PaintMode.TEXT -> {
                paintPaint = Paint()
                paintPaint.textSize = 100f
            }
            else -> {}
        }
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
