package com.vdurmont.emoji;

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
        System.getProperties().setProperty("file-encoding", "UTF-8");
        try (InputStream stream = EmojiLoader.class.getResourceAsStream(PATH)) {
            List<Emoji> emojis = EmojiLoader.loadEmojis(stream);
            ALL_EMOJIS = emojis;
            for (Emoji emoji : emojis) {
                Set<Emoji> set = EMOJIS_BY_CATEGORY.computeIfAbsent(emoji.getCategory(), k -> new HashSet<>());
                set.add(emoji);

                for (String tag : emoji.getTags()) {
                    set = EMOJIS_BY_TAG.computeIfAbsent(tag, k -> new HashSet<>());
                    set.add(emoji);
                }

                for (String alias : emoji.getAliases()) {
                    EMOJIS_BY_ALIAS.put(alias, emoji);
                }
            }

            EMOJI_TRIE = new EmojiTrie(emojis);
            ALL_EMOJIS.sort((e1, e2) -> e2.getUnicode().length() - e1.getUnicode().length());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * No need for a constructor, all the methods are static.
     */
    private EmojiManager() {
    }

    /**
     * Returns all the {@link com.vdurmont.emoji.Emoji}s for a given tag.
     *
     * @param tag the tag
     * @return the associated {@link com.vdurmont.emoji.Emoji}s, null if the tag
     * is unknown
     */
    public static Set<Emoji> getForTag(String tag) {
        if (tag == null) {
            return null;
        }
        return EMOJIS_BY_TAG.get(tag);
    }

    public static Set<Emoji> getForCategory(EmojiCategory category) {
        if (category == null) {
            return null;
        }

        return EMOJIS_BY_CATEGORY.get(category);
    }

    /**
     * Returns the {@link com.vdurmont.emoji.Emoji} for a given alias.
     *
     * @param alias the alias
     * @return the associated {@link com.vdurmont.emoji.Emoji}, null if the alias
     * is unknown
     */
    public static Emoji getForAlias(String alias) {
        if (alias == null || alias.isEmpty()) {
            return null;
        }
        return EMOJIS_BY_ALIAS.get(trimAlias(alias));
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
     * @param unicode the the unicode
     * @return the associated {@link com.vdurmont.emoji.Emoji}, null if the
     * unicode is unknown
     */
    public static Emoji getByUnicode(String unicode) {
        if (unicode == null) {
            return null;
        }
        return EMOJI_TRIE.getEmoji(unicode);
    }

    /**
     * Returns all the {@link com.vdurmont.emoji.Emoji}s
     *
     * @return all the {@link com.vdurmont.emoji.Emoji}s
     */
    public static Collection<Emoji> getAll() {
        return ALL_EMOJIS;
    }

    /**
     * Tests if a given String is an emoji.
     *
     * @param string the string to test
     * @return true if the string is an emoji's unicode, false else
     */
    public static boolean isEmoji(String string) {
        if (string == null) return false;

        EmojiParser.UnicodeCandidate unicodeCandidate = EmojiParser.getNextUnicodeCandidate(string.toCharArray(), 0);
        return unicodeCandidate != null &&
                unicodeCandidate.getEmojiStartIndex() == 0 &&
                unicodeCandidate.getFitzpatrickEndIndex() == string.length();
    }

    /**
     * Tests if a given String contains an emoji.
     *
     * @param string the string to test
     * @return true if the string contains an emoji's unicode, false otherwise
     */
    public static boolean containsEmoji(String string) {
        if (string == null) return false;

        return EmojiParser.getNextUnicodeCandidate(string.toCharArray(), 0) != null;
    }

    /**
     * Tests if a given String only contains emojis.
     *
     * @param string the string to test
     * @return true if the string only contains emojis, false else
     */
    public static boolean isOnlyEmojis(String string) {
        return string != null && EmojiParser.removeAllEmojis(string).isEmpty();
    }

    /**
     * Checks if sequence of chars contain an emoji.
     *
     * @param sequence Sequence of char that may contain emoji in full or
     *                 partially.
     * @return &lt;li&gt;
     * Matches.EXACTLY if char sequence in its entirety is an emoji
     * &lt;/li&gt;
     * &lt;li&gt;
     * Matches.POSSIBLY if char sequence matches prefix of an emoji
     * &lt;/li&gt;
     * &lt;li&gt;
     * Matches.IMPOSSIBLE if char sequence matches no emoji or prefix of an
     * emoji
     * &lt;/li&gt;
     */
    public static EmojiTrie.Matches isEmoji(char[] sequence) {
        return EMOJI_TRIE.isEmoji(sequence);
    }

    /**
     * Returns all the tags in the database
     *
     * @return the tags
     */
    public static Collection<String> getAllTags() {
        return EMOJIS_BY_TAG.keySet();
    }
}
