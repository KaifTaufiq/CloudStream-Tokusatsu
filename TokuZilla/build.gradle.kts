version = 3

cloudstream {
    language = "en"
    authors = listOf("Kaif Shaikh")
    description = "Tokuzilla all in one Site for Tokusatsu"
    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta-only
     * */
    status = 1 // will be 3 if unspecified
    tvTypes = listOf(
        "AnimeMovie",
        "Anime",
        "Cartoon"
    )

    iconUrl = "https://raw.githubusercontent.com/KaifTaufiq/CloudStream-Tokusatsu/refs/heads/master/TokuZilla/icon.png"

    isCrossPlatform = true
}
