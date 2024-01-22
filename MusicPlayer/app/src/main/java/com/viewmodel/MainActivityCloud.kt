package com.viewmodel

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.model.Track
import com.model.cloud.CloudTracks
import com.model.cloud.Data
import com.musicplayer.R
import com.musicplayer.TrackListAdapter
import com.musicplayer.TrackPlayer
import com.musicplayer.cloud.CloudTrackListAdapter
import com.utils.OnSwipeTouchListener
import com.utils.cloud.ApiInterface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit

class MainActivityCloud : AppCompatActivity()
{
    private var tracks: ArrayList<Track> = ArrayList()
    private lateinit var noTrackView: TextView
    private lateinit var trackView: RecyclerView
    private lateinit var trackBitmap: ImageView
    private lateinit var trackBarDetails: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var play_pause_button: ImageButton
    private lateinit var next_button: ImageButton
    private lateinit var previous_button: ImageButton
    private lateinit var trackLayout: ConstraintLayout
    private lateinit var parentLayout: RelativeLayout
    private lateinit var runnable: Runnable
    private lateinit var handler: Handler
    private var currentIndex = -1

    private val retrofitBuilder = Retrofit.Builder()
        .baseUrl("https://deezerdevs-deezer.p.rapidapi.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiInterface::class.java)


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_cloud)

        noTrackView = findViewById(R.id.noTrackView)
        trackView = findViewById(R.id.trackView)
        trackBitmap = findViewById(R.id.trackBitmap)
        trackBarDetails = findViewById(R.id.trackBarDetails)
        progressBar = findViewById(R.id.progressBar)
        play_pause_button = findViewById<ImageButton>(R.id.play_pause_button)
        next_button = findViewById<ImageButton>(R.id.next_button)
        previous_button = findViewById<ImageButton>(R.id.previous_button)
        trackLayout = findViewById(R.id.track_layout)
        parentLayout = findViewById(R.id.parent_layout)
        handler = Handler(Looper.getMainLooper())

        AddEventHandelers()

        runnable = object: Runnable
        {
            override fun run()
            {
                progressBar.setProgress(TrackPlayer.getInstance().currentPosition)

                if(TrackPlayer.getInstance().isPlaying)
                {
                    play_pause_button.setImageResource(R.drawable.pause_button)
                }
                else
                {
                    play_pause_button.setImageResource(R.drawable.play_button)
                }

                if(currentIndex != TrackPlayer.currentIndex && tracks.size != 0)
                {
                    currentIndex = TrackPlayer.currentIndex
                    TrackPlayer.playTrack(
                        tracks[currentIndex],
                        currentIndex,
                        trackBitmap,
                        trackBarDetails,
                        progressBar
                    )
                }
                handler.postDelayed(this, 100)
            }
        }

        this@MainActivityCloud.runOnUiThread(Runnable {
            handler.post(runnable)
        })
    }

    fun convertToMMSS(duration: String): String
    {
        val millis = duration.toLong()
        return String.format("%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean
    {
        menuInflater.inflate(R.menu.menu, menu)

        val menuItem = menu.findItem(R.id.action_search)
        val searchView = menuItem.actionView as androidx.appcompat.widget.SearchView
        searchView.queryHint = "Search music"

        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean
            {
                noTrackView.visibility = View.INVISIBLE
                trackView.layoutManager = LinearLayoutManager(this@MainActivityCloud)

                val data = retrofitBuilder.getData(p0.toString().lowercase())

                data.enqueue(object : Callback<CloudTracks?> {
                    override fun onResponse(call: Call<CloudTracks?>, response: Response<CloudTracks?>)
                    {
                        val dataList = response.body()?.data!!
                        tracks = Track.GetTracks(dataList)
                        trackView.adapter = CloudTrackListAdapter(tracks, this@MainActivityCloud, trackBitmap, trackBarDetails, progressBar)
                    }

                    override fun onFailure(call: Call<CloudTracks?>, t: Throwable)
                    {
                        Log.d("TAG: FAILURE", "FAILURE " + t.message)
                    }
                })
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean
            {
               return false
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            R.id.go_storage ->
            {
                intent = Intent(this, MainActivityStorage::class.java)
                startActivity(intent)
                TrackPlayer.getInstance().stop()
                TrackPlayer.currentIndex = -1
                handler.removeCallbacks(runnable)
                finish()
            }
            R.id.go_cloud ->  Toast.makeText(
                this,
                "Cloud",
                Toast.LENGTH_SHORT
            ).show()
        }
        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    private fun AddEventHandelers()
    {
        play_pause_button.setOnClickListener(View.OnClickListener
        {
            if (TrackPlayer.getInstance().isPlaying)
            {
                TrackPlayer.getInstance().pause();
                play_pause_button.setImageResource(R.drawable.pause_button)
            }
            else
            {
                TrackPlayer.getInstance().start()
                play_pause_button.setImageResource(R.drawable.play_button)
            }
        })

        next_button.setOnClickListener(View.OnClickListener
        {
            val current = TrackPlayer.currentIndex
            if(current == tracks.size - 1)
            {
                TrackPlayer.playTrack(tracks.first(), 0, trackBitmap, trackBarDetails, progressBar)
            }
            else
            {
                TrackPlayer.playTrack(
                    tracks.get(current + 1),
                    current + 1,
                    trackBitmap,
                    trackBarDetails,
                    progressBar
                )
            }
        })

        previous_button.setOnClickListener(View.OnClickListener
        {
            val current = TrackPlayer.currentIndex
            if(current == 0)
            {
                TrackPlayer.playTrack(
                    tracks.last(),
                    tracks.lastIndex,
                    trackBitmap,
                    trackBarDetails,
                    progressBar
                )
            }
            else
            {
                TrackPlayer.playTrack(
                    tracks.get(current - 1),
                    current - 1,
                    trackBitmap,
                    trackBarDetails,
                    progressBar
                )
            }
        })

        progressBar.setOnTouchListener(View.OnTouchListener { v, event ->
            val x = event.x.toInt()
            TrackPlayer.getInstance()
                .seekTo(progressBar.min + (progressBar.max * x / progressBar.width))
            true
        })

        trackLayout.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, TrackViewActivity::class.java)
            intent.putExtra("TRACKS", tracks)
            startActivity(intent)
        })

        parentLayout.setOnTouchListener(object: OnSwipeTouchListener(this) {
            override fun onSwipeRight()
            {
                trackLayout.callOnClick()
            }
        })
    }
}