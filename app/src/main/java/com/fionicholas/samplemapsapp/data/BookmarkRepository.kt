package com.fionicholas.samplemapsapp.data

import android.content.Context
import androidx.lifecycle.LiveData
import com.fionicholas.samplemapsapp.R
import com.fionicholas.samplemapsapp.data.database.BookmarkDao
import com.fionicholas.samplemapsapp.data.database.BookmarkMapsDatabase
import com.fionicholas.samplemapsapp.data.model.Bookmark
import com.google.android.libraries.places.api.model.Place

class BookmarkRepository(private val context: Context) {

    private var db: BookmarkMapsDatabase = BookmarkMapsDatabase.getInstance(context)
    private var bookmarkDao: BookmarkDao = db.bookmarkDao()
    private var categoryMap: HashMap<Place.Type, String> = buildCategoryMap()
    private var allCategories: HashMap<String, Int> = buildCategories()

    val categories: List<String>
        get() = ArrayList(allCategories.keys)


    fun updateBookmark(bookmark: Bookmark) {
        bookmarkDao.updateBookmark(bookmark)
    }

    fun getBookmark(bookmarkId: Long): Bookmark {
        return bookmarkDao.loadBookmark(bookmarkId)
    }

    fun addBookmark(bookmark: Bookmark): Long {
        val newId = bookmarkDao.insertBookmark(bookmark)
        bookmark.id = newId
        return newId
    }

    fun createBookmark(): Bookmark {
        return Bookmark()
    }

    fun deleteBookmark(bookmark: Bookmark) {
        bookmark.deleteImage(context)
        bookmarkDao.deleteBookmark(bookmark)
    }


    fun getLiveBookmark(bookmarkId: Long): LiveData<Bookmark> {
        return bookmarkDao.loadLiveBookmark(bookmarkId)
    }

    fun placeTypeToCategory(placeType: Place.Type): String {
        var category = "Other"
        if (categoryMap.containsKey(placeType)) {
            category = categoryMap[placeType].toString()
        }
        return category
    }

    val allBookmarks: LiveData<List<Bookmark>>
        get() {
            return bookmarkDao.loadAll()
        }

    fun getCategoryResourceId(placeCategory: String): Int? {
        return allCategories[placeCategory]
    }

    private fun buildCategories() : HashMap<String, Int> {
        return hashMapOf(
            "Gas" to R.drawable.ic_gas,
            "Lodging" to R.drawable.ic_lodging,
            "Other" to R.drawable.ic_custom_marker,
            "Restaurant" to R.drawable.ic_restaurant,
            "Shopping" to R.drawable.ic_shopping
        )
    }

    private fun buildCategoryMap() : HashMap<Place.Type, String> {
        return hashMapOf(
            Place.Type.BAKERY to "Restaurant",
            Place.Type.BAR to "Restaurant",
            Place.Type.CAFE to "Restaurant",
            Place.Type.FOOD to "Restaurant",
            Place.Type.RESTAURANT to "Restaurant",
            Place.Type.MEAL_DELIVERY to "Restaurant",
            Place.Type.MEAL_TAKEAWAY to "Restaurant",
            Place.Type.GAS_STATION to "Gas",
            Place.Type.CLOTHING_STORE to "Shopping",
            Place.Type.DEPARTMENT_STORE to "Shopping",
            Place.Type.FURNITURE_STORE to "Shopping",
            Place.Type.GROCERY_OR_SUPERMARKET to "Shopping",
            Place.Type.HARDWARE_STORE to "Shopping",
            Place.Type.HOME_GOODS_STORE to "Shopping",
            Place.Type.JEWELRY_STORE to "Shopping",
            Place.Type.SHOE_STORE to "Shopping",
            Place.Type.SHOPPING_MALL to "Shopping",
            Place.Type.STORE to "Shopping",
            Place.Type.LODGING to "Lodging",
            Place.Type.ROOM to "Lodging"
        )
    }
}