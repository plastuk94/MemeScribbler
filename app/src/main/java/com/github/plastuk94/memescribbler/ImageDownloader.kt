package com.github.plastuk94.memescribbler

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.net.URL

open class ImageDownloader(private val url : URL) {
    fun downloadImage(): Bitmap {
        val downloadThread = DownloadThread(url)
        downloadThread.start()
        downloadThread.join()
        return downloadThread.getDownloadedBitmap()
    }
}

class DownloadThread(private val inURL : URL) : Thread() {
    lateinit var bitmap : Bitmap
    override fun run() {
        bitmap = BitmapFactory.decodeStream(inURL.openConnection().getInputStream())
    }
    fun getDownloadedBitmap() : Bitmap {
        return bitmap
    }
}