package com.vachanammusic

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vachanammusic.databinding.SongFragmentBinding
import com.vachanammusic.databinding.SongFragmentDesignBinding
import de.hdodenhof.circleimageview.CircleImageView
import org.jsoup.Jsoup
import java.io.IOException

class SongFragmentActivity : Fragment() {
//    private lateinit var binding: SongFragmentBinding
    private lateinit var binding: SongFragmentDesignBinding

    private val REQ_CD_UP = 101
    private val songAlias = ""
    private var artistData = HashMap<String, Any>()
    private var conf: SharedPreferences? = null
    private var img: SharedPreferences? = null
    private var isFollowing = false
    private var act: SongFragmentActivity? = null
    private var testString = ""
    private var mact: MainActivity? = null
    private var testLMap = ArrayList<HashMap<String?, Any?>?>()
    private var d: AlertDialog.Builder? = null
    private var database: FirebaseDatabase? = null
    private var followsRef: DatabaseReference? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SongFragmentDesignBinding.inflate(inflater, container, false)
        val view = binding.root
        initialize()
        initializeLogic()
        return view
    }

    private fun initialize() {
        conf = requireContext().getSharedPreferences("conf", Activity.MODE_PRIVATE)
        img = requireContext().getSharedPreferences("img", Activity.MODE_PRIVATE)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance()
        followsRef = database?.getReference("follows")    }

    private fun initializeLogic() {
        start()

        binding.artistImg.setColorFilter(-0x1, PorterDuff.Mode.MULTIPLY)
        binding.artistDes.ellipsize = TextUtils.TruncateAt.MARQUEE
        binding.artistDes.marqueeRepeatLimit = -1
        binding.artistDes.isSingleLine = true
        binding.artistDes.isSelected = true
        binding.followTxt.text = if (isFollowing) "Following" else "Follow"

        binding.linearFollow.background = object : GradientDrawable() {
            fun getIns(a: Int, b: Int, c: Int, d: Int): GradientDrawable {
                this.cornerRadius = a.toFloat()
                this.setStroke(b, c)
                this.setColor(d)
                return this
            }
        }.getIns(50, 2, -0x555556, Color.TRANSPARENT)
//        binding.vscroll6.isVerticalScrollBarEnabled = false
        binding.vscroll8.isVerticalScrollBarEnabled = false
        binding.linkShare.setOnClickListener {

            val artistId = artistData["artist_id"].toString()
            val artistCover = artistData["artist_img_perfil"].toString()
            val artistTitle = artistData["artist_alias"].toString()
            val artistDes = artistData["artist_des"].toString()


            val context = activity ?: return@setOnClickListener
            DynamicHelper.shareLink(
                context as MainActivity,
                "ARTIST",
                artistId,
                artistCover,
                artistTitle,
                artistDes
            )

        }

        binding.linearFollow.setOnClickListener {
            toggleFollowStatus()

        }

    }





    private fun toggleFollowStatus() {
        isFollowing = !isFollowing
        binding.followTxt.text = if (isFollowing) "Following" else "Follow"

        val artistId = artistData["artist_id"].toString()
        val userId = getUserId()

        val followData = hashMapOf(
            "artistId" to artistId,
            "userId" to userId,
            "isFollowing" to isFollowing
        )

        // Add follow status to Realtime Database
        followsRef?.child(userId)?.child(artistId)?.setValue(followData)
            ?.addOnSuccessListener {
                // Successfully added to Realtime Database
            }?.addOnFailureListener { e ->
                // Failed to add to Realtime Database
            }
    }

    private fun getUserId(): String {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        return currentUser?.uid ?: ""
    }

    private fun loadFollowStatus() {
        val artistId = artistData["artist_id"].toString()
        val userId = getUserId()

        // Fetch follow status from Realtime Database
        followsRef?.child(userId)?.child(artistId)?.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val isFollowing = dataSnapshot.child("isFollowing").getValue(Boolean::class.java) ?: false
                // Update UI based on follow status
                binding.followTxt.text = if (isFollowing) "Following" else "Follow"
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_CD_UP -> if (resultCode == Activity.RESULT_OK) {
                // Handle result
            }
        }
    }

    override fun onStart() {
        super.onStart()
        loadFollowStatus()

        if (img!!.getString("imgF", "") == "") {
        } else {
            binding.artistImg.setImageBitmap(
                FileUtil.decodeSampleBitmapFromPath(
                    img!!.getString(
                        "imgF",
                        ""
                    ), 1024, 1024
                )
            )
        }
    }

