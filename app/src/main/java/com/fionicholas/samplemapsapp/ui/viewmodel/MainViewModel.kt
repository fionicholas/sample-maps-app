package com.fionicholas.samplemapsapp.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.fionicholas.samplemapsapp.data.BookmarkRepository
import com.fionicholas.samplemapsapp.data.model.Bookmark
import com.fionicholas.samplemapsapp.data.model.BookmarkView
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "MapsViewModel"

    private var bookmarkRepository: BookmarkRepository = BookmarkRepository(
        getApplication())
    private var bookmarks: LiveData<List<BookmarkView>>? = null

    fun addBookmark(latLng: LatLng): Long? {
        val bookmark = bookmarkRepository.createBookmark()
        bookmark.name = "Untitled"
        bookmark.longitude = latLng.longitude
        bookmark.latitude = latLng.latitude
        bookmark.category = "Other"
        return bookmarkRepository.addBookmark(bookmark)
    }

    fun addBookmarkFromPlace(place: Place, image: Bitmap?) {

        val bookmark = bookmarkRepository.createBookmark()
        bookmark.placeId = place.id
        bookmark.name = place.name.toString()
        bookmark.longitude = place.latLng?.longitude ?: 0.0
        bookmark.latitude = place.latLng?.latitude ?: 0.0
        bookmark.phone = place.phoneNumber.toString()
        bookmark.address = place.address.toString()
        bookmark.category = getPlaceCategory(place)

        val newId = bookmarkRepository.addBookmark(bookmark)
        image?.let { bookmark.setImage(it, getApplication()) }
        Log.i(TAG, "New bookmark $newId added to the database.")
    }

    fun getBookmarkViews():
            LiveData<List<BookmarkView>>? {
        if (bookmarks == null) {
            mapBookmarksToBookmarkView()
        }
        return bookmarks
    }

    private fun mapBookmarksToBookmarkView() {
        bookmarks = Transformations.map(bookmarkRepository.allBookmarks) { repoBookmarks ->
            repoBookmarks.map { bookmark ->
                bookmarkToBookmarkView(bookmark)
            }
        }
    }

    private fun getPlaceCategory(place: Place): String {

        var category = "Other"
        val placeTypes = place.types

        placeTypes?.let { placeTypes ->
            if (placeTypes.size > 0) {
                val placeType = placeTypes[0]
                category = bookmarkRepository.placeTypeToCategory(placeType)
            }
        }

        return category
    }

    private fun bookmarkToBookmarkView(bookmark: Bookmark): BookmarkView {
        return BookmarkView(
            bookmark.id,
            LatLng(bookmark.latitude, bookmark.longitude),
            bookmark.name,
            bookmark.phone,
            bookmarkRepository.getCategoryResourceId(bookmark.category))
    }
}