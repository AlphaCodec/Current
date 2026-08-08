package com.current.news.data

/** NewsData.io expects lowercase ISO 3166-1 alpha-2 country codes. */
data class Country(val code: String?, val label: String)

val AVAILABLE_COUNTRIES: List<Country> = listOf(
    Country(null, "Global (all countries)"),
    Country("us", "United States"),
    Country("gb", "United Kingdom"),
    Country("in", "India"),
    Country("ca", "Canada"),
    Country("au", "Australia"),
    Country("de", "Germany"),
    Country("fr", "France"),
    Country("jp", "Japan"),
    Country("br", "Brazil"),
    Country("za", "South Africa"),
    Country("sg", "Singapore"),
    Country("ae", "United Arab Emirates"),
    Country("ng", "Nigeria"),
    Country("mx", "Mexico"),
    Country("it", "Italy"),
    Country("es", "Spain"),
    Country("kr", "South Korea"),
    Country("cn", "China"),
    Country("ru", "Russia"),
    Country("nz", "New Zealand")
)