//    fun start() {
//        testString = Gson().toJson(testLMap)
//        testLMap = Gson().fromJson(
//            testString,
//            object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
//        )
//        act = this
//        mact = this.activity as MainActivity?
//        artistData = Gson().fromJson(
//            conf!!.getString("artistData", ""),
//            object : TypeToken<HashMap<String?, Any?>?>() {}.type
//        )
//        if (artistData["artist_alias"].toString() == "") {
//            if (conf!!.getString("favList", "") != "") {
//                binding.artistDes.visibility = View.GONE
//                binding.recyclerview1.adapter = Recyclerview1Adapter(
//                    Gson().fromJson<Any>(
//                        conf!!.getString("favList", ""),
//                        object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
//                    ) as ArrayList<HashMap<String?, Any?>?>
//                )
//
//
//                binding.recyclerview1.layoutManager = LinearLayoutManager(context)
//                binding.linearFollow.visibility = View.GONE
//            }
//        } else {
////            binding.textview9.text = "Albums •"
////            binding.linearfollow.visibility = View.VISIBLE
//            Glide.with(requireContext().applicationContext)
//                .load(Uri.parse(artistData["artist_img_portada"].toString() + "?" + artistData["edited"].toString()))
//                .into(
//                    binding.artistCover
//                )
//            Glide.with(requireContext().applicationContext)
//                .load(Uri.parse(artistData["artist_img_perfil"].toString() + "?" + artistData["edited"].toString()))
//                .into(
//                    binding.artistImg
//                )
//            binding.artistName.text = artistData["artist_alias"].toString()
//            binding.artistDes.text = artistData["artist_des"].toString()
//            val id = artistData["artist_id"].toString()
//            Thread(
//                Runnable {
//                    try {
//                        val doc = Jsoup.connect("https://vachanammusic.com/api.php")
//                            .data("api", "getSong").data("artist", id).get().text()
//                        if (activity == null) return@Runnable
//                        requireActivity().runOnUiThread {
//                            binding.recyclerview1.adapter =
//                                Recyclerview1Adapter(
//                                    (Gson().fromJson<Any>(
//                                        doc,
//                                        object :
//                                            TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
//                                    ) as ArrayList<HashMap<String?, Any?>?>)
//                                )
//                            binding.recyclerview1.layoutManager = LinearLayoutManager(context)
//
//
//
//                        }
//                    } catch (_: IOException) {
//                    }
//                }).start()
//        }
//    }

    fun start() {
        testString = Gson().toJson(testLMap)
        testLMap = Gson().fromJson(
            testString,
            object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
        )
        act = this
        mact = this.activity as MainActivity?
        artistData = Gson().fromJson(
            conf!!.getString("artistData", ""),
            object : TypeToken<HashMap<String?, Any?>?>() {}.type
        )
        if (artistData["artist_alias"].toString() == "") {
            if (conf!!.getString("favList", "") != "") {
                binding.artistDes.visibility = View.GONE
                val favList = Gson().fromJson<ArrayList<HashMap<String?, Any?>?>>(
                    conf!!.getString("favList", ""),
                    object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                )
                binding.recyclerview1.adapter = Recyclerview1Adapter(favList)
                binding.recyclerview1.layoutManager = LinearLayoutManager(context)
                binding.linearFollow.visibility = View.GONE
            }
        } else {
            Glide.with(requireContext().applicationContext)
                .load(Uri.parse(artistData["artist_img_portada"].toString() + "?" + artistData["edited"].toString()))
                .into(binding.artistCover)
            Glide.with(requireContext().applicationContext)
                .load(Uri.parse(artistData["artist_img_perfil"].toString() + "?" + artistData["edited"].toString()))
                .into(binding.artistImg)
            binding.artistName.text = artistData["artist_alias"].toString()
            binding.artistDes.text = artistData["artist_des"].toString()
            val id = artistData["artist_id"].toString()
            Thread(
                Runnable {
                    try {
                        val doc = Jsoup.connect("https://vachanammusic.com/api.php")
                            .data("api", "getSong").data("artist", id).get().text()
                        if (activity == null) return@Runnable
                        requireActivity().runOnUiThread {
                            val songList = Gson().fromJson<ArrayList<HashMap<String?, Any?>?>>(
                                doc,
                                object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                            )
                            binding.recyclerview1.adapter = Recyclerview1Adapter(songList)
                            binding.recyclerview1.layoutManager = LinearLayoutManager(context)
//                            var totalSongViews = 0
//                            var totalSongs = songList.size
//                            for (song in songList) {
//                                val views = song?.get("views") as? Int
//                                if (views != null) {
//                                    totalSongViews += views
//                                }
//                            }

                            // Calculate total song views and total songs
                            var totalSongViews = 0
                            var totalSongs = songList.size

                            // Calculate total song views
                            for (song in songList) {
                                val viewsString = song!!["song_view"] as? String
                                val views = viewsString?.toIntOrNull()
                                if (views != null) {
                                    totalSongViews += views
                                }
                            }

                            // Update TextView with the totals
                            binding.totalViewsTxt.text = "$totalSongViews Views"
                            binding.totalSongsTxt.text = "$totalSongs Songs"
                        }
                    } catch (_: IOException) {
                    }
                }).start()
        }
    }


    inner class Recyclerview1Adapter(var data: ArrayList<HashMap<String?, Any?>?>) :
        RecyclerView.Adapter<Recyclerview1Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = activity!!.layoutInflater
            val v = inflater.inflate(R.layout.song_title_slno, null)
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
            val linear3 = view.findViewById<LinearLayout>(R.id.linear3)
            val imageview1 = view.findViewById<CircleImageView>(R.id.imageview1)
            val imageview2 = view.findViewById<ImageView>(R.id.imageview2)
            val textview1 = view.findViewById<TextView>(R.id.textview1)
            val textview2 = view.findViewById<TextView>(R.id.textview2)
            val imageview3 = view.findViewById<ImageView>(R.id.imageview3)
            val textview3 = view.findViewById<TextView>(R.id.textview3)

             val slno = view.findViewById<TextView>(R.id.slnoTxt)
            val serialNumber = position + 1
            slno.text = serialNumber.toString()


            val sortedData = data.sortedByDescending {
                (it!!["song_view"] as? String)?.toDoubleOrNull() ?: 0.0
            }.take(4) // Take top 5 songs based on song views


            textview1.text = sortedData[position]!!["song_title"].toString()
            textview2.text = sortedData[position]!!["song_description"].toString()
            Glide.with(context!!.applicationContext)
                .load(Uri.parse(sortedData[position]!!["song_cover"].toString())).into(imageview1)

            if (songAlias == "Mis Favoritas") {
                imageview3.visibility = View.GONE
                textview3.visibility = View.GONE
            } else {
                textview3.text =
                    ((data[position]!!["song_view"].toString().toDouble()).toLong()).toString()
            }
            if (data.size - 1 == position) {
                linear3.visibility = View.VISIBLE
            } else {
                linear3.visibility = View.GONE
            }
            linear1.setOnClickListener {
                textview3.text = ((data[position]!!["song_view"].toString()
                    .toDouble() + 1).toLong()).toString()
                mact!!.prepare(data, position.toDouble())
            }
            fun shareMusicLink() {
                val songUrl = data[position]!!["song_url"].toString()
                val songCover = data[position]!!["song_cover"].toString()
                val songTitle = data[position]!!["song_title"].toString()
                val songDes = data[position]!!["song_description"].toString()


                val context = activity ?: return
                DynamicHelper.shareLink(
                    context as MainActivity,
                    "MUSIC",
                    songUrl,
                    songCover,
                    songTitle,
                    songDes
                )

            }
            imageview2.setOnClickListener {
                d!!.setTitle(data[position]!!["song_title"].toString())
                d!!.setMessage(data[position]!!["song_description"].toString())
                d!!.setPositiveButton(
                    "Share"
                ) { dialog, which ->
                    shareMusicLink()
                }
                d!!.setNegativeButton(
                    "Cancel"
                ) { dialog, which -> }
                d!!.create().show()
            }

            textview1.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.light), Typeface.NORMAL

            )
            textview2.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.hevo_light), Typeface.NORMAL

            )
            textview3.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.hevo_light), Typeface.NORMAL
            )
            textview1.ellipsize = TextUtils.TruncateAt.MARQUEE
            textview1.marqueeRepeatLimit = -1
            textview1.isSingleLine = true
            textview1.isSelected = true
            textview2.ellipsize = TextUtils.TruncateAt.MARQUEE
            textview2.marqueeRepeatLimit = -1
            textview2.isSingleLine = true
            textview2.isSelected = true
        }

        override fun getItemCount(): Int {
            return minOf(data.size, 4) // Ensure only up to 5 items are displayed
        }
        inner class ViewHolder(v: View?) : RecyclerView.ViewHolder(v!!)
    }


}
