package com.vachanammusic

import android.animation.ObjectAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.vachanammusic.databinding.SplashBinding
import java.util.Timer
import java.util.TimerTask

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: SplashBinding
    private val one = ObjectAnimator()
    private val two = ObjectAnimator()
    private val three = ObjectAnimator()
    private val four = ObjectAnimator()

    private val intent = Intent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialize()
        FirebaseApp.initializeApp(this)
        initializeLogic()
    }

    private fun initialize() {
        binding.linear1
        binding.linear2
        binding.imageview1
    }

    interface RetrievalListener {
        fun onPostRetrieved(path: String?)
        fun onPostRetrievalFailed(error: String?)
    }

    private fun initializeLogic() {
        setBackground(binding.linear2, 600.0, 0.0, "#ffffff", false)
        one.target = binding.linear2
        one.setPropertyName("alpha")
        one.setFloatValues(0f, 1f)
        one.duration = 900
        one.start()

        val timer = object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    val deepLink = getIntent().data
                    if (deepLink != null) {
                        DynamicHelper.retrieveLink(
                            getIntent(),
                            this@SplashActivity,
                            object : DynamicHelper.RetrievalListener {
                                override fun onPostRetrieved(path: String?) {
                                    val path2 = path!!.substring(1, path.length - 1)
                                    val tokens =
                                        path2.split(", ".toRegex()).dropLastWhile { it.isEmpty() }
                                            .toTypedArray()
                                    val linkType = tokens[0]

                                    if (linkType.equals("MUSIC", ignoreCase = true)) {
                                        val id = tokens[1]
                                        TempBase.addData("link_id", id)
                                        nextActivity()
                                    }

                                    if (linkType.equals("ARTIST", ignoreCase = true)) {
                                        val id = tokens[1]
                                        TempBase.addData("artist_id", id)
                                        nextActivity()
                                    }
                                }

                                override fun onPostRetrievalFailed(error: String?) {
                                    Util.showMessage(this@SplashActivity, error)
                                }
                            })
                        return@runOnUiThread
                    }
                    nextActivity()
                }
            }
        }
        Timer().schedule(timer, 1000)
    }

    public override fun onStart() {
        super.onStart()
    }

    private fun imageScaleXY() {
        three.target = binding.imageview1
        three.setPropertyName("scaleY")
        three.setFloatValues(1.toFloat(), 0.toFloat())
        three.setDuration(300.toLong())
        three.start()
        four.target = binding.imageview1
        four.setPropertyName("scaleX")
        four.setFloatValues(1.toFloat(), 0.toFloat())
        four.setDuration(300.toLong())
        four.start()
    }

    private fun linearScaleY() {
        two.target = binding.linear2
        two.setPropertyName("scaleY")
        two.setFloatValues(1.toFloat(), 30.toFloat())
        two.setDuration(1200.toLong())
        two.start()
    }

    private fun setBackground(
        view: View?,
        radius: Double,
        shadow: Double,
        color: String?,
        ripple: Boolean
    ) {
        if (ripple) {
            val gd = GradientDrawable()
            gd.setColor(Color.parseColor(color))
            gd.cornerRadius = radius.toInt().toFloat()
            view!!.elevation = shadow.toInt().toFloat()
            val clrb =
                ColorStateList(arrayOf(intArrayOf()), intArrayOf(Color.parseColor("#9e9e9e")))
            val ripdrb = RippleDrawable(clrb, gd, null)
            view.isClickable = true
            view.background = ripdrb
        } else {
            val gd = GradientDrawable()
            gd.setColor(Color.parseColor(color))
            gd.cornerRadius = radius.toInt().toFloat()
            view!!.background = gd
            view.elevation = shadow.toInt().toFloat()
        }
    }

    fun nextActivity() {
        one.target = binding.linear2
        one.setPropertyName("scaleX")
        one.setFloatValues(1.toFloat(), 30.toFloat())
        one.setDuration(1200.toLong())
        one.start()
        imageScaleXY()
        linearScaleY()
        intent.setClass(applicationContext, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}