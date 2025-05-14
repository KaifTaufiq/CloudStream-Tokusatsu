package com.kaiftaufiq

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.network.CloudflareKiller
import com.lagradost.nicehttp.NiceResponse
import com.lagradost.api.Log
import org.jsoup.nodes.Element

class TokuZillaProvider : MainAPI() {
  override var mainUrl = "https://tokuzilla.net"
  override var name = "TokuZilla"
  override val supportedTypes = setOf(
    TvType.Cartoon,
    TvType.Anime,
    TvType.AnimeMovie,
    TvType.Movie,
  )
  override val hasMainPage = true
  override var lang = "en"
  override val hasDownloadSupport = false

  private suspend fun cfKiller(url: String): NiceResponse {
    var doc = app.get(url)
    if (doc.document.select("title").text() == "Just a moment...") {
        doc = app.get(url, interceptor = CloudflareKiller())
    }
    return doc
  }

  override val mainPage = mainPageOf(
    "/" to "Home",
    // "/categories/super-sentai" to "Super Sentai",
    // "/categories/kamen-rider" to "Kamen Rider",
    // "/anime" to "Tokusatsu Anime",
    // "/series" to "Anime",
    // "/movie" to "Movie",
  )

  override suspend fun getMainPage(
    page: Int,
    request: MainPageRequest
    ): HomePageResponse {
    
    val url = if(page == 1) "$mainUrl${request.data}/" else "$mainUrl${request.data}/page/$page/"
    var document = cfKiller(url).document

    var home = document.select("div.col-sm-4").mapNotNull {
      it.toSearchResult()
    }
    return newHomePageResponse(
      list = HomePageList(
        name = request.name,
        list = home,
        isHorizontalImages = true
      ),
      hasNext = true
    )
  }

  private fun Element.toSearchResult(): SearchResponse? {
    val title = this.select("h3 a").text().trim()
    val href = fixUrl(this.select("h3 a").attr("href"))
    val posterUrl = this.select("a img").attr("data-src").ifEmpty { this.select("a img").attr("src") }

    return newMovieSearchResponse(title, href, TvType.Movie) {
      this.posterUrl = posterUrl
    }
  }

  override suspend fun search(query: String): List<SearchResponse> {
    val document = cfKiller("$mainUrl?s=$query").document
    return document.select("div.col-sm-4").mapNotNull {
      it.toSearchResult()
    }
  }

  override suspend fun load(url: String): LoadResponse {
    val document = cfKiller(url).document
    var title = document.select("h1").text()
    var posterUrl = document.select("div.thumb img").attr("data-src").ifEmpty { document.select("div.thumb img").attr("src") }
    // var plot = document.selectFirst("div.post-entry p").text().ifEmpty { null }
    val yearText = document.select("div.top-detail div.right tr:has(th:contains(Year)) td span.meta").firstOrNull()?.text()?.trim()
    val year = yearText?.toIntOrNull()
    val div = document.select("div.top-detail").text()
    val tvtype = if (div.contains("episode", ignoreCase = true) == true) "series" else "movie"
    if (tvtype == "series") {
      val episodes = mutableListOf<Episode>()

      document.select("ul.pagination.post-tape li.page-item a.page-link").forEach { linkElement ->
        val href = linkElement.attr("href")
        val number = Regex("[?&]ep=(\\d+)").find(href)?.groupValues?.get(1)?.toIntOrNull()
        val name = "Episode ${number ?: "?"}"
        if (number != null) {
          episodes += newEpisode(href) {
              this.name = name
              this.episode = number
          }
        }
      }
      return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
          this.posterUrl = posterUrl
          this.plot = plot
          this.year = year
      }
    } else {
      return newMovieLoadResponse(title, url, TvType.Movie, url) {
        this.posterUrl = posterUrl
        this.plot = plot
        this.year = year
      }
    }
  }

  override suspend fun loadLinks(
    data: String,
    isCasting: Boolean,
    subtitleCallback: (SubtitleFile) -> Unit,
    callback: (ExtractorLink) -> Unit
  ) : Boolean {
    val urlBody = cfKiller(data).document
    val elements = urlBody.select("div.player")
    val vidSrc = elements.select("iframe").attr("src")
    if (!vidSrc.isNullOrEmpty()) {
      loadExtractor(vidSrc,subtitleCallback, callback)
      return true
    }
    return false

  }
}
