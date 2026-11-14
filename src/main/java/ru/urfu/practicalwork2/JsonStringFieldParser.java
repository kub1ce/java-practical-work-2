package ru.urfu.practicalwork2;

import java.io.IOException;

final class JsonStringFieldParser {
    private final String json;
    private int position;

    private JsonStringFieldParser(String json) {
        this.json = json;
    }

    static String extractStringField(String json, String fieldName) throws IOException {
        if (json == null || fieldName == null) {
            throw new IllegalArgumentException("JSON или имя поля null");
        }
        JsonStringFieldParser parser = new JsonStringFieldParser(json);
        return parser.readTopLevelStringField(fieldName);
    }

    private String readTopLevelStringField(String fieldName) throws IOException {
        skipWhitespace();
        expect('{');
        skipWhitespace();

        if (consume('}')) {
            throw new IOException("В json отсутствует поле \"" + fieldName + "\"");
        }

        while (true) {
            skipWhitespace();
            String key = readString();
            skipWhitespace();
            expect(':');
            skipWhitespace();

            if (key.equals(fieldName)) {
                if (position >= json.length() || json.charAt(position) != '"') {
                    throw new IOException("поле \"" + fieldName + "\" должно быть строкой");
                }
                return readString();
            }

            skipValue();
            skipWhitespace();
            if (consume('}')) {
                break;
            }
            expect(',');
        }

        throw new IOException("В json отсутствует поле \"" + fieldName + "\"");
    }

    private void skipValue() throws IOException {
        skipWhitespace();
        if (position >= json.length()) {
            throw new IOException("ошибка");
        }

        char current = json.charAt(position);
        if (current == '"') {
            readString();
        } else if (current == '{') {
            position++;
            skipWhitespace();
            if (consume('}')) {
                return;
            }
            while (true) {
                skipWhitespace();
                readString();
                skipWhitespace();
                expect(':');
                skipValue();
                skipWhitespace();
                if (consume('}')) {
                    return;
                }
                expect(',');
            }
        } else if (current == '[') {
            position++;
            skipWhitespace();
            if (consume(']')) {
                return;
            }
            while (true) {
                skipValue();
                skipWhitespace();
                if (consume(']')) {
                    return;
                }
                expect(',');
            }
        } else {
            int start = position;
            while (position < json.length()) {
                char c = json.charAt(position);
                if (c == ',' || c == '}' || c == ']' || Character.isWhitespace(c)) {
                    break;
                }
                position++;
            }
            if (position == start) {
                throw new IOException("ошибка на позиции " + position);
            }
        }
    }

    private String readString() throws IOException {
        expect('"');
        StringBuilder result = new StringBuilder();
        while (position < json.length()) {
            char current = json.charAt(position++);
            if (current == '"') {
                return result.toString();
            }
            if (current < 0x20) {
                throw new IOException("некорректный символ, ошибка");
            }
            if (current != '\\') {
                result.append(current);
                continue;
            }

            if (position >= json.length()) {
                throw new IOException("некорректная escape-последовательность");
            }
            char escaped = json.charAt(position++);
            switch (escaped) {
                case '"' -> result.append('"');
                case '\\' -> result.append('\\');
                case '/' -> result.append('/');
                case 'b' -> result.append('\b');
                case 'f' -> result.append('\f');
                case 'n' -> result.append('\n');
                case 'r' -> result.append('\r');
                case 't' -> result.append('\t');
                case 'u' -> result.append(readUnicodeEscape());
                default -> throw new IOException("некорректная escape-последовательность: \\" + escaped);
            }
        }
        throw new IOException("Не закрытая строка");
    }

    private char readUnicodeEscape() throws IOException {
        if (position + 4 > json.length()) {
            throw new IOException("некорректная unicode последовательность");
        }
        int code = 0;
        for (int i = 0; i < 4; i++) {
            int digit = Character.digit(json.charAt(position++), 16);
            if (digit < 0) {
                throw new IOException("некорректная unicode последовательность");
            }
            code = (code << 4) | digit;
        }
        return (char) code;
    }

    private void skipWhitespace() {
        while (position < json.length() && Character.isWhitespace(json.charAt(position))) {
            position++;
        }
    }

    private boolean consume(char expected) {
        if (position < json.length() && json.charAt(position) == expected) {
            position++;
            return true;
        }
        return false;
    }

    private void expect(char expected) throws IOException {
        if (!consume(expected)) {
            throw new IOException("expected '" + expected + "' at " + position);
        }
    }
}
