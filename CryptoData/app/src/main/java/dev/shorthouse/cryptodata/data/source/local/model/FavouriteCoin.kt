package dev.shorthouse.cryptodata.data.source.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FavouriteCoin(
    @PrimaryKey
    val id: String
)
