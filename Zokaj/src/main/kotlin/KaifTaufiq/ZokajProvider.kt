package com.kaiftaufiq

import com.lagradost.cloudstream3.*
import com.lagradost.api.Log
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
    
      var home = document.select("div.col-sm-3").mapNotNull {
        it.toSearchResult()
      }

      return newHomePageResponse(arrayListOf(HomePageList(request.name, home)), hasNext = true)
    }

  private fun Element.toSearchResult(): SearchResponse {
    val title = this.select("h3 a").text().trim()
    val href = fixUrl(this.select("h3 a").attr("href"))
    val posterUrl = this.select("a img").attr("data-src").ifEmpty { this.select("a img").attr("src") }
    
      return newMovieSearchResponse(title, href, TvType.Movie) {
          this.posterUrl = posterUrl
      }
  }

  override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/?s=$query").document
        return document.select("div.col-sm-4").mapNotNull {
            it.toSearchResult()
        }
    }
  
  override suspend fun load(url: String): LoadResponse {
        Log.d("Zokaj load",url)
        val document = app.get(media.url).document
        Log.d("Zokaj",document.toString())
        return newMovieLoadResponse(title, url, TvType.Movie, Media(
                media.url,
                mediaType = 1
            ).toJson()) {
                this.posterUrl = "https://raw.githubusercontent.com/KaifTaufiq/CloudStream-Tokusatsu/refs/heads/master/TokuZilla/icon.png"
                this.plot = "Plot Test"
                this.year = "2000"
            }
}
