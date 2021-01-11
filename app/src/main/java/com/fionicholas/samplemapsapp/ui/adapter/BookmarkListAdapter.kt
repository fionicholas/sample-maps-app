package com.fionicholas.samplemapsapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fionicholas.samplemapsapp.ui.MainActivity
import com.fionicholas.samplemapsapp.R
import com.fionicholas.samplemapsapp.data.model.BookmarkView
import kotlinx.android.synthetic.main.item_bookmark.view.*

class BookmarkListAdapter(
    private var bookmarkData: List<BookmarkView>?,
    private val mapsActivity: MainActivity) :
    RecyclerView.Adapter<BookmarkListAdapter.ViewHolder>() {

    class ViewHolder(v: View,
                     private val mapsActivity: MainActivity) :
        RecyclerView.ViewHolder(v) {
        val nameTextView: TextView = v.tvBookmarkName
        val categoryImageView: ImageView = v.imgBookmark

        init {
            v.setOnClickListener {
                val bookmarkView = itemView.tag as BookmarkView
                mapsActivity.moveToBookmark(bookmarkView)
            }
        }

    }

    fun setBookmarkData(bookmarks: List<BookmarkView>) {
        this.bookmarkData = bookmarks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_bookmark, parent, false), mapsActivity)
    }

    override fun onBindViewHolder(holder: ViewHolder,
                                  position: Int) {

        val bookmarkData = bookmarkData ?: return
        val bookmarkViewData = bookmarkData[position]

        holder.itemView.tag = bookmarkViewData
        holder.nameTextView.text = bookmarkViewData.name
        bookmarkViewData.categoryResourceId?.let {
            holder.categoryImageView.setImageResource(it)
        }
    }

    override fun getItemCount(): Int {
        return bookmarkData?.size ?: 0
    }
}