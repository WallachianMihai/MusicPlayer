package com.viewmodel

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.model.Track
import com.musicplayer.R
import com.musicplayer.TrackPlayer
import com.squareup.picasso.Picasso
import com.utils.OnSwipeTouchListener
import java.util.concurrent.TimeUnit

class TrackViewActivity : AppCompatActivity()
{
    private lateinit var title: TextView
    private lateinit var currentTime: TextView
    private lateinit var totalTime: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var pausePlay: ImageView
    private lateinit var skipNext: ImageView
    private lateinit var skipPrevious: ImageView
    private lateinit var trackIcon: ImageView

    private lateinit var trackList: ArrayList<Track>
    private lateinit var currentTrack: Track

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_player)

        title = findViewById(R.id.song_title)
        currentTime = findViewById(R.id.current_time)
        totalTime = findViewById(R.id.total_time)
        seekBar = findViewById(R.id.seek_bar)
        pausePlay = findViewById(R.id.pause_play)
        skipNext = findViewById(R.id.next)
        skipPrevious = findViewById(R.id.previous)
        trackIcon = findViewById(R.id.music_icon)

        title.isSelected = true

        trackList = intent.getSerializableExtra("TRACKS") as ArrayList<Track>
        currentTrack = trackList[TrackPlayer.currentIndex]

        setTrack(currentTrack)
        AddEventHandelers()

        this@TrackViewActivity.runOnUiThread(Runnable {
            val mainHandler = Handler(Looper.getMainLooper())
            mainHandler.post(object : Runnable
            {
                override fun run()
                {
                    seekBar.setProgress(TrackPlayer.getInstance().currentPosition)
                    currentTime.setText(convertToMMSS(TrackPlayer.getInstance().currentPosition.toString()))

                    mainHandler.postDelayed(this, 100)
                }
            })
        })
    }

    override fun onBackPressed()
    {
        intent = Intent(this, MainActivityStorage::class.java)

        finish()
    }

    fun convertToMMSS(duration: String): String
    {
        val millis = duration.toLong()
        return String.format("%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1))
    }

    @SuppressLint("SetTextI18n")
    fun setTrack(track: Track)
    {
        if(!track.cloud)
        {
            if(track.bitmap != null)
                trackIcon.setImageBitmap(track.bitmap.bitmap)
            else
                trackIcon.setImageResource(R.drawable.ic_no_album_cover)
        }
        else
        {
            Picasso.get().load(track.uri).into(trackIcon)
        }

        title.setText(track.artist + " - " + track.title)
        totalTime.setText(convertToMMSS(TrackPlayer.getInstance().duration.toString()))
        seekBar.setProgress(0)
        seekBar.max = TrackPlayer.getInstance().duration
    }



    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    private fun AddEventHandelers()
    {
        pausePlay.setOnClickListener(
            View.OnClickListener
        {
            if (TrackPlayer.getInstance().isPlaying)
            {
                TrackPlayer.getInstance().pause();
                pausePlay.setImageResource(R.drawable.play_button_64)
            }
            else
            {
                TrackPlayer.getInstance().start()
                pausePlay.setImageResource(R.drawable.pause_button_64)
            }
        })

        skipNext.setOnClickListener(
            View.OnClickListener
        {
            val current = TrackPlayer.currentIndex
            if(current == trackList.size - 1)
            {
                TrackPlayer.currentIndex = 0
                setTrack(trackList.first())
            }
            else
            {
                TrackPlayer.currentIndex = current + 1
                setTrack(trackList.get(current + 1))
            }
        })

        skipPrevious.setOnClickListener(
            View.OnClickListener
        {
            val current = TrackPlayer.currentIndex
            if(current == 0)
            {
                TrackPlayer.currentIndex = trackList.lastIndex
                setTrack(trackList.last())
            }
            else
            {
                TrackPlayer.currentIndex = current - 1
                setTrack(trackList.get(current - 1))
            }
        })

        seekBar.setOnTouchListener(View.OnTouchListener { v, event ->
            val x = event.x.toInt()
            TrackPlayer.getInstance()
                .seekTo(seekBar.min + (seekBar.max * x / seekBar.width))
            true
        })

        trackIcon.setOnTouchListener(object: OnSwipeTouchListener(this) {
            override fun onSwipeLeft()
            {
                onBackPressed()
            }
        })
    }
}
