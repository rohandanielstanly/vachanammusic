package com.vachanammusic

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Random
import java.util.Timer
import java.util.TimerTask

class LanguageSelectionActivity : AppCompatActivity() {
    private var bg: LinearLayout? = null
    private var h1: TextView? = null
    private var bg2: LinearLayout? = null
    private var linear25: LinearLayout? = null
    private var linear30: LinearLayout? = null
    private var linear1: LinearLayout? = null
    private var linear2: LinearLayout? = null
    private var linear3: LinearLayout? = null
    private var c1: CircleImageView? = null
    private var t1: TextView? = null
    private var c2: CircleImageView? = null
    private var t2: TextView? = null
    private var c3: CircleImageView? = null
    private var t3: TextView? = null
    private var linear4: LinearLayout? = null
    private var linear5: LinearLayout? = null
    private var linear6: LinearLayout? = null
    private var c4: CircleImageView? = null
    private var t4: TextView? = null
    private var c5: CircleImageView? = null
    private var t5: TextView? = null
    private var c6: CircleImageView? = null
    private var t6: TextView? = null
    private val ln = Intent()
    private var sp: SharedPreferences? = null
    override fun onCreate(_savedInstanceState: Bundle?) {
        super.onCreate(_savedInstanceState)
        setContentView(R.layout.language_selection)
        initialize(_savedInstanceState)
        FirebaseApp.initializeApp(this)
        initializeLogic()
    }

    private fun initialize(_savedInstanceState: Bundle?) {
        bg = findViewById(R.id.bg)
        h1 = findViewById(R.id.h1)
        bg2 = findViewById(R.id.bg2)
        linear25 = findViewById(R.id.linear25)
        linear30 = findViewById(R.id.linear30)
        linear1 = findViewById(R.id.linear1)
        linear2 = findViewById(R.id.linear2)
        linear3 = findViewById(R.id.linear3)
        c1 = findViewById(R.id.c1)
        t1 = findViewById(R.id.t1)
        c2 = findViewById(R.id.c2)
        t2 = findViewById(R.id.t2)
        c3 = findViewById(R.id.c3)
        t3 = findViewById(R.id.t3)
        linear4 = findViewById(R.id.linear4)
        linear5 = findViewById(R.id.linear5)
        linear6 = findViewById(R.id.linear6)
        c4 = findViewById(R.id.c4)
        t4 = findViewById(R.id.t4)
        c5 = findViewById(R.id.c5)
        t5 = findViewById(R.id.t5)
        c6 = findViewById(R.id.c6)
        t6 = findViewById(R.id.t6)
        sp = getSharedPreferences("sp", MODE_PRIVATE)
        linear1!!.setOnClickListener(View.OnClickListener {
            sp!!.edit().putString("language", "ml").commit()
            ln.setClass(applicationContext, MainActivity::class.java)
            startActivity(ln)
        })
        linear2!!.setOnClickListener(View.OnClickListener {
            sp!!.edit().putString("language", "en").commit()
            ln.setClass(applicationContext, MainActivity::class.java)
            startActivity(ln)
        })
        linear3!!.setOnClickListener(View.OnClickListener {
            sp!!.edit().putString("language", "hi").commit()
            ln.setClass(applicationContext, MainActivity::class.java)
            startActivity(ln)
        })
        linear4!!.setOnClickListener(View.OnClickListener {
            sp!!.edit().putString("language", "ta").commit()
            ln.setClass(applicationContext, MainActivity::class.java)
            startActivity(ln)
        })
        linear5!!.setOnClickListener(View.OnClickListener {
            sp!!.edit().putString("language", "te").commit()
            ln.setClass(applicationContext, MainActivity::class.java)
            startActivity(ln)
        })
        linear6!!.setOnClickListener(View.OnClickListener {
            sp!!.edit().putString("language", "gu").commit()
            ln.setClass(applicationContext, MainActivity::class.java)
            startActivity(ln)
        })
    }

    private fun initializeLogic() {}
    @Deprecated("")
    fun showMessage(_s: String?) {
        Toast.makeText(applicationContext, _s, Toast.LENGTH_SHORT).show()
    }

    @Deprecated("")
    fun getLocationX(_v: View): Int {
        val _location = IntArray(2)
        _v.getLocationInWindow(_location)
        return _location[0]
    }

    @Deprecated("")
    fun getLocationY(_v: View): Int {
        val _location = IntArray(2)
        _v.getLocationInWindow(_location)
        return _location[1]
    }

    @Deprecated("")
    fun getRandom(_min: Int, _max: Int): Int {
        val random = Random()
        return random.nextInt(_max - _min + 1) + _min
    }

    @Deprecated("")
    fun getCheckedItemPositionsToArray(_list: ListView): ArrayList<Double> {
        val _result = ArrayList<Double>()
        val _arr = _list.checkedItemPositions
        for (_iIdx in 0 until _arr.size()) {
            if (_arr.valueAt(_iIdx)) _result.add(_arr.keyAt(_iIdx).toDouble())
        }
        return _result
    }

    @Deprecated("")
    fun getDip(_input: Int): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            _input.toFloat(),
            resources.displayMetrics
        )
    }

    @get:Deprecated("")
    val displayWidthPixels: Int
        get() = resources.displayMetrics.widthPixels

    @get:Deprecated("")
    val displayHeightPixels: Int
        get() = resources.displayMetrics.heightPixels
}
