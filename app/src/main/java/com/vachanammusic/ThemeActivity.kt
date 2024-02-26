package com.vachanammusic

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.FirebaseApp
import com.vachanammusic.databinding.ThemeBinding

class ThemeActivity : AppCompatActivity() {

    private lateinit var binding: ThemeBinding
    private lateinit var sp: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ThemeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)
        initialize()
        initializeLogic()
    }

    private fun initialize() {
        sp = getSharedPreferences("sp", MODE_PRIVATE)

        binding.radiobutton5.setOnClickListener { binding.radiobutton5.isChecked = true }
        binding.radiobutton5.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sp.edit().putString("theme", "5").apply()
                setRadioButtonsUncheckedExcept(binding.radiobutton5)
                binding.main.setBackgroundResource(R.drawable.theme_5)
            }
        }

        binding.radiobutton2.setOnClickListener { binding.radiobutton2.isChecked = true }
        binding.radiobutton2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sp.edit().putString("theme", "2").apply()
                setRadioButtonsUncheckedExcept(binding.radiobutton2)
                binding.main.setBackgroundResource(R.drawable.theme_2)
            }
        }

        binding.radiobutton4.setOnClickListener { binding.radiobutton4.isChecked = true }
        binding.radiobutton4.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sp.edit().putString("theme", "10").apply()
                setRadioButtonsUncheckedExcept(binding.radiobutton4)
                binding.main.setBackgroundResource(R.drawable.theme_9_1)
            }
        }

        binding.radiobutton11.setOnClickListener { binding.radiobutton11.isChecked = true }
        binding.radiobutton11.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sp.edit().putString("theme", "11").apply()
                setRadioButtonsUncheckedExcept(binding.radiobutton11)
                binding.main.setBackgroundResource(R.drawable.theme_9_2)
            }
        }

        binding.radiobutton13.setOnClickListener { binding.radiobutton13.isChecked = true }
        binding.radiobutton13.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sp.edit().putString("theme", "13").apply()
                setRadioButtonsUncheckedExcept(binding.radiobutton13)
                binding.main.setBackgroundResource(R.drawable.theme_9_4)
            }
        }

        binding.radiobutton7.setOnClickListener { binding.radiobutton7.isChecked = true }
        binding.radiobutton7.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sp.edit().putString("theme", "6").apply()
                setRadioButtonsUncheckedExcept(binding.radiobutton7)
                binding.main.setBackgroundResource(R.drawable.theme_6)
            }
        }

        binding.radiobutton3.setOnClickListener { binding.radiobutton3.isChecked = true }
        binding.radiobutton3.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                sp.edit().putString("theme", "9").apply()
                setRadioButtonsUncheckedExcept(binding.radiobutton3)
                binding.main.setBackgroundResource(R.drawable.main_bg)
            }
        }


        binding.fab.setOnClickListener {
            val inflater = layoutInflater
            val inflate = inflater.inflate(R.layout.favorites_added, null)
            val t = Toast.makeText(applicationContext, "", Toast.LENGTH_LONG)
            t.view = inflate
            t.setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 0)
            t.show()
            val image1 = inflate.findViewById<View>(R.id.img1) as ImageView
            val lin1 = inflate.findViewById<View>(R.id.linear1) as LinearLayout
            val tin1 = inflate.findViewById<View>(R.id.tin1) as TextView
            image1.visibility = View.GONE
            lin1.background = GradientDrawable().apply {
                cornerRadius = 35f
                setColor(-0x1)
            }
            tin1.setTypeface(
                ResourcesCompat.getFont(this, R.font.spotify)
                , Typeface.NORMAL)
            tin1.text = "Theme Changed!"
            intent.setClass(applicationContext, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setRadioButtonsUncheckedExcept(checkedRadioButton: RadioButton) {
        listOf(
            binding.radiobutton2, binding.radiobutton3, binding.radiobutton4,
            binding.radiobutton11, binding.radiobutton13, binding.radiobutton5, binding.radiobutton7
        ).filter { it != checkedRadioButton }.forEach { it.isChecked = false }
    }

    private fun initializeLogic() {
        themeColour()
        design()

        when (sp.getString("theme", "") ?: "") {
            "2" -> binding.radiobutton2.isChecked = true
            "4" -> binding.radiobutton4.isChecked = true
            "5" -> binding.radiobutton5.isChecked = true
            "11" -> binding.radiobutton11.isChecked = true
            "13" -> binding.radiobutton13.isChecked = true
            "7" -> binding.radiobutton7.isChecked = true
            "3" -> binding.radiobutton3.isChecked = true
            else -> binding.radiobutton3.isChecked = true
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        intent.setClass(applicationContext, SettingsActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun themeColour() {
        when (sp.getString("theme", "")) {

            "2" -> binding.main.setBackgroundResource(R.drawable.theme_2)
            "4" -> binding.main.setBackgroundResource(R.drawable.theme_9_1)
            "5" -> binding.main.setBackgroundResource(R.drawable.theme_5)
            "11" -> binding.main.setBackgroundResource(R.drawable.theme_9_2)
            "13" -> binding.main.setBackgroundResource(R.drawable.theme_9_4)
            "7" -> binding.main.setBackgroundResource(R.drawable.theme_6)
            "3" -> binding.main.setBackgroundResource(R.drawable.main_bg)
            else -> binding.main.setBackgroundResource(R.drawable.main_bg)
        }
    }

    private fun design() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.parseColor("#000000")
    }
}
