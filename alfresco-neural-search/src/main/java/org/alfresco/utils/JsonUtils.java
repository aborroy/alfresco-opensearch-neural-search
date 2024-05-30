package org.alfresco.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for JSON-related operations.
 */
public class JsonUtils {

    /**
     * Escapes special characters in a given input string for JSON serialization.
     *
     * @param input the input string to be escaped
     * @return the escaped string
     */
    public static String escape(String input) {
        StringBuilder output = new StringBuilder();

        // Iterate through each character in the input string
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);

            // Escape special characters
            switch (ch) {
                case '\n':
                    output.append("\\n");
                    break;
                case '\t':
                    output.append("\\t");
                    break;
                case '\r':
                    output.append("\\r");
                    break;
                case '\\':
                    output.append("\\\\");
                    break;
                case '"':
                    output.append("\\\"");
                    break;
                case '\b':
                    output.append("\\b");
                    break;
                case '\f':
                    output.append("\\f");
                    break;
                default:
                    // Check if the character is outside the ASCII range
                    if ((int) ch > 127) {
                        // Escape non-ASCII characters using Unicode escape sequence
                        output.append(String.format("\\u%04x", (int) ch));
                    } else {
                        // Append the character as is
                        output.append(ch);
                    }
                    break;
            }
        }

        return output.toString();
    }

    /**
     * Replaces all Unicode escape sequences in the form \\uXXXX within the given text
     * with their corresponding characters.
     *
     * @param text The input string containing Unicode escape sequences.
     * @return A new string with all Unicode escape sequences replaced by their
     *         corresponding characters.
     */
    public static String replaceUnicode(String text) {

        Pattern unicodePattern = Pattern.compile("\\\\u([\\dA-Fa-f]{4})");
        Matcher matcher = unicodePattern.matcher(text);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String unicodeChar = matcher.group(1);
            int charCode = Integer.parseInt(unicodeChar, 16);
            matcher.appendReplacement(result, Character.toString((char) charCode));
        }
        matcher.appendTail(result);

        return result.toString();
    }

}