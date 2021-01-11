package com.fionicholas.samplemapsapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fionicholas.samplemapsapp.data.model.Bookmark

@Database(entities = arrayOf(Bookmark::class), version = 1)
abstract class BookmarkMapsDatabase : RoomDatabase() {

    abstract fun bookmarkDao(): BookmarkDao

    companion object {

        private var instance: BookmarkMapsDatabase? = null

        fun getInstance(context: Context): BookmarkMapsDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    BookmarkMapsDatabase::class.java, "BookmarkMaps"
                )
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return instance as BookmarkMapsDatabase
        }
    }
}