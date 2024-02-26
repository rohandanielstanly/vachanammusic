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
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vachanammusic.databinding.HomeFragmentBinding
import de.hdodenhof.circleimageview.CircleImageView
import org.jsoup.Jsoup
import java.io.IOException
import java.text.DecimalFormat
import java.util.Calendar

class HomeFragmentActivity : Fragment() {
    private lateinit var binding: HomeFragmentBinding
    private val firebase = FirebaseDatabase.getInstance()
    private var act: HomeFragmentActivity? = null
    private var snapHelper: SnapHelper? = null
    private var mact: MainActivity? = null
    private var artistData = HashMap<String, Any>()
    private var admin = false
    private var apiUrl: String? = ""
    private val value = 0.0
    private var testString = ""
    private var type = ""
    private var testLMap = ArrayList<HashMap<String, Any>>()
    private var allSongListMap = ArrayList<HashMap<String?, Any?>?>()
    private var conf: SharedPreferences? = null
    private val intent = Intent()
    private var time = Calendar.getInstance()
    private val timeOld = Calendar.getInstance()
    private var dialog1: AlertDialog.Builder? = null
    private var d: AlertDialog.Builder? = null
    private var img: SharedPreferences? = null
    private var name: SharedPreferences? = null
    private val heading = firebase.getReference("Heading")
    private var Heading_child_listener: ChildEventListener? = null
    private var sp: SharedPreferences? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        val view = binding.root
        initialize()
        FirebaseApp.initializeApp(requireContext())
        initializeLogic()
        return view
    }


    private fun initialize() {

        conf = requireContext().getSharedPreferences("conf", Activity.MODE_PRIVATE)
        dialog1 = AlertDialog.Builder(activity)
        d = AlertDialog.Builder(activity)
        img = requireContext().getSharedPreferences("img", Activity.MODE_PRIVATE)
        name = requireContext().getSharedPreferences("name", Activity.MODE_PRIVATE)
        sp = requireContext().getSharedPreferences("sp", Activity.MODE_PRIVATE)
        binding.imageview1.setOnClickListener {
            intent.setClass(
                requireContext().applicationContext,
                NotificationActivity::class.java
            )
            startActivity(intent)
        }
        binding.circleImageview1.setOnClickListener {
            intent.setClass(requireContext().applicationContext, ProfileActivity::class.java)
            startActivity(intent)
        }
        binding.edittext1.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(
                param1: CharSequence,
                param2: Int,
                param3: Int,
                param4: Int
            ) {
                val charSeq = param1.toString()
                if (charSeq.isNotEmpty()) {
                    binding.imageview4.visibility = View.VISIBLE
                    binding.imageview3.visibility = View.VISIBLE
                    Thread(
                        Runnable {
                            try {
                                val doc = Jsoup.connect("https://vachanammusic.com/api.php")
                                    .data("api", "search").data("search", charSeq)
                                    .data("type", type).get().text()
                                if (activity == null) return@Runnable
                                act!!.requireActivity().runOnUiThread {
                                    if (doc.isNotEmpty()) {
                                        if (doc.substring(0, 1) == "[") {
                                            val data: ArrayList<HashMap<String?, Any?>> =
                                                Gson().fromJson(
                                                    doc,
                                                    object :
                                                        TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                                                ) as ArrayList<HashMap<String?, Any?>>

                                            val filteredData =
                                                data.filterNotNull() as ArrayList<HashMap<String?, Any?>>

                                            binding.listview1.adapter =
                                                Listview1Adapter(filteredData)

                                        }
                                    }
                                }
                            } catch (_: IOException) {
                            }
                        }).start()
                }
            }

            override fun beforeTextChanged(
                param1: CharSequence,
                param2: Int,
                param3: Int,
                param4: Int
            ) {
            }

            override fun afterTextChanged(param1: Editable) {}
        })

        binding.imageview4.setOnClickListener { (binding.listview1.adapter as BaseAdapter).notifyDataSetChanged() }
        binding.imageview3.setOnClickListener {
            binding.edittext1.setText("")
            (binding.listview1.adapter as BaseAdapter).notifyDataSetChanged()
        }
        binding.materialbutton3.setOnClickListener { mact!!.buscar1() }
        binding.more.setOnClickListener {
            binding.recyclerview5.visibility = View.GONE
            binding.linear4.visibility = View.GONE
            binding.linear72.visibility = View.VISIBLE
            binding.listview1.visibility = View.VISIBLE
            binding.linear60.visibility = View.VISIBLE
            binding.less.visibility = View.VISIBLE
            binding.more.visibility = View.GONE
        }
        binding.less.setOnClickListener {
            binding.recyclerview5.visibility = View.VISIBLE
            binding.linear4.visibility = View.VISIBLE
            binding.linear72.visibility = View.GONE
            binding.listview1.visibility = View.GONE
            binding.linear60.visibility = View.GONE
            binding.less.visibility = View.GONE
            binding.more.visibility = View.VISIBLE
        }

        Heading_child_listener = object : ChildEventListener {
            override fun onChildAdded(param1: DataSnapshot, param2: String?) {
                val ind: GenericTypeIndicator<HashMap<String, Any>> =
                    object : GenericTypeIndicator<HashMap<String, Any>>() {}
                param1.key
                val childValue = param1.getValue(ind)!!
                // Rest of your code handling _childKey and _childValue

                binding.apply {
                    HL.text = childValue["HL"].toString()
                    H1.text = childValue["H1"].toString()
                    H2.text = childValue["H2"].toString()
                    H3.text = childValue["H3"].toString()
                    H4.text = childValue["H4"].toString()
                    H5.text = childValue["H5"].toString()
                    H6.text = childValue["H6"].toString()
                    H7.text = childValue["H7"].toString()
                    H8.text = childValue["H8"].toString()
                    H9.text = childValue["H9"].toString()
                    H10.text = childValue["H10"].toString()
                    H11.text = childValue["H11"].toString()
                    H12.text = childValue["H12"].toString()
                    H13.text = childValue["H13"].toString()
                    H14.text = childValue["H14"].toString()
                    H15.text = childValue["H15"].toString()
                    H16.text = childValue["H16"].toString()
                    H17.text = childValue["H17"].toString()
                }

            }

            override fun onChildChanged(param1: DataSnapshot, param2: String?) {
                val ind: GenericTypeIndicator<HashMap<String, Any?>> =
                    object : GenericTypeIndicator<HashMap<String, Any?>>() {}

                param1.key
                val childValue = param1.getValue(ind)!!
                binding.apply {
                    HL.text = childValue["HL"].toString()
                    H1.text = childValue["H1"].toString()
                    H2.text = childValue["H2"].toString()
                    H3.text = childValue["H3"].toString()
                    H4.text = childValue["H4"].toString()
                    H5.text = childValue["H5"].toString()
                    H6.text = childValue["H6"].toString()
                    H7.text = childValue["H7"].toString()
                    H8.text = childValue["H8"].toString()
                    H9.text = childValue["H9"].toString()
                    H10.text = childValue["H10"].toString()
                    H11.text = childValue["H11"].toString()
                    H12.text = childValue["H12"].toString()
                    H13.text = childValue["H13"].toString()
                    H14.text = childValue["H14"].toString()
                    H15.text = childValue["H15"].toString()
                    H16.text = childValue["H16"].toString()
                    H17.text = childValue["H17"].toString()
                }
            }

            override fun onChildMoved(param1: DataSnapshot, param2: String?) {}
            override fun onChildRemoved(param1: DataSnapshot) {
                val ind: GenericTypeIndicator<HashMap<String, Any?>> =
                    object : GenericTypeIndicator<HashMap<String, Any?>>() {}

                param1.key
                param1.getValue(ind)!!
            }

            override fun onCancelled(param1: DatabaseError) {
                param1.code
                param1.message
            }
        }
        val listener = Heading_child_listener
        if (listener != null) {
            heading.addChildEventListener(listener)
        } else {
            // Handle the case where _Heading_child_listener is null
        }
    }

    private fun initializeLogic() {
        admin = true

        start()
        type()

        binding.linear59.visibility = View.GONE
        binding.narrowline2.visibility = View.GONE

        binding.vscroll1.isVerticalScrollBarEnabled = false
        binding.hscroll1.isHorizontalScrollBarEnabled = false
        binding.linear72.visibility = View.GONE
        binding.listview1.visibility = View.GONE
        binding.linear60.visibility = View.GONE
        binding.less.visibility = View.GONE
        binding.linear60.background = object : GradientDrawable() {
            fun getIns(a: Int, b: Int, c: Int, d: Int): GradientDrawable {
                this.cornerRadius = a.toFloat()
                this.setStroke(b, c)
                this.setColor(d)
                return this
            }
        }.getIns(80, 1, -0x9f8275, Color.TRANSPARENT)
        binding.imageview4.visibility = View.GONE
        binding.imageview3.visibility = View.GONE
    }

    override fun onStart() {
        super.onStart()
//        if (img!!.getString("img", "") == "") {
//        } else {
//            binding.circleImageview1.setImageBitmap(
//                FileUtil.decodeSampleBitmapFromPath(
//                    img!!.getString(
//                        "img",
//                        ""
//                    ), 1024, 1024
//                )
//            )
//        }
//        if (img!!.getString("img", "") == "") {
//        } else {
//            binding.circleimageview4.setImageBitmap(
//                FileUtil.decodeSampleBitmapFromPath(
//                    img!!.getString(
//                        "img",
//                        ""
//                    ), 1024, 1024
//                )
//            )
//
//        }


        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        if (name!!.getString("name", "") == "") {
            val userName = currentUser?.displayName
            binding.textview17.text = "Welcome $userName"
        } else {
            binding.textview17.text = "Welcome " + name!!.getString("name", "")
        }

        // Load and set the profile image from Firebase or manually added
        if (currentUser != null) {
            val photoUrl = currentUser.photoUrl

            // Check if the user has a profile image URL
            if (photoUrl != null) {
                // Load the profile image using an image loading library (e.g., Glide)
                Glide.with(this)
                    .load(photoUrl)
                    .into(binding.circleimageview4)
            } else {
                // If no profile image URL is available, set a default image
                binding.circleimageview4.setImageResource(R.drawable.profile_pic)
            }
        }

    }


    fun start() {
        apiUrl = conf!!.getString("apiUrl", "")
        val testString = Gson().toJson(testLMap)
        testLMap = Gson().fromJson(
            testString,
            object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
        )
        act = this
        snapHelper = PagerSnapHelper()
        mact = this.activity as MainActivity?
        home()
    }

    private fun home() {
        time = Calendar.getInstance()
        if (conf!!.getString("lastUpdate", "") == "") {
            conf!!.edit().putString("lastUpdate", (time.timeInMillis - 60000).toString()).apply()
        }
        timeOld.timeInMillis = conf!!.getString("lastUpdate", "")!!.toDouble().toLong()
        binding.apply {
            recyclerview1.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            recyclerview2.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            recyclerview3.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            recyclerview6.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            recyclerview7.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            recyclerview10.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            recyclerview4.layoutManager = LinearLayoutManager(context)
            recyclerview5.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            gridview1.numColumns = 1
            recyclerview11.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            recyclerview12.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            recyclerview13.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            recyclerview15.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            recyclerview17.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        if (conf!!.getString("homeCache", "") != "") {
            val cacheMap = Gson().fromJson<HashMap<String, Any>>(
                conf!!.getString("homeCache", ""),
                object : TypeToken<HashMap<String?, Any?>?>() {}.type
            )
            binding.recyclerview1.adapter = Recyclerview1Adapter(
                Gson().fromJson<Any>(
                    cacheMap["recoSong"].toString(),
                    object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                ) as ArrayList<HashMap<String?, Any?>?>
            )
            binding.recyclerview2.adapter =
                Recyclerview2Adapter(
                    Gson().fromJson<Any>(
                        cacheMap["hot"].toString(),
                        object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                    ) as ArrayList<HashMap<String?, Any?>?>
                )
            val data: ArrayList<HashMap<String?, Any?>>? = Gson().fromJson(
                cacheMap["allSong"].toString(),
                object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
            )

            val filteredData = data?.toList() ?: ArrayList()

            binding.listview1.adapter =
                Listview1Adapter(filteredData as ArrayList<HashMap<String?, Any?>>)

            TempBase.allMusicListMap =
                Gson().fromJson<Any>(
                    cacheMap["allSong"].toString(),
                    object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                ) as ArrayList<HashMap<String?, Any?>?>
            allSongListMap = TempBase.allMusicListMap
            binding.recyclerview3.adapter =
                Recyclerview3Adapter(
                    (Gson().fromJson<Any>(
                        cacheMap["topArtists"].toString(),
                        object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                    ) as ArrayList<HashMap<String, Any>>)
                )
            binding.recyclerview6.adapter =
                Recyclerview6Adapter(
                    (Gson().fromJson<Any>(
                        cacheMap["weeSong"].toString(),
                        object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                    ) as ArrayList<HashMap<String?, Any?>?>)
                )
            binding.recyclerview7.adapter =
                Recyclerview7Adapter(
                    (Gson().fromJson<Any>(
                        cacheMap["albSong"].toString(),
                        object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                    ) as ArrayList<HashMap<String?, Any?>?>)
                )
            binding.recyclerview10.adapter =
                Recyclerview10Adapter(
                    (Gson().fromJson<Any>(
                        cacheMap["dailySong"].toString(),
                        object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                    ) as ArrayList<HashMap<String?, Any?>?>)
                )
            binding.recyclerview4.adapter =
                Recyclerview4Adapter(
                    (Gson().fromJson<Any>(
                        cacheMap["topSong"].toString(),
                        object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                    ) as ArrayList<HashMap<String?, Any?>?>)
                )
            binding.recyclerview5.adapter =
                Recyclerview5Adapter(
                    (Gson().fromJson<Any>(
                        cacheMap["allArt"].toString(),
                        object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                    ) as ArrayList<HashMap<String, Any>>)
                )
            binding.gridview1.adapter = Gridview1Adapter(
                (Gson().fromJson<Any>(
                    cacheMap["allArt"].toString(),
                    object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                ) as ArrayList<HashMap<String, Any>>)
            )


            binding.recyclerview11.adapter =
                Recyclerview11Adapter(
                    (Gson().fromJson<Any>(
                        cacheMap["newArt"].toString(),
                        object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                    ) as ArrayList<HashMap<String, Any>>)
                )
            binding.recyclerview12.adapter =
                Recyclerview12Adapter(
                    (Gson().fromJson<Any>(
                        cacheMap["episode"].toString(),
                        object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                    ) as ArrayList<HashMap<String?, Any?>?>)
                )
            binding.recyclerview13.adapter =
                Recyclerview13Adapter(
                    (Gson().fromJson<Any>(
                        cacheMap["podcast"].toString(),
                        object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                    ) as ArrayList<HashMap<String?, Any?>?>)
                )
            binding.recyclerview15.adapter =
                Recyclerview15Adapter(
                    (Gson().fromJson<Any>(
                        cacheMap["newSpcl"].toString(),
                        object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                    ) as ArrayList<HashMap<String?, Any?>?>)
                )
            binding.recyclerview17.adapter =
                Recyclerview17Adapter(
                    (Gson().fromJson<Any>(
                        cacheMap["newMix"].toString(),
                        object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                    ) as ArrayList<HashMap<String?, Any?>?>)
                )
        }
        if ((time.timeInMillis - timeOld.timeInMillis) > 59999) {
            conf!!.edit().putString("lastUpdate", time.timeInMillis.toString()).apply()
            Thread(
                Runnable {
                    try {
                        val doc = apiUrl?.let { Jsoup.connect(it).data("api", "home").get().text() }
                        try {
                            val map = Gson().fromJson<HashMap<String, Any>>(
                                doc,
                                object : TypeToken<HashMap<String?, Any?>?>() {}.type
                            )
                            conf!!.edit().putString("homeCache", doc).apply()
                            if (activity == null) return@Runnable
                            requireActivity().runOnUiThread {
                                binding.recyclerview1.adapter =
                                    Recyclerview1Adapter(
                                        (Gson().fromJson<Any>(
                                            map["recoSong"].toString(),
                                            object :
                                                TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                                        ) as ArrayList<HashMap<String?, Any?>?>)
                                    )
                                binding.recyclerview2.adapter = Recyclerview2Adapter(
                                    (Gson().fromJson<Any>(
                                        map["hot"].toString(),
                                        object :
                                            TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                                    ) as ArrayList<HashMap<String?, Any?>?>)
                                )
                                binding.listview1.adapter =
                                    Listview1Adapter(
                                        (Gson().fromJson<Any>(
                                            map["allSong"].toString(),
                                            object :
                                                TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                                        ) as ArrayList<HashMap<String?, Any?>>)
                                    )
                                val allSongListType =
                                    object : TypeToken<ArrayList<HashMap<String?, Any?>?>>() {}.type
                                val rawList: ArrayList<HashMap<String?, Any?>?> =
                                    Gson().fromJson(map["allSong"].toString(), allSongListType)

                                val allSong = ArrayList<HashMap<String?, Any?>>()
                                rawList.forEach { item ->
                                    if (item != null) {
                                        val castedItem = item.mapKeys { it.key }
                                            .toMap() // Casting keys to String?
                                        allSong.add(castedItem as HashMap<String?, Any?>)
                                    }
                                }
                                binding.listview1.adapter = Listview1Adapter(allSong)

                                TempBase.allMusicListMap = (Gson().fromJson<Any>(
                                    map["allSong"].toString(),
                                    object :
                                        TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                                ) as ArrayList<HashMap<String?, Any?>?>)
                                allSongListMap = TempBase.allMusicListMap
                                binding.recyclerview3.adapter = Recyclerview3Adapter(
                                    (Gson().fromJson<Any>(
                                        map["topArtists"].toString(),
                                        object :
                                            TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                                    ) as ArrayList<HashMap<String, Any>>)
                                )
                                binding.recyclerview6.adapter = Recyclerview6Adapter(
                                    (Gson().fromJson<Any>(
                                        map["weeSong"].toString(),
                                        object :
                                            TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                                    ) as ArrayList<HashMap<String?, Any?>?>)
                                )
                                binding.recyclerview7.adapter = Recyclerview7Adapter(
                                    (Gson().fromJson<Any>(
                                        map["albSong"].toString(),
                                        object :
                                            TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                                    ) as ArrayList<HashMap<String?, Any?>?>)
                                )
                                binding.recyclerview10.adapter = Recyclerview10Adapter(
                                    (Gson().fromJson<Any>(
                                        map["dailySong"].toString(),
                                        object :
                                            TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                                    ) as ArrayList<HashMap<String?, Any?>?>)
                                )
                                binding.recyclerview4.adapter = Recyclerview4Adapter(
                                    (Gson().fromJson<Any>(
                                        map["topSong"].toString(),
                                        object :
                                            TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                                    ) as ArrayList<HashMap<String?, Any?>?>)
                                )
                                binding.recyclerview5.adapter = Recyclerview5Adapter(
                                    (Gson().fromJson<Any>(
                                        map["allArt"].toString(),
                                        object :
                                            TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                                    ) as ArrayList<HashMap<String, Any>>)
                                )
                                binding.gridview1.adapter =
                                    Gridview1Adapter(
                                        (Gson().fromJson<Any>(
                                            map["allArt"].toString(),
                                            object :
                                                TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                                        ) as ArrayList<HashMap<String, Any>>)
                                    )


                                binding.recyclerview11.adapter = Recyclerview11Adapter(
                                    (Gson().fromJson<Any>(
                                        map["newArt"].toString(),
                                        object :
                                            TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                                    ) as ArrayList<HashMap<String, Any>>)
                                )
                                binding.recyclerview12.adapter = Recyclerview12Adapter(
                                    (Gson().fromJson<Any>(
                                        map["episode"].toString(),
                                        object :
                                            TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                                    ) as ArrayList<HashMap<String?, Any?>?>)
                                )
                                binding.recyclerview13.adapter = Recyclerview13Adapter(
                                    (Gson().fromJson<Any>(
                                        map["podcast"].toString(),
                                        object :
                                            TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                                    ) as ArrayList<HashMap<String?, Any?>?>)
                                )
                                binding.recyclerview15.adapter = Recyclerview15Adapter(
                                    (Gson().fromJson<Any>(
                                        map["newSpcl"].toString(),
                                        object :
                                            TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                                    ) as ArrayList<HashMap<String?, Any?>?>)
                                )
                                binding.recyclerview17.adapter = Recyclerview17Adapter(
                                    (Gson().fromJson<Any>(
                                        map["newMix"].toString(),
                                        object :
                                            TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                                    ) as ArrayList<HashMap<String?, Any?>?>)
                                )
                            }
                        } catch (e: Exception) {
                            home()
                        }
                    } catch (_: IOException) {
                    }
                }).start()
        }
        if (TempBase.getData("link_id") != "") {
            val linkId = TempBase.getData("link_id")
            for (i in allSongListMap.indices) {
                if (Uri.parse(allSongListMap[i]!!["song_url"].toString()).lastPathSegment.toString()
                        .equals(linkId, ignoreCase = true)
                ) {
                    mact!!.prepare(allSongListMap, i.toDouble())
                    (activity as MainActivity?)!!.sheet()
                    break
                }
            }
        }

        if (TempBase.getData("artist_id") != "") {
            Thread {
                try {
                    val doc = Jsoup.connect("https://vachanammusic.com/api.php")
                        .data("api", "getArtists").data("data", "").get().text()
                    activity?.runOnUiThread {
                        val arrayTemp = Gson().fromJson<ArrayList<HashMap<String, Any>>>(
                            doc,
                            object : TypeToken<ArrayList<HashMap<String, Any>>>() {}.type
                        )
                        for (i in 1 until arrayTemp.size) {
                            if (arrayTemp[i]["artist_id"].toString() == TempBase.getData("artist_id")) {
                                val hm: HashMap<String, Any> = HashMap(arrayTemp[i])
                                mact!!.song(hm)
                                TempBase.removeData("artist_id")
                                break
                            }
                        }
                    }
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }.start()
        }
    }

    fun type() {
        mact = this.activity as MainActivity?
        act = this
        testString = Gson().toJson(testLMap)
        testLMap = Gson().fromJson(
            testString,
            object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
        )
        type = "1"
    }

    fun shareText(subject: String?, text: String?) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.setType("text/plain")
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(intent, "Share using"))
    }


    inner class Recyclerview5Adapter(var data: ArrayList<HashMap<String, Any>>) :
        RecyclerView.Adapter<Recyclerview5Adapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = activity!!.layoutInflater
            val v = inflater.inflate(R.layout.songs_category, null)
            val lp = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            v.layoutParams = lp
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val view = holder.itemView
            val hscroll2 = view.findViewById<HorizontalScrollView>(R.id.hscroll2)
            val linear7 = view.findViewById<LinearLayout>(R.id.linear7)
            val linear12 = view.findViewById<LinearLayout>(R.id.linear12)
            val linear9 = view.findViewById<LinearLayout>(R.id.linear9)
            val linear11 = view.findViewById<LinearLayout>(R.id.linear11)
            val linear57 = view.findViewById<LinearLayout>(R.id.linear57)
            val linear10 = view.findViewById<LinearLayout>(R.id.linear10)
            val textview5 = view.findViewById<TextView>(R.id.textview5)
            val textview10 = view.findViewById<TextView>(R.id.textview10)
            val textview12 = view.findViewById<TextView>(R.id.textview12)
            val textview9 = view.findViewById<TextView>(R.id.textview9)
            val textview11 = view.findViewById<TextView>(R.id.textview11)
            linear57.background = object : GradientDrawable() {
                fun getIns(a: Int, b: Int, c: Int, d: Int): GradientDrawable {
                    this.cornerRadius = a.toFloat()
                    this.setStroke(b, c)
                    this.setColor(d)
                    return this
                }
            }.getIns(80, 1, -0x876f64, Color.TRANSPARENT)
            linear9.background = object : GradientDrawable() {
                fun getIns(a: Int, b: Int, c: Int, d: Int): GradientDrawable {
                    this.cornerRadius = a.toFloat()
                    this.setStroke(b, c)
                    this.setColor(d)
                    return this
                }
            }.getIns(80, 1, -0x876f64, Color.TRANSPARENT)
            linear10.background = object : GradientDrawable() {
                fun getIns(a: Int, b: Int, c: Int, d: Int): GradientDrawable {
                    this.cornerRadius = a.toFloat()
                    this.setStroke(b, c)
                    this.setColor(d)
                    return this
                }
            }.getIns(80, 1, -0x876f64, Color.TRANSPARENT)
            linear11.background = object : GradientDrawable() {
                fun getIns(a: Int, b: Int, c: Int, d: Int): GradientDrawable {
                    this.cornerRadius = a.toFloat()
                    this.setStroke(b, c)
                    this.setColor(d)
                    return this
                }
            }.getIns(80, 1, -0x876f64, Color.TRANSPARENT)
            linear12.background = object : GradientDrawable() {
                fun getIns(a: Int, b: Int, c: Int, d: Int): GradientDrawable {
                    this.cornerRadius = a.toFloat()
                    this.setStroke(b, c)
                    this.setColor(d)
                    return this
                }
            }.getIns(80, 1, -0x876f64, Color.TRANSPARENT)
            textview9.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.light),
                Typeface.BOLD
            )
            textview10.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.light),
                Typeface.BOLD
            )
            textview11.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.light),
                Typeface.BOLD
            )
            textview12.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.light),
                Typeface.BOLD
            )
            textview5.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.light),
                Typeface.BOLD
            )
            val animation: Animation? = AnimationUtils.loadAnimation(
                context!!.applicationContext,
                android.R.anim.slide_in_left
            )
            animation?.duration = 300
            linear7.startAnimation(animation)
            linear57.setOnClickListener {
                artistData = data[6]
                mact!!.song(artistData)
            }
            linear9.setOnClickListener {
                artistData = data[2]
                mact!!.song(artistData)
            }
            linear12.setOnClickListener {
                artistData = data[12]
                mact!!.song(artistData)
            }
            linear11.setOnClickListener {
                artistData = data[3]
                mact!!.song(artistData)
            }
            linear10.setOnClickListener {
                artistData = data[11]
                mact!!.song(artistData)
            }
            hscroll2.isHorizontalScrollBarEnabled = false
        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class ViewHolder(v: View?) : RecyclerView.ViewHolder(v!!)
    }

    inner class Listview1Adapter(var data: ArrayList<HashMap<String?, Any?>>) :
        BaseAdapter() {
        override fun getCount(): Int {
            return data.size
        }

        override fun getItem(index: Int): HashMap<String?, Any?> {
            return data[index]
        }

        override fun getItemId(index: Int): Long {
            return index.toLong()
        }

        override fun getView(position: Int, v: View?, container: ViewGroup?): View {
            val view: View =
                v ?: activity?.layoutInflater?.inflate(R.layout.square_tile, null) ?: View(context)

            val linear1 = view.findViewById<LinearLayout>(R.id.linear1)
            val linear3 = view.findViewById<LinearLayout>(R.id.linear3)
            val imageview1 = view.findViewById<CircleImageView>(R.id.imageview1)
            val imageview18 = view.findViewById<ImageView>(R.id.imageview18)
            val textview1 = view.findViewById<TextView>(R.id.textview1)
            val textview31 = view.findViewById<TextView>(R.id.textview31)
            val textview2 = view.findViewById<TextView>(R.id.textview2)
            Glide.with(context!!.applicationContext)
                .load(Uri.parse(data[position]["song_cover"].toString())).into(imageview1)
            textview1.text = data[position]["song_title"].toString()
            textview2.text =
                ((data[position]["song_view"].toString().toDouble()).toLong()).toString()
            textview31.text = data[position]["song_description"].toString()
            binding.listview1.isVerticalScrollBarEnabled = false


            linear1.setOnClickListener {
                textview2.text = ((data[position]["song_view"].toString()
                    .toDouble() + 1).toLong()).toString()

                val nullableData: ArrayList<HashMap<String?, Any?>?> = ArrayList(data)
                mact!!.prepare(nullableData, position.toDouble())

            }

             fun shareMusicLink() {
               val songUrl = data[position]["song_url"].toString()
                val songCover = data[position]["song_cover"].toString()
                val songTitle = data[position]["song_title"].toString()
                val songDes = data[position]["song_description"].toString()


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
            imageview18.setOnClickListener {
                d!!.setTitle(data[position]["song_title"].toString())
                d!!.setMessage(data[position]["song_description"].toString())
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





            linear3.visibility = View.GONE
            textview1.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.light),
                Typeface.BOLD
            )
            textview2.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.light),
                Typeface.NORMAL
            )
            textview31.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.light),
                Typeface.NORMAL
            )
            textview1.ellipsize = TextUtils.TruncateAt.MARQUEE
            textview1.marqueeRepeatLimit = -1
            textview1.isSingleLine = true
            textview1.isSelected = true
            textview31.ellipsize = TextUtils.TruncateAt.MARQUEE
            textview31.marqueeRepeatLimit = -1
            textview31.isSingleLine = true
            textview31.isSelected = true
            var animation: Animation? = AnimationUtils.loadAnimation(
                context!!.applicationContext,
                android.R.anim.slide_in_left
            )
            animation?.duration = 300
            linear1.startAnimation(animation)
            return view
        }
    }

    inner class Recyclerview4Adapter(var data: ArrayList<HashMap<String?, Any?>?>) :
        RecyclerView.Adapter<Recyclerview4Adapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = activity!!.layoutInflater
            val v = inflater.inflate(R.layout.square_tile, null)
            val lp = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            v.layoutParams = lp
            return ViewHolder(v)
        }



        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val view = holder.itemView
            val linear2 = view.findViewById<FrameLayout>(R.id.linear2)
            val linear1 = view.findViewById<LinearLayout>(R.id.linear1)
            val linear3 = view.findViewById<LinearLayout>(R.id.linear3)
            val imageview1 = view.findViewById<CircleImageView>(R.id.imageview1)
            val imageview18 = view.findViewById<ImageView>(R.id.imageview18)
            val textview1 = view.findViewById<TextView>(R.id.textview1)
            val textview31 = view.findViewById<TextView>(R.id.textview31)
            val textview2 = view.findViewById<TextView>(R.id.textview2)

            (linear2.parent as LinearLayout).layoutParams = linear2.layoutParams
            var animation: Animation?
            animation = AnimationUtils.loadAnimation(
                context!!.applicationContext,
                android.R.anim.slide_in_left
            )
            animation.duration = 300
            linear1.startAnimation(animation)
            animation = null
            Glide.with(context!!.applicationContext).load(
                Uri.parse(
                    data[position]!!["song_cover"].toString()
                )
            ).into(imageview1)
            textview1.text = data[position]!!["song_title"].toString()
            textview2.text =
                ((data[position]!!["song_view"].toString().toDouble()).toLong()).toString()


            textview31.text = data[position]!!["song_description"].toString()


            linear1.setOnClickListener {
                textview2.text = ((data[position]!!["song_view"].toString()
                    .toDouble() + 1).toLong()).toString()
                mact!!.prepare(data, position.toDouble())
            }



            linear3.visibility = View.GONE
            binding.recyclerview4.isVerticalScrollBarEnabled = false
            textview1.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.light),
                Typeface.BOLD
            )
            textview2.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.light),
                Typeface.NORMAL
            )
            textview31.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.light),
                Typeface.NORMAL
            )


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
            imageview18.setOnClickListener {
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

            textview1.ellipsize = TextUtils.TruncateAt.MARQUEE
            textview1.marqueeRepeatLimit = -1
            textview1.isSingleLine = true
            textview1.isSelected = true
            textview31.ellipsize = TextUtils.TruncateAt.MARQUEE
            textview31.marqueeRepeatLimit = -1
            textview31.isSingleLine = true
            textview31.isSelected = true
        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class ViewHolder(v: View?) : RecyclerView.ViewHolder(v!!)
    }

    inner class Recyclerview1Adapter(var data: ArrayList<HashMap<String?, Any?>?>) :
        RecyclerView.Adapter<Recyclerview1Adapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = activity!!.layoutInflater
            val v = inflater.inflate(R.layout.round_tile, null)
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
            val textview1 = view.findViewById<TextView>(R.id.textview1)
            val imageview1 = view.findViewById<ImageView>(R.id.imageview1)
            val linear3 = view.findViewById<LinearLayout>(R.id.linear3)
            val linear4 = view.findViewById<LinearLayout>(R.id.linear4)
            val textview2 = view.findViewById<TextView>(R.id.textview2)
            (linear1.parent as LinearLayout).layoutParams =
                LinearLayout.LayoutParams(
                    ((binding.recyclerview1.width * 0.75f).toInt()),
                    binding.recyclerview1.height
                )
            Glide.with(context!!.applicationContext).load(
                Uri.parse(
                    data[position]!!["song_cover"].toString()
                )
            ).into(imageview1)
            textview1.text = data[position]!!["song_title"].toString()
            linear1.setOnClickListener {
                if (data[position]!!["song_view"].toString().toDouble() > 998) {
                    textview2.text = DecimalFormat("#.#K").format(
                        (data[position]!!["song_view"].toString().toDouble() + 1) / 1000
                    )
                } else {
                    textview2.text =
                        ((data[position]!!["song_view"].toString()
                            .toDouble() + 1).toLong()).toString()
                }
                mact!!.prepare(data, position.toDouble())
            }
            linear4.background = object : GradientDrawable() {
                fun getIns(a: Int, b: Int, c: Int, d: Int): GradientDrawable {
                    this.cornerRadius = a.toFloat()
                    this.setStroke(b, c)
                    this.setColor(d)
                    return this
                }
            }.getIns(80, 1, -0x876f64, Color.TRANSPARENT)
            linear1.background = object : GradientDrawable() {
                fun getIns(a: Int, b: Int, c: Int, d: Int): GradientDrawable {
                    this.cornerRadius = a.toFloat()
                    this.setStroke(b, c)
                    this.setColor(d)
                    return this
                }
            }.getIns(15, 1, -0x4f413b, Color.TRANSPARENT)
            linear3.visibility = View.GONE
            textview2.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.light),
                Typeface.BOLD
            )
            textview1.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.light),
                Typeface.BOLD
            )
            textview1.ellipsize = TextUtils.TruncateAt.MARQUEE
            textview1.marqueeRepeatLimit = -1
            textview1.isSingleLine = true
            textview1.isSelected = true
            var animation: Animation? = AnimationUtils.loadAnimation(
                context!!.applicationContext,
                android.R.anim.slide_in_left
            )
            if (animation != null) {
                animation.duration = 300
            }
            linear1.startAnimation(animation)
        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class ViewHolder(v: View?) : RecyclerView.ViewHolder(v!!)
    }

    inner class Recyclerview2Adapter(var data: ArrayList<HashMap<String?, Any?>?>) :
        RecyclerView.Adapter<Recyclerview2Adapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = activity!!.layoutInflater
            val v = inflater.inflate(R.layout.square_song_title, null)
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
            val imageview1 = view.findViewById<ImageView>(R.id.imageview1)
            val textview1 = view.findViewById<TextView>(R.id.textview1)
            val textview3 = view.findViewById<TextView>(R.id.textview3)
            val textview2 = view.findViewById<TextView>(R.id.textview2)
            (linear1.parent as LinearLayout).layoutParams = linear1.layoutParams
            Glide.with(context!!.applicationContext).load(
                Uri.parse(
                    data[position]!!["song_cover"].toString()
                )
            ).into(imageview1)
            textview1.text = data[position]!!["song_title"].toString()
            textview3.text = data[position]!!["song_description"].toString()
            textview2.text =
                ((data[position]!!["song_view"].toString().toDouble()).toLong()).toString()
            linear1.setOnClickListener {
                textview2.text = ((data[position]!!["song_view"].toString()
                    .toDouble() + 1).toLong()).toString()
                mact!!.prepare(data, position.toDouble())
            }
            binding.recyclerview2.isHorizontalScrollBarEnabled = false
            var animation: Animation?
            animation = AnimationUtils.loadAnimation(
                context!!.applicationContext,
                android.R.anim.slide_in_left
            )
            animation.duration = 300
            linear1.startAnimation(animation)
            textview1.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.spotify),
                Typeface.BOLD
            )
            textview3.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.spotify),
                Typeface.BOLD
            )
            textview2.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.spotify),
                Typeface.BOLD
            )
            textview1.ellipsize = TextUtils.TruncateAt.MARQUEE
            textview1.marqueeRepeatLimit = -1
            textview1.isSingleLine = true
            textview1.isSelected = true
            textview3.ellipsize = TextUtils.TruncateAt.MARQUEE
            textview3.marqueeRepeatLimit = -1
            textview3.isSingleLine = true
            textview3.isSelected = true
        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class ViewHolder(v: View?) : RecyclerView.ViewHolder(v!!)
    }

    inner class Recyclerview10Adapter(var data: ArrayList<HashMap<String?, Any?>?>) :
        RecyclerView.Adapter<Recyclerview10Adapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = activity!!.layoutInflater
            val v = inflater.inflate(R.layout.daily, null)
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
            val imageview1 = view.findViewById<ImageView>(R.id.imageview1)
            val textview1 = view.findViewById<TextView>(R.id.textview1)
            val textview3 = view.findViewById<TextView>(R.id.textview3)
            (linear1.parent as LinearLayout).layoutParams = linear1.layoutParams
            Glide.with(context!!.applicationContext).load(
                Uri.parse(
                    data[position]!!["song_cover"].toString()
                )
            ).into(imageview1)
            textview1.text = data[position]!!["song_title"].toString()
            textview3.text = data[position]!!["song_description"].toString()
            linear1.setOnClickListener { mact!!.prepare(data, position.toDouble()) }
            var animation: Animation?
            animation = AnimationUtils.loadAnimation(
                context!!.applicationContext,
                android.R.anim.slide_in_left
            )
            animation.duration = 300
            linear1.startAnimation(animation)
            textview1.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.spotify),
                Typeface.BOLD
            )
            textview3.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.spotify),
                Typeface.BOLD
            )
            textview1.ellipsize = TextUtils.TruncateAt.MARQUEE
            textview1.marqueeRepeatLimit = -1
            textview1.isSingleLine = true
            textview1.isSelected = true
            textview3.ellipsize = TextUtils.TruncateAt.MARQUEE
            textview3.marqueeRepeatLimit = -1
            textview3.isSingleLine = true
            textview3.isSelected = true
        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class ViewHolder(v: View?) : RecyclerView.ViewHolder(v!!)
    }

    inner class Recyclerview11Adapter(var data: ArrayList<HashMap<String, Any>>) :
        RecyclerView.Adapter<Recyclerview11Adapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = activity!!.layoutInflater
            val v = inflater.inflate(R.layout.newartist_title, null)
            val lp = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            v.layoutParams = lp
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val view = holder.itemView
            val linear5 = view.findViewById<LinearLayout>(R.id.linear5)
            val textview1 = view.findViewById<TextView>(R.id.textview1)
            val circleimageview2 = view.findViewById<CircleImageView>(R.id.circleimageview2)
            (linear5.parent as LinearLayout).layoutParams = linear5.layoutParams
            Glide.with(context!!.applicationContext)
                .load(Uri.parse(data[position]["artist_img_perfil"].toString() + "?" + data[position]["edited"].toString()))
                .into(circleimageview2)
            textview1.text = data[position]["artist_alias"].toString()
            linear5.setOnClickListener {
                artistData = data[position]
                mact!!.song(artistData)
            }
            textview1.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.spotify),
                Typeface.BOLD
            )
        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class ViewHolder(v: View?) : RecyclerView.ViewHolder(v!!)
    }

    inner class Recyclerview6Adapter(var data: ArrayList<HashMap<String?, Any?>?>) :
        RecyclerView.Adapter<Recyclerview6Adapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = activity!!.layoutInflater
            val v = inflater.inflate(R.layout.album, null)
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
            val textview2 = view.findViewById<TextView>(R.id.textview2)
            val textview1 = view.findViewById<TextView>(R.id.textview1)
            val textview3 = view.findViewById<TextView>(R.id.textview3)
            val imageview1 = view.findViewById<ImageView>(R.id.imageview1)
            Glide.with(context!!.applicationContext).load(
                Uri.parse(
                    data[position]!!["song_cover"].toString()
                )
            ).into(imageview1)
            textview1.text = data[position]!!["song_title"].toString()
            textview2.text =
                ((data[position]!!["song_view"].toString().toDouble()).toLong()).toString()
            textview3.text = data[position]!!["song_description"].toString()
            imageview1.setOnClickListener {
                textview2.text = ((data[position]!!["song_view"].toString()
                    .toDouble() + 1).toLong()).toString()
                mact!!.prepare(data, position.toDouble())
            }
            binding.recyclerview6.isHorizontalScrollBarEnabled = false
            var animation: Animation? = AnimationUtils.loadAnimation(
                context!!.applicationContext,
                android.R.anim.slide_in_left
            )
            if (animation != null) {
                animation.duration = 300
            }
            linear1.startAnimation(animation)
            textview1.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.spotify),
                        Typeface.BOLD
            )
            textview2.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.spotify),
                Typeface.BOLD
            )
            textview3.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.spotify),
                Typeface.BOLD
            )
            textview1.ellipsize = TextUtils.TruncateAt.MARQUEE
            textview1.marqueeRepeatLimit = -1
            textview1.isSingleLine = true
            textview1.isSelected = true
            textview3.ellipsize = TextUtils.TruncateAt.MARQUEE
            textview3.marqueeRepeatLimit = -1
            textview3.isSingleLine = true
            textview3.isSelected = true
            linear1.background = object : GradientDrawable() {
                fun getIns(a: Int, b: Int, c: Int, d: Int): GradientDrawable {
                    this.cornerRadius = a.toFloat()
                    this.setStroke(b, c)
                    this.setColor(d)
                    return this
                }
            }.getIns(15, 1, -0x4f413b, Color.TRANSPARENT)
        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class ViewHolder(v: View?) : RecyclerView.ViewHolder(v!!)
    }

    inner class Recyclerview7Adapter(var data: ArrayList<HashMap<String?, Any?>?>) :
        RecyclerView.Adapter<Recyclerview7Adapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = activity!!.layoutInflater
            val v = inflater.inflate(R.layout.weekly, null)
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
            val imageview1 = view.findViewById<ImageView>(R.id.imageview1)
            val textview1 = view.findViewById<TextView>(R.id.textview1)
            val textview3 = view.findViewById<TextView>(R.id.textview3)
            (linear1.parent as LinearLayout).layoutParams = linear1.layoutParams
            Glide.with(context!!.applicationContext).load(
                Uri.parse(
                    data[position]!!["song_cover"].toString()
                )
            ).into(imageview1)
            textview1.text = data[position]!!["song_title"].toString()
            textview3.text = data[position]!!["song_description"].toString()
            linear1.setOnClickListener { mact!!.prepare(data, position.toDouble()) }
            binding.recyclerview2.isHorizontalScrollBarEnabled = false
            textview1.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.spotify),
                Typeface.BOLD
            )
            textview3.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.spotify),
                Typeface.BOLD
            )
            var animation: Animation? = AnimationUtils.loadAnimation(
                context!!.applicationContext,
                android.R.anim.slide_in_left
            )
            if (animation != null) {
                animation.duration = 300
            }
            linear1.startAnimation(animation)
            textview1.ellipsize = TextUtils.TruncateAt.MARQUEE
            textview1.marqueeRepeatLimit = -1
            textview1.isSingleLine = true
            textview1.isSelected = true
            textview3.ellipsize = TextUtils.TruncateAt.MARQUEE
            textview3.marqueeRepeatLimit = -1
            textview3.isSingleLine = true
            textview3.isSelected = true
        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class ViewHolder(v: View?) : RecyclerView.ViewHolder(v!!)
    }


    inner class Gridview1Adapter(var data: ArrayList<HashMap<String, Any>>) :
        BaseAdapter() {
        override fun getCount(): Int {
            return data.size
        }

        override fun getItem(index: Int): HashMap<String, Any> {
            return data[index]
        }

        override fun getItemId(index: Int): Long {
            return index.toLong()
        }


        override fun getView(position: Int, v: View?, container: ViewGroup): View {
            val inflater = activity!!.layoutInflater
            var view = v ?: inflater.inflate(R.layout.large_tile, container, false)

            if (view == null) {
                view = inflater.inflate(R.layout.large_tile, container, false)
            }


            val linear29 = view?.findViewById<LinearLayout>(R.id.linear29)
            val linear4 = view?.findViewById<LinearLayout>(R.id.linear4)
            val imageview13 = view?.findViewById<ImageView>(R.id.imageview13)
            val textview11 = view?.findViewById<TextView>(R.id.textview11)
            val imageview12 = view?.findViewById<ImageView>(R.id.imageview12)
            val textview10 = view?.findViewById<TextView>(R.id.textview10)
            val imageview6 = view?.findViewById<ImageView>(R.id.imageview6)
            val textview4 = view?.findViewById<TextView>(R.id.textview4)
            val imageview5 = view?.findViewById<ImageView>(R.id.imageview5)
            val textview3 = view?.findViewById<TextView>(R.id.textview3)
            textview3?.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.light),
                Typeface.NORMAL
            )
            textview4?.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.light),
                Typeface.NORMAL
            )
            textview11?.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.light),
                Typeface.NORMAL
            )
            textview10?.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.light),
                Typeface.NORMAL
            )
            imageview5?.setColorFilter(-0xaffa9, PorterDuff.Mode.MULTIPLY)
            imageview6?.setColorFilter(-0xaffa9, PorterDuff.Mode.MULTIPLY)
            imageview13?.setColorFilter(-0xaffa9, PorterDuff.Mode.MULTIPLY)
            imageview12?.setColorFilter(-0xaffa9, PorterDuff.Mode.MULTIPLY)
            imageview13?.setOnClickListener {
                artistData = data[1]
                mact!!.song(artistData)
            }
            imageview12?.setOnClickListener {
                artistData = data[0]
                mact!!.song(artistData)
            }
            imageview6?.setOnClickListener {
                artistData = data[5]
                mact!!.song(artistData)
            }
            imageview5?.setOnClickListener {
                artistData = data[7]
                mact!!.song(artistData)
            }
            binding.gridview1.isVerticalScrollBarEnabled = false
            var animation: Animation?
            animation = AnimationUtils.loadAnimation(
                context!!.applicationContext,
                android.R.anim.slide_in_left
            )
            animation.duration = 300
            linear29?.startAnimation(animation)
            linear4?.startAnimation(animation)
            return view
        }
    }

    inner class Recyclerview3Adapter(var data: ArrayList<HashMap<String, Any>>) :
        RecyclerView.Adapter<Recyclerview3Adapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = activity!!.layoutInflater
            val v = inflater.inflate(R.layout.circle_tile, null)
            val lp = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            v.layoutParams = lp
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val view = holder.itemView
            val linear4 = view.findViewById<LinearLayout>(R.id.linear4)
            val linear5 = view.findViewById<LinearLayout>(R.id.linear5)
            val linear3 = view.findViewById<LinearLayout>(R.id.linear3)
            val linear1 = view.findViewById<LinearLayout>(R.id.linear1)
            val textview1 = view.findViewById<TextView>(R.id.textview1)
            val circleimageview2 = view.findViewById<CircleImageView>(R.id.circleimageview2)
            (linear5.parent as LinearLayout).layoutParams = linear5.layoutParams
            Glide.with(context!!.applicationContext)
                .load(Uri.parse(data[position]["artist_img_perfil"].toString() + "?" + data[position]["edited"].toString()))
                .into(circleimageview2)
            textview1.text = data[position]["artist_alias"].toString()
            linear1.setOnClickListener {
                artistData = data[position]
                mact!!.song(artistData)
            }
            textview1.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.spotify),
                Typeface.BOLD
            )
            linear3.visibility = View.GONE
            linear4.visibility = View.GONE
            var animation: Animation?
            animation = AnimationUtils.loadAnimation(
                context!!.applicationContext,
                android.R.anim.slide_in_left
            )
            animation.duration = 300
            linear1.startAnimation(animation)
        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class ViewHolder(v: View?) : RecyclerView.ViewHolder(v!!)
    }

    inner class Recyclerview12Adapter(var data: ArrayList<HashMap<String?, Any?>?>) :
        RecyclerView.Adapter<Recyclerview12Adapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = activity!!.layoutInflater
            val v = inflater.inflate(R.layout.imagerectangle_title, null)
            val lp = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            v.layoutParams = lp
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val view = holder.itemView
            val linear5 = view.findViewById<LinearLayout>(R.id.linear5)
            val textview1 = view.findViewById<TextView>(R.id.textview1)
            val imageview1 = view.findViewById<ImageView>(R.id.imageview1)
            (linear5.parent as LinearLayout).layoutParams = linear5.layoutParams
            Glide.with(context!!.applicationContext).load(
                Uri.parse(
                    data[position]!!["song_cover"].toString()
                )
            ).into(imageview1)
            textview1.text = data[position]!!["song_title"].toString()
            linear5.setOnClickListener { mact!!.prepare(data, position.toDouble()) }
            binding.recyclerview12.isHorizontalScrollBarEnabled = false
            textview1.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.spotify),
                Typeface.BOLD
            )
            textview1.ellipsize = TextUtils.TruncateAt.MARQUEE
            textview1.marqueeRepeatLimit = -1
            textview1.isSingleLine = true
            textview1.isSelected = true
            linear5.background = object : GradientDrawable() {
                fun getIns(a: Int, b: Int, c: Int, d: Int): GradientDrawable {
                    this.cornerRadius = a.toFloat()
                    this.setStroke(b, c)
                    this.setColor(d)
                    return this
                }
            }.getIns(15, 1, -0x4f413b, Color.TRANSPARENT)
        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class ViewHolder(v: View?) : RecyclerView.ViewHolder(v!!)
    }

    inner class Recyclerview13Adapter(var data: ArrayList<HashMap<String?, Any?>?>) :
        RecyclerView.Adapter<Recyclerview13Adapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = activity!!.layoutInflater
            val v = inflater.inflate(R.layout.new_title, null)
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
            val imageview1 = view.findViewById<ImageView>(R.id.imageview1)
            (linear1.parent as LinearLayout).layoutParams = linear1.layoutParams
            Glide.with(context!!.applicationContext).load(
                Uri.parse(
                    data[position]!!["song_cover"].toString()
                )
            ).into(imageview1)
            linear1.setOnClickListener { mact!!.prepare(data, position.toDouble()) }
            binding.recyclerview13.isHorizontalScrollBarEnabled = false
        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class ViewHolder(v: View?) : RecyclerView.ViewHolder(v!!)
    }

    inner class Recyclerview15Adapter(var data: ArrayList<HashMap<String?, Any?>?>) :
        RecyclerView.Adapter<Recyclerview15Adapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = activity!!.layoutInflater
            val v = inflater.inflate(R.layout.episode_title, null)
            val lp = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            v.layoutParams = lp
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val view = holder.itemView
            val linear5 = view.findViewById<LinearLayout>(R.id.linear5)
            val imageview1 = view.findViewById<ImageView>(R.id.imageview1)
            val textview1 = view.findViewById<TextView>(R.id.textview1)
            val textview2 = view.findViewById<TextView>(R.id.textview2)
            (linear5.parent as LinearLayout).layoutParams = linear5.layoutParams
            Glide.with(context!!.applicationContext).load(
                Uri.parse(
                    data[position]!!["song_cover"].toString()
                )
            ).into(imageview1)
            textview1.text = data[position]!!["song_title"].toString()
            textview2.text = data[position]!!["song_description"].toString()
            linear5.setOnClickListener { mact!!.prepare(data, position.toDouble()) }
            binding.recyclerview15.isHorizontalScrollBarEnabled = false
            linear5.background = object : GradientDrawable() {
                fun getIns(a: Int, b: Int, c: Int, d: Int): GradientDrawable {
                    this.cornerRadius = a.toFloat()
                    this.setStroke(b, c)
                    this.setColor(d)
                    return this
                }
            }.getIns(15, 1, -0x4f413b, Color.TRANSPARENT)
            textview1.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.spotify),
                Typeface.BOLD
            )
            textview2.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.spotify),
                Typeface.BOLD
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
            return data.size
        }

        inner class ViewHolder(v: View?) : RecyclerView.ViewHolder(v!!)
    }

    inner class Recyclerview17Adapter(var data: ArrayList<HashMap<String?, Any?>?>) :
        RecyclerView.Adapter<Recyclerview17Adapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = activity!!.layoutInflater
            val v = inflater.inflate(R.layout.podcast_title, null)
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
            val imageview1 = view.findViewById<ImageView>(R.id.imageview1)
            val textview1 = view.findViewById<TextView>(R.id.textview1)
            val textview3 = view.findViewById<TextView>(R.id.textview3)
            (linear1.parent as LinearLayout).layoutParams = linear1.layoutParams
            Glide.with(context!!.applicationContext).load(
                Uri.parse(
                    data[position]!!["song_cover"].toString()
                )
            ).into(imageview1)
            textview1.text = data[position]!!["song_title"].toString()
            textview3.text = data[position]!!["song_description"].toString()
            linear1.setOnClickListener { mact!!.prepare(data, position.toDouble()) }
            binding.recyclerview17.isHorizontalScrollBarEnabled = false
            textview1.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.spotify),
                Typeface.BOLD
            )
            textview3.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.spotify),
                Typeface.BOLD
            )
            textview1.ellipsize = TextUtils.TruncateAt.MARQUEE
            textview1.marqueeRepeatLimit = -1
            textview1.isSingleLine = true
            textview1.isSelected = true
            textview3.ellipsize = TextUtils.TruncateAt.MARQUEE
            textview3.marqueeRepeatLimit = -1
            textview3.isSingleLine = true
            textview3.isSelected = true
        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class ViewHolder(v: View?) : RecyclerView.ViewHolder(v!!)
    }



}
