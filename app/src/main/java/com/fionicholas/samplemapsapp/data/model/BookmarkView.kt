package com.fionicholas.samplemapsapp.data.model

import com.google.android.gms.maps.model.LatLng

data class BookmarkView(val id: Long? = null,
                        val location: LatLng = LatLng(0.0, 0.0),
                        val name: String = "",
                        val phone: String = "",
                        val categoryResourceId: Int? = null)