package com.vdurmont.emoji;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.*;

/**
 * Holds the loaded emojis and provides search functions.
 *
 * @author Vincent DURMONT [vdurmont@gmail.com]
 */
public class EmojiManager {
    static final EmojiTrie EMOJI_TRIE;
    private static final String PATH = "/emoji-list.json";
    private static final Map<String, Emoji> EMOJIS_BY_ALIAS = new HashMap<>();
    private static final Map<String, Set<Emoji>> EMOJIS_BY_TAG = new HashMap<>();
    private static final Map<EmojiCategory, Set<Emoji>> EMOJIS_BY_CATEGORY = new HashMap<>();
    private static final List<Emoji> ALL_EMOJIS;

    static {
        try (InputStream stream = EmojiLoader.class.getResourceAsStream(PATH)) {
            List<Emoji> emojis = EmojiLoader.loadEmojis(stream);
            Map<String, Emoji> definitions = EmojiLoader.loadEmojiBundle();

            ALL_EMOJIS = emojis;
            for (ListIterator<Emoji> iter = emojis.listIterator(); iter.hasNext();) {
                Emoji emoji = iter.next();
                Emoji definition = definitions.remove(emoji.getUnicode());
                // Update the aliases with the additional information
                if (definition != null) {
                    Set<String> joint = new HashSet<>(emoji.aliases);
                    joint.addAll(definition.aliases);
                    iter.set(
                        emoji = emoji.setAliases(new ArrayList<>(joint))
                                     .setFitzpatrick(definition.supportsFitzpatrick || emoji.supportsFitzpatrick)
                    );
                }

                loadEmoji(emoji);
            }

            // Add all the missing emojis defined in the definitions list
            for (Map.Entry<String, Emoji> entry : definitions.entrySet()) {
                Emoji emoji = entry.getValue();
                loadEmoji(emoji);
                emojis.add(emoji);
            }

            EMOJI_TRIE = new EmojiTrie(emojis);
            ALL_EMOJIS.sort((e1, e2) -> e2.getUnicode().length() - e1.getUnicode().length());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void loadEmoji(Emoji emoji) {
        Set<Emoji> set = EMOJIS_BY_CATEGORY.computeIfAbsent(emoji.getCategory(), k -> new HashSet<>());
        set.add(emoji);

        for (String tag : emoji.getTags()) {
            set = EMOJIS_BY_TAG.computeIfAbsent(tag.toLowerCase(Locale.ROOT), k -> new HashSet<>());
            set.add(emoji);
        }

        for (String alias : emoji.getAliases()) {
            EMOJIS_BY_ALIAS.put(alias.toLowerCase(Locale.ROOT), emoji);
        }
    }

    /**
     * No need for a constructor, all the methods are static.
     */
    private EmojiManager() {
    }

    /**
     * Returns all the {@link com.vdurmont.emoji.Emoji Emojis} for a given tag.
     *
     * @param  tag
     *         the tag
     *
     * @return {@link Set} of {@link com.vdurmont.emoji.Emoji Emojis}, empty set if the tag is unknown
     *
     * @see    #getAllTags()
     */
    @NotNull
    public static Set<Emoji> getForTag(@NotNull String tag) {
        if (tag == null) {
            return Collections.emptySet();
        }

        Set<Emoji> emojis = EMOJIS_BY_TAG.get(tag.toLowerCase(Locale.ROOT));
        return emojis == null ? Collections.emptySet() : emojis;
    }

    /**
     * Returns all the {@link com.vdurmont.emoji.Emoji Emojis} for a given category.
     *
     * @param  category
     *         the {@link EmojiCategory}
     *
     * @return {@link Set} of {@link com.vdurmont.emoji.Emoji Emojis}, empty set if the category is unknown or null
     */
    @NotNull
    public static Set<Emoji> getForCategory(@NotNull EmojiCategory category) {
        if (category == null) {
            return Collections.emptySet();
        }

        Set<Emoji> emojis = EMOJIS_BY_CATEGORY.get(category);
        return emojis == null ? Collections.emptySet() : emojis;
    }

    /**
     * Returns the {@link com.vdurmont.emoji.Emoji} for a given alias.
     *
     * @param  alias
     *         the alias
     *
     * @return The associated {@link com.vdurmont.emoji.Emoji}, null if the alias is unknown
     */
    @Nullable
    public static Emoji getForAlias(@NotNull String alias) {
        if (alias == null || alias.isEmpty()) {
            return null;
        }

        return EMOJIS_BY_ALIAS.get(trimAlias(alias).toLowerCase(Locale.ROOT));
    }

    private static String trimAlias(String alias) {
        int len = alias.length();
        return alias.substring(
                alias.charAt(0) == ':' ? 1 : 0,
                alias.charAt(len - 1) == ':' ? len - 1 : len);
    }


    /**
     * Returns the {@link com.vdurmont.emoji.Emoji} for a given unicode.
     *
     * @param  unicode
     *         the unicode
     *
     * @return The associated {@link com.vdurmont.emoji.Emoji}, null if the unicode is unknown
     */
    @Nullable
    public static Emoji getByUnicode(@NotNull String unicode) {
        if (unicode == null) {
            return null;
        }

        return EMOJI_TRIE.getEmoji(unicode);
    }

    /**
     * Returns all the {@link com.vdurmont.emoji.Emoji Emojis}
     *
     * @return Immutable list of all the {@link com.vdurmont.emoji.Emoji Emojis}
     */
    @NotNull
    public static List<Emoji> getAll() {
        return ALL_EMOJIS;
    }

    /**
     * Tests if a given String is an emoji.
     *
     * @param  string
     *         the string to test
     *
     * @return true, if the string is an emoji's unicode, false otherwise
     */
    public static boolean isEmoji(@NotNull String string) {
        if (string == null) return false;

        EmojiParser.UnicodeCandidate unicodeCandidate = EmojiParser.getNextUnicodeCandidate(string.toCharArray(), 0);
        return unicodeCandidate != null &&
                unicodeCandidate.getEmojiStartIndex() == 0 &&
                unicodeCandidate.getFitzpatrickEndIndex() == string.length();
    }

    /**
     * Tests if a given String contains an emoji.
     *
     * @param  string
     *         the string to test
     *
     * @return true, if the string contains an emoji's unicode, false otherwise
     */
    public static boolean containsEmoji(@NotNull String string) {
        if (string == null) return false;

        return EmojiParser.getNextUnicodeCandidate(string.toCharArray(), 0) != null;
    }

    /**
     * Tests if a given String only contains emojis.
     *
     * @param  string
     *         the string to test
     *
     * @return true, if the string only contains emojis, false otherwise
     */
    public static boolean isOnlyEmojis(@NotNull String string) {
        return string != null && EmojiParser.removeAllEmojis(string).isEmpty();
    }

    /**
     * Checks if sequence of chars contain an emoji.
     *
     * @param sequence
     *       Sequence of char that may contain emoji in full or partially.
     *
     * @return
     * <ul>
     *   <li>{@link EmojiTrie.Matches#EXACTLY} if char sequence in its entirety is an emoji </li>
     *   <li>{@link EmojiTrie.Matches#POSSIBLY} if char sequence matches prefix of an emoji</li>
     *   <li>{@link EmojiTrie.Matches#IMPOSSIBLE} if char sequence matches no emoji or prefix of an emoji</li>
     * </ul>
     */
    @NotNull
    public static EmojiTrie.Matches isEmoji(@NotNull char[] sequence) {
        return EMOJI_TRIE.isEmoji(sequence);
    }

    /**
     * Returns all the tags in the database
     *
     * @return Immutable {@link Set} of known tags
     */
    @NotNull
    public static Set<String> getAllTags() {
        return Collections.unmodifiableSet(EMOJIS_BY_TAG.keySet());
    }
}
