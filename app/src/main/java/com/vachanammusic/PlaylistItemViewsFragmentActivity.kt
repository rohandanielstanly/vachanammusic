package com.vachanammusic
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vachanammusic.databinding.PlaylistItemViewsFragmentBinding
import com.vachanammusic.databinding.SongTileBinding
import java.util.concurrent.Executors

class PlaylistItemViewsFragmentActivity : Fragment() {

    private lateinit var binding: PlaylistItemViewsFragmentBinding
    private var pName: String? = ""
    private val playlistHelper = PlaylistHelper
    private var mact: MainActivity? = null
    private var d: AlertDialog.Builder? = null
    private var sp: SharedPreferences? = null
    private var testString = ""
    private var testLMap = ArrayList<HashMap<String?, Any?>?>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PlaylistItemViewsFragmentBinding.inflate(inflater, container, false)
        initialize()
        initializeLogic()
        start()
        return binding.root
    }

    private fun initialize() {
        d = AlertDialog.Builder(requireActivity())
        sp = requireContext().getSharedPreferences("sp", Activity.MODE_PRIVATE)
    }

    private fun initializeLogic() {
        mact = activity as? MainActivity
        mact?.let { initViews() }

        pName = requireArguments().getString("name")
        binding.recyclerview1.layoutManager = LinearLayoutManager(requireContext())
        binding.textview1.text = pName

        PlaylistHelper.retrieve(requireContext(), pName!!) { songsList ->
            updateSongs(songsList)
        }
        binding.textview1.setOnClickListener {
            showRenameDialog(requireContext())
        }

        binding.delete.setOnLongClickListener {
            val playlistName = pName ?: ""

            context?.let { it1 ->
                showConfirmationDialog(it1) {
                    // Delete the entire playlist
                    PlaylistHelper.removePlaylist(it1, playlistName)
                    val mainActivityIntent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(mainActivityIntent)
                    Toast.makeText(it1, "Playlist removed successfully", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }
    }

    private fun initViews() {
        binding.textview1.setTypeface(
                    ResourcesCompat.getFont(requireContext(), R.font.spotify),
            Typeface.BOLD
        )
        binding.textview2.setTypeface(
            ResourcesCompat.getFont(requireContext(), R.font.spotify),
            Typeface.BOLD
        )
        binding.circleimageview2.setColorFilter(-0x1, PorterDuff.Mode.MULTIPLY)
        binding.textview3.setTypeface(
            ResourcesCompat.getFont(requireContext(), R.font.light),
            Typeface.BOLD
        )
    }

    private fun start() {
        testString = Gson().toJson(testLMap)
        testLMap = Gson().fromJson(
            testString,
            object : TypeToken<ArrayList<HashMap<String?, Any>?>?>() {}.type
        )
        mact = this.activity as? MainActivity
    }

    private fun updateSongs(playlistEntries: List<Pair<String, SongDetails>>) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            val songDetails = playlistEntries.map { it.second }

            requireActivity().runOnUiThread {
                binding.recyclerview1.adapter = Recyclerview1Adapter(songDetails)
            }
        }
    }

    inner class Recyclerview1Adapter(private val data: List<SongDetails>) :
        RecyclerView.Adapter<Recyclerview1Adapter.ViewHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = SongTileBinding.inflate(inflater, parent, false)
            val lp = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            binding.root.layoutParams = lp
            return ViewHolder(binding)
        }


        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = data[position]


            holder.bind(item)
        }

        override fun getItemCount(): Int = data.size

        inner class ViewHolder(private val binding: SongTileBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(item: SongDetails) {
                binding.textview1.text = item.song_title
                binding.textview2.text = item.song_description
                binding.textview3.text = item.song_view

                Glide.with(binding.root.context.applicationContext)
                    .load(item.song_cover)
                    .into(binding.imageview1)

                if (adapterPosition == data.size - 1) {
                    binding.linear3.visibility = View.VISIBLE
                } else {
                    binding.linear3.visibility = View.GONE
                }

                binding.linear1.setOnClickListener {
                    item.song_view = (item.song_view.toInt() + 1).toString()

                    val songDetailsList = data
                    val hashMapList = convertToHashMapList(songDetailsList)

                    mact?.prepare(hashMapList, adapterPosition.toDouble())
                }



                binding.linear1.setOnLongClickListener {
                    val playlistName = pName ?: ""
                    val songUrl = item.song_url

                    context?.let { it1 ->
                        showConfirmationDialog(it1) {
                            PlaylistHelper.remove(context!!, playlistName, songUrl) { success ->
                                if (success as? Boolean == true) {
                                    Toast.makeText(
                                        context,
                                        "Song removed from playlist",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    (data as MutableList<SongDetails>).remove(item)
                                    notifyDataSetChanged()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Failed to remove song from playlist",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                    true // Consume long click event
                }

                fun shareMusicLink() {

                    val songUrl = item.song_url
                    val songCover = item.song_cover
                    val songTitle = item.song_title
                    val songDes = item.song_description


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
                binding.imageview2.setOnClickListener {
                    d!!.setTitle(item.song_title)
                    d!!.setMessage(item.song_description)
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



                binding.textview1.setTypeface(
                    ResourcesCompat.getFont(requireContext(), R.font.light),
                            Typeface.NORMAL
                )

                binding.textview1.ellipsize = TextUtils.TruncateAt.MARQUEE
                binding.textview1.marqueeRepeatLimit = -1
                binding.textview1.isSingleLine = true
                binding.textview1.isSelected = true
                binding.textview2.ellipsize = TextUtils.TruncateAt.MARQUEE
                binding.textview2.marqueeRepeatLimit = -1
                binding.textview2.isSingleLine = true
                binding.textview2.isSelected = true
            }
        }
    }

    private fun convertToHashMapList(songDetailsList: List<SongDetails>): ArrayList<HashMap<String?, Any?>?> {
        val arrayList = ArrayList<HashMap<String?, Any?>?>()
        for (songDetails in songDetailsList) {
            val hashMap = HashMap<String?, Any?>()
            hashMap["song_title"] = songDetails.song_title
            hashMap["song_description"] = songDetails.song_description
            hashMap["song_view"] = songDetails.song_view
            hashMap["song_cover"] = songDetails.song_cover
            hashMap["song_url"] = songDetails.song_url

            hashMap["artist_alias"] = songDetails.artist_alias
            hashMap["song_lyrics"] = songDetails.song_lyrics
            hashMap["song_lyrics_english"] = songDetails.song_lyrics_english
            hashMap["video_url"] = songDetails.video_url

            arrayList.add(hashMap)
        }
        return arrayList
    }

    private fun showConfirmationDialog(context: Context, onDeleteConfirmed: () -> Unit) {

        val addDialog = AlertDialog.Builder(context).create()
        val addDialogLI = LayoutInflater.from(context)
        val addDialogCV = addDialogLI.inflate(R.layout.confirmation_dialog, null) as View
        addDialog.setView(addDialogCV)

        val linear1 = addDialogCV.findViewById<LinearLayout>(R.id.linear1)
        val yesBtn = addDialogCV.findViewById<LinearLayout>(R.id.yesBtn)
        val noBtn = addDialogCV.findViewById<LinearLayout>(R.id.noBtn)


         radius("#FFFFFF", "#FFFFFF", 35.0, 35.0, 35.0, 35.0, 35.0, linear1)
         radius("#FF1744", "#FFFFFF", 0.0, 0.0, 0.0, 0.0, 35.0, yesBtn)
         radius("#FF1744", "#FFFFFF", 0.0, 0.0, 0.0, 35.0, 0.0, noBtn)

        addDialog.setCancelable(true)
        addDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        addDialog.show()

        yesBtn.setOnClickListener {
            onDeleteConfirmed()
            addDialog.dismiss()
        }

        noBtn.setOnClickListener {
            addDialog.dismiss()
        }
    }


    private fun showRenameDialog(context: Context) {
        val addDialog = AlertDialog.Builder(context).create()
        val addDialogLI = LayoutInflater.from(context)
        val addDialogCV = addDialogLI.inflate(R.layout.rename_playlist, null) as View
        addDialog.setView(addDialogCV)

        val linear1 = addDialogCV.findViewById<LinearLayout>(R.id.linear1)
        val removeBtn = addDialogCV.findViewById<LinearLayout>(R.id.removeBtn)
        val changeBtn = addDialogCV.findViewById<LinearLayout>(R.id.changeBtn)
        val editText = addDialogCV.findViewById<EditText>(R.id.editText)
        editText.setText(pName)
        editText.setSelection(pName?.length ?: 0)

        radius("#FFFFFF", "#FFFFFF", 35.0, 35.0, 35.0, 35.0, 35.0, linear1)
        radius("#FF1744", "#FFFFFF", 0.0, 0.0, 0.0, 0.0, 35.0, removeBtn)
        radius("#FF1744", "#FFFFFF", 0.0, 0.0, 0.0, 35.0, 0.0, changeBtn)

        addDialog.setCancelable(true)
        addDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        addDialog.show()

        removeBtn.setOnClickListener {
            addDialog.dismiss()
        }

        changeBtn.setOnClickListener {
            val newName = editText.text.toString().trim()
            if (newName.isNotEmpty()) {
                renamePlaylist(newName)
                addDialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Playlist name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renamePlaylist(newName: String) {
        val oldName = pName ?: ""
        PlaylistHelper.renamePlaylist(requireContext(), oldName, newName) { success ->
            if (success) {
                pName = newName
                binding.textview1.text = newName
                Toast.makeText(
                    requireContext(),
                    "Playlist renamed successfully",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(requireContext(), "Failed to rename playlist", Toast.LENGTH_SHORT)
                    .show()
            }
        }
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
