package com.vdurmont.emoji;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * This class represents an emoji.
 *
 * <p>This object is immutable, so it can be used safely in a multi-threaded context.
 *
 * @author Vincent DURMONT [vdurmont@gmail.com]
 */
public class Emoji {
    protected final String description;
    protected final boolean supportsFitzpatrick;
    protected final boolean hasVariation;
    protected final List<String> aliases;
    protected final List<String> tags;
    protected final EmojiCategory category;
    protected final String unicode;
    protected final String trimmedUnicode;
    protected final String htmlDec;
    protected final String htmlHex;

    /**
     * Constructor for the Emoji.
     *
     * @param description
     *        The description of the emoji
     * @param supportsFitzpatrick
     *        Whether the emoji supports Fitzpatrick modifiers
     * @param category
     *        The emoji category
     * @param aliases
     *        The aliases for this emoji
     * @param tags
     *        The tags associated with this emoji
     * @param bytes
     *        The bytes that represent the emoji
     */
    protected Emoji(
            String description,
            boolean supportsFitzpatrick,
            EmojiCategory category,
            List<String> aliases,
            List<String> tags,
            byte... bytes
    ) {
        this.description = description;
        this.supportsFitzpatrick = supportsFitzpatrick;
        this.category = category;
        this.aliases = Collections.unmodifiableList(aliases);
        this.tags = Collections.unmodifiableList(tags);

        int count = 0;
        this.unicode = new String(bytes, StandardCharsets.UTF_8);
        int stringLength = getUnicode().length();
        String[] pointCodes = new String[stringLength];
        String[] pointCodesHex = new String[stringLength];

        for (int offset = 0; offset < stringLength; ) {
            final int codePoint = getUnicode().codePointAt(offset);

            pointCodes[count] = String.format(Locale.ROOT, "&#%d;", codePoint);
            pointCodesHex[count++] = String.format(Locale.ROOT, "&#x%x;", codePoint);

            offset += Character.charCount(codePoint);
        }
        this.htmlDec = String.join("", Arrays.copyOf(pointCodes, count));
        this.htmlHex = String.join("", Arrays.copyOf(pointCodesHex, count));
        this.hasVariation = unicode.contains("\uFE0F");
        this.trimmedUnicode = hasVariation ? unicode.replace("\uFE0F", "") : unicode;
    }

    protected Emoji setDescription(String description) {
        return new Emoji(description, supportsFitzpatrick, category, aliases, tags, unicode.getBytes(StandardCharsets.UTF_8));
    }

    protected Emoji setFitzpatrick(boolean supportsFitzpatrick) {
        return new Emoji(description, supportsFitzpatrick, category, aliases, tags, unicode.getBytes(StandardCharsets.UTF_8));
    }

    protected Emoji setCategory(EmojiCategory category) {
        return new Emoji(description, supportsFitzpatrick, category, aliases, tags, unicode.getBytes(StandardCharsets.UTF_8));
    }

    protected Emoji setAliases(List<String> aliases) {
        return new Emoji(description, supportsFitzpatrick, category, aliases, tags, unicode.getBytes(StandardCharsets.UTF_8));
    }

    protected Emoji setTags(List<String> tags) {
        return new Emoji(description, supportsFitzpatrick, category, aliases, tags, unicode.getBytes(StandardCharsets.UTF_8));
    }

    protected Emoji setBytes(byte... bytes) {
        return new Emoji(description, supportsFitzpatrick, category, aliases, tags, bytes);
    }

    /**
     * Returns the description of the emoji
     *
     * @return the description
     */
    @NotNull
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns whether the emoji supports the Fitzpatrick modifiers or not
     *
     * @return true if the emoji supports the Fitzpatrick modifiers
     */
    public boolean supportsFitzpatrick() {
        return this.supportsFitzpatrick;
    }

    /**
     * Returns whether the emoji supports a variation selection modifier or not
     *
     * @return true, if the emoji supports variation selector
     */
    public boolean supportsVariation() {
        return hasVariation;
    }

    /**
     * Returns the aliases of the emoji
     *
     * @return Immutable list of aliases
     */
    @NotNull
    public List<String> getAliases() {
        return this.aliases;
    }

