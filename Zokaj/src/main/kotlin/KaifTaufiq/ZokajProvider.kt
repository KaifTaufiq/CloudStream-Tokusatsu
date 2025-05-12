package com.kaiftaufiq

import com.lagradost.cloudstream3.*

class ZokajProvider : MainAPI() {
  override var mainUrl = "https://zokaj.com/"
  override var name = "Zokaj"
  override val supportedTypes = setOf(
    TvType.Cartoon,
    TvType.Anime,
    TvType.AnimeMovie,
    TvType.Movie,
  )
  override val hasMainPage = true
  override var lang = "en"

  override val mainPage = mainPageOf(
    "/super-sentai" to "Super Sentai",
    "/kamen-rider" to "Kamen Rider",
    "/metal-hero" to "Meta Hero",
  )
  
}
