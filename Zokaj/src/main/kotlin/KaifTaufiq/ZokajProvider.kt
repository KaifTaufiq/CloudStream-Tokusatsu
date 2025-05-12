package com.kaiftaufiq

import com.lagradost.cloudstream3.*
import org.jsoup.nodes.Element
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

  override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {
      val url = if(page == 1) "$mainUrl${request.data}/" else "$mainUrl${request.data}/page/$page/"
      var document = app.get(url).document
    
      var home = document.select("div.video-section").mapNotNull {
        it.toSearchResult()
      }

      return newHomePageResponse(arrayListOf(HomePageList(request.name, home)), hasNext = true)
    }

  private fun Element.toSearchResult(): SearchResponse {
    val title = this.select("h3.ftoc-heading-3 > a").text().trim()
    val href = fixUrl(this.select("h3.ftoc-heading-3 > a").attr("href")) // Corrected
    val posterUrl = this.select("a img").attr("data-src").ifEmpty { this.select("a img").attr("src") }
    
    return newMovieSearchResponse(title, href, TvType.Movie) {
        this.posterUrl = posterUrl
    }
}

  
}
