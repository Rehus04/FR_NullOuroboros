package destiny.null_ouroboros.server.terminal.format;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class TerminusTextFormat {
    private static final int TEXT_COLOR = 0xFF0000;

    private TerminusTextFormat() {}

    public static Component parseForDisplay(String raw) {
        if (raw == null || raw.isEmpty()) {
            return Component.literal("").withStyle(Style.EMPTY.withColor(TEXT_COLOR));
        }

        MutableComponent result = Component.empty();
        int index = 0;
        while (index < raw.length()) {
            MarkupMatch match = findNextMatch(raw, index);
            if (match == null) {
                result.append(literal(raw.substring(index)));
                break;
            }

            if (match.openStart > index) {
                result.append(literal(raw.substring(index, match.openStart)));
            }

            result.append(styled(raw.substring(match.contentStart, match.contentEnd), match.style));
            index = match.closeEnd;
        }

        return result;
    }

    private static MarkupMatch findNextMatch(String raw, int from) {
        MarkupMatch best = null;
        for (int i = from; i < raw.length(); i++) {
            MarkupMatch candidate = tryMatchAt(raw, i);
            if (candidate != null && (best == null || candidate.openStart < best.openStart)) {
                best = candidate;
            }
        }
        return best;
    }

    private static MarkupMatch tryMatchAt(String raw, int index) {
        MarkupMatch bold = tryDelimiter(raw, index, "**", Style.EMPTY.withBold(true));
        if (bold != null) return bold;

        MarkupMatch underline = tryDelimiter(raw, index, "__", Style.EMPTY.withUnderlined(true));
        if (underline != null) return underline;

        MarkupMatch strike = tryDelimiter(raw, index, "~~", Style.EMPTY.withStrikethrough(true));
        if (strike != null) return strike;

        return tryDelimiter(raw, index, "*", Style.EMPTY.withItalic(true));
    }

    private static MarkupMatch tryDelimiter(String raw, int index, String delimiter, Style style) {
        if (!raw.startsWith(delimiter, index)) {
            return null;
        }

        int contentStart = index + delimiter.length();
        int closeStart = raw.indexOf(delimiter, contentStart);
        if (closeStart < 0 || closeStart < contentStart) {
            return null;
        }

        return new MarkupMatch(index, contentStart, closeStart, closeStart + delimiter.length(), style);
    }

    private static MutableComponent literal(String text) {
        return Component.literal(text).withStyle(Style.EMPTY.withColor(TEXT_COLOR));
    }

    private static MutableComponent styled(String text, Style style) {
        return Component.literal(text).withStyle(style.withColor(TEXT_COLOR));
    }

    private record MarkupMatch(int openStart, int contentStart, int contentEnd, int closeEnd, Style style) {}
}
