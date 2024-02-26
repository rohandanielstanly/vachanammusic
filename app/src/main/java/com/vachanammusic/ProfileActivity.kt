package com.vachanammusic

import android.Manifest
import android.content.Context
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
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.vachanammusic.databinding.ProfileBinding
import java.util.Random

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ProfileBinding
    private val intent = Intent()
    private var name: SharedPreferences? = null
    private var img: SharedPreferences? = null
    private val up = Intent(Intent.ACTION_GET_CONTENT)
    private var sp: SharedPreferences? = null
    private var size: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialize()
        binding.edit.visibility = View.GONE
        binding.linear36.visibility = View.GONE

        FirebaseApp.initializeApp(this)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
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

        name = getSharedPreferences("name", MODE_PRIVATE)
        img = getSharedPreferences("img", MODE_PRIVATE)
        up.setType("image/*")
        up.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        sp = getSharedPreferences("sp", MODE_PRIVATE)
        size = getSharedPreferences("size", MODE_PRIVATE)
        binding.imageview10.setOnClickListener {
            intent.setClass(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.linear34.setOnClickListener {
            intent.setClass(applicationContext, SettingsActivity::class.java)
            startActivity(intent)
        }
        binding.linear38.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://api.whatsapp.com/send?phone=+918075408534")
            startActivity(intent)
        }
        binding.linear30.setOnClickListener {
            shareText(
                "Listen to your favourite Christian Music - Malayalam, English, Tamil, Hindi\n\nThis is the app for listening Christian Music on your smartphone \nEnjoy the best songs for christians! It's a FREE app.",
                "https://play.google.com/store/apps/details?id=com.vachanammusic"
            )
        }
        binding.linear11.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            intent.setClass(applicationContext, MainsigninActivity::class.java)
            startActivity(intent)
            Util.showMessage(applicationContext, "Signed Out")
            finish()
        }
        binding.edit.setOnClickListener { binding.linearEditName.visibility = View.VISIBLE }
//        binding.profile.setOnClickListener { startActivityForResult(up, REQ_CD_UP) }


        binding.buttonSaveName.setOnClickListener {
            val newInfo = binding.edittext1.text.toString().trim()

            if (newInfo.isNotEmpty()) {
                binding.linearEditName.visibility = View.GONE

                // Load and set the profile image
                val imagePath = img?.getString("img", "")
                if (imagePath != null) {
                    val bitmap = FileUtil.decodeSampleBitmapFromPath(imagePath, 1024, 1024)
                    binding.profile.setImageBitmap(bitmap)
                }

                // Save the new name to SharedPreferences
                name?.edit()?.putString("name", newInfo)?.apply()

                // Update the UI with the saved name
                binding.textview32.text = name?.getString("name", "")

                // Clear the edit text
                binding.edittext1.setText("")

                Util.showMessage(applicationContext, "Saved")
            } else {
                Util.showMessage(applicationContext, "Enter your new info")
            }
        }

        binding.textview10.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            intent.setClass(applicationContext, MainsigninActivity::class.java)
            startActivity(intent)
            Util.showMessage(applicationContext, "Signed Out")
            finish()
        }
    }

    private fun initializeLogic() {
        binding.textview33.text = FirebaseAuth.getInstance().currentUser!!.email
        changeActivityFont("spotify")
        // for transparent status bar//
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.parseColor("#000000")
        binding.linearEditName.visibility = View.GONE
        themecolour()
        addCardView(binding.profile, 0.0, 360.0, 4.0, 4.0, true, "#101D24")
        rippleRoundStroke(binding.edit, "#FF4433", "#FFFFFF", 360.0, 2.0, "#FF4433")
        rippleRoundStroke(binding.linear44, "#F50057", "#FFFFFF", 80.0, 0.0, "#FFFFFF")
        rippleRoundStroke(binding.buttonSaveName, "#00C853", "#FFFFFF", 80.0, 0.0, "#FFFFFF")
        clickAnimation(binding.linear11)
        clickAnimation(binding.linear34)
        clickAnimation(binding.linear30)
        clickAnimation(binding.buttonSaveName)
        clickAnimation(binding.linear38)
        binding.vscroll1.isVerticalScrollBarEnabled = false
    }

    override fun onStart() {
        super.onStart()

        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        if (name!!.getString("name", "") == "") {
            if (currentUser != null) {
                val userName = currentUser.displayName

                binding.textview32.text = userName
            }
        } else {
            binding.textview32.text = name!!.getString("name", "")
        }
             // Load and set the profile image from Firebase or manually added
        if (currentUser != null) {
            val photoUrl = currentUser.photoUrl

            // Check if the user has a profile image URL
            if (photoUrl != null) {
                // Load the profile image using an image loading library (e.g., Glide)
                Glide.with(this)
                    .load(photoUrl)
                    .into(binding.profile)
            } else {
                // If no profile image URL is available, set a default image
                binding.profile.setImageResource(R.drawable.profile_pic)
            }
        }

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

    fun rippleRoundStroke(
        view: View?,
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
        view!!.background = RE
    }

    fun themecolour() {
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

    fun removeScollBar(view: View) {
        view.isVerticalScrollBarEnabled = false
        view.isHorizontalScrollBarEnabled = false
    }

    fun shareText(subject: String?, text: String?) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.setType("text/plain")
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(intent, "Share using"))
    }

    fun addCardView(
        layoutView: View?,
        margins: Double,
        cornerRadius: Double,
        cardElevation: Double,
        cardMaxElevation: Double,
        preventCornerOverlap: Boolean,
        backgroundColor: String?
    ) {
        val cv = CardView(this)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val m = margins.toInt()
        lp.setMargins(m, m, m, m)
        cv.layoutParams = lp
        val c = Color.parseColor(backgroundColor)
        cv.setCardBackgroundColor(c)
        cv.radius = cornerRadius.toFloat()
        cv.cardElevation = cardElevation.toFloat()
        cv.maxCardElevation = cardMaxElevation.toFloat()
        cv.preventCornerOverlap = preventCornerOverlap
        if (layoutView!!.parent is LinearLayout) {
            val vg = layoutView.parent as ViewGroup
            vg.removeView(layoutView)
            vg.removeAllViews()
            vg.addView(cv)
            cv.addView(layoutView)
        } else {
        }
    }

    fun clickAnimation(view: View?) {
        val fade_in = ScaleAnimation(
            0.9f,
            1f,
            0.9f,
            1f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.7f
        )
        fade_in.duration = 300
        fade_in.fillAfter = true
        view!!.startAnimation(fade_in)
    }

    @Deprecated("")
    fun showMessage(applicationContext: Context, s: String?) {
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