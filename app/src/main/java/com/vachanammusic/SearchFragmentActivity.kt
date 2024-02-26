package com.vachanammusic

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
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
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseApp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vachanammusic.databinding.SearchFragmentBinding
import de.hdodenhof.circleimageview.CircleImageView
import org.jsoup.Jsoup
import java.io.IOException
import java.util.Calendar

class SearchFragmentActivity : Fragment() {
    private lateinit var binding: SearchFragmentBinding
    private var act: SearchFragmentActivity? = null
    private var testString = ""
    private var type = ""
    private var mact: MainActivity? = null
    private val apiUrl = ""
    private var testLMap = ArrayList<HashMap<String?, Any?>>()
    private var d: AlertDialog.Builder? = null
    private var conf: SharedPreferences? = null
    private var time = Calendar.getInstance()
    private val timeOld = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SearchFragmentBinding.inflate(inflater, container, false)
        val view = binding.root
        initialize()
        FirebaseApp.initializeApp(requireContext())
        initializeLogic()
        return view
    }

    private fun initialize() {
        d = AlertDialog.Builder(activity)
        conf = requireContext().getSharedPreferences("conf", Activity.MODE_PRIVATE)
        binding.edittext1.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(
                param1: CharSequence,
                param2: Int,
                param3: Int,
                param4: Int
            ) {
                val charSeq = param1.toString()
                if (charSeq.isNotEmpty()) {
                    binding.imageview1.visibility = View.VISIBLE
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
                                            binding.listview1.adapter = Listview1Adapter(
                                                Gson().fromJson<Any>(
                                                    doc,
                                                    object :
                                                        TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                                                ) as ArrayList<HashMap<String?, Any?>?>
                                            )
                                            (binding.listview1.adapter as BaseAdapter).notifyDataSetChanged()
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
        binding.imageview1.setOnClickListener { (binding.listview1.adapter as BaseAdapter).notifyDataSetChanged() }
        binding.imageview3.setOnClickListener {
            binding.edittext1.setText("")
            (binding.listview1.adapter as BaseAdapter).notifyDataSetChanged()
        }
        binding.linear126.setOnClickListener {
            time = Calendar.getInstance()
            if (conf?.getString("lastUpdate", "") == "") {
                conf?.edit()?.putString("lastUpdate", (time.timeInMillis - 60000).toString())
                    ?.apply()
            }
            timeOld.timeInMillis = conf?.getString("lastUpdate", "")!!.toDouble().toLong()
            if (conf?.getString("homeCache", "") != "") {
                val cacheMap = Gson().fromJson<HashMap<String, Any>>(
                    conf?.getString("homeCache", ""),
                    object : TypeToken<HashMap<String?, Any?>?>() {}.type
                )
                binding.listview1.adapter =
                    Listview1Adapter(
                        Gson().fromJson<Any>(
                            cacheMap["hot"].toString(),
                            object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                        ) as ArrayList<HashMap<String?, Any?>?>
                    )
            }
            if ((time.timeInMillis - timeOld.timeInMillis) > 59999) {
                conf?.edit()?.putString("lastUpdate", time.timeInMillis.toString())?.apply()
                Thread(
                    Runnable {
                        try {
                            val doc = Jsoup.connect(apiUrl).data("api", "home").get().text()
                            try {
                                val map = Gson().fromJson<HashMap<String, Any>>(
                                    doc,
                                    object : TypeToken<HashMap<String?, Any?>?>() {}.type
                                )
                                conf?.edit()?.putString("homeCache", doc)?.apply()
                                if (activity == null) return@Runnable
                                requireActivity().runOnUiThread {
                                    binding.listview1.adapter =
                                        Listview1Adapter(
                                            Gson().fromJson<Any>(
                                                map["hot"].toString(),
                                                object :
                                                    TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                                            ) as ArrayList<HashMap<String?, Any?>?>
                                        )
                                }
                            } catch (_: Exception) {
                            }
                        } catch (_: IOException) {
                        }
                    }).start()
            }
        }
        binding.linear9.setOnClickListener {
            time = Calendar.getInstance()
            if (conf?.getString("lastUpdate", "") == "") {
                conf?.edit()?.putString("lastUpdate", (time.timeInMillis - 60000).toString())
                    ?.apply()
            }
            timeOld.timeInMillis = conf?.getString("lastUpdate", "")!!.toDouble().toLong()
            if (conf?.getString("homeCache", "") != "") {
                val cacheMap = Gson().fromJson<HashMap<String, Any>>(
                    conf?.getString("homeCache", ""),
                    object : TypeToken<HashMap<String?, Any?>?>() {}.type
                )
                binding.listview1.adapter =
                    Listview1Adapter(
                        Gson().fromJson<Any>(
                            cacheMap["topSong"].toString(),
                            object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                        ) as ArrayList<HashMap<String?, Any?>?>
                    )
            }
            if ((time.timeInMillis - timeOld.timeInMillis) > 59999) {
                conf?.edit()?.putString("lastUpdate", (time.timeInMillis).toString())?.apply()
                Thread(
                    Runnable {
                        try {
                            val doc = Jsoup.connect(apiUrl).data("api", "home").get().text()
                            try {
                                val map = Gson().fromJson<HashMap<String, Any>>(
                                    doc,
                                    object : TypeToken<HashMap<String?, Any?>?>() {}.type
                                )
                                conf?.edit()?.putString("homeCache", doc)?.apply()
                                if (activity == null) return@Runnable
                                requireActivity().runOnUiThread {
                                    binding.listview1.adapter =
                                        Listview1Adapter(
                                            (Gson().fromJson<Any>(
                                                map["topSong"].toString(),
                                                object :
                                                    TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                                            ) as ArrayList<HashMap<String?, Any?>?>)
                                        )
                                }
                            } catch (_: Exception) {
                            }
                        } catch (_: IOException) {
                        }
                    }).start()
            }
        }
        binding.linear12.setOnClickListener {
            time = Calendar.getInstance()
            if ((conf?.getString("lastUpdate", "") == "")) {
                conf?.edit()?.putString("lastUpdate", (time.timeInMillis - 60000).toString())
                    ?.apply()
            }
            timeOld.timeInMillis = (conf?.getString("lastUpdate", "")!!.toDouble()).toLong()
            if (conf?.getString("homeCache", "") != "") {
                val cacheMap = Gson().fromJson<HashMap<String, Any>>(
                    conf?.getString("homeCache", ""),
                    object : TypeToken<HashMap<String?, Any?>?>() {}.type
                )
                binding.listview1.adapter =
                    Listview1Adapter(
                        (Gson().fromJson<Any>(
                            cacheMap["weeSong"].toString(),
                            object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                        ) as ArrayList<HashMap<String?, Any?>?>)
                    )
            }
            if ((time.timeInMillis - timeOld.timeInMillis) > 59999) {
                conf?.edit()?.putString("lastUpdate", (time.timeInMillis).toString())?.apply()
                Thread(
                    Runnable {
                        try {
                            val doc = Jsoup.connect(apiUrl).data("api", "home").get().text()
                            try {
                                val map = Gson().fromJson<HashMap<String, Any>>(
                                    doc,
                                    object : TypeToken<HashMap<String?, Any?>?>() {}.type
                                )
                                conf?.edit()?.putString("homeCache", doc)?.apply()
                                if (activity == null) return@Runnable
                                requireActivity().runOnUiThread {
                                    binding.listview1.adapter =
                                        Listview1Adapter(
                                            (Gson().fromJson<Any>(
                                                map["weeSong"].toString(),
                                                object :
                                                    TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                                            ) as ArrayList<HashMap<String?, Any?>?>)
                                        )
                                }
                            } catch (_: Exception) {
                            }
                        } catch (_: IOException) {
                        }
                    }).start()
            }
        }
        binding.linear11.setOnClickListener {
            time = Calendar.getInstance()
            if ((conf?.getString("lastUpdate", "") == "")) {
                conf?.edit()?.putString("lastUpdate", (time.timeInMillis - 60000).toString())
                    ?.apply()
            }
            timeOld.timeInMillis = (conf?.getString("lastUpdate", "")!!.toDouble()).toLong()
            if (conf?.getString("homeCache", "") != "") {
                val cacheMap = Gson().fromJson<HashMap<String, Any>>(
                    conf?.getString("homeCache", ""),
                    object : TypeToken<HashMap<String?, Any?>?>() {}.type
                )
                binding.listview1.adapter =
                    Listview1Adapter(
                        (Gson().fromJson<Any>(
                            cacheMap["dailySong"].toString(),
                            object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                        ) as ArrayList<HashMap<String?, Any?>?>)
                    )
            }
            if ((time.timeInMillis - timeOld.timeInMillis) > 59999) {
                conf?.edit()?.putString("lastUpdate", (time.timeInMillis).toString())?.apply()
                Thread(
                    Runnable {
                        try {
                            val doc = Jsoup.connect(apiUrl).data("api", "home").get().text()
                            try {
                                val map = Gson().fromJson<HashMap<String, Any>>(
                                    doc,
                                    object : TypeToken<HashMap<String?, Any?>?>() {}.type
                                )
                                conf?.edit()?.putString("homeCache", doc)?.apply()
                                if (activity == null) return@Runnable
                                requireActivity().runOnUiThread {
                                    binding.listview1.adapter =
                                        Listview1Adapter(
                                            (Gson().fromJson<Any>(
                                                map["dailySong"].toString(),
                                                object :
                                                    TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                                            ) as ArrayList<HashMap<String?, Any?>?>)
                                        )
                                }
                            } catch (_: Exception) {
                            }
                        } catch (_: IOException) {
                        }
                    }).start()
            }
        }
        binding.linear124.setOnClickListener { mact!!.buscar1() }
        binding.linear57.setOnClickListener {
            time = Calendar.getInstance()
            if ((conf?.getString("lastUpdate", "") == "")) {
                conf?.edit()?.putString("lastUpdate", (time.timeInMillis - 60000).toString())
                    ?.apply()
            }
            timeOld.timeInMillis = (conf?.getString("lastUpdate", "")!!.toDouble()).toLong()
            if (conf?.getString("homeCache", "") != "") {
                val cacheMap = Gson().fromJson<HashMap<String, Any>>(
                    conf?.getString("homeCache", ""),
                    object : TypeToken<HashMap<String?, Any?>?>() {}.type
                )
                binding.listview1.adapter =
                    Listview1Adapter(
                        (Gson().fromJson<Any>(
                            cacheMap["allSong"].toString(),
                            object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                        ) as ArrayList<HashMap<String?, Any?>?>)
                    )
            }
            if ((time.timeInMillis - timeOld.timeInMillis) > 59999) {
                conf?.edit()?.putString("lastUpdate", (time.timeInMillis).toString())?.apply()
                Thread(
                    Runnable {
                        try {
                            val doc = Jsoup.connect(apiUrl).data("api", "home").get().text()
                            try {
                                val map = Gson().fromJson<HashMap<String, Any>>(
                                    doc,
                                    object : TypeToken<HashMap<String?, Any?>?>() {}.type
                                )
                                conf?.edit()?.putString("homeCache", doc)?.apply()
                                if (activity == null) return@Runnable
                                requireActivity().runOnUiThread {
                                    binding.listview1.adapter =
                                        Listview1Adapter(
                                            (Gson().fromJson<Any>(
                                                map["allSong"].toString(),
                                                object :
                                                    TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                                            ) as ArrayList<HashMap<String?, Any?>?>)
                                        )
                                }
                            } catch (_: Exception) {
                            }
                        } catch (_: IOException) {
                        }
                    }).start()
            }
        }
    }

    private fun initializeLogic() {
        start()
        binding.edittext1.setTypeface(
            ResourcesCompat.getFont(requireContext(), R.font.light),
            Typeface.BOLD
        )
        binding.textview4.setTypeface(
            ResourcesCompat.getFont(requireContext(), R.font.light),
            Typeface.BOLD
        )
        binding.textview9.setTypeface(
            ResourcesCompat.getFont(requireContext(), R.font.light),
            Typeface.BOLD
        )
        binding.textview10.setTypeface(
            ResourcesCompat.getFont(requireContext(), R.font.light),
            Typeface.BOLD
        )
        binding.textview15.setTypeface(
            ResourcesCompat.getFont(requireContext(), R.font.light),
            Typeface.BOLD
        )
        binding.textview16.setTypeface(
            ResourcesCompat.getFont(requireContext(), R.font.light),
            Typeface.BOLD
        )
        binding.textview5.setTypeface(
            ResourcesCompat.getFont(requireContext(), R.font.light),
            Typeface.BOLD
        )
        binding.textview12.setTypeface(
            ResourcesCompat.getFont(requireContext(), R.font.light),
            Typeface.BOLD
        )
        binding.linear124.background = object : GradientDrawable() {
            fun getIns(a: Int, b: Int, c: Int, d: Int): GradientDrawable {
                this.cornerRadius = a.toFloat()
                this.setStroke(b, c)
                this.setColor(d)
                return this
            }
        }.getIns(50, 1, -0x1, Color.TRANSPARENT)
        binding.linear57.background = object : GradientDrawable() {
            fun getIns(a: Int, b: Int, c: Int, d: Int): GradientDrawable {
                this.cornerRadius = a.toFloat()
                this.setStroke(b, c)
                this.setColor(d)
                return this
            }
        }.getIns(50, 1, -0x1, Color.TRANSPARENT)
        binding.linear126.background = object : GradientDrawable() {
            fun getIns(a: Int, b: Int, c: Int, d: Int): GradientDrawable {
                this.cornerRadius = a.toFloat()
                this.setStroke(b, c)
                this.setColor(d)
                return this
            }
        }.getIns(50, 1, -0x1, Color.TRANSPARENT)
        binding.linear9.background = object : GradientDrawable() {
            fun getIns(a: Int, b: Int, c: Int, d: Int): GradientDrawable {
                this.cornerRadius = a.toFloat()
                this.setStroke(b, c)
                this.setColor(d)
                return this
            }
        }.getIns(50, 1, -0x1, Color.TRANSPARENT)
        binding.linear12.background = object : GradientDrawable() {
            fun getIns(a: Int, b: Int, c: Int, d: Int): GradientDrawable {
                this.cornerRadius = a.toFloat()
                this.setStroke(b, c)
                this.setColor(d)
                return this
            }
        }.getIns(50, 1, -0x1, Color.TRANSPARENT)
        binding.linear11.background = object : GradientDrawable() {
            fun getIns(a: Int, b: Int, c: Int, d: Int): GradientDrawable {
                this.cornerRadius = a.toFloat()
                this.setStroke(b, c)
                this.setColor(d)
                return this
            }
        }.getIns(50, 1, -0x1, Color.TRANSPARENT)
        binding.linear1.background = object : GradientDrawable() {
            fun getIns(a: Int, b: Int, c: Int, d: Int): GradientDrawable {
                this.cornerRadius = a.toFloat()
                this.setStroke(b, c)
                this.setColor(d)
                return this
            }
        }.getIns(80, 1, -0x9f8275, Color.TRANSPARENT)
        binding.hscroll1.isHorizontalScrollBarEnabled = false
        binding.imageview1.visibility = View.GONE
        binding.imageview3.visibility = View.GONE
    }

    private fun start() {
        mact = activity as MainActivity?
        act = this
        testString = Gson().toJson(testLMap)
        testLMap = Gson().fromJson(
            testString,
            object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
        )
        type = "1"
        home()
    }

    private fun shareText(_subject: String?, _text: String?) {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_SUBJECT, _subject)
        i.putExtra(Intent.EXTRA_TEXT, _text)
        startActivity(Intent.createChooser(i, "Share using"))
    }

    private fun home() {
        time = Calendar.getInstance()
        if (conf!!.getString("lastUpdate", "") == "") {
            conf!!.edit().putString("lastUpdate", (time.timeInMillis - 60000).toString()).apply()
        }
        timeOld.timeInMillis = (conf!!.getString("lastUpdate", "")!!.toDouble()).toLong()
        if (conf!!.getString("homeCache", "") != "") {
            val cacheMap = Gson().fromJson<HashMap<String, Any>>(
                conf!!.getString("homeCache", ""),
                object : TypeToken<HashMap<String?, Any?>?>() {}.type
            )
            binding.listview1.adapter =
                Listview1Adapter(
                    Gson().fromJson<Any>(
                        cacheMap["hot"].toString(),
                        object : TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                    ) as ArrayList<HashMap<String?, Any?>?>
                )
        }
        if ((time.timeInMillis - timeOld.timeInMillis) > 59999) {
            conf!!.edit().putString("lastUpdate", time.timeInMillis.toString()).apply()
            Thread(
                Runnable {
                    try {
                        val doc = Jsoup.connect(apiUrl).data("api", "home").get().text()
                        try {
                            val map = Gson().fromJson<HashMap<String, Any>>(
                                doc,
                                object : TypeToken<HashMap<String?, Any?>?>() {}.type
                            )
                            conf!!.edit().putString("homeCache", doc).apply()
                            if (activity == null) return@Runnable
                            requireActivity().runOnUiThread {
                                binding.listview1.adapter =
                                    Listview1Adapter(
                                        Gson().fromJson<Any>(
                                            map["hot"].toString(),
                                            object :
                                                TypeToken<ArrayList<HashMap<String?, Any?>?>?>() {}.type
                                        ) as ArrayList<HashMap<String?, Any?>?>
                                    )
                            }
                        } catch (_: Exception) {
                        }
                    } catch (_: IOException) {
                    }
                }).start()
        }
    }

    inner class Listview1Adapter(var data: ArrayList<HashMap<String?, Any?>?>) :
        BaseAdapter() {
        override fun getCount(): Int {
            return data.size
        }

        override fun getItem(index: Int): java.util.HashMap<String?, Any?>? {
            return data[index]
        }

        override fun getItemId(index: Int): Long {
            return index.toLong()
        }

        override fun getView(position: Int, v: View?, container: ViewGroup): View? {
            val inflater = activity!!.layoutInflater
            var view = v
            if (view == null) {
                view = inflater.inflate(R.layout.square_tile, null)
            }
            val linear1 = view?.findViewById<LinearLayout>(R.id.linear1)
            val linear3 = view?.findViewById<LinearLayout>(R.id.linear3)
            val imageview1 = view?.findViewById<CircleImageView>(R.id.imageview1)
            val imageview18 = view?.findViewById<ImageView>(R.id.imageview18)
            val textview1 = view?.findViewById<TextView>(R.id.textview1)
            val textview31 = view?.findViewById<TextView>(R.id.textview31)
            val textview2 = view?.findViewById<TextView>(R.id.textview2)

            if (imageview1 != null) {
                Glide.with(context!!.applicationContext)
                    .load(Uri.parse(data[position]!!["song_cover"].toString())).into(imageview1)
            }


            textview1?.text = data[position]!!["song_title"].toString()
            textview2?.text =
                ((data[position]!!["song_view"].toString().toDouble()).toLong()).toString()
            textview31?.text = data[position]!!["song_description"].toString()
            textview1?.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.light),
                Typeface.BOLD
            )
            textview2?.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.light),
                Typeface.NORMAL
            )
            textview31?.setTypeface(
                ResourcesCompat.getFont(requireContext(), R.font.light),
                Typeface.NORMAL
            )
            linear3?.visibility = View.GONE
            linear1?.setOnClickListener {
                textview2?.text = ((data[position]!!["song_view"].toString()
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
            imageview18?.setOnClickListener {
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

            textview1?.ellipsize = TextUtils.TruncateAt.MARQUEE
            textview1?.marqueeRepeatLimit = -1
            textview1?.isSingleLine = true
            textview1?.isSelected = true
            textview31?.ellipsize = TextUtils.TruncateAt.MARQUEE
            textview31?.marqueeRepeatLimit = -1
            textview31?.isSingleLine = true
            textview31?.isSelected = true
            binding.listview1.isVerticalScrollBarEnabled = false
            return view
        }
    }

}
