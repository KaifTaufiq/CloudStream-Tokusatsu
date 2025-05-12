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
}