    /**
     * Returns the tags of the emoji
     *
     * @return Immutable list if tags
     */
    @NotNull
    public List<String> getTags() {
        return this.tags;
    }

    /**
     * Returns the unicode representation of the emoji
     *
     * @return the unicode representation
     */
    @NotNull
    public String getUnicode() {
        return this.unicode;
    }

    /**
     * Returns the unicode representation of the emoji without the variation selector
     *
     * @return the unicode representation without variation selector
     */
    @NotNull
    public String getTrimmedUnicode() {
        return trimmedUnicode;
    }

    /**
     * Returns the {@link EmojiCategory} for this emoji
     *
     * @return The {@link EmojiCategory}
     */
    @NotNull
    public EmojiCategory getCategory() {
        return category;
    }

    /**
     * Returns the unicode representation of the emoji associated with the provided Fitzpatrick modifier.
     * <br>If the modifier is null, then the result is similar to {@link Emoji#getUnicode()}.
     *
     * @param  fitzpatrick
     *         the fitzpatrick modifier or null
     *
     * @throws IllegalStateException
     *         if the emoji doesn't support the Fitzpatrick modifiers
     *
     * @return the unicode representation
     */
    @NotNull
    public String getUnicode(@Nullable Fitzpatrick fitzpatrick) {
        if (!this.supportsFitzpatrick()) {
            throw new IllegalStateException("Cannot get the unicode with a fitzpatrick modifier, the emoji doesn't support fitzpatrick.");
        } else if (fitzpatrick == null) {
            return this.getUnicode();
        }
        return this.getUnicode() + fitzpatrick.unicode;
    }

    /**
     * Returns the unicode representation of the emoji associated with the provided Fitzpatrick modifier.
     * <br>If the modifier is null, then the result is similar to {@link Emoji#getTrimmedUnicode()}.
     *
     * @param  fitzpatrick
     *         the fitzpatrick modifier or null
     *
     * @throws IllegalStateException
     *         if the emoji doesn't support the Fitzpatrick modifiers
     *
     * @return the unicode representation
     */
    @NotNull
    public String getTrimmedUnicode(@Nullable Fitzpatrick fitzpatrick) {
        if (!this.supportsFitzpatrick()) {
            throw new IllegalStateException("Cannot get the unicode with a fitzpatrick modifier, the emoji doesn't support fitzpatrick.");
        } else if (fitzpatrick == null) {
            return this.getTrimmedUnicode();
        }
        return this.getTrimmedUnicode() + fitzpatrick.unicode;
    }

    /**
     * Returns the HTML decimal representation of the emoji
     *
     * @return the HTML decimal representation
     */
    @NotNull
    public String getHtmlDecimal() {
        return this.htmlDec;
    }

    /**
     * Returns the HTML hexadecimal representation of the emoji
     *
     * @return the HTML hexadecimal representation
     */
    @NotNull
    public String getHtmlHexadecimal() {
        return this.htmlHex;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Emoji &&
                ((Emoji) other).getUnicode().equals(getUnicode());
    }

    @Override
    public int hashCode() {
        return unicode.hashCode();
    }

    /**
     * Returns the String representation of the Emoji object.
     *
     * <h2>Example</h2>
     *
     * <pre>{@code Emoji {
     * description='smiling face with open mouth and smiling eyes',
     * supportsFitzpatrick=false,
     * aliases=[smile],
     * tags=[happy, joy, pleased],
     * category='Smileys & Emotion',
     * unicode='😄',
     * htmlDec='&#128516;',
     * htmlHex='&#x1f604;'
     * }}</pre>
     *
     * @return the string representation
     */
    @Override
    @NotNull
    public String toString() {
        return "Emoji{" +
                "description='" + description + '\'' +
                ", supportsFitzpatrick=" + supportsFitzpatrick +
                ", aliases=" + aliases +
                ", tags=" + tags +
                ", category='" + category.getDisplayName() + '\'' +
                ", unicode='" + unicode + '\'' +
                ", htmlDec='" + htmlDec + '\'' +
                ", htmlHex='" + htmlHex + '\'' +
        '}';
    }
}
