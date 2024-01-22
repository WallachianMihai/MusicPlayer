package com.musicplayer.cloud

import com.musicplayer.R
import com.musicplayer.TrackPlayer

import android.R.attr.duration
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.model.Track
import com.model.cloud.Data
import java.util.Timer
import java.util.TimerTask


class CloudTrackListAdapter(var tracks:  List<Track>, val context: Context,
                       val trackBitmap: ImageView, val trackBarDetails: TextView,
                       val progressBar: ProgressBar)
    : RecyclerView.Adapter<CloudTrackListAdapter.ViewHolder>()
{
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        lateinit var titleTextView: TextView
        lateinit var detailTextView: TextView
        lateinit var lengthTextView: TextView
        lateinit var iconImageView: ImageView

        init
        {
            titleTextView = itemView.findViewById(R.id.track_title)
            detailTextView = itemView.findViewById(R.id.track_details)
            lengthTextView = itemView.findViewById(R.id.track_length)
            iconImageView = itemView.findViewById(R.id.track_icon)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val view: View = LayoutInflater.from(context).inflate(R.layout.track_item, parent, false)
        return CloudTrackListAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int
    {
        return tracks.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val track: Track = tracks[position]
        //val title = track.artist + " - " + track.title
        holder.titleTextView.setText(track.title)

        val details = track.artist + " - " + track.album

        holder.detailTextView.setText(details)
        holder.lengthTextView.setText(track.length)

        holder.itemView.setOnClickListener(View.OnClickListener {
            TrackPlayer.playTrack(track, position, trackBitmap, trackBarDetails, progressBar)
        })
    }
}