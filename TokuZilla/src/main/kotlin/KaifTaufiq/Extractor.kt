package com.kaiftaufiq

import android.annotation.SuppressLint
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.lagradost.cloudstream3.USER_AGENT
import com.google.gson.JsonParser
import com.lagradost.api.Log
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.amap
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.base64DecodeArray
import com.lagradost.cloudstream3.utils.AppUtils.toJson
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.INFER_TYPE
import com.lagradost.cloudstream3.utils.JsUnpacker
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.loadExtractor
import com.lagradost.cloudstream3.utils.newExtractorLink
import com.lagradost.nicehttp.RequestBodyTypes
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.math.BigInteger
import java.net.URI
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

// @PlayerX, Nice choice with Diffie-Hellman! 🔐
// At this point, you're not even a security challenge... you're just my warm-up exercise.
// Keep trying, maybe one day you’ll at least trigger my firewall.
// 23rd attempt at cracking you—haha! 💥😂
// Contact: businesshackerindia@gmail.com 📧

class BoosterX : Chillx() {
  override val name = "BoosterX"
  override val mainUrl = "https://boosterx.stream"
  override val requiresReferer = true
}
open class Chillx : ExtractorApi() {
  override val name = "Chillx"
  override val mainUrl = "https://chillx.top"
  override val requiresReferer = true

  override suspend fun getUrl(
    url: String,
    referer: String?,
    subtitleCallback: (SubtitleFile) -> Unit,
    callback: (ExtractorLink) -> Unit
  ) {
    val baseurl = getBaseUrl(url)
    val headers = mapOf(
      "Origin" to baseurl,
      "Referer" to baseurl,
      "User-Agent" to "Mozilla/5.0 (Linux; Android 11; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
    )
    try {
      val res = app.get(url, referer = referer, headers = headers).toString()

      val encodedString = Regex("(?:const|let|var|window\\.\\w+)\\s+\\w*\\s*=\\s*'([^']{30,})'").find(res)
        ?.groupValues?.get(1)?.trim() ?: ""
      if (encodedString.isEmpty()) {
        throw Exception("Encoded string not found")
      }
      
      val passwordHex = app.get("https://pastebin.com/raw/DCmJyUSi").text
      val password passwordHex.chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()

      val decryptedData = decryptAESCBC(encodedString, password)
        ?: throw Exception("Decryption failed")

      val m3u8 = Regex("(https?://[^\\s\"'\\\\]*m3u8[^\\s\"'\\\\]*)").find(decryptedData)
        ?.groupValues?.get(1)?.trim() ?: ""
      if (m3u8.isEmpty()) {
        throw Exception("m3u8 URL not found")
      }

      val header = mapOf(
        "accept" to "*/*",
        "accept-language" to "en-US,en;q=0.5",
        "Origin" to mainUrl,
        "Accept-Encoding" to "gzip, deflate, br",
        "Connection" to "keep-alive",
        "Sec-Fetch-Dest" to "empty",
        "Sec-Fetch-Mode" to "cors",
        "Sec-Fetch-Site" to "cross-site",
        "user-agent" to USER_AGENT
      )

      callback.invoke(
        newExtractorLink(
          name,
          name,
          url = m3u8,
          INFER_TYPE
        ) {
          this.referer = mainUrl
          this.quality = Qualities.P1080.value
          this.headers = header
        }
      )

      // Extract and return subtitles
      val subtitles = extractSrtSubtitles(decryptedData)
      subtitles.forEachIndexed { _, (language, url) ->
        subtitleCallback.invoke(SubtitleFile(language, url))
      }
    } catch (e: Exception) {
      Log.e("Anisaga Stream", "Error: ${e.message}")
    }
  }


  private fun getBaseUrl(url: String): String {
    return URI(url).let {
      "${it.scheme}://${it.host}"
    }
  }
  fun decryptAESCBC(encryptedData: String, password: String): String? {
    try {
      // Base64 decode the encrypted data
      val decodedBytes = Base64.getDecoder().decode(encryptedData)

      // Extract IV (first 16 bytes) and encrypted data (remaining bytes)
      val ivBytes = decodedBytes.copyOfRange(0, 16)
      val encryptedBytes = decodedBytes.copyOfRange(16, decodedBytes.size)

      // Prepare key
      val keyBytes = password.toByteArray(Charsets.UTF_8)
      val secretKey = SecretKeySpec(keyBytes, "AES")
      val ivSpec = IvParameterSpec(ivBytes)

      // Decrypt using AES-CBC
      val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
      cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)

      val decryptedBytes = cipher.doFinal(encryptedBytes)
      return String(decryptedBytes, Charsets.UTF_8)

    } catch (e: BadPaddingException) {
      println("Decryption failed: Bad padding or incorrect password.")
      return null
    } catch (e: Exception) {
      e.printStackTrace()
      return null
    }
  }
  private fun extractSrtSubtitles(subtitle: String): List<Pair<String, String>> {
    val regex = """\[(.*?)](https?://[^\s,"]+\.srt)""".toRegex()
    return regex.findAll(subtitle).map {
        it.groupValues[1] to it.groupValues[2]
    }.toList()
  }
}