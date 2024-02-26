package com.vachanammusic

import android.Manifest
import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.InstanceIdResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.vachanammusic.AudioPlayerService.LocalBinder
import com.vachanammusic.DynamicHelper.shareLink
import com.vachanammusic.FileUtil.externalStorageDir
import com.vachanammusic.FileUtil.writeFile
import com.vachanammusic.PlaylistHelper.PlayList
import com.vachanammusic.PlaylistHelper.getAllPlaylists
import com.vachanammusic.TempBase.getData
import com.vachanammusic.TempBase.removeData
import com.vachanammusic.Util.getDisplayHeightPixels
import com.vachanammusic.Util.showMessage
import com.vachanammusic.databinding.MainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.Timer
import java.util.TimerTask


class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainBinding
    private val timer = Timer()
    private var tab = ""
    private var isSong = false
    val ONESIGNAL_APP_ID = "7ce82c20-7866-4ab1-b002-49e9f04e20f6"
    private var lastTab = ""
    private var isSheet = false
    private var isDrag = false
    private var isAll = false
    private var x1 = 0.0
    private var x2 = 0.0
    private var y1 = 0.0
    private var y2 = 0.0
    private val testLMap = ArrayList<HashMap<String?, Any?>?>()
    private val testLString = ArrayList<String>()
    private var favList = ArrayList<HashMap<String, Any>>()
    private var mainAdapter: MainAdapterFragmentAdapter? = null
    private var conf: SharedPreferences? = null
    private val sheetAnim = ObjectAnimator()
    private var seekBarTmr: TimerTask? = null
    private var songData: SharedPreferences? = null
    private var st: SharedPreferences? = null
    private var warning: AlertDialog.Builder? = null
    private var sp: SharedPreferences? = null
    private var loading: TimerTask? = null
    private val intent = Intent()
    private var dialog: AlertDialog.Builder? = null
    private var cek: RequestNetwork? = null
    private var cek_request_listener: RequestNetwork.RequestListener? = null
    private var FCM_onCompleteListener: OnCompleteListener<*>? = null
    private var don: SharedPreferences? = null
    private var timert: TimerTask? = null
    private var name: SharedPreferences? = null
    private var styl: SharedPreferences? = null
    private var addDialog: AlertDialog? = null
    private var writeAddDialog: AlertDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        OneSignal.Debug.logLevel = LogLevel.VERBOSE
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID)

        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(true)
        }


        binding = MainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialize()
        favList = ArrayList()


        FirebaseApp.initializeApp(this)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED
            || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                1000
            )
        } else {
            initializeLogic()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000) {
            initializeLogic()
        }
    }

    private fun initialize() {
        mainAdapter = MainAdapterFragmentAdapter(
            applicationContext,
            supportFragmentManager
        )
        conf = getSharedPreferences("conf", MODE_PRIVATE)
        songData = getSharedPreferences("songData", MODE_PRIVATE)
        st = getSharedPreferences("st", MODE_PRIVATE)
        warning = AlertDialog.Builder(this)
        dialog = AlertDialog.Builder(this)
        cek = RequestNetwork(this)
        don = getSharedPreferences("don", MODE_PRIVATE)
        name = getSharedPreferences("name", MODE_PRIVATE)
        styl = getSharedPreferences("styl", MODE_PRIVATE)
        binding.bottomToolBar.setOnClickListener { sheet() }
        sp = getSharedPreferences("sp", MODE_PRIVATE)



        // Define a TextWatcher for textView16
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed for your case
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Check if textView16's text equals "Click any song to play !"
                if (binding.textview16.text.toString() == "Click any song to play !") {
                    // If the text is "Click any song to play!", make the bottom sheet invisible
                    binding.cardview3.visibility = View.INVISIBLE
                    binding.narrowline.visibility = View.INVISIBLE
                } else {
                    // If the text is different, make the bottom sheet visible
                    binding.cardview3.visibility = View.VISIBLE
                    binding.narrowline.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Not needed for your case
            }
        }

        // Add the TextWatcher to textView16
        binding.textview16.addTextChangedListener(textWatcher)



        binding.addFavorites.setOnClickListener {
            if (mBound) {
                favList.clear()
                favList = Gson().fromJson(
                    conf!!.getString("favList", ""),
                    object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                )
                val tmp = conf!!.getString("fav", "")
                val LSTmp = Gson().fromJson<ArrayList<String>>(
                    tmp,
                    object : TypeToken<ArrayList<String?>?>() {}.type
                )
                if (LSTmp.contains(mService!!.songs[player!!.currentWindowIndex]["song_url"].toString())) {
                    taost1()
                    LSTmp.removeAt(LSTmp.indexOf(mService!!.songs[player!!.currentWindowIndex]["song_url"].toString()))

                    removeFavoriteFromFirebase(mService!!.songs[player!!.currentWindowIndex]["song_url"].toString())

                    binding.addtoFavorites.setImageResource(R.drawable.fav_white_outline)
                    binding.addFavorites.setImageResource(R.drawable.fav_white_outline)
                    binding.addtoFavorites.setColorFilter(-0x1, PorterDuff.Mode.MULTIPLY)
                    binding.addFavorites.setColorFilter(-0x1, PorterDuff.Mode.MULTIPLY)
                } else {
                    blocked()
                    LSTmp.add(mService!!.songs[player!!.currentWindowIndex]["song_url"].toString())
                    val song = mService!!.songs[player!!.currentWindowIndex]
                    addFavoriteToFirebase(song)

                    binding.addFavorites.setImageResource(R.drawable.fav_white)
                    binding.addtoFavorites.setImageResource(R.drawable.fav_white)
                    binding.addtoFavorites.setColorFilter(-0x16e19d, PorterDuff.Mode.MULTIPLY)
                    binding.addFavorites.setColorFilter(-0x16e19d, PorterDuff.Mode.MULTIPLY)
                }
                conf!!.edit().putString("fav", Gson().toJson(LSTmp)).apply()
                conf!!.edit().putString("favList", Gson().toJson(favList)).apply()
            } else {
                // Handle the case when service is not bound
            }
        }

        binding.imageview33.setOnClickListener {
            if (mBound) {
                player!!.playWhenReady = !player!!.playWhenReady
            }
            if (isMyServiceRunning(AudioPlayerService::class.java)) {
            } else {
                prepare(
                    Gson().fromJson<Any>(
                        conf!!.getString("playList", ""),
                        object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                    ) as ArrayList<HashMap<String?, Any?>?>, conf!!.getString("playListPos", "")!!
                        .toDouble()
                )
            }
        }
        binding.linear105.setOnClickListener {
            tab = "home"
            mainAdapter!!.setCustomTabCount(1)
            mainAdapter?.setHomeTabImages()
            binding.viewpager1.adapter = mainAdapter
            binding.viewpager1.adapter!!.notifyDataSetChanged()
        }

        binding.linear103.setOnClickListener {
            tab = "search"
            mainAdapter!!.setCustomTabCount(1)
            mainAdapter?.setSearchTabImages()
            binding.viewpager1.adapter = mainAdapter
            binding.viewpager1.adapter!!.notifyDataSetChanged()
        }
        binding.linear8.setOnClickListener { playlistView() }


        binding.linear104.setOnClickListener {

            favorites()
        }
        binding.back.setOnClickListener { sheet() }

        binding.more.setOnClickListener {
            if (mBound) {


                val addDialog = AlertDialog.Builder(this@MainActivity).create()

                // Inflate the custom layout into the AlertDialog
                val addDialogLI = layoutInflater
                val addDialogCV = addDialogLI.inflate(R.layout.mediaplayer_items, null)
                addDialog.setView(addDialogCV)


                val l1 = addDialogCV.findViewById<LinearLayout>(R.id.playlistsContainer)
                val l2 = addDialogCV.findViewById<LinearLayout>(R.id.favoritesContainer)
                val l3 = addDialogCV.findViewById<LinearLayout>(R.id.equalizerContainer)
                val l4 = addDialogCV.findViewById<LinearLayout>(R.id.linkShareContainer)

                val l5 = addDialogCV.findViewById<LinearLayout>(R.id.lyricsContainer)
                val l6 = addDialogCV.findViewById<LinearLayout>(R.id.songVideoContainer)
                val l7 = addDialogCV.findViewById<LinearLayout>(R.id.songDetailsContainer)

                val t1 = addDialogCV.findViewById<TextView>(R.id.textview1)
                val t2 = addDialogCV.findViewById<TextView>(R.id.textview2)
                val i8 = addDialogCV.findViewById<ImageView>(R.id.imageview1)
                val close = addDialogCV.findViewById<ImageView>(R.id.close)


                val bg = addDialogCV.findViewById<LinearLayout>(R.id.linear1)


                t1.text = mService!!.songs[player!!.currentWindowIndex]["song_title"].toString()
                t2.text = mService!!.songs[player!!.currentWindowIndex]["song_description"].toString()
                Glide.with(applicationContext)
                    .load(Uri.parse(mService!!.songs[player!!.currentWindowIndex]["song_cover"].toString()))
                    .into(i8)



                t1.ellipsize = TextUtils.TruncateAt.MARQUEE
                t1.marqueeRepeatLimit = -1
                t1.isSingleLine = true
                t1.isSelected = true

                t2.ellipsize = TextUtils.TruncateAt.MARQUEE
                t2.marqueeRepeatLimit = -1
                t2.isSingleLine = true
                t2.isSelected = true
                rippleRoundStroke(bg, "#FFFFFF", "#FFFFFF", 25.0, 0.0, "#000000")


                l1.setOnClickListener {
                    addToPlaylist(
                        conf!!.getString("playListPos", "")!!.toDouble(),
                        Gson().fromJson<Any>(
                            conf!!.getString("playList", ""),
                            object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                        ) as ArrayList<HashMap<String?, Any>>
                    )
                    addDialog.dismiss()

                }
                l2.setOnClickListener {
                    if (mBound) {
                        favList.clear()
                        favList = Gson().fromJson(
                            conf!!.getString("favList", ""),
                            object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                        )
                        val tmp = conf!!.getString("fav", "")
                        val LSTmp = Gson().fromJson<ArrayList<String>>(
                            tmp,
                            object : TypeToken<ArrayList<String?>?>() {}.type
                        )
                        if (LSTmp.contains(mService!!.songs[player!!.currentWindowIndex]["song_url"].toString())) {
                            taost1()
                            LSTmp.removeAt(LSTmp.indexOf(mService!!.songs[player!!.currentWindowIndex]["song_url"].toString()))

                            removeFavoriteFromFirebase(mService!!.songs[player!!.currentWindowIndex]["song_url"].toString())

                            binding.addtoFavorites.setImageResource(R.drawable.fav_white_outline)
                            binding.addFavorites.setImageResource(R.drawable.fav_white_outline)
                            binding.addtoFavorites.setColorFilter(-0x1, PorterDuff.Mode.MULTIPLY)
                            binding.addFavorites.setColorFilter(-0x1, PorterDuff.Mode.MULTIPLY)
                        } else {
                            blocked()
                            LSTmp.add(mService!!.songs[player!!.currentWindowIndex]["song_url"].toString())
                            val song = mService!!.songs[player!!.currentWindowIndex]
                            addFavoriteToFirebase(song)

                            binding.addFavorites.setImageResource(R.drawable.fav_white)
                            binding.addtoFavorites.setImageResource(R.drawable.fav_white)
                            binding.addtoFavorites.setColorFilter(-0x16e19d, PorterDuff.Mode.MULTIPLY)
                            binding.addFavorites.setColorFilter(-0x16e19d, PorterDuff.Mode.MULTIPLY)
                        }
                        conf!!.edit().putString("fav", Gson().toJson(LSTmp)).apply()
                        conf!!.edit().putString("favList", Gson().toJson(favList)).apply()
                    } else {
                        // Handle the case when service is not bound
                    }
                    addDialog.dismiss()

                }


                l3.setOnClickListener {
                    val intent = Intent()
                    intent.setAction("android.media.action.DISPLAY_AUDIO_EFFECT_CONTROL_PANEL")
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivityForResult(intent, 1)
                    } else {
                        showMessage(applicationContext, "No equilizer")
                    }
                    addDialog.dismiss()

                }


                l4.setOnClickListener {
                    if (mBound) {
                        shareLink(
                            this@MainActivity,
                            "MUSIC",
                            mService!!.songs[player!!.currentWindowIndex]["song_url"].toString(),
                            mService!!.songs[player!!.currentWindowIndex]["song_cover"].toString(),
                            mService!!.songs[player!!.currentWindowIndex]["song_title"].toString(),
                            mService!!.songs[player!!.currentWindowIndex]["song_description"].toString()
                        )
                    } else {
                    }
                    addDialog.dismiss()

                }
                l5.setOnClickListener {
                    showSongLyrics()
                    addDialog.dismiss()

                }
                l6.setOnClickListener {
                    if (mBound) {
                        val videoUrl = mService!!.songs[player!!.currentWindowIndex]["video_url"].toString()
                        if (videoUrl.isEmpty()) {
                            // Show toast indicating that video is not available
                            showMessage(applicationContext, "Video is not available for this song")
                        } else {
                            // Start video playback
                            intent.action = Intent.ACTION_VIEW
                            if (mBound) {
                                player!!.playWhenReady = !player!!.playWhenReady
                            }
                            intent.data = Uri.parse(videoUrl)
                            startActivity(intent)
                        }
                    } else {
                        // Handle if service is not bound
                    }

                    addDialog.dismiss()


                }


                l7.setOnClickListener {
                    if (mBound) {
                        songInfo()
                    } else {
                    }
                    addDialog.dismiss()
                }

                close.setOnClickListener {
                    if (mBound) {
                    } else {
                    }
                    addDialog.dismiss()
                }


                // Show the dialog at the bottom
                addDialog.setCancelable(true)
                addDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                val layoutParams = WindowManager.LayoutParams()
                layoutParams.copyFrom(addDialog.window?.attributes)
                layoutParams.gravity = Gravity.BOTTOM
                addDialog.window?.attributes = layoutParams
                addDialog.show()
            } else {
            }
        }


        binding.lyricsShow.setOnClickListener { showSongLyrics()}

        binding.equalizer.setOnClickListener {
            val intent = Intent()
            intent.setAction("android.media.action.DISPLAY_AUDIO_EFFECT_CONTROL_PANEL")
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, 1)
            } else {
                showMessage(applicationContext, "No equilizer")
            }
            addDialog?.dismiss()

        }
        binding.addtoFavorites.setOnClickListener {
            if (mBound) {
                favList.clear()
                favList = Gson().fromJson(
                    conf!!.getString("favList", ""),
                    object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                )
                val tmp = conf!!.getString("fav", "")
                val LSTmp = Gson().fromJson<ArrayList<String>>(
                    tmp,
                    object : TypeToken<ArrayList<String?>?>() {}.type
                )
                if (LSTmp.contains(mService!!.songs[player!!.currentWindowIndex]["song_url"].toString())) {
                    taost1()
                    LSTmp.removeAt(LSTmp.indexOf(mService!!.songs[player!!.currentWindowIndex]["song_url"].toString()))

                    removeFavoriteFromFirebase(mService!!.songs[player!!.currentWindowIndex]["song_url"].toString())

                    binding.addtoFavorites.setImageResource(R.drawable.fav_white_outline)
                    binding.addFavorites.setImageResource(R.drawable.fav_white_outline)
                    binding.addtoFavorites.setColorFilter(-0x1, PorterDuff.Mode.MULTIPLY)
                    binding.addFavorites.setColorFilter(-0x1, PorterDuff.Mode.MULTIPLY)
                } else {
                    blocked()
                    LSTmp.add(mService!!.songs[player!!.currentWindowIndex]["song_url"].toString())
                    val song = mService!!.songs[player!!.currentWindowIndex]
                    addFavoriteToFirebase(song)

                    binding.addFavorites.setImageResource(R.drawable.fav_white)
                    binding.addtoFavorites.setImageResource(R.drawable.fav_white)
                    binding.addtoFavorites.setColorFilter(-0x16e19d, PorterDuff.Mode.MULTIPLY)
                    binding.addFavorites.setColorFilter(-0x16e19d, PorterDuff.Mode.MULTIPLY)
                }
                conf!!.edit().putString("fav", Gson().toJson(LSTmp)).apply()
                conf!!.edit().putString("favList", Gson().toJson(favList)).apply()
            } else {
                // Handle the case when service is not bound
            }
        }

        binding.poster.setOnClickListener {
            if (mBound) {
                songInfo()
            } else {
            }
        }
        binding.playlistShow.setOnClickListener {
            addToPlaylist(
                conf!!.getString("playListPos", "")!!.toDouble(),
                Gson().fromJson<Any>(
                    conf!!.getString("playList", ""),
                    object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                ) as ArrayList<HashMap<String?, Any>>
            )
        }
        binding.shareSongLink.setOnClickListener {
            if (mBound) {
                shareLink(
                    this@MainActivity,
                    "MUSIC",
                    mService!!.songs[player!!.currentWindowIndex]["song_url"].toString(),
                    mService!!.songs[player!!.currentWindowIndex]["song_cover"].toString(),
                    mService!!.songs[player!!.currentWindowIndex]["song_title"].toString(),
                    mService!!.songs[player!!.currentWindowIndex]["song_description"].toString()
                )
            } else {
            }
        }

        binding.mSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(param1: SeekBar, param2: Int, param3: Boolean) {
                param2
            }

            override fun onStartTrackingTouch(param1: SeekBar) {
                isDrag = true
            }

            override fun onStopTrackingTouch(param2: SeekBar) {
                isDrag = false
                if (mBound) {
                    player!!.seekTo((binding.mSeekBar.progress * player!!.duration / 100))
                }
            }
        })
        binding.previousSong.setOnClickListener {
            if (mBound) {
                var currentWindowIndex = player!!.currentWindowIndex
                if (currentWindowIndex > 0) {
                    // Decrement the current window index for the previous song
                    currentWindowIndex--

                    // Update playlist position and playlist in shared preferences
                    conf!!.edit().putString("playListPos", currentWindowIndex.toString()).apply()
                    conf!!.edit().putString("playList", Gson().toJson(mService!!.songs)).apply()
                    player!!.seekTo(currentWindowIndex, 0)
                }
                val tmp = conf!!.getString("fav", "")
                val LSTmp = Gson().fromJson<ArrayList<String>>(
                    tmp,
                    object : TypeToken<ArrayList<String?>?>() {}.type
                )
                if (LSTmp.contains(mService!!.songs[currentWindowIndex]["song_url"].toString())) {
                    binding.addFavorites.setImageResource(R.drawable.fav_white)
                    binding.addFavorites.setColorFilter(-0x16e19d, PorterDuff.Mode.MULTIPLY)
                    binding.addtoFavorites.setImageResource(R.drawable.fav_white)
                    binding.addtoFavorites.setColorFilter(-0x16e19d, PorterDuff.Mode.MULTIPLY)
                } else {
                    binding.addFavorites.setImageResource(R.drawable.fav_white_outline)
                    binding.addFavorites.setColorFilter(-0x1, PorterDuff.Mode.MULTIPLY)
                    binding.addtoFavorites.setImageResource(R.drawable.fav_white_outline)
                    binding.addtoFavorites.setColorFilter(-0x1, PorterDuff.Mode.MULTIPLY)
                }
            } else {
                // Your code for the else case
            }
        }





        binding.nextSong.setOnClickListener {
            if (mBound) {
                var currentWindowIndex = player!!.currentWindowIndex
                if (currentWindowIndex < mService!!.songs.size - 1) {
                    player!!.seekTo(currentWindowIndex + 1, 0)
                }
                if (currentWindowIndex < mService!!.songs.size - 1) {
                    // Increment the current window index if there is a next window
                    currentWindowIndex++

                    // Update playlist position and playlist in shared preferences
                    conf!!.edit().putString("playListPos", currentWindowIndex.toString()).apply()
                    conf!!.edit().putString("playList", Gson().toJson(mService!!.songs)).apply()
                    player!!.seekTo(currentWindowIndex, 0)
                }
                val tmp = conf!!.getString("fav", "")
                val LSTmp = Gson().fromJson<ArrayList<String>>(
                    tmp,
                    object : TypeToken<ArrayList<String?>?>() {}.type
                )
                if (LSTmp.contains(mService!!.songs[currentWindowIndex]["song_url"].toString())) {
                    binding.addFavorites.setImageResource(R.drawable.fav_white)
                    binding.addFavorites.setColorFilter(-0x16e19d, PorterDuff.Mode.MULTIPLY)
                    binding.addtoFavorites.setImageResource(R.drawable.fav_white)
                    binding.addtoFavorites.setColorFilter(-0x16e19d, PorterDuff.Mode.MULTIPLY)
                } else {
                    binding.addFavorites.setImageResource(R.drawable.fav_white_outline)
                    binding.addFavorites.setColorFilter(-0x1, PorterDuff.Mode.MULTIPLY)
                    binding.addtoFavorites.setImageResource(R.drawable.fav_white_outline)
                    binding.addtoFavorites.setColorFilter(-0x1, PorterDuff.Mode.MULTIPLY)
                }
            } else {
                // Your code for the else case
            }
        }
        sheetAnim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(param1: Animator) {}
            override fun onAnimationEnd(param1: Animator) {}
            override fun onAnimationCancel(param1: Animator) {}
            override fun onAnimationRepeat(param1: Animator) {}
        })
        cek_request_listener = object : RequestNetwork.RequestListener {
            override fun onResponse(
                param1: String?,
                param2: String?,
                param3: HashMap<String, Any?>
            ) {
                param1
                param2
                param3
            }

            override fun onErrorResponse(param1: String?, param2: String?) {
                param1
                param2
            }
        }
        FCM_onCompleteListener =
            OnCompleteListener<InstanceIdResult> { task ->
                val success = task.isSuccessful
                task.result.token
                if (task.exception != null) task.exception!!.message else ""
                if (success) {
                } else {
                }
            }

    }

    private fun initializeLogic() {


        binding.textview16.ellipsize = TextUtils.TruncateAt.MARQUEE
        binding.textview16.marqueeRepeatLimit = -1
        binding.textview16.isSingleLine = true
        binding.textview16.isSelected = true
        binding.textview18.ellipsize = TextUtils.TruncateAt.MARQUEE
        binding.textview18.marqueeRepeatLimit = -1
        binding.textview18.isSingleLine = true
        binding.textview18.isSelected = true
        binding.textview19.ellipsize = TextUtils.TruncateAt.MARQUEE
        binding.textview19.marqueeRepeatLimit = -1
        binding.textview19.isSingleLine = true
        binding.textview19.isSelected = true
        design()
//        sp = getSharedPreferences("sp", MODE_PRIVATE)

        start()
        changeActivityFont("spotify")
        binding.linear2.background = object : GradientDrawable() {
            fun getIns(a: Int, b: Int): GradientDrawable {
                this.cornerRadius = a.toFloat()
                this.setColor(b)
                return this
            }
        }.getIns(0, -0x1000000)

        themeColour()
        clickAnimation(binding.linear103)
        clickAnimation(binding.linear105)
        clickAnimation(binding.linear1)
        clickAnimation(binding.linear104)
        clickAnimation(binding.imageview33)
        clickAnimation(binding.playPauseSong)
        clickAnimation(binding.more)
        clickAnimation(binding.addtoFavorites)
        clickAnimation(binding.shuffleSong)
        clickAnimation(binding.previousSong)
        clickAnimation(binding.nextSong)
        clickAnimation(binding.lyricsShow)
        swipe()
    }

    inner class MainAdapterFragmentAdapter(// This class is deprecated, you should migrate to ViewPager2:
        // https://developer.android.com/reference/androidx/viewpager2/widget/ViewPager2
        var context: Context, manager: FragmentManager?,
        private var tabCount: Int = 0

    ) :
        FragmentStatePagerAdapter(manager!!) {


        fun setCustomTabCount(newTabCount: Int) {
            tabCount = newTabCount
        }

        override fun getCount(): Int {
            return tabCount
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return null
        }

        override fun getItem(position: Int): Fragment {
            return when (tab) {
                "home" -> {
                    setHomeTabImages()
                    HomeFragmentActivity()
                }

                "search" -> {
                    setSearchTabImages()
                    SearchFragmentActivity()
                }

                "favorites" -> {
                    setFavoritesTabImages()
                    FavoritesFragmentActivity()
                }
                "song" -> {
                    setSongTabImages()
                    SongFragmentActivity()
                }

                "artist" -> {
                    setArtistTabImages()
                    ArtistFragmentActivity()
                }

                "playlists" -> {
                    setPlaylistsTabImages()
                    PlaylistItemViewsFragmentActivity()
                }

                else -> HomeFragmentActivity()
            }
        }

        fun setHomeTabImages() {
            // Set images for the "home" tab
            binding.imageview30.setImageResource(R.drawable.home_1)
            binding.imageview31.setImageResource(R.drawable.playlits)
            binding.imageview29.setImageResource(R.drawable.search_light)
            binding.imageview1.setImageResource(R.drawable.fav_white)
            setToolbarVisibility(true)
            setViewPagerVisibility(true)
        }

        fun setSearchTabImages() {
            // Set images for the "search" tab
            binding.imageview29.setImageResource(R.drawable.search)
            binding.imageview30.setImageResource(R.drawable.home_2)
            binding.imageview31.setImageResource(R.drawable.playlits)
            binding.imageview1.setImageResource(R.drawable.fav_white)
            setToolbarVisibility(true)
            setViewPagerVisibility(true)
        }

        fun setFavoritesTabImages() {
            // Set images for the "favorites" tab
            binding.imageview29.setImageResource(R.drawable.search_light)
            binding.imageview30.setImageResource(R.drawable.home_2)
            binding.imageview31.setImageResource(R.drawable.playlits)
            binding.imageview1.setImageResource(R.drawable.fav_white)
            setToolbarVisibility(true)
            setViewPagerVisibility(true)
        }

        fun setSongTabImages() {
            // Set images for the "song" tab
            binding.imageview29.setImageResource(R.drawable.search_light)
            binding.imageview30.setImageResource(R.drawable.home_2)
            binding.imageview31.setImageResource(R.drawable.playlits)
            binding.imageview1.setImageResource(R.drawable.fav_white)
            setToolbarVisibility(true)
            setViewPagerVisibility(true)
        }

        fun setArtistTabImages() {
            // Set images for the "artist" tab
            setToolbarVisibility(true)
            setViewPagerVisibility(true)
        }

        fun setPlaylistsTabImages() {
            // Set images for the "playlists" tab
            binding.imageview30.setImageResource(R.drawable.home_1)
            binding.imageview31.setImageResource(R.drawable.playlits)
            binding.imageview29.setImageResource(R.drawable.search_light)
            binding.imageview1.setImageResource(R.drawable.fav_white)
            setToolbarVisibility(true)
            setViewPagerVisibility(false)
            setLinearVisibility(true)
        }

        private fun setToolbarVisibility(visibility: Boolean) {
            binding.bottomToolBar.visibility = if (visibility) View.VISIBLE else View.GONE
            binding.linear2.visibility = if (visibility) View.VISIBLE else View.GONE
        }

        private fun setViewPagerVisibility(visibility: Boolean) {
            binding.viewpager1.visibility = if (visibility) View.VISIBLE else View.GONE
            binding.linear.visibility = if (visibility) View.GONE else View.VISIBLE
        }

        private fun setLinearVisibility(visibility: Boolean) {
            binding.linear.visibility = if (visibility) View.VISIBLE else View.GONE
        }
    }


    // rest of your onBackPressed code


    var backButtonPressed = false

    override fun onBackPressed() {
        backButtonPressed = true

        when {
            isSheet -> {
                sheet()
            }


            isSong -> {
                handleBackForSong()
            }

            isAll -> {
                handleBackForAll()
            }

            lastTab == "home" -> {
                if (mBound) {
                    player!!.removeListener(myPlayerListener)
                    mService!!.ui = false
                    unbindService(mConnection)
                    mBound = false
                }
                finishAffinity()
            }


            else -> {
                super.onBackPressed()
            }
        }
    }


    private fun handleBackForSong() {
        isSong = false
        tab = lastTab
        if (isAll) {
            lastTab = "home"
        }
        updateViewPager()
    }


    private fun handleBackForAll() {
        isAll = false
        tab = lastTab
        updateViewPager()
    }

    private fun updateViewPager() {
        mainAdapter?.setCustomTabCount(1)
        binding.viewpager1.adapter = mainAdapter
        binding.viewpager1.adapter?.notifyDataSetChanged()
    }


    public override fun onDestroy() {
        super.onDestroy()
        if (mBound && !backButtonPressed) {
            player!!.removeListener(myPlayerListener)
            mService!!.ui = false
            unbindService(mConnection)
            mBound = false
            run {
                mService!!.stopAudioService()
            }
        }
    }


    public override fun onStart() {
        super.onStart()
        themeColour()
        if (FirebaseAuth.getInstance().currentUser != null) {
        } else {
            intent.action = Intent.ACTION_VIEW
            intent.setClass(applicationContext, MainsigninActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun extra() {}
    var mService: AudioPlayerService? = null
    var player: SimpleExoPlayer? = null
    var mBound = false
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    var myPlayerListener: Player.EventListener = object : Player.EventListener {
        //			@Override
        //			public void onTimelineChanged(Timeline timeline, Object manifest, int pos) {
        //
        //			}
        override fun onTracksChanged(
            trackGroups: TrackGroupArray,
            trackSelections: TrackSelectionArray
        ) {
        }

        override fun onLoadingChanged(isLoading: Boolean) {}
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            updateProgressBar()
            if (player!!.playWhenReady) {
                binding.imageview33.setImageResource(R.drawable.pause)
                binding.playPauseSong.setImageResource(R.drawable.pause)
            }
            if (playWhenReady && playbackState == Player.STATE_READY) {
                binding.imageview33.setImageResource(R.drawable.pause)
                binding.playPauseSong.setImageResource(R.drawable.pause)
            }
            if (playWhenReady && playbackState != Player.STATE_READY) {
                binding.imageview33.setImageResource(R.drawable.load)
                binding.playPauseSong.setImageResource(R.drawable.load)
            }
            if (!playWhenReady) {
                binding.playPauseSong.setImageResource(R.drawable.play)
                binding.imageview33.setImageResource(R.drawable.play)
            }
        }

        override fun onRepeatModeChanged(repeatMode: Int) {}
        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {}
        override fun onSeekProcessed() {}
        override fun onShuffleModeEnabledChanged(bool: Boolean) {}
        override fun onPositionDiscontinuity(pos: Int) {
            sincUi()
        }
    }
    var mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName,
            service: IBinder
        ) {
            val binder = service as LocalBinder
            mService = binder.service
            mBound = true
            player = mService!!.player
            player!!.addListener(myPlayerListener)
            mService!!.ui = true
            sincUi()
            updateProgressBar()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    private fun updateProgressBar() {
        val duration = if (player == null) 0 else player!!.duration
        val position = if (player == null) 0 else player!!.currentPosition
        if (!isDrag) {
            binding.mSeekBar.progress = (position * 100 / duration).toInt()
            val bufferedPosition = if (player == null) 0 else player!!.bufferedPosition
            binding.mSeekBar.secondaryProgress = (bufferedPosition * 100 / duration).toInt()
        }

        binding.textview9.text = setCorrectDuration(position.toString())
        binding.textview10.text = setCorrectDuration(duration.toString())
        if (seekBarTmr != null) {
            seekBarTmr!!.cancel()
        }
        val playbackState = if (player == null) Player.STATE_IDLE else player!!.playbackState
        if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
            var delayMs: Long
            if (player!!.playWhenReady && playbackState == Player.STATE_READY) {
                delayMs = 1000 - position % 1000
                if (delayMs < 200) {
                    delayMs += 1000
                }
            } else {
                delayMs = 1000
            }
            seekBarTmr = object : TimerTask() {
                override fun run() {
                    runOnUiThread { updateProgressBar() }
                }
            }
            timer.schedule(seekBarTmr, delayMs.toInt().toLong())
        }
    }


    private fun setCorrectDuration(songs_duration: String): String? {
        var songs_duration: String? = songs_duration
        return try {
            run {
                val time = songs_duration?.let { Integer.valueOf(it) }
                val song_duration: String
                var seconds = time?.div(1000)
                val minutes = seconds?.div(60)
                if (seconds != null) {
                    seconds = seconds % 60
                }
                if (seconds != null) {
                    if (seconds < 10) {
                        songs_duration = "$minutes:0$seconds"
                        song_duration = songs_duration as String
                    } else {
                        songs_duration = "$minutes:$seconds"
                        song_duration = songs_duration as String
                    }
                }
                songs_duration
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun design() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.parseColor("#000000")
    }

    fun start() {

        conf!!.edit().putString(
            "apiUrl",

            "https://vachanammusic.com/api1.php" + "?api=home&lang=" + "ml" + "&alang=" + "aml"


        ).apply()
        writeFile(externalStorageDir, externalStorageDir)
        if (conf!!.getString("topSong", "") == "") {
            conf!!.edit().putString("topSong", Gson().toJson(testLMap)).apply()
        }
        if (conf!!.getString("topArtist", "") == "") {
            conf!!.edit().putString("topArtist", Gson().toJson(testLMap)).apply()
        }
        if (conf!!.getString("newArt", "") == "") {
            conf!!.edit().putString("newArt", Gson().toJson(testLMap)).apply()
        }
        if (conf!!.getString("recoSong", "") == "") {
            conf!!.edit().putString("recoSong", Gson().toJson(testLMap)).apply()
        }
        if (conf!!.getString("weeSong", "") == "") {
            conf!!.edit().putString("weeSong", Gson().toJson(testLMap)).apply()
        }
        if (conf!!.getString("albSong", "") == "") {
            conf!!.edit().putString("albSong", Gson().toJson(testLMap)).apply()
        }
        if (conf!!.getString("topPromo", "") == "") {
            conf!!.edit().putString("topPromo", Gson().toJson(testLMap)).apply()
        }
        if (conf!!.getString("fav", "") == "") {
            conf!!.edit().putString("fav", Gson().toJson(testLString)).apply()
        }
        if (conf!!.getString("favList", "") == "") {
            conf!!.edit().putString("favList", Gson().toJson(testLMap)).apply()
        }

        if (conf!!.getString("dailySong", "") == "") {
            conf!!.edit().putString("dailySong", Gson().toJson(testLMap)).apply()
        }
        if (conf!!.getString("episode", "") == "") {
            conf!!.edit().putString("episode", Gson().toJson(testLMap)).apply()
        }
        if (conf!!.getString("podcast", "") == "") {
            conf!!.edit().putString("podcast", Gson().toJson(testLMap)).apply()
        }
        if (conf!!.getString("newMix", "") == "") {
            conf!!.edit().putString("newMix", Gson().toJson(testLMap)).apply()
        }
        if (conf!!.getString("newSpcl", "") == "") {
            conf!!.edit().putString("newSpcl", Gson().toJson(testLMap)).apply()
        }
        tab = "home"
        mainAdapter!!.setCustomTabCount(1)

        binding.viewpager1.adapter = mainAdapter
        binding.viewpager1.adapter!!.notifyDataSetChanged()
        if (isMyServiceRunning(AudioPlayerService::class.java)) {
            binding.imageview33.setImageResource(R.drawable.pause)
            binding.playPauseSong.setImageResource(R.drawable.pause)
            val intent = Intent(this, AudioPlayerService::class.java)
            bindService(intent, mConnection, BIND_AUTO_CREATE)
        } else {
            if (conf!!.getString("playList", "") != "") {
                startSincUi()
            }
        }
        binding.playPauseSong.setOnClickListener {
            if (mBound) {
                player!!.playWhenReady = !player!!.playWhenReady
            }
            if (isMyServiceRunning(AudioPlayerService::class.java)) {
            } else {
                prepare(
                    Gson().fromJson<Any>(
                        conf!!.getString("playList", ""),
                        object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                    ) as ArrayList<HashMap<String?, Any?>?>, conf!!.getString("playListPos", "")!!
                        .toDouble()
                )
            }
        }
    }



    fun prepare(lmap: ArrayList<HashMap<String?, Any?>?>, pos: Double) {
        val newPlayList = Gson().toJson(lmap)
        val oldPlayList = conf!!.getString("playList", "")
        conf!!.edit().putString("playListPos", pos.toLong().toString()).apply()
        if (newPlayList == oldPlayList) {
            if (mBound) {
                if (mService!!.songs[player!!.currentWindowIndex]["song_url"].toString() != (lmap[pos.toInt()]?.get(
                        "song_url"
                    )
                        ?.toString() ?: "")
                ) {
                    player!!.seekTo(pos.toInt(), 0)
                }
            } else {
                if (mBound) {
                    player!!.removeListener(myPlayerListener)
                    mService!!.stopAudioService()
                    unbindService(mConnection)
                    mBound = false
                }
                val intent = Intent(this, AudioPlayerService::class.java)
                intent.putExtra("pos", pos.toString())
                Util.startForegroundService(this, intent)
                bindService(intent, mConnection, BIND_AUTO_CREATE)
            }
        } else {
            conf!!.edit().putString("playList", newPlayList).apply()
            if (mBound) {
                player!!.removeListener(myPlayerListener)
                mService!!.stopAudioService()
                unbindService(mConnection)
                mBound = false
            }
            val intent = Intent(this, AudioPlayerService::class.java)
            intent.putExtra("pos", pos.toString())
            Util.startForegroundService(this, intent)
            bindService(intent, mConnection, BIND_AUTO_CREATE)
        }
    }

    private fun showSongLyrics() {
        if (mBound) {
            val currentSong = mService!!.songs[player!!.currentWindowIndex]

            // Create the AlertDialog
            val addDialog = AlertDialog.Builder(this@MainActivity).create()

            // Inflate the custom layout into the AlertDialog
            val addDialogLI = layoutInflater
            val addDialogCV = addDialogLI.inflate(R.layout.lyrics_dialog, null)
            addDialog.setView(addDialogCV)

            // Find views inside the inflated layout
            val text = addDialogCV.findViewById<TextView>(R.id.text)
            val bg = addDialogCV.findViewById<LinearLayout>(R.id.bg)
            val l1 = addDialogCV.findViewById<LinearLayout>(R.id.copyLyricsLayout)

            // Apply radius to background
            rippleRoundStroke(bg, "#00FFFFFF", "#00FFFFFF", 35.0, 0.0, "#000000")

            text.typeface = ResourcesCompat.getFont(this, R.font.hevo_light)


            if (currentSong["song_lyrics"].toString().isEmpty()) {
                l1.visibility = View.GONE
                text.text = "Lyrics not available !"
            } else {
                text.text = currentSong["song_lyrics"].toString()
                l1.visibility = View.VISIBLE
            }

            l1.setOnClickListener {
                val clipboardManager =
                    getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

                if (clipboardManager != null) {
                    val lyricsToCopy = currentSong["song_lyrics"].toString()
                    clipboardManager.setPrimaryClip(
                        ClipData.newPlainText(
                            "clipboard",
                            lyricsToCopy
                        )
                    )
                    Toast.makeText(this, "Lyrics Copied", Toast.LENGTH_SHORT).show()
                } else {
                }
            }

            addDialog.setCancelable(true)
            addDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(addDialog.window?.attributes)
            layoutParams.gravity = Gravity.BOTTOM
            addDialog.window?.attributes = layoutParams
            addDialog.show()
        } else {
        }
    }


    fun sincUi() {
        binding.textview16.text =
            mService!!.songs[player!!.currentWindowIndex]["song_title"].toString()
        binding.textview17.text =
            mService!!.songs[player!!.currentWindowIndex]["song_description"].toString()
        binding.textview18.text =
            mService!!.songs[player!!.currentWindowIndex]["song_title"].toString()
        binding.textview19.text =
            mService!!.songs[player!!.currentWindowIndex]["song_description"].toString()
        binding.textview23.text =
            mService!!.songs[player!!.currentWindowIndex]["artist_alias"].toString()

        Glide.with(applicationContext)
            .asBitmap()
            .load(Uri.parse(mService!!.songs[player!!.currentWindowIndex]["song_cover"].toString()))
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                    Palette.from(resource).generate { palette ->
                        val dominantColor = palette?.dominantSwatch?.rgb ?: Color.WHITE
                        val adjustedColor = adjustColor(dominantColor)
                        val gradientDrawable = GradientDrawable(
                            GradientDrawable.Orientation.TOP_BOTTOM,
                            intArrayOf(adjustedColor, Color.BLACK)
                        )
                        binding.linear1.background = gradientDrawable
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // This is called when the image is being removed from the view.
                    // You can optionally clear any resources here.
                }
            })

        Glide.with(applicationContext)
            .load(Uri.parse(mService!!.songs[player!!.currentWindowIndex]["song_cover"].toString()))
            .into(binding.imageview27)

        Glide.with(applicationContext)
            .load(Uri.parse(mService!!.songs[player!!.currentWindowIndex]["song_cover"].toString()))
            .into(binding.poster)


    }

    // Function to adjust the dominant color to a darker version
    private fun adjustColor(color: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[2] *= 0.8f // Reduce brightness by 20%
        return Color.HSVToColor(hsv)
    }






    fun startSincUi() {
        binding.textview16.text = (Gson().fromJson<Any>(
            conf!!.getString("playList", ""),
            object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
        ) as ArrayList<HashMap<String?, Any>>)[conf!!.getString("playListPos", "")!!.toDouble()
            .toInt()]["song_title"].toString()
        binding.textview17.text = (Gson().fromJson<Any>(
            conf!!.getString("playList", ""),
            object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
        ) as ArrayList<HashMap<String?, Any>>)[conf!!.getString("playListPos", "")!!.toDouble()
            .toInt()]["song_description"].toString()
        binding.textview18.text = (Gson().fromJson<Any>(
            conf!!.getString("playList", ""),
            object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
        ) as ArrayList<HashMap<String?, Any>>)[conf!!.getString("playListPos", "")!!.toDouble()
            .toInt()]["song_title"].toString()
        binding.textview19.text = (Gson().fromJson<Any>(
            conf!!.getString("playList", ""),
            object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
        ) as ArrayList<HashMap<String?, Any>>)[conf!!.getString("playListPos", "")!!.toDouble()
            .toInt()]["song_description"].toString()
        Glide.with(applicationContext).load(
            Uri.parse(
                (Gson().fromJson<Any>(
                    conf!!.getString("playList", ""),
                    object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                ) as ArrayList<HashMap<String?, Any>>)[conf!!.getString("playListPos", "")!!
                    .toDouble().toInt()]["song_cover"].toString()
            )
        ).into(
            binding.imageview27
        )
        Glide.with(applicationContext).load(
            Uri.parse(
                (Gson().fromJson<Any>(
                    conf!!.getString("playList", ""),
                    object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                ) as ArrayList<HashMap<String?, Any>>)[conf!!.getString("playListPos", "")!!
                    .toDouble().toInt()]["song_cover"].toString()
            )
        ).into(
            binding.poster
        )


        if ((Gson().fromJson<Any>(
                conf!!.getString("playList", ""),
                object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
            ) as ArrayList<HashMap<String?, Any>>)[conf!!.getString("playListPos", "")!!.toDouble()
                .toInt()]["local"].toString() == "true"
        ) {

        } else {
            Glide.with(applicationContext).load(
                Uri.parse(
                    (Gson().fromJson<Any>(
                        conf!!.getString("playList", ""),
                        object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                    ) as ArrayList<HashMap<String?, Any>>)[conf!!.getString("playListPos", "")!!
                        .toDouble().toInt()]["song_cover"].toString()
                )
            ).into(
                binding.imageview27
            )
            Glide.with(applicationContext).load(
                Uri.parse(
                    (Gson().fromJson<Any>(
                        conf!!.getString("playList", ""),
                        object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                    ) as ArrayList<HashMap<String?, Any>>)[conf!!.getString("playListPos", "")!!
                        .toDouble().toInt()]["song_cover"].toString()
                )
            ).into(
                binding.poster
            )
        }
        val tmp = conf!!.getString("fav", "")
        val LSTmp = Gson().fromJson<ArrayList<String>>(
            tmp,
            object : TypeToken<ArrayList<String?>?>() {}.type
        )
        if (LSTmp.contains(
                (Gson().fromJson<Any>(
                    conf!!.getString("playList", ""),
                    object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                ) as ArrayList<HashMap<String?, Any>>)[conf!!.getString("playListPos", "")!!
                    .toDouble().toInt()]["song_url"].toString()
            )
        ) {
            binding.addFavorites.setImageResource(R.drawable.fav_white)
            binding.addFavorites.setColorFilter(-0x16e19d, PorterDuff.Mode.MULTIPLY)
            binding.addtoFavorites.setImageResource(R.drawable.fav_white)
            binding.addtoFavorites.setColorFilter(-0x16e19d, PorterDuff.Mode.MULTIPLY)
        } else {
            binding.addFavorites.setImageResource(R.drawable.fav_white_outline)
            binding.addFavorites.setColorFilter(-0x1, PorterDuff.Mode.MULTIPLY)
            binding.addtoFavorites.setImageResource(R.drawable.fav_white_outline)
            binding.addtoFavorites.setColorFilter(-0x1, PorterDuff.Mode.MULTIPLY)
        }
    }



    fun sheet() {
        if (getData("link_id") != "") {
            removeData("link_id")
            binding.viewpager1.visibility = View.GONE
            binding.linearTopLayout.visibility = View.GONE
            binding.detail.layoutParams.height = getDisplayHeightPixels(applicationContext)
            binding.detail.translationY = -getDisplayHeightPixels(applicationContext).toFloat()
            design()
            isSheet = true
        } else {
            if (isSheet) {
                sheetAnim.target = binding.detail
                sheetAnim.setPropertyName("translationY")
                sheetAnim.setFloatValues(0.toFloat())
                sheetAnim.setDuration(300.toLong())
                sheetAnim.interpolator = LinearInterpolator()
                sheetAnim.start()
                binding.viewpager1.visibility = View.VISIBLE
                binding.linear.visibility = View.GONE
                binding.bottomToolBar.visibility = View.VISIBLE
                binding.narrowline.visibility = View.VISIBLE
                binding.narrowline2.visibility = View.VISIBLE
                binding.linear2.visibility = View.VISIBLE

                isSheet = false
            } else {
                binding.detail.layoutParams =
                    LinearLayout.LayoutParams(binding.main.width, binding.main.height)
                binding.linearTopLayout.visibility = View.VISIBLE
                sheetAnim.target = binding.detail
                sheetAnim.setPropertyName("translationY")
                sheetAnim.setFloatValues((binding.main.height * -1).toFloat())
                sheetAnim.setDuration(300.toLong())
                sheetAnim.interpolator = LinearInterpolator()
                binding.bottomToolBar.visibility = View.GONE
                binding.narrowline.visibility = View.GONE
                binding.narrowline2.visibility = View.GONE
                binding.linear2.visibility = View.GONE
                sheetAnim.start()
                timert = object : TimerTask() {
                    override fun run() {
                        runOnUiThread { binding.viewpager1.visibility = View.GONE }
                    }
                }
                timer.schedule(timert, 300.toLong())
                isSheet = true
            }
        }
    }

    fun song(map: HashMap<String, Any>?) {
        conf!!.edit().putString("artistData", Gson().toJson(map)).apply()
        lastTab = tab
        tab = "song"
        mainAdapter!!.setCustomTabCount(1)
        mainAdapter?.setSongTabImages()
        binding.viewpager1.adapter = mainAdapter
        binding.viewpager1.adapter!!.notifyDataSetChanged()
        isSong = true
    }


    fun favorites() {
        lastTab = tab
        tab = "favorites"
        mainAdapter!!.setCustomTabCount(1)
        mainAdapter?.setFavoritesTabImages()
        binding.viewpager1.adapter = mainAdapter
        binding.viewpager1.adapter!!.notifyDataSetChanged()
        isSong = true
    }

    private fun changeActivityFont(fontName: String) {
        val resourceId = resources.getIdentifier(fontName, "font", packageName)
        val typeface = ResourcesCompat.getFont(this, resourceId)
        typeface?.let {
            overrideFonts(window.decorView, it)
        } ?: run {
            showMessage(applicationContext, "Error Loading Font")
        }
    }

    private fun overrideFonts(v: View, typeface: Typeface) {
        if (v is ViewGroup) {
            for (i in 0 until v.childCount) {
                val child = v.getChildAt(i)
                overrideFonts(child, typeface)
            }
        } else {
            if (v is TextView) {
                v.typeface = typeface
            } else if (v is EditText) {
                v.typeface = typeface
            } else if (v is Button) {
                v.typeface = typeface
            }
        }
    }

    private fun songInfo() {
        val dialog1 = AlertDialog.Builder(this@MainActivity).create()
        val inflate = layoutInflater.inflate(R.layout.songinfo_dialog, null)
        dialog1.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog1.setView(inflate)
        val t1 = inflate.findViewById<View>(R.id.t1) as TextView
        val t2 = inflate.findViewById<View>(R.id.t2) as TextView
        val t3 = inflate.findViewById<View>(R.id.t3) as TextView
        val i1 = inflate.findViewById<View>(R.id.i1) as ImageView
        val i2 = inflate.findViewById<View>(R.id.i2) as ImageView
        val bg = inflate.findViewById<View>(R.id.bg) as LinearLayout
        t1.setTypeface(ResourcesCompat.getFont(this, R.font.hevo_light)
            , Typeface.BOLD)
        t2.setTypeface(ResourcesCompat.getFont(this, R.font.hevo_light)
            , Typeface.NORMAL)
        t3.setTypeface(ResourcesCompat.getFont(this, R.font.hevo_light)
            , Typeface.BOLD)
        i1.setImageResource(R.drawable.close_grey)
        Glide.with(applicationContext)
            .load(Uri.parse(mService!!.songs[player!!.currentWindowIndex]["song_cover"].toString()))
            .into(i2)
        t1.text =
            mService!!.songs[player!!.currentWindowIndex]["song_title"].toString()
        t3.text =
            mService!!.songs[player!!.currentWindowIndex]["artist_alias"].toString()
        t2.text =
            mService!!.songs[player!!.currentWindowIndex]["song_description"].toString()
        rippleRoundStroke(bg, "#FAFAFA", "#000000", 35.0, 0.0, "#000000")
        rippleRoundStroke(i2, "#FFFFFF", "#ffffff", 35.0, 0.0, "#ffffff")
        ICC(i1, "#273238", "#9E9E9E")
        i1.setOnClickListener { dialog1.dismiss() }
        dialog1.setCancelable(false)
        dialog1.show()
    }

    private fun ICC(img: ImageView, c1: String?, c2: String?) {
        img.imageTintList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_pressed),
                intArrayOf(android.R.attr.state_pressed)
            ), intArrayOf(
                Color.parseColor(c1), Color.parseColor(c2)
            )
        )
    }

    private fun rippleRoundStroke(
        view: View,
        focus: String?,
        pressed: String?,
        round: Double,
        stroke: Double,
        strokeclr: String
    ) {
        val GG = GradientDrawable()
        GG.setColor(Color.parseColor(focus))
        GG.cornerRadius = round.toFloat()
        GG.setStroke(
            stroke.toInt(),
            Color.parseColor("#" + strokeclr.replace("#", ""))
        )
        val RE = RippleDrawable(
            ColorStateList(
                arrayOf(intArrayOf()),
                intArrayOf(Color.parseColor(pressed))
            ), GG, null
        )
        view.background = RE
    }

    private fun themeColour() {
        if (sp!!.getString("theme", "") == "2") {
            binding.main.setBackgroundResource(R.drawable.theme_2)
            binding.linear1.setBackgroundResource(R.drawable.theme_2)
        }
        if (sp!!.getString("theme", "") == "5") {
            binding.main.setBackgroundResource(R.drawable.theme_5)
            binding.linear1.setBackgroundResource(R.drawable.theme_5)
        }
        if (sp!!.getString("theme", "") == "6") {
            binding.main.setBackgroundResource(R.drawable.theme_6)
            binding.linear1.setBackgroundResource(R.drawable.theme_6)
        }
        if (sp!!.getString("theme", "") == "9") {
            binding.main.setBackgroundResource(R.drawable.main_bg)
            binding.linear1.setBackgroundResource(R.drawable.main_bg)
        }
        if (sp!!.getString("theme", "") == "10") {
            binding.main.setBackgroundResource(R.drawable.theme_9_1)
            binding.linear1.setBackgroundResource(R.drawable.theme_9_1)
        }
        if (sp!!.getString("theme", "") == "11") {
            binding.main.setBackgroundResource(R.drawable.theme_9_2)
            binding.linear1.setBackgroundResource(R.drawable.theme_9_2)
        }
        if (sp!!.getString("theme", "") == "13") {
            binding.main.setBackgroundResource(R.drawable.theme_9_4)
            binding.linear1.setBackgroundResource(R.drawable.theme_9_4)
        }
    }

    private fun loading5(txt: TextView?) {
        loading = object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    loading = object : TimerTask() {
                        override fun run() {
                            runOnUiThread { txt!!.text = "𝍠" }
                        }
                    }
                    timer.schedule(loading, 0.toLong())
                    loading = object : TimerTask() {
                        override fun run() {
                            runOnUiThread { txt!!.text = "𝍡" }
                        }
                    }
                    timer.schedule(loading, 100.toLong())
                    loading = object : TimerTask() {
                        override fun run() {
                            runOnUiThread { txt!!.text = "𝍢" }
                        }
                    }
                    timer.schedule(loading, 200.toLong())
                    loading = object : TimerTask() {
                        override fun run() {
                            runOnUiThread { txt!!.text = "𝍣" }
                        }
                    }
                    timer.schedule(loading, 300.toLong())
                    loading = object : TimerTask() {
                        override fun run() {
                            runOnUiThread { txt!!.text = "𝍤" }
                        }
                    }
                    timer.schedule(loading, 400.toLong())
                    loading = object : TimerTask() {
                        override fun run() {
                            runOnUiThread { txt!!.text = "𝍣" }
                        }
                    }
                    timer.schedule(loading, 500.toLong())
                    loading = object : TimerTask() {
                        override fun run() {
                            runOnUiThread { txt!!.text = "𝍢" }
                        }
                    }
                    timer.schedule(loading, 600.toLong())
                    loading = object : TimerTask() {
                        override fun run() {
                            runOnUiThread { txt!!.text = "𝍡" }
                        }
                    }
                    timer.schedule(loading, 700.toLong())
                }
            }
        }
        timer.scheduleAtFixedRate(loading, 0.toLong(), 800.toLong())
    }

    private fun blocked() {
        layoutInflater
        val inflate = layoutInflater.inflate(R.layout.favorites_added, null)
        val t = Toast.makeText(applicationContext, "", Toast.LENGTH_LONG)
        t.view = inflate
        t.setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 0)
        t.show()
        inflate.findViewById<View>(R.id.img1) as ImageView
        val lin1 = inflate.findViewById<View>(R.id.linear1) as LinearLayout
        val tin1 = inflate.findViewById<View>(R.id.tin1) as TextView
        lin1.background = object : GradientDrawable() {
            fun getIns(a: Int, b: Int): GradientDrawable {
                this.cornerRadius = a.toFloat()
                this.setColor(b)
                return this
            }
        }.getIns(35, -0x1)
        tin1.setTypeface(ResourcesCompat.getFont(this, R.font.spotify)
            , Typeface.NORMAL)
    }

    private fun taost1() {
        layoutInflater
        val inflate = layoutInflater.inflate(R.layout.favorites_removed, null)
        val t = Toast.makeText(applicationContext, "", Toast.LENGTH_LONG)
        t.view = inflate
        t.setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 0)
        t.show()
        inflate.findViewById<View>(R.id.img1) as ImageView
        val lin1 = inflate.findViewById<View>(R.id.linear1) as LinearLayout
        val tin1 = inflate.findViewById<View>(R.id.tin1) as TextView
        lin1.background = object : GradientDrawable() {
            fun getIns(a: Int, b: Int): GradientDrawable {
                this.cornerRadius = a.toFloat()
                this.setColor(b)
                return this
            }
        }.getIns(35, -0x1)
        tin1.setTypeface(ResourcesCompat.getFont(this, R.font.spotify)
            , Typeface.NORMAL)
    }

    fun buscar1() {
        tab = "artist"
        mainAdapter!!.setCustomTabCount(1)
        mainAdapter?.setArtistTabImages()
        binding.viewpager1.adapter = mainAdapter
        binding.viewpager1.adapter!!.notifyDataSetChanged()
    }

    private fun round(viewRound: View, viewRoundSetRadius: Double) {
        val gd = GradientDrawable()

        // Color
        var cd = ColorDrawable()
        cd = viewRound.background as ColorDrawable
        val colorId = cd.color
        gd.setColor(colorId)
        gd.cornerRadius = viewRoundSetRadius.toInt().toFloat()
        viewRound.background = gd
    }



    fun clickAnimation(view: View?) {
        val action_down = ScaleAnimation(
            0.9f,
            1f,
            0.9f,
            1f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.7f
        )
        action_down.duration = 300
        action_down.fillAfter = true
        view!!.startAnimation(action_down)
    }


    fun languagecheck() {
        if (sp!!.getString("onetime", "") == "true") {
        } else {
            sp!!.edit().putString("onetime", "true").apply()
            startActivity(intent)
        }
    }

    private fun swipe() {
        binding.linear1.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    y1 = event.y.toDouble()
                    x1 = event.x.toDouble()
                }

                MotionEvent.ACTION_UP -> {
                    y2 = event.y.toDouble()
                    x2 = event.x.toDouble()

                    val moveThreshold = 250

                    // Calculate the horizontal and vertical distance
                    val deltaY = y2 - y1
                    val deltaX = x2 - x1

                    // Check if the movement is predominantly horizontal
                    if (Math.abs(deltaX) > Math.abs(deltaY)) {
                        when {
                            deltaX > moveThreshold -> {
                                // Swipe right
                                if (mBound && player?.currentWindowIndex ?: 0 > 0) {
                                    player?.seekTo(player!!.currentWindowIndex - 1, 0)
                                    updateFavImage()
                                }
                            }

                            deltaX < -moveThreshold -> {
                                // Swipe left
                                if (mBound && player?.currentWindowIndex ?: 0 < mService?.songs?.size ?: 0 - 1) {
                                    player?.seekTo(player!!.currentWindowIndex + 1, 0)
                                    updateFavImage()
                                }
                            }
                        }
                    }
                }
            }
            true
        }
    }



    private fun updateFavImage() {
        val tmp = conf?.getString("fav", "")
        val LSTmp = Gson().fromJson<ArrayList<String>>(
            tmp,
            object : TypeToken<ArrayList<String?>?>() {}.type
        )

        val isFav = LSTmp?.contains(
            mService?.songs?.get(player?.currentWindowIndex ?: 0)?.get("song_url").toString()
        ) == true

        val drawableRes = if (isFav) R.drawable.fav_white else R.drawable.fav_white_outline
        val colorFilter = if (isFav) -0x16e19d else -0x1

        binding.addFavorites.setImageResource(drawableRes)
        binding.addFavorites.setColorFilter(colorFilter, PorterDuff.Mode.MULTIPLY)
        binding.addtoFavorites.setImageResource(drawableRes)
        binding.addtoFavorites.setColorFilter(colorFilter, PorterDuff.Mode.MULTIPLY)
    }


    private fun addToPlaylist(position: Double, data: ArrayList<HashMap<String?, Any>>) {
        val selectedPath = data[position.toInt()]["song_url"].toString()
        val selectedTitle = data[position.toInt()]["song_title"].toString()
        val selectedDes = data[position.toInt()]["song_description"].toString()
        val selectedCover = data[position.toInt()]["song_cover"].toString()
        val selectedView = data[position.toInt()]["song_view"].toString()

        val selectedArtist = data[position.toInt()]["artist_alias"].toString()
        val selectedLyrics = data[position.toInt()]["song_lyrics"].toString()
        val selectedLyricsEnglish = data[position.toInt()]["song_lyrics_english"].toString()
        val selectedVideo = data[position.toInt()]["video_url"].toString()


        addDialog = AlertDialog.Builder(this@MainActivity).create()
        val addDialogLI = layoutInflater
        val addDialogCV = addDialogLI.inflate(R.layout.add_to_playlist, null) as View
        addDialog!!.setView(addDialogCV)
        val createNew = addDialogCV.findViewById<View>(R.id.createNew) as LinearLayout
        val linear1 = addDialogCV.findViewById<View>(R.id.linear1) as LinearLayout
        val crv = addDialogCV.findViewById<View>(R.id.crv) as RecyclerView
        val textview2 = addDialogCV.findViewById<View>(R.id.textview2) as TextView

        radius("#FFFFFF", "#FFFFFF", 35.0, 35.0, 35.0, 35.0, 35.0, linear1)
        radius("#FF1744", "#FFFFFF", 0.0, 35.0, 35.0, 0.0, 0.0, createNew)
        textview2.setTypeface(ResourcesCompat.getFont(this, R.font.spotify)
            , Typeface.BOLD)

        addDialog!!.setCancelable(true)
        addDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        addDialog!!.show()

        crv.layoutManager = LinearLayoutManager(this)
        val userId = getUserId()

        getAllPlaylists(this, userId) { playlists ->
            crv.adapter = SelectRecyclerviewAdapter(
                playlists,
                userId,
                selectedPath,
                selectedTitle,
                selectedCover,
                selectedDes,
                selectedView,
                selectedArtist,
                selectedLyrics,
                selectedLyricsEnglish,
                selectedVideo
            )
        }




        createNew.setOnClickListener {
            writeAddDialog = AlertDialog.Builder(this@MainActivity).create()
            val writeAddDialogLI = layoutInflater
            val writeAddDialogCV = writeAddDialogLI.inflate(R.layout.add_new_playlist, null) as View
            writeAddDialog!!.setView(writeAddDialogCV)
            val editText = writeAddDialogCV.findViewById<View>(R.id.editText) as EditText
            val createBtn = writeAddDialogCV.findViewById<View>(R.id.createBtn) as LinearLayout
            val linear1 = writeAddDialogCV.findViewById<View>(R.id.linear1) as LinearLayout

            radius("#FFFFFF", "#FFFFFF", 35.0, 35.0, 35.0, 35.0, 35.0, linear1)
            radius("#FF1744", "#FFFFFF", 0.0, 0.0, 0.0, 35.0, 35.0, createBtn)
            writeAddDialog!!.setCancelable(true)
            writeAddDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            writeAddDialog!!.show()

            createBtn.setOnClickListener {
                if (editText.text.toString().isNotEmpty()) {
                    val playlistName = editText.text.toString()

                    finalAdd(
                        selectedPath,
                        userId,
                        playlistName,
                        selectedTitle,
                        selectedCover,
                        selectedDes,
                        selectedView,
                        selectedArtist,
                        selectedLyrics,
                        selectedLyricsEnglish,
                        selectedVideo
                    ) { success ->
                        if (success) {
                            // Update the playlist after adding the song
                            getAllPlaylists(this@MainActivity, userId) { playlists ->
                                crv.adapter = SelectRecyclerviewAdapter(
                                    playlists,
                                    userId,
                                    selectedPath,
                                    selectedTitle,
                                    selectedCover,
                                    selectedDes,
                                    selectedView,
                                    selectedArtist,
                                    selectedLyrics,
                                    selectedLyricsEnglish,
                                    selectedVideo
                                )
                            }
                            writeAddDialog!!.dismiss()
                        } else {

                        }
                    }
                } else {
                }
            }

        }
    }

    inner class SelectRecyclerviewAdapter(
        var data: ArrayList<PlayList?>,
        var userId: String,
        var selectedPath: String,
        var selectedTitle: String,
        var selectedCover: String,
        var selectedDes: String,
        var selectedView: String,
        var selectedArtist: String,
        var selectedLyrics: String,
        var selectedLyricsEnglish: String,
        var selectedVideo: String
    ) : RecyclerView.Adapter<SelectRecyclerviewAdapter.ViewHolder>() {

        init {
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = layoutInflater
            val v = inflater.inflate(R.layout.playlists, null)
            val lp = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            v.layoutParams = lp
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val view = holder.itemView
            val linear1 = view.findViewById<LinearLayout>(R.id.linear1)
            val linear2 = view.findViewById<LinearLayout>(R.id.linear2)
            val pName = view.findViewById<TextView>(R.id.pName)
            val imageview2 = view.findViewById<ImageView>(R.id.imageview2)
            val textview1 = view.findViewById<TextView>(R.id.textview1)
            pName.setTypeface(
                ResourcesCompat.getFont(this@MainActivity, R.font.spotify),
                Typeface.NORMAL
            )
            textview1.setTypeface(
                ResourcesCompat.getFont(this@MainActivity, R.font.spotify),
                Typeface.NORMAL
            )
            if (position == data.size - 1) {
                linear2.visibility = View.GONE
            } else {
                linear2.visibility = View.VISIBLE
            }
            imageview2.visibility = View.GONE
            pName.text = data[position]!!.fetchName()
            textview1.text = data[position]!!.amount


            linear1.setOnClickListener {
                val playlistName = pName.text.toString()
                finalAdd(
                    selectedPath,
                    userId,
                    playlistName,
                    selectedTitle,
                    selectedCover,
                    selectedDes,
                    selectedView,
                    selectedArtist,
                    selectedLyrics,
                    selectedLyricsEnglish,
                    selectedVideo
                )

            }

        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class ViewHolder(v: View?) : RecyclerView.ViewHolder(v!!)
    }

    fun finalAdd(
        songUrl: String?,
        userId: String,
        playlistName: String,
        songTitle: String,
        songCover: String,
        songDescription: String,
        songView: String,
        artistAlias: String,
        songLyrics: String,
        songLyricsEnglish: String,
        videoUrl: String,
        callback: ((Boolean) -> Unit)? = null
    ) {
        if (songUrl != null && playlistName.isNotEmpty()) {
            val playlistRef = FirebaseDatabase.getInstance().getReference("playlists")
                .child(userId)
                .child(playlistName)

            playlistRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var songAlreadyExists = false
                    for (songSnapshot in snapshot.children) {
                        val existingSongUrl =
                            songSnapshot.child("song_url").getValue(String::class.java)
                        if (existingSongUrl == songUrl) {
                            songAlreadyExists = true
                            break
                        }
                    }

                    if (!songAlreadyExists) {
                        val newSongRef = playlistRef.push()
                        newSongRef.child("song_url").setValue(songUrl)
                        newSongRef.child("song_title").setValue(songTitle)
                        newSongRef.child("song_description").setValue(songDescription)
                        newSongRef.child("song_cover").setValue(songCover)
                        newSongRef.child("song_view").setValue(songView)
                        newSongRef.child("artist_alias").setValue(artistAlias)
                        newSongRef.child("song_lyrics").setValue(songLyrics)
                        newSongRef.child("song_lyrics_english").setValue(songLyricsEnglish)
                        newSongRef.child("video_url").setValue(videoUrl)


                        callback?.invoke(true)
                        showMessage(applicationContext, "Song added successfully")

                    } else {
                        callback?.invoke(false)
                        showMessage(applicationContext, "Song is already added to the playlist")

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback?.invoke(false)
                }
            })
        } else {
            callback?.invoke(false)
        }
    }


    private fun getUserId(): String {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        return currentUser?.uid ?: ""
    }


    private fun playlistView() {
        addDialog = AlertDialog.Builder(this@MainActivity).create()
        val addDialogLI = layoutInflater
        val addDialogCV = addDialogLI.inflate(R.layout.playlists_view, null) as View
        addDialog!!.setView(addDialogCV)
        val crv = addDialogCV.findViewById<View>(R.id.crv) as RecyclerView
        val titleLinear = addDialogCV.findViewById<View>(R.id.titleLinear) as LinearLayout
        val linear1 = addDialogCV.findViewById<View>(R.id.linear1) as LinearLayout
        val textview2 = addDialogCV.findViewById<View>(R.id.textview2) as TextView

        radius("#FFFFFF", "#FFFFFF", 35.0, 35.0, 35.0, 40.0, 40.0, linear1)

        addDialog!!.setCancelable(true)
        addDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        addDialog!!.show()
        textview2.setTypeface(ResourcesCompat.getFont(this, R.font.spotify),
             Typeface.BOLD)
        val userId = getUserId()
        getAllPlaylists(this, userId) { playlists ->
            crv.layoutManager = LinearLayoutManager(this)
            crv.adapter = SelectPlayRecyclerviewAdapter(playlists)
            titleLinear.setOnClickListener { }

        }
    }

    inner class SelectPlayRecyclerviewAdapter(private val data: ArrayList<PlayList?>) :
        RecyclerView.Adapter<SelectPlayRecyclerviewAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = layoutInflater
            val v = inflater.inflate(R.layout.playlists, null)
            val lp = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            v.layoutParams = lp
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val view = holder.itemView
            val linear1 = view.findViewById<LinearLayout>(R.id.linear1)
            val linear2 = view.findViewById<LinearLayout>(R.id.linear2)
            val pName = view.findViewById<TextView>(R.id.pName)
            val imageview2 = view.findViewById<ImageView>(R.id.imageview2)
            val textview1 = view.findViewById<TextView>(R.id.textview1)
            pName.setTypeface(
                ResourcesCompat.getFont(this@MainActivity, R.font.spotify),
                Typeface.NORMAL
            )
            textview1.setTypeface(
                ResourcesCompat.getFont(this@MainActivity, R.font.spotify),
                Typeface.NORMAL
            )
            if (position == data.size - 1) {
                linear2.visibility = View.GONE
            } else {
                linear2.visibility = View.VISIBLE
            }
            imageview2.visibility = View.GONE
            pName.text = data[position]!!.fetchName()
            textview1.text = data[position]!!.amount

            linear1.setOnClickListener {
                tab = "playlists"
                mainAdapter?.setPlaylistsTabImages()

                val bundle = Bundle()
                bundle.putString(
                    "name",
                    pName.text.toString()
                )

                val fragment = PlaylistItemViewsFragmentActivity()
                binding.linear.visibility = View.VISIBLE
                binding.viewpager1.visibility = View.GONE
                addDialog!!.dismiss()
                fragment.arguments = bundle

                supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.linear,
                        fragment
                    )
                    .addToBackStack(null)
                    .commit()
            }

        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v)
    }



    private fun addFavoriteToFirebase(song: HashMap<String, Any>) {
        val database = FirebaseDatabase.getInstance()
        val userUid = getUserId() // Replace with your user's UID

        // Generate a unique key based on the song URL
        val songUrl = song["song_url"].toString()
        val hashedKey = hashString(songUrl)

        // Create a reference to the favorites node in your database
        val favoritesRef = database.getReference("favorites/$userUid")

        // Add the favorite song to the database using the hashed key
        favoritesRef.child(hashedKey).setValue(song)
    }

    private fun removeFavoriteFromFirebase(songUrl: String) {
        val database = FirebaseDatabase.getInstance()
        val userUid = getUserId() // Replace with your user's UID

        // Generate a unique key based on the song URL
        val hashedKey = hashString(songUrl)

        // Create a reference to the favorites node in your database
        val favoritesRef = database.getReference("favorites/$userUid")

        // Remove the favorite song from the database using the hashed key
        favoritesRef.child(hashedKey).removeValue()
    }

    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun radius(
        color1: String?,
        color2: String?,
        str: Double,
        n1: Double,
        n2: Double,
        n3: Double,
        n4: Double,
        view: View
    ) {
        val gd = GradientDrawable()
        gd.setColor(Color.parseColor(color1))
        gd.setStroke(str.toInt(), Color.parseColor(color2))
        gd.cornerRadii = floatArrayOf(
            n1.toInt().toFloat(),
            n1.toInt().toFloat(),
            n2.toInt()
                .toFloat(),
            n2.toInt().toFloat(),
            n3.toInt().toFloat(),
            n3.toInt().toFloat(),
            n4.toInt()
                .toFloat(),
            n4.toInt().toFloat()
        )
        view.background = gd
        val RE = RippleDrawable(
            ColorStateList(
                arrayOf(intArrayOf()),
                intArrayOf(Color.parseColor("#9E9E9E"))
            ), gd, null
        )
        view.background = RE
    }

}