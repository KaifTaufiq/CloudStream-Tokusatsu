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

  override val mainPage = mainPageof(
    "/categories/super-sentai" = "Super Sentai",
    "/categories/kamen-rider" = "Kamen Rider",
    "/anime" = "Tokusatsu Anime",
    "/series" = "Anime",
    "/movie" = "Movie",
  )
  
}
