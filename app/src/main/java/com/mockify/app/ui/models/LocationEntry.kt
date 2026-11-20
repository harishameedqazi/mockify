package com.mockify.app.ui.models

import com.google.android.gms.maps.model.LatLng

data class LocationEntry(
    var latLng: LatLng,
    var addressLine: String? = null,
)
