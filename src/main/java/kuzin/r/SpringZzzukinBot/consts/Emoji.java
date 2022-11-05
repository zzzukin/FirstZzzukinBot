package kuzin.r.SpringZzzukinBot.consts;

import com.vdurmont.emoji.EmojiParser;

public enum Emoji {
    SMILING_FACE_WITH_SUNGLASSES("😎"),
    FISH("🐟"),
    FISHING_POLE_AND_FISH("🎣"),
    DISGUISED_FACE("\uD83E\uDD78"),
    RAGE("😡"),
    WINKING_FACE("😉"),
    SLIGHTLY_SMILING_FACE("🙂"),
    SMIRKING_FACE("😏"),
    CLINKING_GLASSES("🥂"),
    SUN_BEHIND_CLOUD("⛅");


    private final String emoji;

    Emoji(String key) {
        this.emoji = parseEmoji(key);
    }

    private String parseEmoji(String kod) {
        return EmojiParser.parseToUnicode(kod);
    }

    @Override
    public String toString() {
        return emoji;
    }

    public String getKey() {
        return emoji;
    }
}
