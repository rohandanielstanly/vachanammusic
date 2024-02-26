package com.vachanammusic

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.EditText
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.vachanammusic.databinding.SettingsBinding
import java.util.Random


class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: SettingsBinding
    private var fontName = ""
    private val intent = Intent()
    private lateinit var sp: SharedPreferences
    private lateinit var size: SharedPreferences
    private lateinit var name: SharedPreferences
    private lateinit var img: SharedPreferences
    private lateinit var styl: SharedPreferences
    private val ln = Intent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialize()
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
        sp = getSharedPreferences("sp", MODE_PRIVATE)
        size = getSharedPreferences("size", MODE_PRIVATE)
        name = getSharedPreferences("name", MODE_PRIVATE)
        img = getSharedPreferences("img", MODE_PRIVATE)
        styl = getSharedPreferences("styl", MODE_PRIVATE)

        binding.imageview13.setOnClickListener {
            intent.setClass(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.linear37.setOnClickListener {
            intent.setClass(applicationContext, ProfileActivity::class.java)
            startActivity(intent)
        }

        binding.linear34.setOnClickListener {
            intent.setClass(applicationContext, ThemeActivity::class.java)
            startActivity(intent)
        }

        binding.linear28.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://vachanammusic.com/privacypolicy.pdf")
            startActivity(intent)
        }

        binding.linear29.setOnClickListener {
            // Create the AlertDialog
            val addDialog = AlertDialog.Builder(this@SettingsActivity).create()

            // Inflate the custom layout into the AlertDialog
            val addDialogLI = layoutInflater
            val addDialogCV = addDialogLI.inflate(R.layout.contact_dialog, null)
            addDialog.setView(addDialogCV)

            // Find views inside the inflated layout
            val t1 = addDialogCV.findViewById<TextView>(R.id.t1)
            val t2 = addDialogCV.findViewById<TextView>(R.id.t2)
            val t3 = addDialogCV.findViewById<TextView>(R.id.t3)
            val t4 = addDialogCV.findViewById<TextView>(R.id.t4)
            val b2 = addDialogCV.findViewById<TextView>(R.id.b2)
            val bg = addDialogCV.findViewById<LinearLayout>(R.id.bg)

            // Apply radius to background
            rippleRoundStroke(bg, "#FFFFFF", "#FFFFFF", 35.0, 0.0, "#000000")
            rippleRoundStroke(b2, "#3D5AFE", "#FAFAFA", 35.0, 0.0, "#000000")

            // Set typeface for TextViews
            t1.typeface = ResourcesCompat.getFont(this, R.font.light)
            t2.typeface = ResourcesCompat.getFont(this, R.font.light)
            t3.typeface = ResourcesCompat.getFont(this, R.font.light)
            t4.typeface = ResourcesCompat.getFont(this, R.font.light)

            // Set OnClickListener for TextViews
            t1.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://vachanammusic.com")
                startActivity(intent)
                addDialog.dismiss()
            }
            t2.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://api.whatsapp.com/send?phone=+918075408534")
                startActivity(intent)
                addDialog.dismiss()
            }
            t3.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://www.facebook.com/vachanammusic/")
                startActivity(intent)
                addDialog.dismiss()
            }
            t4.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://api.instagram.com/vachanam_music?utm_medium=copy_link")
                startActivity(intent)
                addDialog.dismiss()
            }
            b2.setOnClickListener {
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
        }

        binding.linear10.setOnClickListener {
            intent.setAction(Intent.ACTION_VIEW)
            intent.setData(Uri.parse("https://api.whatsapp.com/send?phone=+918075408534"))
            startActivity(intent)
        }

        binding.linear9.setOnClickListener { appInfo() }


        binding.linear50.setOnClickListener { language() }

    }

    private fun initializeLogic() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.parseColor("#000000")
        changeActivityFont("spotify")
        themeColour()
        clickAnimation(binding.linear37)
        clickAnimation(binding.linear36)
        clickAnimation(binding.linear34)
        clickAnimation(binding.linear28)
        clickAnimation(binding.linear29)
        clickAnimation(binding.linear9)
        clickAnimation(binding.linear10)
        clickAnimation(binding.linear50)

        binding.vscroll1.isVerticalScrollBarEnabled = false
    }

    override fun onStart() {
        super.onStart()
        binding.textview33.text = FirebaseAuth.getInstance().currentUser!!.email

        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        if (name.getString("name", "") == "") {
            val userName = currentUser?.displayName

            binding.textview32.text = "$userName"
        } else {
            binding.textview32.text = name.getString("name", "")
        }


        // Load and set the profile image from Firebase or manually added
        if (currentUser != null) {
            val photoUrl = currentUser.photoUrl

            // Check if the user has a profile image URL
            if (photoUrl != null) {
                // Load the profile image using an image loading library (e.g., Glide)
                Glide.with(this)
                    .load(photoUrl)
                    .into(binding.circleimageview1)
            } else {
                // If no profile image URL is available, set a default image
                binding.circleimageview1.setImageResource(R.drawable.profile_pic)
            }
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        intent.setClass(applicationContext, MainActivity::class.java)
        startActivity(intent)
        finish()
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

    private fun appInfo() {
        val addDialog = AlertDialog.Builder(this@SettingsActivity).create()

        val addDialogLI = layoutInflater
        val addDialogCV = addDialogLI.inflate(R.layout.appinfo_dialog, null)
        addDialog.setView(addDialogCV)

        val t1 = addDialogCV.findViewById<TextView>(R.id.t1)
        val t3 = addDialogCV.findViewById<TextView>(R.id.t3)
        val t8 = addDialogCV.findViewById<TextView>(R.id.t8)
        val bg = addDialogCV.findViewById<LinearLayout>(R.id.bg)
        val b2 = addDialogCV.findViewById<LinearLayout>(R.id.b2)
        val i1 = addDialogCV.findViewById<ImageView>(R.id.i1)
        val i2 = addDialogCV.findViewById<ImageView>(R.id.i2)

        t1.setTypeface(ResourcesCompat.getFont(this, R.font.hevo_light), Typeface.NORMAL)
        t3.setTypeface(ResourcesCompat.getFont(this, R.font.hevo_light), Typeface.NORMAL)
        t8.setTypeface(ResourcesCompat.getFont(this, R.font.hevo_light), Typeface.NORMAL)

        t1.text = "Rozz Developz"
        t3.text = "Say Hello!"
        val versionName = applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0).versionName
        t8.text = "Version $versionName"
        i1.setImageResource(R.drawable.vm_logo)
        i2.setImageResource(R.drawable.close_white)


        t1.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://rozzdevelopz.com"))
            startActivity(intent)
        }

        t3.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://api.whatsapp.com/send?phone=+919061204568")
            startActivity(intent)
        }

        radius("#820062", "#FFFFFF", 0.0, 0.0, 0.0, 35.0, 35.0, bg)
        rippleRoundStroke(bg, "#FFFFFF", "#000000", 35.0, 0.0, "#000000")
        radius("#820062", "#FFFFFF", 0.0, 0.0, 0.0, 35.0, 35.0, b2)
        i2.setOnClickListener {
            addDialog.dismiss()
        }
        b2.setOnClickListener {
            addDialog.dismiss()
        }

        addDialog.setCancelable(true)
        addDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(addDialog.window?.attributes)
        layoutParams.gravity = Gravity.BOTTOM
        addDialog.window?.attributes = layoutParams
        addDialog.show()
    }

    fun themeColour() {
        if (sp.getString("theme", "") == "2") {
            binding.main.setBackgroundResource(R.drawable.theme_2)
        }
        if (sp.getString("theme", "") == "5") {
            binding.main.setBackgroundResource(R.drawable.theme_5)
        }
        if (sp.getString("theme", "") == "6") {
            binding.main.setBackgroundResource(R.drawable.theme_6)
        }
        if (sp.getString("theme", "") == "9") {
            binding.main.setBackgroundResource(R.drawable.main_bg)
        }
        if (sp.getString("theme", "") == "10") {
            binding.main.setBackgroundResource(R.drawable.theme_9_1)
        }
        if (sp.getString("theme", "") == "11") {
            binding.main.setBackgroundResource(R.drawable.theme_9_2)
        }
        if (sp.getString("theme", "") == "13") {
            binding.main.setBackgroundResource(R.drawable.theme_9_4)
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

fun language() {
    val bottomSheetDialog = BottomSheetDialog(this@SettingsActivity)
    val bottomSheetView = layoutInflater.inflate(R.layout.language_dialog, null)

    bottomSheetDialog.setContentView(bottomSheetView)
    val linear1 = bottomSheetView.findViewById<View>(R.id.linear1) as LinearLayout
    val linear2 = bottomSheetView.findViewById<View>(R.id.linear2) as LinearLayout
    val linear3 = bottomSheetView.findViewById<View>(R.id.linear3) as LinearLayout
    val linear4 = bottomSheetView.findViewById<View>(R.id.linear4) as LinearLayout
    val linear5 = bottomSheetView.findViewById<View>(R.id.linear5) as LinearLayout
    val linear6 = bottomSheetView.findViewById<View>(R.id.linear6) as LinearLayout

    sp = getSharedPreferences("sp", MODE_PRIVATE)
    linear1.setOnClickListener {
        sp.edit().putString("language", "ml").apply()
        ln.setClass(applicationContext, MainActivity::class.java)
        startActivity(ln)
    }
    linear2.setOnClickListener {
        sp.edit().putString("language", "en").apply()
        ln.setClass(applicationContext, MainActivity::class.java)
        startActivity(ln)
    }
    linear3.setOnClickListener {
        sp.edit().putString("language", "hi").apply()
        ln.setClass(applicationContext, MainActivity::class.java)
        startActivity(ln)
    }
    linear4.setOnClickListener {
        sp.edit().putString("language", "ta").apply()
        ln.setClass(applicationContext, MainActivity::class.java)
        startActivity(ln)
    }
    linear5.setOnClickListener {
        sp.edit().putString("language", "te").apply()
        ln.setClass(applicationContext, MainActivity::class.java)
        startActivity(ln)
    }
    linear6.setOnClickListener {
        sp.edit().putString("language", "gu").apply()
        ln.setClass(applicationContext, MainActivity::class.java)
        startActivity(ln)
    }

    bottomSheetDialog.setCancelable(true)
    bottomSheetDialog.show()
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
