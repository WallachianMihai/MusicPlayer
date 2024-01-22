package com.viewmodel

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.model.Track
import com.musicplayer.R
import com.musicplayer.TrackListAdapter
import com.musicplayer.TrackPlayer
import com.utils.OnSwipeTouchListener
import com.utils.SerialBitmap
import java.io.File


class MainActivityStorage : AppCompatActivity()
{
    private lateinit var tracks: ArrayList<Track>
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


    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_storage)

        tracks = ArrayList<Track>()
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


        if (!CheckPermission())
        {
            ReqPermission()
            return;
        }

        GetMusic()
        ShowTracks()
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

                if(currentIndex != TrackPlayer.currentIndex)
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

        this@MainActivityStorage.runOnUiThread(Runnable {
            handler.post(runnable) })
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
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean
            {
                var filteredList = ArrayList<Track>()
                for (track in tracks)
                {
                    if(track.title.lowercase().contains(p0.toString().lowercase()) ||
                       track.album.lowercase().contains(p0.toString().lowercase()) ||
                       track.artist.lowercase().contains(p0.toString().lowercase()))
                    {
                        filteredList.add(track)
                    }
                }

                trackView.adapter = TrackListAdapter(filteredList, applicationContext, trackBitmap, trackBarDetails, progressBar)

                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            R.id.go_cloud ->
            {
                intent = Intent(this, MainActivityCloud::class.java)
                startActivity(intent)
                TrackPlayer.getInstance().stop()
                TrackPlayer.currentIndex = -1
                handler.removeCallbacks(runnable)
                finish()
            }
            R.id.go_storage ->  Toast.makeText(
                this,
                "Storage",
                Toast.LENGTH_SHORT
            ).show()
        }
        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun CheckPermission() : Boolean
    {
        val result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_AUDIO)
        return result == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun ReqPermission()
    {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_MEDIA_AUDIO))
        {
            Toast.makeText(
                this,
                "READ PERMISSION IS REQUIRED, PLEASE ALLOW FROM SETTINGS",
                Toast.LENGTH_SHORT
            ).show()
        }
        else
        {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO),
                123)
        }
    }


    private fun GetMusic()
    {
        val projection = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID)

        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"

        // Display videos in alphabetical order based on their display name.
        val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

        val cursor: Cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder) ?: return

        while (cursor.moveToNext())
        {
            if (File(cursor.getString(3)).exists())
            {
                val result = getAlbumart(cursor.getLong(6))
                val bitmap = if (result == null) null else SerialBitmap(result)
                tracks.add(
                    Track(
                        cursor.getString(0),
                        cursor.getString(1),
                        (Math.round(((cursor.getString(2).toDouble() / 6e+4) * 100.0)) / 100.0).toString(),
                        cursor.getString(3),
                        Math.round((cursor.getDouble(4) * 1e-6) * 100.0) / 100.0,
                        cursor.getString(5),
                        cursor.getString(3).substringAfter("."),
                        bitmap,
                        null,
                        false
                    ))
            }
        }
    }

    fun getAlbumart(album_id: Long?): Bitmap?
    {
        var bm: Bitmap? = null
        try
        {
            val sArtworkUri = Uri
                .parse("content://media/external/audio/albumart")
            val uri = ContentUris.withAppendedId(sArtworkUri, album_id!!)
            val pfd: ParcelFileDescriptor? = contentResolver
                .openFileDescriptor(uri, "r")

            if (pfd != null)
            {
                val fd = pfd.fileDescriptor
                bm = BitmapFactory.decodeFileDescriptor(fd)
            }
        }
        catch (e: Exception)
        {
            println(e.message)
        }
        return bm
    }

    private fun ShowTracks()
    {
        if (tracks.size != 0)
        {
            noTrackView.visibility = View.INVISIBLE
            trackView.layoutManager = LinearLayoutManager(this)
            trackView.adapter = TrackListAdapter(tracks, this@MainActivityStorage, trackBitmap, trackBarDetails, progressBar)
        }
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
                currentIndex = 0;
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
                currentIndex = current + 1
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
                currentIndex = tracks.lastIndex
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
                currentIndex = current - 1
            }
        })

        progressBar.setOnTouchListener(OnTouchListener { v, event ->
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