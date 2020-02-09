package dev.esophose.discordbot.misc.embed;

import discord4j.core.spec.EmbedCreateSpec;
import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class EmbedStorage {

    private String title;
    private String description;
    private String url;
    private Instant timestamp;
    private Color color;
    private EmbedFooter footer;
    private String imageUrl;
    private String thumbnailUrl;
    private EmbedAuthor author;
    private List<EmbedField> fields;

    public EmbedStorage() {
        this.fields = new ArrayList<>();
    }

    public EmbedStorage setTitle(String title) {
        this.title = title;
        return this;
    }

    public EmbedStorage setDescription(String description) {
        this.description = description;
        return this;
    }

    public EmbedStorage setUrl(String url) {
        this.url = url;
        return this;
    }

    public EmbedStorage setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public EmbedStorage setColor(Color color) {
        this.color = color;
        return this;
    }

    public EmbedStorage setFooter(String text, String iconUrl) {
        this.footer = new EmbedFooter(text, iconUrl);
        return this;
    }

    public EmbedStorage setImage(String url) {
        this.imageUrl = url;
        return this;
    }

    public EmbedStorage setThumbnail(String url) {
        this.thumbnailUrl = url;
        return this;
    }

    public EmbedStorage setAuthor(String name, String url, String iconUrl) {
        this.author = new EmbedAuthor(name, url, iconUrl);
        return this;
    }

    public EmbedStorage addField(String name, String value, boolean inline) {
        this.fields.add(new EmbedField(name, value, inline));
        return this;
    }

    public void applyToCreateSpec(EmbedCreateSpec embedCreateSpec, int currentPageNumber, int maxPageNumber) {
        if (this.title != null) embedCreateSpec.setTitle(this.applyPageReplacements(this.title, currentPageNumber, maxPageNumber));
        if (this.description != null) embedCreateSpec.setDescription(this.applyPageReplacements(this.description, currentPageNumber, maxPageNumber));
        if (this.url != null) embedCreateSpec.setUrl(this.url);
        if (this.timestamp != null) embedCreateSpec.setTimestamp(this.timestamp);
        if (this.color != null) embedCreateSpec.setColor(this.color);
        if (this.footer != null) embedCreateSpec.setFooter(this.applyPageReplacements(this.footer.getText(), currentPageNumber, maxPageNumber), this.footer.getIconUrl());
        if (this.imageUrl != null) embedCreateSpec.setImage(this.imageUrl);
        if (this.thumbnailUrl != null) embedCreateSpec.setThumbnail(this.thumbnailUrl);
        if (this.author != null) embedCreateSpec.setAuthor(this.applyPageReplacements(this.author.getName(), currentPageNumber, maxPageNumber), this.author.getUrl(), this.author.getIconUrl());
        this.fields.forEach(x -> embedCreateSpec.addField(this.applyPageReplacements(x.getName(), currentPageNumber, maxPageNumber), this.applyPageReplacements(x.getValue(), currentPageNumber, maxPageNumber), x.isInline()));
    }

    private String applyPageReplacements(String value, int current, int max) {
        return value.replaceAll("%currentPage%", String.valueOf(current)).replaceAll("%maxPage%", String.valueOf(max));
    }

    public static class EmbedFooter {

        private String text;
        private String iconUrl;

        public EmbedFooter(String text, String iconUrl) {
            this.text = text;
            this.iconUrl = iconUrl;
        }

        public String getText() {
            return this.text;
        }

        public String getIconUrl() {
            return this.iconUrl;
        }

    }

    private static class EmbedAuthor {

        private String name;
        private String url;
        private String iconUrl;

        private EmbedAuthor(String name, String url, String iconUrl) {
            this.name = name;
            this.url = url;
            this.iconUrl = iconUrl;
        }

        public String getName() {
            return this.name;
        }

        public String getUrl() {
            return this.url;
        }

        public String getIconUrl() {
            return this.iconUrl;
        }

    }

    public static class EmbedField {

        private String name;
        private String value;
        private boolean inline;

        public EmbedField(String name, String value, boolean inline) {
            this.name = name;
            this.value = value;
            this.inline = inline;
        }

        public String getName() {
            return this.name;
        }

        public String getValue() {
            return this.value;
        }

        public boolean isInline() {
            return this.inline;
        }

    }

}
