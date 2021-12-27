package com.vdurmont.emoji;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Loads the emojis from a JSON database.
 *
 * @author Vincent DURMONT [vdurmont@gmail.com]
 */
public class EmojiLoader {
    /**
     * No need for a constructor, all the methods are static.
     */
    private EmojiLoader() {
    }

    /**
     * Loads a JSONArray of emojis from an InputStream,
     * parses it and returns the associated list of {@link com.vdurmont.emoji.Emoji Emojis}.
     *
     * @param  stream
     *         The stream of the JSONArray
     *
     * @throws NullPointerException
     *         If the provided stream is null
     * @throws org.json.JSONException
     *         If the json representation is invalid
     * @throws IOException
     *         If an error occurs while reading the stream or parsing the JSONArray
     *
     * @return The list of {@link com.vdurmont.emoji.Emoji Emojis}
     */
    @NotNull
    public static List<Emoji> loadEmojis(@NotNull InputStream stream) throws IOException {
        Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        JSONArray emojisJSON = new JSONArray(new JSONTokener(reader));
        List<Emoji> emojis = new ArrayList<>(emojisJSON.length());
        for (int i = 0; i < emojisJSON.length(); i++) {
            Emoji emoji = buildEmojiFromJSON(emojisJSON.getJSONObject(i));
            if (emoji != null) {
                emojis.add(emoji);
            }
        }
        return emojis;
    }

    /**
     * Loads the emoji-definitions from the resources.
     *
     * @throws IOException
     *         If there is an I/O error when trying to read the resource file
     *
     * @return {@link Map} of emoji characters to emoji instances
     */
    @NotNull
    public static Map<String, Emoji> loadEmojiBundle() throws IOException {
        try (Reader reader = new InputStreamReader(EmojiLoader.class.getResourceAsStream("/emoji-definitions.json"), StandardCharsets.UTF_8)) {
            JSONObject file = new JSONObject(new JSONTokener(reader));
            JSONArray definitions = file.getJSONArray("emojiDefinitions");
            Map<String, Emoji> map = new HashMap<>(definitions.length()+1);
            for (int i = 0; i < definitions.length(); i++) {
                JSONObject json = definitions.getJSONObject(i);
                if (!json.has("category")) continue;

                String key = json.getString("surrogates");
                String primaryName = json.getString("primaryName");
                boolean supportsFitzpatrick = primaryName.contains("_tone");
                if (supportsFitzpatrick) {
                    key = key.substring(0, key.length() - 2);
                    if (map.containsKey(key)) {
                        map.put(key, map.get(key).setFitzpatrick(true));
                        continue;
                    }
                }

                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                List<String> aliases = jsonArrayToStringList(json.getJSONArray("names"));
                List<String> tags = Collections.emptyList();
                EmojiCategory category = convertCategory(json.getString("category"));
                Emoji emoji = new Emoji("", supportsFitzpatrick, category, aliases, tags, bytes);
                map.put(key, emoji);
            }
            return map;
        }
    }

    private static EmojiCategory convertCategory(String raw) {
        for (EmojiCategory category : EmojiCategory.values()) {
            if (category.name().equalsIgnoreCase(raw))
                return category;
        }
        return EmojiCategory.UNKNOWN;
    }

    protected static Emoji buildEmojiFromJSON(JSONObject json) {
        if (!json.has("emoji")) {
            return null;
        }

        byte[] bytes = json.getString("emoji").getBytes(StandardCharsets.UTF_8);
        boolean supportsFitzpatrick = json.optBoolean("skin_tones", false);
        List<String> aliases = jsonArrayToStringList(json.getJSONArray("aliases"));
        List<String> tags = jsonArrayToStringList(json.getJSONArray("tags"));
        String description = json.getString("description");
        EmojiCategory category = EmojiCategory.fromString(json.optString("category", "UNKNOWN"));
        return new Emoji(description, supportsFitzpatrick, category, aliases, tags, bytes);
    }

    private static List<String> jsonArrayToStringList(JSONArray array) {
        List<String> strings = new ArrayList<>(array.length());
        for (int i = 0; i < array.length(); i++) {
            strings.add(array.getString(i));
        }
        return strings;
    }
}
