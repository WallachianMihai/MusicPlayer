package com.utils

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable


class SerialBitmap(var bitmap: Bitmap?) : Serializable
{
    @Transient
    var compressFormat = CompressFormat.PNG

    @Transient
    var compressQuality = 100

    fun recycle()
    {
        if (bitmap != null && !bitmap!!.isRecycled) bitmap!!.recycle()
    }

    @Throws(IOException::class)
    private fun writeObject(out: ObjectOutputStream)
    {
        val stream = ByteArrayOutputStream()
        bitmap!!.compress(compressFormat, compressQuality, stream)
        val byteArray = stream.toByteArray()
        out.writeInt(byteArray.size)
        out.write(byteArray)
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(`in`: ObjectInputStream)
    {
        val bufferLength = `in`.readInt()
        val byteArray = ByteArray(bufferLength)
        var pos = 0
        do
        {
            val read = `in`.read(byteArray, pos, bufferLength - pos)
            pos += if (read != -1)
            {
                read
            }
            else
            {
                break
            }
        } while (pos < bufferLength)
        bitmap = BitmapFactory.decodeByteArray(byteArray, 0, bufferLength)
    }
}