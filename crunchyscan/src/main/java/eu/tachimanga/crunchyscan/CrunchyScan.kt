package eu.tachimanga.crunchyscan

import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.network.GET
import okhttp3.Request
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class CrunchyScan : ParsedHttpSource() {

    override val name = "CrunchyScan"
    override val baseUrl = "https://crunchyscan.fr"
    override val lang = "fr"
    override val supportsLatest = true

    override fun headersBuilder() = super.headersBuilder()
        .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115 Safari/537.36")
        .add("Referer", baseUrl)

    // --- Popular ---
    override fun popularMangaRequest(page: Int): Request = GET("$baseUrl/manga/?page=$page", headers)

    override fun popularMangaSelector() = "div.bsx"

    override fun popularMangaFromElement(element: Element): SManga = SManga.create().apply {
        title = element.selectFirst("div.tt a")?.text().orEmpty()
        url = element.selectFirst("div.tt a")?.attr("href").orEmpty()
        thumbnail_url = element.selectFirst("img")?.attr("data-src")
    }

    override fun popularMangaNextPageSelector() = "a.next"

    // --- Search ---
    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request =
        GET("$baseUrl/?s=$query&post_type=wp-manga&page=$page", headers)

    override fun searchMangaSelector() = popularMangaSelector()
    override fun searchMangaFromElement(element: Element) = popularMangaFromElement(element)
    override fun searchMangaNextPageSelector() = popularMangaNextPageSelector()

    // --- Manga details ---
    override fun mangaDetailsParse(document: Document): SManga = SManga.create().apply {
        title = document.selectFirst("h1.post-title")?.text().orEmpty()
        author = document.select("div.author a").joinToString { it.text() }
        description = document.selectFirst(".description-summary, .entry-content")?.text().orEmpty()
        thumbnail_url = document.selectFirst("div.summary_image img")?.attr("data-src")
    }

    // --- Chapters ---
    override fun chapterListSelector() = "li.wp-manga-chapter a"

    override fun chapterFromElement(element: Element): SChapter = SChapter.create().apply {
        name = element.text()
        url = element.attr("href")
    }

    // --- Pages ---
    override fun pageListParse(document: Document): List<Page> {
        return document.select("div.reading-content img").mapIndexed { i, el ->
            Page(i, "", el.attr("data-src"))
        }
    }

    override fun imageUrlParse(document: Document): String = document.selectFirst("img")!!.attr("src")

    // --- Latest updates (optionnel) ---
    override fun latestUpdatesRequest(page: Int): Request = GET("$baseUrl/?s=&post_type=wp-manga&page=$page", headers)
    override fun latestUpdatesSelector() = popularMangaSelector()
    override fun latestUpdatesFromElement(element: Element) = popularMangaFromElement(element)
    override fun latestUpdatesNextPageSelector() = popularMangaNextPageSelector()
}
