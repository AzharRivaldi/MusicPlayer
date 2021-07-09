package com.azhar.mymusic.activities

import android.app.Activity
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.azhar.mymusic.R
import com.azhar.mymusic.utils.SongTimer
import com.azhar.mymusic.utils.SongsManager
import kotlinx.android.synthetic.main.activity_play_song.*
import java.io.IOException
import java.util.*

class PlaySongActivity : AppCompatActivity(), OnSeekBarChangeListener, OnCompletionListener {

    lateinit var mediaPlayer: MediaPlayer
    lateinit var songManager: SongsManager
    lateinit var songTimer: SongTimer
    lateinit var songTitle: String
    var handler = Handler()
    var seekForwardTime = 5000
    var seekBackwardTime = 5000
    var currentSongIndex = 0
    var isShuffle = false
    var isRepeat = false
    var songList = ArrayList<HashMap<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_song)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }

        setSupportActionBar(toolbar)
        assert(supportActionBar != null)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        tvJudulLagu.setSelected(true)

        //get data intent from adapter
        val intent = intent
        val bundle = intent.extras
        if (bundle != null) {
            currentSongIndex = bundle.getInt("songIndex")
        }

        mediaPlayer = MediaPlayer()
        songManager = SongsManager()
        songTimer = SongTimer()

        seekBar.setOnSeekBarChangeListener(this)
        mediaPlayer.setOnCompletionListener(this)
        songList = songManager.getPlayList()

        //get data song
        getPlaySong(currentSongIndex)

        //methods button action
        getButtonSong()
    }

    fun getButtonSong() {
        imagePlay.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                imagePlay.setBackgroundResource(R.drawable.ic_play)
            } else {
                mediaPlayer.start()
                visualizerView.getPathMedia(mediaPlayer)
                imagePlay.setBackgroundResource(R.drawable.ic_pause)
            }
        }

        imageNext.setOnClickListener {
            currentSongIndex = currentSongIndex + 1
            if (currentSongIndex < songList.size) {
                mediaPlayer.stop()
                imagePlay.setBackgroundResource(R.drawable.ic_play)
                getPlaySong(currentSongIndex)
            } else {
                currentSongIndex = currentSongIndex - 1
            }
        }

        imagePrev.setOnClickListener {
            currentSongIndex = currentSongIndex - 1
            if (currentSongIndex >= 0) {
                mediaPlayer.stop()
                imagePlay.setBackgroundResource(R.drawable.ic_play)
                getPlaySong(currentSongIndex)
            } else {
                currentSongIndex = currentSongIndex + 1
            }
        }

        imageForward.setOnClickListener {
            val currentPosition = mediaPlayer.currentPosition
            if (currentPosition + seekForwardTime <= mediaPlayer.duration) {
                mediaPlayer.seekTo(currentPosition + seekForwardTime)
            } else {
                mediaPlayer.seekTo(mediaPlayer.duration)
            }
        }

        imageRewind.setOnClickListener {
            val currentPosition = mediaPlayer.currentPosition
            if (currentPosition - seekBackwardTime >= 0) {
                mediaPlayer.seekTo(currentPosition - seekBackwardTime)
            } else {
                mediaPlayer.seekTo(0)
            }
        }

        imageRepeat.setOnClickListener {
            if (isRepeat) {
                isRepeat = false
                Toast.makeText(this@PlaySongActivity, "Mengulang Lagu", Toast.LENGTH_SHORT).show()
                imageRepeat.setImageResource(R.drawable.btn_repeat)
            } else {
                isRepeat = true
                Toast.makeText(this@PlaySongActivity, "Mengulang Tidak Aktif", Toast.LENGTH_SHORT).show()
                isShuffle = false
                imageRepeat.setImageResource(R.drawable.btn_repeat_focused)
                imageShuffle.setImageResource(R.drawable.btn_shuffle)
            }
        }

        imageShuffle.setOnClickListener {
            if (isShuffle) {
                isShuffle = false
                Toast.makeText(this@PlaySongActivity, "Acak Lagu, Aktif", Toast.LENGTH_SHORT).show()
                imageShuffle.setImageResource(R.drawable.btn_shuffle)
            } else {
                isShuffle = true
                Toast.makeText(this@PlaySongActivity, "Acak Lagu, Tidak Aktif", Toast.LENGTH_SHORT).show()
                isRepeat = false
                imageShuffle.setImageResource(R.drawable.btn_shuffle_focused)
                imageRepeat.setImageResource(R.drawable.btn_repeat)
            }
        }
    }

    private fun getPlaySong(songIndex: Int) {
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(songList[songIndex]["songPath"])
            mediaPlayer.prepare()
            songTitle = songList[songIndex]["songTitle"]?.replace("_", " ").toString()
            tvJudulLagu.text = songTitle
            seekBar.progress = 0
            seekBar.max = 100

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun updateSeekBar() {
        handler.postDelayed(runnable, 100)
    }

    private val runnable: Runnable = object : Runnable {
        override fun run() {
            val totalDuration = mediaPlayer.duration.toLong()
            val currentDuration = mediaPlayer.currentPosition.toLong()
            tvTotalDuration.text = "" + songTimer.milliSecondsToTimer(totalDuration)
            tvCurrentDuration.text = "" + songTimer.milliSecondsToTimer(currentDuration)
            val progress = songTimer.getProgressPercentage(currentDuration, totalDuration)
            seekBar.progress = progress
            handler.postDelayed(this, 100)
        }
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        handler.removeCallbacks(runnable)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        handler.removeCallbacks(runnable)
        val totalDuration = mediaPlayer.duration
        val currentPosition = songTimer.progressToTimer(seekBar.progress, totalDuration)
        mediaPlayer.seekTo(currentPosition)

        //run seekbar
        updateSeekBar()
    }

    override fun onCompletion(mp: MediaPlayer) {
        if (isRepeat) {
            getPlaySong(currentSongIndex)
        } else if (isShuffle) {
            val rand = Random()
            currentSongIndex = rand.nextInt(songList.size - 1 - 0 + 1) + 0
            getPlaySong(currentSongIndex)
        } else {
            currentSongIndex = if (currentSongIndex < songList.size - 1) {
                getPlaySong(currentSongIndex + 1)
                currentSongIndex + 1
            } else {
                getPlaySong(0)
                currentSongIndex = 0
            }
        }
    }

    companion object {
        fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
            val window = activity.window
            val layoutParams = window.attributes
            if (on) {
                layoutParams.flags = layoutParams.flags or bits
            } else {
                layoutParams.flags = layoutParams.flags and bits.inv()
            }
            window.attributes = layoutParams
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

}