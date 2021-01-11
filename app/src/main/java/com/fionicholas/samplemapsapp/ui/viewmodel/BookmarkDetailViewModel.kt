package com.fionicholas.samplemapsapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.fionicholas.samplemapsapp.data.BookmarkRepository
import com.fionicholas.samplemapsapp.data.model.Bookmark
import com.fionicholas.samplemapsapp.data.model.BookmarkDetailsView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BookmarkDetailViewModel(application: Application) :
    AndroidViewModel(application) {

    private var bookmarkRepository: BookmarkRepository =
        BookmarkRepository(getApplication())
    private var bookmarkDetailsView: LiveData<BookmarkDetailsView>? = null

    fun getCategories(): List<String> {
        return bookmarkRepository.categories
    }

    fun getBookmark(bookmarkId: Long): LiveData<BookmarkDetailsView>? {
        if (bookmarkDetailsView == null) {
            mapBookmarkToBookmarkView(bookmarkId)
        }
        return bookmarkDetailsView
    }

    fun updateBookmark(bookmarkDetailsView: BookmarkDetailsView) {

        GlobalScope.launch {
            val bookmark = bookmarkViewToBookmark(bookmarkDetailsView)
            bookmark?.let { bookmarkRepository.updateBookmark(it) }
        }
    }

    fun deleteBookmark(bookmarkDetailsView: BookmarkDetailsView) {
        GlobalScope.launch {
            val bookmark = bookmarkDetailsView.id?.let {
                bookmarkRepository.getBookmark(it)
            }
            bookmark?.let {
                bookmarkRepository.deleteBookmark(it)
            }
        }
    }

    fun getCategoryResourceId(category: String): Int? {
        return bookmarkRepository.getCategoryResourceId(category)
    }

    private fun bookmarkViewToBookmark(bookmarkDetailsView: BookmarkDetailsView):
            Bookmark? {
        val bookmark = bookmarkDetailsView.id?.let {
            bookmarkRepository.getBookmark(it)
        }
        if (bookmark != null) {
            bookmark.id = bookmarkDetailsView.id
            bookmark.name = bookmarkDetailsView.name
            bookmark.phone = bookmarkDetailsView.phone
            bookmark.address = bookmarkDetailsView.address
            bookmark.notes = bookmarkDetailsView.notes
            bookmark.category = bookmarkDetailsView.category
        }
        return bookmark
    }

    private fun mapBookmarkToBookmarkView(bookmarkId: Long) {
        val bookmark = bookmarkRepository.getLiveBookmark(bookmarkId)
        bookmarkDetailsView = Transformations.map(bookmark) { repoBookmark ->
            repoBookmark?.let {
                bookmarkToBookmarkView(repoBookmark)
            }
        }
    }

    private fun bookmarkToBookmarkView(bookmark: Bookmark): BookmarkDetailsView {
        return BookmarkDetailsView(
            bookmark.id,
            bookmark.name,
            bookmark.phone,
            bookmark.address,
            bookmark.notes,
            bookmark.category,
            bookmark.longitude,
            bookmark.latitude,
            bookmark.placeId
        )
    }
}