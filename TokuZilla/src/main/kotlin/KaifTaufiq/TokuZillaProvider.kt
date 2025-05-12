package com.kaiftaufiq

import com.lagradost.cloudstream3.*

class TokuZillaProvider : MainAPI() {
  override var mainUrl = "https://tokuzilla.net/"
  override var name = "TokuZilla"
  override val supportedTypes = setOf(
    TvType.Cartoon,
    TvType.Anime,
    TvType.AnimeMovie,
    TvType.Movie,
  )
  override val hasMainPage = true
  override var lang = "en"

  override val mainPage = mainPageOf(
    "/categories/super-sentai" to "Super Sentai",
    "/categories/kamen-rider" to "Kamen Rider",
    "/anime" to "Tokusatsu Anime",
    "/series" to "Anime",
    "/movie" to "Movie",
  )
  
}
