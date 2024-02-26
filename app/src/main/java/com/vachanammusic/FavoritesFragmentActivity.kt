package com.vachanammusic

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import com.vachanammusic.databinding.FavoritesFragmentBinding
import de.hdodenhof.circleimageview.CircleImageView

class FavoritesFragmentActivity : Fragment() {

    private lateinit var binding: FavoritesFragmentBinding
    private val REQ_CD_UP = 101
    private var conf: SharedPreferences? = null
    private var img: SharedPreferences? = null
    private var testString = ""
    private var testLMap = ArrayList<HashMap<String?, Any?>?>()

    private var mact: MainActivity? = null

    private var d: AlertDialog.Builder? = null

    private lateinit var favoritesRef: DatabaseReference
    private var favoritesList: ArrayList<HashMap<String?, Any?>?> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FavoritesFragmentBinding.inflate(inflater, container, false)
        val view = binding.root
        initialize()
        FirebaseApp.initializeApp(requireContext())
        initializeLogic()
        loadFavoritesFromFirebase()
        return view
    }

    private fun initialize() {
        conf = requireContext().getSharedPreferences("conf", Activity.MODE_PRIVATE)
        img = requireContext().getSharedPreferences("img", Activity.MODE_PRIVATE)
        val userUid = getUserId()
        d = AlertDialog.Builder(requireActivity())

        favoritesRef = FirebaseDatabase.getInstance().reference
            .child("favorites").child(userUid)
    }

    private fun initializeLogic() {
        start()

        binding.circleimageview1.setColorFilter(-0x1, PorterDuff.Mode.MULTIPLY)

        binding.vscroll6.isVerticalScrollBarEnabled = false
        binding.vscroll8.isVerticalScrollBarEnabled = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_CD_UP -> if (resultCode == Activity.RESULT_OK) {
                val filePath = ArrayList<String>()
                if (data != null) {
                    if (data.clipData != null) {
                        var index = 0
                        while (index < data.clipData!!.itemCount) {
                            val item = data.clipData!!.getItemAt(index)
                            FileUtil.convertUriToFilePath(
                                requireContext().applicationContext,
                                item.uri
                            )?.let {
                                filePath.add(it)
                            }
                            index++
                        }
                    } else {
                        data.data?.let {
                            FileUtil.convertUriToFilePath(
                                requireContext().applicationContext,
                                it
                            )?.let {
                                filePath.add(it)
                            }
                        }
                    }
                }
                val path = filePath[0]
                binding.circleimageview1.setImageBitmap(
                    FileUtil.decodeSampleBitmapFromPath(
                        path,
                        1024,
                        1024
                    )
                )
                img!!.edit().putString("imgF", path).apply()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (img!!.getString("imgF", "") == "") {
        } else {
            binding.circleimageview1.setImageBitmap(
                FileUtil.decodeSampleBitmapFromPath(
                    img!!.getString(
                        "imgF",
                        ""
                    ), 1024, 1024
                )
            )
        }
    }

    private fun loadFavoritesFromFirebase() {
        favoritesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempList = mutableListOf<HashMap<String?, Any?>?>()

                for (userSnapshot in snapshot.children) {
                    val songMap = userSnapshot.getValue(object : GenericTypeIndicator<HashMap<String?, Any?>?>() {})
                    songMap?.let {
                        tempList.add(it)
                    }
                }

                favoritesList.clear()
                favoritesList.addAll(tempList)

                updateFavoritesRecyclerView()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FavoritesFragment", "Error fetching data from Firebase: $error")
            }
        })
    }

    private fun updateFavoritesRecyclerView() {
        if (favoritesList.isEmpty()) {
            binding.recyclerview1.visibility = View.GONE
        } else {
            binding.recyclerview1.visibility = View.VISIBLE
            binding.recyclerview1.adapter =
                Recyclerview1Adapter(favoritesList)
            binding.recyclerview1.layoutManager = LinearLayoutManager(context)
        }
    }

    private fun start() {
        testString = Gson().toJson(testLMap)
        testLMap = Gson().fromJson(
            testString,
            object : TypeToken<ArrayList<HashMap<String?, Any>?>?>() {}.type
        )
        mact = this.activity as? MainActivity
    }

    inner class Recyclerview1Adapter(var data: ArrayList<HashMap<String?, Any?>?>) :
        RecyclerView.Adapter<Recyclerview1Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//            val inflater = requireActivity().layoutInflater
            val inflater = LayoutInflater.from(parent.context)

            val v = inflater.inflate(R.layout.song_tile, null)
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

            textview1.text = data[position]!!.get("song_title").toString()
            textview2.text = data[position]!!["song_description"].toString()
            Glide.with(context!!.applicationContext)
                .load(Uri.parse(data[position]!!["song_cover"].toString())).into(imageview1)

            textview3.text =
                ((data[position]!!["song_view"].toString().toDouble()).toLong()).toString()

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
                ResourcesCompat.getFont(requireContext(), R.font.light), Typeface.NORMAL
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

    private fun getUserId(): String {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        return currentUser?.uid ?: ""
    }
}
