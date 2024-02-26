package com.vachanammusic

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
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
import org.jsoup.Jsoup
import java.io.IOException
import com.vachanammusic.databinding.ArtistFragmentBinding

class ArtistFragmentActivity : Fragment() {

    private var testString = ""
    private var type = ""
    private lateinit var act: ArtistFragmentActivity
    private lateinit var mact: MainActivity
    private val artistData = hashMapOf<String, Any>()
    private val testLMap = arrayListOf<HashMap<String, Any>>()
    private val artistas = arrayListOf<HashMap<String, Any>>()
    private lateinit var binding: ArtistFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ArtistFragmentBinding.inflate(inflater, container, false)
        initialize()
        FirebaseApp.initializeApp(requireContext())
        initializeLogic()
        return binding.root
    }

    private fun initialize() {
        binding.edittext1.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(charSeq: CharSequence, param2: Int, param3: Int, param4: Int) {
                val charSeq = charSeq.toString()
                if (charSeq.isNotEmpty()) {
                    Thread {
                        try {
                            val doc = Jsoup.connect("https://vachanammusic.com/api.php")
                                .data("api", "search")
                                .data("search", charSeq)
                                .data("type", type)
                                .get().text()
                            if (activity == null) return@Thread
                            requireActivity().runOnUiThread {
                                if (doc.length > 0 && doc.substring(0, 1) == "[") {
                                    binding.gridview1.adapter = Gridview1Adapter(Gson().fromJson(doc, object : TypeToken<ArrayList<HashMap<String, Any>>>() {}.type))
                                }
                            }
                        } catch (ex: IOException) {
                            // Handle exception
                        }
                    }.start()
                }
            }

            override fun beforeTextChanged(param1: CharSequence, param2: Int, param3: Int, param4: Int) {}

            override fun afterTextChanged(param1: Editable) {}
        })

        binding.imageview1.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(requireContext().applicationContext, android.R.anim.slide_in_left)
            animation.duration = 300
            binding.gridview1.startAnimation(animation)
        }
    }

    private fun initializeLogic() {
        start()
        binding.edittext1.typeface = ResourcesCompat.getFont(requireContext(), R.font.light)
        binding.textview4.typeface = ResourcesCompat.getFont(requireContext(), R.font.light)

        binding.linear1.background = GradientDrawable().apply {
            cornerRadius = 80F
            setStroke(1, 0xFF607D8B.toInt())
            setColor(Color.TRANSPARENT)
        }
    }

    private fun start() {
        mact = requireActivity() as MainActivity
        act = this
        testString = Gson().toJson(testLMap)
        testLMap.clear()
        testLMap.addAll(Gson().fromJson(testString, object : TypeToken<ArrayList<HashMap<String, Any>>>() {}.type))
        type = "0"
        Thread {
            try {
                val doc = Jsoup.connect("https://vachanammusic.com/api.php")
                    .data("api", "getArtists")
                    .data("data", "")
                    .get().text()
                requireActivity().runOnUiThread {
                    artistas.clear()
                    artistas.addAll(Gson().fromJson(doc, object : TypeToken<ArrayList<HashMap<String, Any>>>() {}.type))
                    binding.gridview1.adapter = Gridview1Adapter(Gson().fromJson(doc, object : TypeToken<ArrayList<HashMap<String, Any>>>() {}.type))
                }
            } catch (ex: IOException) {
                // Handle exception
            }
        }.start()
    }

    inner class Gridview1Adapter(private val data: ArrayList<HashMap<String, Any>>) : BaseAdapter() {
        override fun getCount(): Int = data.size

        override fun getItem(position: Int): Any = data[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, v: View?, container: ViewGroup?): View {
            var view = v
            val inflater = activity!!.layoutInflater
            if (view == null) {
                view = inflater.inflate(R.layout.search_tile, null)
            }

            val linear2 = view!!.findViewById<LinearLayout>(R.id.linear2)
            val circleimageview2 = view.findViewById<ImageView>(R.id.circleimageview2)
            val textview1 = view.findViewById<TextView>(R.id.textview1)

            Glide.with(requireContext().applicationContext)
                .load(Uri.parse(data[position]["artist_img_perfil"].toString().plus("?").plus(data[position]["edited"].toString())))
                .into(circleimageview2)
            textview1.text = data[position]["artist_alias"].toString()


            linear2.setOnClickListener {
                artistData.clear()
                artistData.putAll(data[position])
                mact.song(artistData)
            }



            textview1.typeface = ResourcesCompat.getFont(requireContext(), R.font.spotify)
            textview1.ellipsize = TextUtils.TruncateAt.MARQUEE
            textview1.isSingleLine = true
            textview1.isSelected = true
            binding.gridview1.isVerticalScrollBarEnabled = false

            return view
        }
    }
}