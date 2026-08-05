package com.current.news.data

import androidx.compose.ui.graphics.Color

/**
 * In-memory sample data source. Swap this out for a real API/DB-backed
 * repository (e.g. Retrofit + Room) once the app is wired to a backend —
 * the ViewModel layer above this is already written against a plain
 * suspend/StateFlow contract so the swap is mechanical.
 */
object NewsRepository {

    val editions = listOf("For you", "World", "Business", "Technology", "Climate", "Culture")

    val topics = listOf(
        Topic("world", "World", Color(0xFF3A1F1C), Color(0xFF1A1210)),
        Topic("climate", "Climate", Color(0xFF1F2A1C), Color(0xFF101A12)),
        Topic("technology", "Technology", Color(0xFF1C2430), Color(0xFF10151C)),
        Topic("culture", "Culture", Color(0xFF2C2417), Color(0xFF1A1610))
    )

    val writers = listOf(
        Writer("w1", "Priya Shah", "Logistics"),
        Writer("w2", "Amara Osei", "Climate"),
        Writer("w3", "Leo Marchetti", "Markets"),
        Writer("w4", "Daniel Kwon", "Technology")
    )

    val articles: List<Article> = listOf(
        Article(
            id = "a1",
            category = "Investigation",
            title = "Inside the shipping delays reshaping global retail",
            dek = "Ports from Rotterdam to Long Beach face a backlog that traces back to a single canal closure — and retailers are quietly rewriting their sourcing playbooks.",
            author = "Priya Shah",
            timeAgo = "12m ago",
            readTime = "6 min read",
            isHero = true,
            thumbColorStart = Color(0xFF3A3D44),
            thumbColorEnd = Color(0xFF1B1D21),
            caption = "A container ship idles outside the Port of Rotterdam, October 2026.",
            body = listOf(
                "The backlog began, as these things often do, with a single point of failure. A canal closure in early autumn set off a chain reaction that logistics firms are still untangling three months later.",
                "Retailers who once prized just-in-time delivery are now paying a premium for redundancy — warehousing closer to demand, diversified shipping lanes, and contracts that price in delay as the default, not the exception.",
                "\"We used to optimize for cost per container,\" said one regional supply chain director. \"Now we optimize for the number of ways a shipment can still arrive if the first three routes fail.\"",
                "Analysts expect the realignment to outlast the immediate crisis, permanently raising the baseline cost of global trade by an estimated two to four percent."
            )
        ),
        Article(
            id = "a2",
            category = "Politics",
            title = "Parliament reconvenes to debate infrastructure bill",
            dek = "Lawmakers return from recess facing a narrowing window to pass the funding package before the fiscal year closes.",
            author = "Reuters",
            timeAgo = "24m ago",
            readTime = "4 min read",
            thumbColorStart = Color(0xFF3A3D44),
            thumbColorEnd = Color(0xFF1B1D21),
            body = listOf(
                "The session opened with sharp exchanges over funding priorities, as opposition lawmakers pushed for regional infrastructure guarantees before agreeing to advance the bill.",
                "Committee leaders say a revised draft could reach the floor within two weeks, though several riders remain contested."
            )
        ),
        Article(
            id = "a3",
            category = "Climate",
            title = "Coastal cities test new flood barrier design ahead of monsoon",
            dek = "Engineers say the modular system can be deployed in under six hours, a fraction of the time required by permanent seawalls.",
            author = "Amara Osei",
            timeAgo = "41m ago",
            readTime = "5 min read",
            thumbColorStart = Color(0xFF3A3D44),
            thumbColorEnd = Color(0xFF1B1D21),
            body = listOf(
                "Pilot installations along three low-lying districts will be monitored through the coming monsoon season, with early data feeding into a national resilience plan.",
                "Local officials remain cautiously optimistic, noting the barriers are a stopgap rather than a substitute for longer-term drainage investment."
            )
        ),
        Article(
            id = "a4",
            category = "Markets",
            title = "Tech rally cools as investors rotate into value stocks",
            dek = "A three-week run in growth names lost steam this week as traders locked in gains ahead of earnings season.",
            author = "Bloomberg",
            timeAgo = "1h ago",
            readTime = "3 min read",
            thumbColorStart = Color(0xFF3A3D44),
            thumbColorEnd = Color(0xFF1B1D21),
            body = listOf(
                "The pullback was broad but shallow, with defensive sectors picking up the flows that growth stocks shed.",
                "Strategists caution against reading too much into a single week, pointing to still-strong forward guidance across most large-cap tech names."
            )
        ),
        Article(
            id = "a5",
            category = "Opinion",
            title = "Why \"just-in-time\" was always a bet on calm seas",
            dek = "The efficiency doctrine that defined a generation of supply chains never priced in the storm.",
            author = "Leo Marchetti",
            timeAgo = "2h ago",
            readTime = "7 min read",
            thumbColorStart = Color(0xFF3A3D44),
            thumbColorEnd = Color(0xFF1B1D21),
            body = listOf(
                "Efficiency and resilience have always been in tension, and for thirty years efficiency won almost every argument.",
                "That era is quietly ending, and the companies that adapt first will set the terms for everyone else."
            )
        ),
        Article(
            id = "a6",
            category = "World",
            title = "Canal authority outlines dredging timeline for early 2027",
            dek = "A phased restoration plan aims to restore full transit capacity within fourteen months.",
            author = "AP",
            timeAgo = "1d ago",
            readTime = "4 min read",
            thumbColorStart = Color(0xFF3A3D44),
            thumbColorEnd = Color(0xFF1B1D21),
            body = listOf(
                "The authority's revised timeline splits the project into three phases, prioritizing the deepest channel sections first.",
                "Shipping alliances have welcomed the clarity even as they continue to route around the corridor in the interim."
            )
        )
    )

    val liveHeadline = "Central bank holds rates steady, signals cuts in Q1"

    fun search(query: String): List<Article> {
        if (query.isBlank()) return emptyList()
        val q = query.trim().lowercase()
        return articles.filter {
            it.title.lowercase().contains(q) ||
                it.category.lowercase().contains(q) ||
                it.author.lowercase().contains(q) ||
                it.dek.lowercase().contains(q)
        }
    }

    fun byId(id: String): Article? = articles.find { it.id == id }
}
