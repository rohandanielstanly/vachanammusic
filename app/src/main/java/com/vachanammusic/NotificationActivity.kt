package com.vachanammusic

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.text.util.Linkify
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseApp
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.vachanammusic.databinding.NotificationBinding
import java.util.Random

class NotificationActivity : AppCompatActivity() {
    private val firebase = FirebaseDatabase.getInstance()

    private lateinit var binding: NotificationBinding
    private val news = firebase.getReference("news")
    private var news_child_listener: ChildEventListener? = null

    private val pick = Intent(Intent.ACTION_GET_CONTENT)
    private var sp: SharedPreferences? = null
    private val intent = Intent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialize()
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
        pick.setType("image/*")
        pick.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        sp = getSharedPreferences("sp", MODE_PRIVATE)
        binding.imageview11.setOnClickListener {
            intent.setClass(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        news_child_listener = object : ChildEventListener {
            override fun onChildAdded(param1: DataSnapshot, param2: String?) {
                val ind: GenericTypeIndicator<HashMap<String?, Any?>?> =
                    object : GenericTypeIndicator<HashMap<String?, Any?>?>() {}
                param1.key
                param1.getValue(ind)!!
                news.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val map = ArrayList<HashMap<String, Any>>()
                        try {
                            val ind: GenericTypeIndicator<HashMap<String, Any>> =
                                object : GenericTypeIndicator<HashMap<String, Any>>() {}
                            for (data in dataSnapshot.children) {
                                val _map = data.getValue(ind)!!
                                map.add(_map)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace() // Handle specific exceptions here
                        }
                        binding.listview1.adapter = Listview1Adapter(map)
                        (binding.listview1.adapter as? BaseAdapter)?.notifyDataSetChanged()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }

            override fun onChildChanged(param1: DataSnapshot, param2: String?) {
                val ind: GenericTypeIndicator<HashMap<String?, Any?>?> =
                    object : GenericTypeIndicator<HashMap<String?, Any?>?>() {}
                param1.key
                param1.getValue(ind)!!
                news.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val map = ArrayList<HashMap<String, Any>>() // Use non-nullable types
                        try {
                            val ind: GenericTypeIndicator<HashMap<String, Any>> =
                                object : GenericTypeIndicator<HashMap<String, Any>>() {}
                            for (data in dataSnapshot.children) {
                                val _map = data.getValue(ind)
                                if (_map != null) {
                                    map.add(_map)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace() // Handle specific exceptions here
                        }
                        binding.listview1.adapter = Listview1Adapter(map)
                        (binding.listview1.adapter as? BaseAdapter)?.notifyDataSetChanged()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }

            override fun onChildMoved(param1: DataSnapshot, param2: String?) {}
            override fun onChildRemoved(param1: DataSnapshot) {
                val ind: GenericTypeIndicator<HashMap<String?, Any?>?> =
                    object : GenericTypeIndicator<HashMap<String?, Any?>?>() {}
                param1.key
                param1.getValue(ind)!!
                news.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val map = ArrayList<HashMap<String, Any>>()
                        try {
                            val ind: GenericTypeIndicator<HashMap<String, Any>> =
                                object : GenericTypeIndicator<HashMap<String, Any>>() {}
                            for (data in dataSnapshot.children) {
                                val _map = data.getValue(ind)
                                _map?.let {
                                    map.add(it)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace() // Handle specific exceptions here
                        }
                        binding.listview1.adapter = Listview1Adapter(map)
                        (binding.listview1.adapter as? BaseAdapter)?.notifyDataSetChanged()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }

            override fun onCancelled(param1: DatabaseError) {
                param1.code
                param1.message
            }
        }
        news.addChildEventListener(news_child_listener as ChildEventListener)

    }

    private fun initializeLogic() {
        binding.textview1.setTypeface(
            ResourcesCompat.getFont(this, R.font.light),
            Typeface.BOLD
        )
        // for transparent status bar👇//
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.parseColor("#000000")
        themecolour()
        removeScollBar(binding.listview1)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        intent.setClass(applicationContext, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun themecolour() {
        if (sp!!.getString("theme", "") == "2") {
            binding.main.setBackgroundResource(R.drawable.theme_2)
        }
        if (sp!!.getString("theme", "") == "5") {
            binding.main.setBackgroundResource(R.drawable.theme_5)
        }
        if (sp!!.getString("theme", "") == "6") {
            binding.main.setBackgroundResource(R.drawable.theme_6)
        }
        if (sp!!.getString("theme", "") == "9") {
            binding.main.setBackgroundResource(R.drawable.main_bg)
        }
        if (sp!!.getString("theme", "") == "10") {
            binding.main.setBackgroundResource(R.drawable.theme_9_1)
        }
        if (sp!!.getString("theme", "") == "11") {
            binding.main.setBackgroundResource(R.drawable.theme_9_2)
        }
        if (sp!!.getString("theme", "") == "13") {
            binding.main.setBackgroundResource(R.drawable.theme_9_4)
        }
    }

    fun setTextLink(txt: TextView, _message: String?) {
        txt.text = _message
        txt.isClickable = true
        Linkify.addLinks(txt, Linkify.ALL)
        txt.setLinkTextColor(Color.parseColor("#2196F3"))
        txt.linksClickable = true
    }

    private var prog: ProgressDialog? = null

    fun removeScollBar(view: View?) {
        view!!.isVerticalScrollBarEnabled = false
        view.isHorizontalScrollBarEnabled = false
    }

    fun rippleRoundStroke(
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

    fun radius(
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
    }

    inner class Listview1Adapter(var data: ArrayList<HashMap<String, Any>>) :
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

        override fun getView(position: Int, v: View?, container: ViewGroup): View? {
            var view = v
            val inflater = LayoutInflater.from(container.context)

            if (view == null) {
                view = inflater.inflate(R.layout.notification_view, container, false)
            }

            val linear1 = view?.findViewById<LinearLayout>(R.id.linear1)
            val imageview2 = view?.findViewById<ImageView>(R.id.imageview2)
            val textview1 = view?.findViewById<TextView>(R.id.textview1)
            val textview2 = view?.findViewById<TextView>(R.id.textview2)
            val time = view?.findViewById<TextView>(R.id.time)

            binding.listview1.setSelector(android.R.color.transparent)

            textview1?.setTypeface(
                ResourcesCompat.getFont(this@NotificationActivity, R.font.light),
                Typeface.BOLD
            )
            textview2?.setTypeface(
                ResourcesCompat.getFont(this@NotificationActivity, R.font.light),
                Typeface.BOLD
            )

            val dataIndex = (data.size - 1) - position
            if (dataIndex >= 0 && dataIndex < data.size) {
                val dataItem = data[dataIndex]

                if (dataItem.containsKey("image")) {
                    imageview2?.visibility = View.VISIBLE
                    if (imageview2 != null) {
                        Glide.with(container.context)
                            .load(Uri.parse(dataItem["image"].toString()))
                            .into(imageview2)
                    }
                } else {
                    imageview2?.visibility = View.GONE
                }

                if (dataItem.containsKey("title")) {
                    textview1?.text = dataItem["title"].toString()
                }

                if (dataItem.containsKey("message")) {
                    if (textview2 != null) {
                        setTextLink(textview2, dataItem["message"].toString())
                    }
                }

                if (dataItem.containsKey("date")) {
                    time?.text = dataItem["date"].toString()
                }
            }

            textview2?.ellipsize = TextUtils.TruncateAt.MARQUEE
            textview2?.marqueeRepeatLimit = -1
            textview2?.isSingleLine = true
            textview2?.isSelected = true
            textview1?.ellipsize = TextUtils.TruncateAt.MARQUEE
            textview1?.marqueeRepeatLimit = -1
            textview1?.isSingleLine = true
            textview1?.isSelected = true

            linear1?.background = GradientDrawable().apply {
                cornerRadius = 20f
                setStroke(1, -0x876f64)
                setColor(Color.TRANSPARENT)
            }

            return view
        }
    }

    @Deprecated("")
    fun showMessage(s: String?) {
        Toast.makeText(applicationContext, s, Toast.LENGTH_SHORT).show()
    }

    @Deprecated("")
    fun getLocationX(v: View): Int {
        val location = IntArray(2)
        v.getLocationInWindow(location)
        return location[0]
    }

    @Deprecated("")
    fun getLocationY(v: View): Int {
        val location = IntArray(2)
        v.getLocationInWindow(location)
        return location[1]
    }

    @Deprecated("")
    fun getRandom(min: Int, max: Int): Int {
        val random = Random()
        return random.nextInt(max - min + 1) + min
    }

    @Deprecated("")
    fun getCheckedItemPositionsToArray(list: ListView): ArrayList<Double> {
        val result = ArrayList<Double>()
        val arr = list.checkedItemPositions
        for (iIdx in 0 until arr.size()) {
            if (arr.valueAt(iIdx)) result.add(arr.keyAt(iIdx).toDouble())
        }
        return result
    }

    @Deprecated("")
    fun getDip(input: Int): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            input.toFloat(),
            resources.displayMetrics
        )
    }

}
