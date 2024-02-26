package com.vachanammusic

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.ActivityCompat


import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AudioPlayerService : Service() {
    private var conf: SharedPreferences? = null
    private val mBinder: IBinder = LocalBinder()
    var songs = ArrayList<HashMap<String, Any>>()
    var player: SimpleExoPlayer? = null
    var ui = false
    private var mediaSession: MediaSessionCompat? = null
    private var songTitle = ""
    private var artistName = ""
    private var isPlaying = false
    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSessionCompat(this, "media_session")
        mediaSession!!.setFlags(
            MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        )
        mediaSession!!.setCallback(MediaSessionCallback())
        mediaSession!!.isActive = true
    }




    override fun onDestroy() {
        super.onDestroy()
        mediaSession?.release()
        player?.release()
        player = null
    }


    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            player!!.playWhenReady = true
            isPlaying = true
            showNotification(songTitle, artistName, true)
        }

        override fun onPause() {
            player!!.playWhenReady = false
            isPlaying = false
            showNotification(songTitle, artistName, false)
        }

        override fun onSkipToNext() {
            val nextWindowIndex = player!!.currentWindowIndex + 1
            if (nextWindowIndex < songs.size) {
                player!!.seekTo(nextWindowIndex, 0)
            } else {
                player!!.seekTo(0, 0)
                player!!.playWhenReady = false
            }
        }

        override fun onSkipToPrevious() {
            val previousWindowIndex = player!!.currentWindowIndex - 1
            if (previousWindowIndex >= 0) {
                player!!.seekTo(previousWindowIndex, 0)
            } else {
                player!!.seekTo(songs.size - 1, 0)
                player!!.playWhenReady = false
            }
        }

        override fun onSeekTo(pos: Long) {
            player!!.seekTo(pos)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            // Handle the case when the intent is null
            return START_NOT_STICKY
        }

        val action = intent.action
        if ("com.vachanammusic.PAUSE" == action) {
            player!!.playWhenReady = false
            isPlaying = false
            return START_NOT_STICKY
        } else if ("com.vachanammusic.PLAY" == action) {
            player!!.playWhenReady = true
            isPlaying = true
            return START_NOT_STICKY
        } else if ("com.vachanammusic.NEXT" == action) {
            val nextWindowIndex = player!!.currentWindowIndex + 1
            if (nextWindowIndex < songs.size) {
                player!!.seekTo(nextWindowIndex, 0)
            } else {
                player!!.seekTo(0, 0)
                player!!.playWhenReady = false
            }
            return START_NOT_STICKY
        } else if ("com.vachanammusic.PREVIOUS" == action) {
            val previousWindowIndex = player!!.currentWindowIndex - 1
            if (previousWindowIndex >= 0) {
                player!!.seekTo(previousWindowIndex, 0)
            } else {
                player!!.seekTo(songs.size - 1, 0)
                player!!.playWhenReady = false
            }
            return START_NOT_STICKY
        } else if ("com.vachanammusic.SEEK" == action) {
            val seekPosition = intent.getIntExtra("seekPosition", 0)
            player!!.seekTo(seekPosition.toLong())
            return START_NOT_STICKY
        }



    val context: Context = this
        conf = getSharedPreferences("conf", MODE_PRIVATE)

