package com.example.myapplication2.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.widget.ImageView
import android.util.Base64

class ImageReshape {

    fun decodeBase64(base64Image: String): Bitmap {
        val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val width = Math.min(bitmap.width, bitmap.height)

        val squareBitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(squareBitmap)

        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = Color.BLACK

        canvas.drawCircle((width / 2).toFloat(), (width / 2).toFloat(), (width / 2).toFloat(), paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return squareBitmap
    }

    fun setCircularImage(base64Image: String, imageView: ImageView) {
        val originalBitmap = decodeBase64(base64Image)

        val circularBitmap = getCircularBitmap(originalBitmap)

        imageView.setImageBitmap(circularBitmap)
    }
}
