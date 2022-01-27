package com.project.songovermusicapp.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.project.songovermusicapp.data.constants.Constants.SONG_COLLECTION
import com.project.songovermusicapp.data.entities.Song
import kotlinx.coroutines.tasks.await

class MusicFirestoreDatabase {

    private val firestore = FirebaseFirestore.getInstance()
    private val songCollection = firestore.collection(SONG_COLLECTION)

    suspend fun getAllSongs(): List<Song>{
        return try {
            songCollection.get().await().toObjects(Song::class.java)
        }
        catch (e: Exception){
            emptyList()
        }
    }
}