//        int pos = (int) Double.parseDouble(intent.getStringExtra("pos"));
        var pos = 0
        if (intent.hasExtra("pos") && intent.getStringExtra("pos") != null) {
            pos = intent.getStringExtra("pos")!!.toDouble().toInt()
            // Rest of your code within onStartCommand
        } else {
            // Handle the case when "pos" extra is not available or null
        }
        songs = Gson().fromJson(
            conf?.getString("playList", ""),
            object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
        )
        if (player == null) {
            player = ExoPlayerFactory.newSimpleInstance(this, DefaultTrackSelector())
        }
        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "AudioApp"))
        val concatenatingMediaSource = ConcatenatingMediaSource()
        for (temp in songs) {
            val mediaSource: MediaSource =
                ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(
                    Uri.parse(
                        temp["song_url"].toString()
                    )
                )
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        player!!.prepare(concatenatingMediaSource)
        player!!.seekTo(pos, 0)
        player!!.playWhenReady = true
        player!!.addListener(myPlayerListener)
        return START_STICKY
    }

    private fun showNotification(title: String, artist: String, isPlaying: Boolean) {
        // Notification channel creation for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "audio_player",
                "VachanamMusic",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Intent for opening the app when notification is clicked
        val openAppIntent = packageManager.getLaunchIntentForPackage("com.vachanammusic")
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        // Intent for media actions
        val mediaIntent = Intent(this, AudioPlayerService::class.java)

        val pausePendingIntent = PendingIntent.getService(
            this,
            0,
            mediaIntent.setAction("com.vachanammusic.PAUSE"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val playPendingIntent = PendingIntent.getService(
            this,
            0,
            mediaIntent.setAction("com.vachanammusic.PLAY"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val nextPendingIntent = PendingIntent.getService(
            this,
            0,
            mediaIntent.setAction("com.vachanammusic.NEXT"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val previousPendingIntent = PendingIntent.getService(
            this,
            0,
            mediaIntent.setAction("com.vachanammusic.PREVIOUS"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )


            val builder = NotificationCompat.Builder(this, "audio_player")
                .setSmallIcon(R.drawable.app_logo)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setContentIntent(openAppPendingIntent)
                .addAction(R.drawable.ic_previous, "Previous", previousPendingIntent)
                .addAction(
                    if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                    if (isPlaying) "Pause" else "Play",
                    if (isPlaying) pausePendingIntent else playPendingIntent
                )
                .addAction(R.drawable.ic_next, "Next", nextPendingIntent)
                .setContentTitle(title)
                .setContentText(artist)

            val mediaMetadata = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .build()

            val playbackStateBuilder = PlaybackStateCompat.Builder()
                .setState(
                    if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                    player!!.currentPosition,
                    1.0f
                )
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                            PlaybackStateCompat.ACTION_SEEK_TO
                )
            val playbackState = playbackStateBuilder.build()

            builder.setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2, 3, 4)
                    .setMediaSession(mediaSession?.sessionToken)
            )

            mediaSession?.setMetadata(mediaMetadata)
            mediaSession?.setPlaybackState(playbackState)



        val density = resources.displayMetrics.density
        val xDp = 2000
        val yDp = 2000
        val x = (xDp * density).toInt()
        val y = (yDp * density).toInt()
        val currentWindowIndex = player!!.currentWindowIndex
        val coverUri = songs[currentWindowIndex]["song_cover"].toString()
        Glide.with(applicationContext)
            .asBitmap()
            .load(coverUri)
            .override(x, y)
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    val scaledBitmap = Bitmap.createScaledBitmap(resource, x, y, false)
                    builder.setLargeIcon(resource)
                    val mediaMetadata = MediaMetadataCompat.Builder()
                        .putString(
                            MediaMetadataCompat.METADATA_KEY_TITLE,
                            songs[currentWindowIndex]["song_title"].toString()
                        )
                        .putString(
                            MediaMetadataCompat.METADATA_KEY_ARTIST,
                            songs[currentWindowIndex]["artist_alias"].toString()
                        )
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, player!!.duration)
                        .putBitmap(
                            MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                            scaledBitmap
                        ) // Use the scaled bitmap
                        .build()
                    val playbackStateBuilder = PlaybackStateCompat.Builder()
                        .setState(
                            if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                            player!!.currentPosition,
                            1.0f
                        )
                        .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or PlaybackStateCompat.ACTION_SEEK_TO or PlaybackStateCompat.ACTION_PLAY_PAUSE)
                    val playbackState = playbackStateBuilder.build()
                    builder.setFullScreenIntent(openAppPendingIntent, true)
                    mediaSession?.setMetadata(mediaMetadata)
                    mediaSession?.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
                    mediaSession?.setPlaybackState(playbackState)


                    val notificationManager = NotificationManagerCompat.from(this@AudioPlayerService)
                    if (ActivityCompat.checkSelfPermission(
                            this@AudioPlayerService,
                            Manifest.permission.FOREGROUND_SERVICE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions

                        return
                    }
                    notificationManager.notify(1, builder.build())
                    startForeground(1, builder.build())

                }
            })
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    inner class LocalBinder : Binder() {
        val service: AudioPlayerService
            get() = this@AudioPlayerService
    }

    fun stopAudioService() {
        player!!.removeListener(myPlayerListener)
        stopForeground(true)
        stopSelf()
    }

    var myPlayerListener: Player.EventListener = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            if (playWhenReady && playbackState == Player.STATE_READY) {
                val currentWindowIndex = player!!.currentWindowIndex
                val songTitle = songs[currentWindowIndex]["song_title"].toString()
                val artistName = songs[currentWindowIndex]["artist_alias"].toString()
                showNotification(songTitle, artistName, true)
            } else if (!playWhenReady && playbackState == Player.STATE_READY) {
                val currentWindowIndex = player!!.currentWindowIndex
                val songTitle = songs[currentWindowIndex]["song_title"].toString()
                val artistName = songs[currentWindowIndex]["artist_alias"].toString()
                showNotification(songTitle, artistName, false)
            }
        }

        override fun onPositionDiscontinuity(reason: Int) {
            if (reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION) {
                val currentWindowIndex = player!!.currentWindowIndex
                val songTitle = songs[currentWindowIndex]["song_title"].toString()
                val artistName = songs[currentWindowIndex]["artist_alias"].toString()
                showNotification(songTitle, artistName, true)
            }
        }

        override fun onTimelineChanged(timeline: Timeline, manifest: Any?, pos: Int) {}
        override fun onTracksChanged(
            trackGroups: TrackGroupArray,
            trackSelections: TrackSelectionArray
        ) {
        }

        override fun onLoadingChanged(isLoading: Boolean) {}
        override fun onRepeatModeChanged(repeatMode: Int) {}
        override fun onPlayerError(error: ExoPlaybackException) {}
        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}
        override fun onSeekProcessed() {
            val currentWindowIndex = player!!.currentWindowIndex
            val song = songs[currentWindowIndex]
            songTitle = song["song_title"].toString()
            artistName = song["artist_alias"].toString()
            showNotification(songTitle, artistName, isPlaying)
        }

        override fun onShuffleModeEnabledChanged(bool: Boolean) {}
    }
}