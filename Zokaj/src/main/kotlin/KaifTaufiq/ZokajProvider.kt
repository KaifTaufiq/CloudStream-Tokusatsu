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
  override val hasDownloadSupport = false

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

      return newHomePageResponse(request.name, home)
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
        val document = app.get("$mainUrl/?s=$query").document
        return document.select("div.col-sm-4").mapNotNull {
            it.toSearchResult()
        }
    }
  
  override suspend fun load(url: String): LoadResponse {
        Log.d("Zokaj load",url)
        val document = app.get(url).document
        Log.d("Zokaj",document.toString())

        var title = document.select("h1").text()
        var posterUrl = document.select("#information img").attr("data-src").ifEmpty { document.select("#information img").attr("src") }
        var plot = document.select("div.blockquote").text()
        val yearContent = document.select("div.video-details").text()
        val year = Regex("""\b(18\d{2}|19\d{2}|20\d{2}|2[1-9]\d{2}|3000)\b""").find(yearContent)?.value?.toIntOrNull()
        val div = document.select("div.video-details").text()
        val tvtype = if (div.contains("Previous Series", ignoreCase = true) == true) "series" else "movie"
    
        if(tvtype == "series") {
          val episodes = mutableListOf<Episode>()
          document.select("#list-episode-table tbody tr").forEach { row ->
              val link = row.selectFirst("a.open-dl")?.attr("href") ?: return@forEach
              val fullTitle = row.selectFirst("a.open-dl")?.text() ?: return@forEach
              val nameCell = row.select("td").getOrNull(1)?.text()?.trim() ?: "Episode"
          
              // Extract episode number using regex (e.g., Episode 01)
              val number = Regex("Episode\\s*(\\d+)", RegexOption.IGNORE_CASE)
                  .find(fullTitle)
                  ?.groupValues?.get(1)
                  ?.toIntOrNull()
          
              val name = nameCell.replace("\"", "").substringBefore("(").trim()
          
              episodes += newEpisode(link) {
                  this.name = name
                  this.episode = number
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
}
