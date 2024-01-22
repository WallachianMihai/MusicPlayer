package com.musicplayer

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.model.Track
import com.model.cloud.Data
import com.squareup.picasso.Picasso

class TrackPlayer private constructor()
{
   companion object
   {
       @Volatile
       private var instance: MediaPlayer? = null

       var currentIndex: Int = -1

       fun getInstance() =
           instance ?: synchronized(this)
           {
               instance ?: MediaPlayer().also { instance = it }
           }

       @SuppressLint("SetTextI18n")
       fun playTrack(track: Track, position: Int, trackBitmap: ImageView, trackBarDetails: TextView, progressBar: ProgressBar)
       {
           if(!track.cloud)
           {
               getInstance().reset()
               currentIndex = position
               if(track.bitmap != null)
               {
                   trackBitmap.setImageBitmap(track.bitmap.bitmap)
               }
               else
               {
                   trackBitmap.setImageResource(R.drawable.ic_no_album_cover)
               }

               trackBarDetails.setText(track.title + "\n" + track.artist +
                       "\n" + track.album)

               getInstance().setDataSource(track.path)
               getInstance().prepare()
               getInstance().start()
               progressBar.setProgress(0)
               progressBar.max = getInstance().duration
           }
           else
           {
               getInstance().reset()
               currentIndex = position
               Picasso.get().load(track.uri).into(trackBitmap)

               trackBarDetails.setText(track.title + "\n" + track.artist +
                       "\n" + track.album)

               getInstance().setDataSource(track.path)
               getInstance().prepare()
               getInstance().start()
               progressBar.setProgress(0)
               progressBar.max = getInstance().duration
           }
       }
   }
}