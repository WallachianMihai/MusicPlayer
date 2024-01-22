package com.model

import com.model.cloud.Data
import com.utils.SerialBitmap
import com.squareup.picasso.Picasso
import java.io.Serializable
import java.util.concurrent.TimeUnit

data class Track(val title: String,
                 val artist: String,
                 val length: String,
                 val path: String,
                 val size: Double,
                 val album: String,
                 val type: String,
                 val bitmap: SerialBitmap?,
                 val uri: String?,
                 val cloud: Boolean) : Serializable
{
   companion object {
       fun GetTracks(data: List<Data>): ArrayList<Track>
       {
           val list: ArrayList<Track> = ArrayList()

           for (track in data)
           {
               list.add(Track(track.title, track.artist.name, String.format("%02d:%02d",
                   TimeUnit.SECONDS.toMinutes(track.duration.toLong()) % TimeUnit.HOURS.toMinutes(1),
                   TimeUnit.SECONDS.toSeconds(track.duration.toLong()) % TimeUnit.MINUTES.toSeconds(1)),
                   track.preview, 0.0, track.album.title,
                   track.type, null, track.album.cover, true))
           }

           return list;
       }
   }
}