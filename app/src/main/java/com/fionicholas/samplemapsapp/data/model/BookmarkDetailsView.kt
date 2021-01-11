package com.fionicholas.samplemapsapp.data.model

import android.content.Context
import android.graphics.Bitmap
import com.fionicholas.samplemapsapp.util.ImageUtils

data class BookmarkDetailsView(var id: Long? = null,
                               var name: String = "",
                               var phone: String = "",
                               var address: String = "",
                               var notes: String = "",
                               var category: String = "",
                               var longitude: Double = 0.0,
                               var latitude: Double = 0.0,
                               var placeId: String? = null) {
    fun getImage(context: Context): Bitmap? {
        id?.let {
            return ImageUtils.loadBitmapFromFile(context,
                Bookmark.generateImageFilename(it))
        }
        return null
    }

    fun setImage(context: Context, image: Bitmap) {
        id?.let {
            ImageUtils.saveBitmapToFile(context, image,
                Bookmark.generateImageFilename(it))
        }
    }
}