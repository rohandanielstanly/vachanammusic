package com.vachanammusic

import android.content.Context
import com.google.android.exoplayer2.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

data class SongDetails(
    val song_title: String = "",
    val song_description: String = "",
    val song_url: String = "",
    val song_cover: String = "",
    var song_view: String = "",
    val artist_alias: String = "",
    val song_lyrics: String = "",
    val song_lyrics_english: String = "",
    val video_url: String = "",
)



object PlaylistHelper {

    private val playlistRef: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("playlists")

    fun getUserId(): String {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        return currentUser?.uid ?: ""
    }

    fun add(context: Context, playlistName: String, songDetails: SongDetails) {
        val userId = getUserId()
        val playlistReference = playlistRef.child(userId).child(playlistName)

        // Use push to get a new unique key for each song
        val newSongRef = playlistReference.push()

        // Set the value for the new unique key
        newSongRef.setValue(songDetails).addOnCompleteListener { task ->
            if (task.isSuccessful) {
            } else {
            }
        }
    }


    fun retrieve(
        context: Context,
        playlistName: String,
        callback: (List<Pair<String, SongDetails>>) -> Unit
    ) {
        val userId = getUserId()
        playlistRef.child(userId).child(playlistName).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val songsList = ArrayList<Pair<String, SongDetails>>()
                for (childSnapshot in snapshot.children) {
                    val songPath = childSnapshot.key
                    val songDetails = childSnapshot.getValue(SongDetails::class.java)
                    if (songPath != null && songDetails != null) {
                        songsList.add(Pair(songPath, songDetails))
                    }
                }
                callback(songsList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    // ...
// ...
    fun remove(context: Context, playlistName: String, musicPath: String, param: (Boolean) -> Unit) {
        val userId = getUserId()
        val playlistReference = playlistRef.child(userId).child(playlistName)

        playlistReference.orderByChild("song_url").equalTo(musicPath).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    childSnapshot.ref.removeValue()
                        .addOnSuccessListener {
                            // Handle success
                            Log.d("PlaylistHelper", "Song removed from playlist successfully")
                            param(true)
                        }
                        .addOnFailureListener { e ->
                            // Handle failure
                            Log.e("PlaylistHelper", "Error removing song from playlist", e)
                            param(false)
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.e("PlaylistHelper", "Error retrieving song from playlist", error.toException())
                param(false)
            }
        })
    }




    fun renamePlaylist(context: Context, from: String, to: String, callback: (Boolean) -> Unit) {
        val userId = getUserId()
        val fromReference = playlistRef.child(userId).child(from)
        val toReference = playlistRef.child(userId).child(to)

        // Move all songs from the old playlist to the new one
        fromReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val songPath = childSnapshot.key
                    val songDetails = childSnapshot.getValue(SongDetails::class.java)
                    if (songPath != null && songDetails != null) {
                        toReference.child(songPath).setValue(songDetails)
                    }
                }
                // Remove the old playlist
                fromReference.removeValue()
                    .addOnSuccessListener {
                        // Handle success
                        Log.d("PlaylistHelper", "Playlist renamed successfully")
                        callback(true)
                    }
                    .addOnFailureListener { e ->
                        // Handle failure
                        Log.e("PlaylistHelper", "Error renaming playlist", e)
                        callback(false)
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.e("PlaylistHelper", "Error renaming playlist", error.toException())
                callback(false)
            }
        })
    }


    fun removePlaylist(context: Context, playlistName: String) {
        val userId = getUserId()
        val playlistReference = playlistRef.child(userId).child(playlistName)

        playlistReference.removeValue()
            .addOnSuccessListener {
                // Handle success
                Log.d("PlaylistHelper", "Playlist removed successfully")
            }
            .addOnFailureListener { e ->
                // Handle failure
                Log.e("PlaylistHelper", "Error removing playlist", e)
            }
    }







    fun getAllPlaylists(context: Context, userId: String, callback: (ArrayList<PlayList?>) -> Unit) {
        val playlistRef = FirebaseDatabase.getInstance().getReference("playlists")
            .child(userId)

        playlistRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val playLists = ArrayList<PlayList?>()
                for (childSnapshot in snapshot.children) {
                    val playlistName = childSnapshot.key
                    val amnt = childSnapshot.childrenCount
                    playLists.add(PlayList(playlistName, amnt.toString()))
                }
                playLists.reverse()
                callback(playLists)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    class PlayList(var name: String?, var amnt: String?) {

        constructor() : this(null, null)

        fun fetchName(): String = name ?: "Unknown"

        var amount: String?
            get() = amnt ?: "0"
            set(value) {
                amnt = value
            }

        fun updateName(_name: String?) {
            name = _name
        }

        fun retrieveName(): String = name ?: "Unknown"
    }
}