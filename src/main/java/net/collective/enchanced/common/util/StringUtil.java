package net.collective.enchanced.common.util;

import net.collectively.geode.math.math;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Style;
import net.minecraft.util.math.MathHelper;

public interface StringUtil {
    static String substringSafe(String text, int min, int max) {
        return text.substring(math.max(0, min), math.clamp(max, min + 1, text.length()));
    }

    static String scrollingTextFromTime(TextRenderer textRenderer, int maxCharacters, String text, long time, double speed) {
        double progress = (MathHelper.sin(time / speed) + 1) / 2d;
        return scrollingText(textRenderer, maxCharacters, text, progress);
    }

    static String scrollingText(TextRenderer textRenderer, int maxCharacters, String text, double progress) {
        int trimmedLength = textRenderer.getTextHandler().getTrimmedLength(text, maxCharacters, Style.EMPTY);
        int maxTrimOffset = text.length() - trimmedLength;

        double currentTrimOffset = math.lerp(progress, 0, maxTrimOffset);
        int currentTrimOffsetFloored = (int) Math.round(currentTrimOffset);
        return substringSafe(text, currentTrimOffsetFloored, currentTrimOffsetFloored + trimmedLength);
    }
}
