package dev.esophose.discordbot.misc.embed

import discord4j.core.spec.EmbedCreateSpec
import discord4j.rest.util.Color
import java.time.Instant
import java.util.ArrayList

class EmbedStorage {

    private var title: String? = null
    private var description: String? = null
    private var url: String? = null
    private var timestamp: Instant? = null
    private var color: Color? = null
    private var footer: EmbedFooter? = null
    private var imageUrl: String? = null
    private var thumbnailUrl: String? = null
    private var author: EmbedAuthor? = null
    private val fields: MutableList<EmbedField> = ArrayList()

    fun setTitle(title: String): EmbedStorage = apply { this.title = title }
    fun setDescription(description: String): EmbedStorage = apply { this.description = description }
    fun setUrl(url: String): EmbedStorage = apply { this.url = url}
    fun setTimestamp(timestamp: Instant): EmbedStorage = apply { this.timestamp = timestamp }
    fun setColor(color: Color): EmbedStorage = apply { this.color = color }
    fun setFooter(text: String, iconUrl: String?): EmbedStorage = apply { this.footer = EmbedFooter(text, iconUrl) }
    fun setImage(url: String): EmbedStorage = apply { this.imageUrl = url }
    fun setThumbnail(url: String): EmbedStorage = apply { this.thumbnailUrl = url }
    fun setAuthor(name: String, url: String?, iconUrl: String?): EmbedStorage = apply { this.author = EmbedAuthor(name, url, iconUrl) }
    fun addField(name: String, value: String, inline: Boolean): EmbedStorage = apply { this.fields.add(EmbedField(name, value, inline)) }

    fun applyToCreateSpec(embedCreateSpec: EmbedCreateSpec, currentPageNumber: Int, maxPageNumber: Int) {
        if (this.title != null) embedCreateSpec.setTitle(this.applyPageReplacements(this.title!!, currentPageNumber, maxPageNumber))
        if (this.description != null) embedCreateSpec.setDescription(this.applyPageReplacements(this.description!!, currentPageNumber, maxPageNumber))
        if (this.url != null) embedCreateSpec.setUrl(this.url!!)
        if (this.timestamp != null) embedCreateSpec.setTimestamp(this.timestamp!!)
        if (this.color != null) embedCreateSpec.setColor(this.color!!)
        if (this.footer != null) embedCreateSpec.setFooter(this.applyPageReplacements(this.footer!!.text, currentPageNumber, maxPageNumber), this.footer!!.iconUrl)
        if (this.imageUrl != null) embedCreateSpec.setImage(this.imageUrl!!)
        if (this.thumbnailUrl != null) embedCreateSpec.setThumbnail(this.thumbnailUrl!!)
        if (this.author != null) embedCreateSpec.setAuthor(this.applyPageReplacements(this.author!!.name, currentPageNumber, maxPageNumber), this.author!!.url, this.author!!.iconUrl)
        this.fields.forEach { x -> embedCreateSpec.addField(this.applyPageReplacements(x.name, currentPageNumber, maxPageNumber), this.applyPageReplacements(x.value, currentPageNumber, maxPageNumber), x.isInline) }
    }

    private fun applyPageReplacements(value: String, current: Int, max: Int): String {
        return value.replace("%currentPage%".toRegex(), current.toString()).replace("%maxPage%".toRegex(), max.toString())
    }

    class EmbedFooter(val text: String, val iconUrl: String?)

    class EmbedAuthor(val name: String, val url: String?, val iconUrl: String?)

    class EmbedField(val name: String, val value: String, val isInline: Boolean)

}
