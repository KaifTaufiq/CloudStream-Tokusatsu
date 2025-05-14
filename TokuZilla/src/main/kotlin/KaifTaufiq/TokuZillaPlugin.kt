package com.kaiftaufiq

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import com.lagradost.cloudstream3.extractors.BoosterX
import android.content.Context

@CloudstreamPlugin
class TokuZillaPlugin: Plugin() {
  override fun load(context: Context) {
    // All providers should be added in this manner. Please don't edit the providers list directly.
    registerMainAPI(TokuZillaProvider())
    registerExtractorAPI(BoosterX())
  }
}
