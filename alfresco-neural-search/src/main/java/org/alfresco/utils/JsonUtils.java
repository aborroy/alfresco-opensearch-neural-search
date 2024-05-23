package org.alfresco.utils;

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
}