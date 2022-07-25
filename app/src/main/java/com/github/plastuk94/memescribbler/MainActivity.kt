package com.github.plastuk94.memescribbler

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.slider.Slider
import java.io.IOException


var paintWindow: PopupWindow = PopupWindow()


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val paintView = findViewById<PaintView>(R.id.paint_view)
        val toolNavigationView = findViewById<BottomNavigationView>(R.id.toolNavigationView)
        val paintWindowView: View = layoutInflater.inflate(R.layout.paint_popup_window, null)
        paintWindow = PopupWindow(paintWindowView)
        initPaintWindow(this, paintWindowView, paintView)

        toolNavigationView.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->

            when (item.itemId) {
                R.id.open_image_button -> {
                    Log.d("Test", "Button clicked!")
                    openImageLauncher.launch("image/*")
                }

                R.id.paint_button -> {
                    if (paintWindow.isShowing) {
                        paintWindow.dismiss()
                        paintWindow.isFocusable = false
                        paintView.requestFocus()
                        return@OnItemSelectedListener true
                    }
                    paintWindow.height = RelativeLayout.LayoutParams.WRAP_CONTENT
                    paintWindow.width = RelativeLayout.LayoutParams.WRAP_CONTENT
                    paintWindow.isFocusable = true
                    paintWindow.showAtLocation(findViewById(R.id.mainLayout), 0, 0, 0)
                    return@OnItemSelectedListener true
                }
            }
            true
        })
    }

    private var openImageLauncher = registerForActivityResult(
        GetContent()
    ) { uri ->
        if (uri != null) {
            Log.d("Opened file", uri.toString())
            val paintView = findViewById<PaintView>(R.id.paint_view)
            var imgBitmap: Bitmap? = null
            try {
                imgBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                Log.d("Image", imgBitmap.width.toString())
                Log.d("Image", "Opened bitmap")
            } catch (e: IOException) {
                Log.e("Image", e.toString())
            }
            paintView.setOnClickListener(null)
            paintView.setBitmap(imgBitmap!!)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        paintWindow.dismiss()
    }
}

fun initPaintWindow(context: Context, paintWindowView: View, paintView: PaintView) {

    val redSlider: Slider = paintWindowView.findViewById<Slider>(R.id.red_slider)
    val greenSlider: Slider = paintWindowView.findViewById<Slider>(R.id.green_slider)
    val blueSlider: Slider = paintWindowView.findViewById<Slider>(R.id.blue_slider)
    val paintTextButton: MaterialButton = paintWindowView.findViewById(R.id.paint_text_button)
    val strokeSizeButton: MaterialButton = paintWindowView.findViewById(R.id.stroke_size_button)
    val paintTextField : EditText = paintWindowView.findViewById(R.id.paint_text_field)

    redSlider.addOnChangeListener { _, value, _ ->
        val red = value.toInt()
        val green = greenSlider.value.toInt()
        val blue = blueSlider.value.toInt()
        paintView.paintPaint.setARGB(255, red, green, blue)
        paintView.currentColor = Color.argb(255, red, green, blue)
    }
    greenSlider.addOnChangeListener { _, value, _ ->
        val red = redSlider.value.toInt()
        val green = value.toInt()
        val blue = blueSlider.value.toInt()
        paintView.paintPaint.setARGB(255, red, green, blue)
        paintView.currentColor = Color.argb(255, red, green, blue)
    }
    blueSlider.addOnChangeListener { _, value, _ ->
        val red = redSlider.value.toInt()
        val green = blueSlider.value.toInt()
        val blue = value.toInt()
        paintView.paintPaint.setARGB(255, red, green, blue)
        paintView.currentColor = Color.argb(255, red, green, blue)
    }

    paintTextButton.addOnCheckedChangeListener { button, isChecked ->
        if (isChecked) {
            Log.i("Paint", "Paint text mode enabled")
            button.setIconResource(R.drawable.ic_text)
            button.text = "Text Paint Mode"
            paintView.paintMode = PaintView.PaintMode.PAINT_TEXT
            paintTextField.visibility = View.VISIBLE
            paintTextField.requestFocus()
        } else {
            Log.i("Paint", "Paint text mode disabled")
            button.setIconResource(R.drawable.ic_paint)
            button.text = "Paint Mode"
            paintView.paintMode = PaintView.PaintMode.PAINT
            paintTextField.visibility = View.INVISIBLE

        }
    }

    paintTextField.addTextChangedListener {
        paintView.paintText = paintTextField.text.toString()
    }

    strokeSizeButton.addOnCheckedChangeListener { button, _ ->
        button.iconSize = paintView.togglePaintSize().toInt()
    }

}
