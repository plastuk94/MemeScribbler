package com.github.plastuk94.memescribbler

import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.slider.Slider
import java.io.IOException
import java.net.URL


var toolWindow: PopupWindow = PopupWindow()

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val paintView = findViewById<PaintView>(R.id.paint_view)
        val toolNavigationView = findViewById<BottomNavigationView>(R.id.toolNavigationView)

        toolNavigationView.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.open_image_button -> {
                    val alertBuilder = AlertDialog.Builder(this)
                    val editURL = EditText(this)
                    editURL.inputType = InputType.TYPE_TEXT_VARIATION_URI
                    val dialogListener = DialogInterface.OnClickListener { _, i ->
                        when (i) {
                            DialogInterface.BUTTON_POSITIVE -> {
                                val urlText = editURL.text.toString()
                                val imageDownloader = ImageDownloader(URL(urlText))
                                paintView.setBitmap(imageDownloader.downloadImage())
                            }
                            DialogInterface.BUTTON_NEGATIVE -> {
                                openImageLauncher.launch("image/*")
                            }
                        }
                    }
                    alertBuilder.setMessage("Open from file or URL?")
                        .setPositiveButton("URL", dialogListener)
                        .setNegativeButton("File", dialogListener)
                        .setView(editURL).show()
                    paintView.paintMode = PaintView.PaintMode.OPEN_IMAGE
                    return@OnItemSelectedListener true
                }

                R.id.insert_image_button -> {
                    paintView.paintMode = PaintView.PaintMode.INSERT_IMAGE
                    openImageLauncher.launch("image/*")
                    return@OnItemSelectedListener true
                }

                R.id.paint_button -> {
                    paintView.paintMode = PaintView.PaintMode.PAINT
                    toolWindow = PopupWindow(initToolWindow(this, paintView))
                    paintView.initPaint()
                }

                R.id.text_button -> {
                    paintView.paintMode = PaintView.PaintMode.TEXT
                    toolWindow = PopupWindow(initToolWindow(this, paintView))
                    paintView.initPaint()
                }
            }
            if (toolWindow.isShowing) {
                toolWindow.dismiss()
                toolWindow.isFocusable = false
                paintView.requestFocus()
                return@OnItemSelectedListener true
            }
            toolWindow.height = RelativeLayout.LayoutParams.WRAP_CONTENT
            toolWindow.width = RelativeLayout.LayoutParams.WRAP_CONTENT
            toolWindow.isFocusable = true
            toolWindow.showAtLocation(findViewById(R.id.mainLayout), 0, 0, 0)
            return@OnItemSelectedListener true
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
            if (paintView.paintMode == PaintView.PaintMode.OPEN_IMAGE) {
                paintView.setBitmap(imgBitmap!!)
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        toolWindow.dismiss()
    }
}

fun initToolWindow(context: Context, paintView: PaintView): View {
    var toolWindowView: View = when (paintView.paintMode) {
        PaintView.PaintMode.TEXT -> {
            LayoutInflater.from(context).inflate(R.layout.text_popup_window, null)
        }
        else -> {
            LayoutInflater.from(context).inflate(R.layout.paint_popup_window, null)
        }

    }

    val redSlider: Slider = toolWindowView.findViewById(R.id.red_slider)
    val greenSlider: Slider = toolWindowView.findViewById(R.id.green_slider)
    val blueSlider: Slider = toolWindowView.findViewById(R.id.blue_slider)
    val sizeButton: MaterialButton = toolWindowView.findViewById(R.id.size_button)
    val textField: EditText = toolWindowView.findViewById(R.id.text_field)
    when (paintView.paintMode) {
        PaintView.PaintMode.PAINT, PaintView.PaintMode.PAINT_TEXT -> {
            val paintTextButton: MaterialButton = toolWindowView.findViewById(R.id.paint_text_button)
            paintTextButton.addOnCheckedChangeListener { button, isChecked ->
                if (isChecked) {
                    Log.i("Paint", "Paint text mode enabled")
                    button.setIconResource(R.drawable.ic_text)
                    button.text = "Text Paint Mode"
                    paintView.paintMode = PaintView.PaintMode.PAINT_TEXT
                    textField.visibility = View.VISIBLE
                    textField.requestFocus()
                } else {
                    Log.i("Paint", "Paint text mode disabled")
                    button.setIconResource(R.drawable.ic_paint)
                    button.text = "Paint Mode"
                    paintView.paintMode = PaintView.PaintMode.PAINT
                    textField.visibility = View.INVISIBLE
                }
            }
        }
        PaintView.PaintMode.TEXT -> {}
        else -> {}
    }
    textField.addTextChangedListener {
        paintView.paintText = textField.text.toString()
    }

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

    sizeButton.addOnCheckedChangeListener { button, _ ->
        button.iconSize = paintView.togglePaintSize().toInt()
    }

    return toolWindowView
}
