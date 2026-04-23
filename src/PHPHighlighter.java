import java.util.Vector;

public class PHPHighlighter {

    public static final int COLOR_KEYWORD  = 0x569CD6;
    public static final int COLOR_STRING   = 0xCE9178;
    public static final int COLOR_COMMENT  = 0x6A9955;
    public static final int COLOR_VARIABLE = 0x9CDCFE;
    public static final int COLOR_FUNCTION = 0xDCDCAA;
    public static final int COLOR_NUMBER   = 0xB5CEA8;
    public static final int COLOR_TAG      = 0xFF6B6B;
    public static final int COLOR_CONSTANT = 0x4FC1FF;
    public static final int COLOR_OPERATOR = 0xD4D4D4;
    public static final int COLOR_HTML     = 0x808080;

    private static final String[] KEYWORDS = {
        "echo","print","if","else","elseif","endif",
        "for","foreach","while","do","endfor","endwhile",
        "switch","case","break","continue","default",
        "return","function","class","extends","implements",
        "new","this","self","parent","static",
        "public","private","protected","abstract","final",
        "interface","trait","namespace","use",
        "try","catch","finally","throw",
        "include","include_once","require","require_once",
        "null","true","false","NULL","TRUE","FALSE",
        "array","list","isset","unset","empty",
        "die","exit","var_dump","print_r","var_export",
        "define","defined","const","global","match",
        "fn","enum","readonly","never","void","int",
        "string","float","bool","mixed","object"
    };

    private static final String[] FUNCTIONS = {
        // String functions
        "strlen","strpos","strrpos","substr","str_replace",
        "str_split","strtolower","strtoupper","trim","ltrim",
        "rtrim","str_pad","str_repeat","str_word_count",
        "strcmp","strcasecmp","sprintf","printf","number_format",
        "nl2br","wordwrap","chunk_split","md5","sha1",
        "base64_encode","base64_decode","urlencode","urldecode",
        "htmlspecialchars","htmlentities","strip_tags",
        "addslashes","stripslashes","ucfirst","lcfirst","ucwords",
        // Array functions
        "explode","implode","join","array_push","array_pop",
        "array_shift","array_unshift","array_merge","array_keys",
        "array_values","array_unique","array_flip","array_reverse",
        "array_search","array_splice","array_slice","array_chunk",
        "array_combine","array_diff","array_intersect",
        "array_map","array_filter","array_reduce","array_walk",
        "count","in_array","sort","rsort","asort","arsort",
        "ksort","krsort","shuffle","range","compact","extract",
        // Math functions
        "abs","ceil","floor","round","max","min","pow","sqrt",
        "rand","mt_rand","pi","fmod","intdiv","log","exp",
        // Date/Time
        "time","date","mktime","strtotime","microtime",
        "date_create","date_format","checkdate",
        // File functions
        "file_get_contents","file_put_contents","file_exists",
        "fopen","fclose","fread","fwrite","fgets","feof",
        "is_file","is_dir","mkdir","rmdir","unlink","rename",
        "copy","glob","scandir","realpath","basename","dirname",
        // Type functions
        "intval","floatval","strval","boolval",
        "is_array","is_string","is_int","is_float","is_null",
        "is_bool","is_numeric","is_object","is_callable",
        "gettype","settype","cast",
        // DB functions
        "mysqli_connect","mysqli_query","mysqli_fetch_array",
        "mysqli_fetch_assoc","mysqli_close","mysqli_error",
        "mysql_query","mysql_fetch_array","mysql_connect",
        "PDO","json_encode","json_decode","serialize","unserialize",
        // Misc
        "header","ob_start","ob_end_clean","ob_get_contents",
        "session_start","session_destroy","isset","empty",
        "preg_match","preg_replace","preg_split",
        "class_exists","method_exists","property_exists",
        "call_user_func","function_exists","get_class"
    };

    public int[][] highlight(String line) {
        if (line == null || line.length() == 0) return null;

        Vector segments = new Vector();
        String trimmed  = line.trim();

        // Full line comment
        if (trimmed.indexOf("//") == 0 || trimmed.indexOf("#") == 0) {
            segments.addElement(new int[]{0, line.length(), COLOR_COMMENT});
            return toArray(segments);
        }

        // Block comment line
        if (trimmed.indexOf("*") == 0 || trimmed.indexOf("/*") == 0) {
            segments.addElement(new int[]{0, line.length(), COLOR_COMMENT});
            return toArray(segments);
        }

        // PHP tags on their own line
        if (trimmed.equals("<?php") || trimmed.equals("?>")) {
            segments.addElement(new int[]{0, line.length(), COLOR_TAG});
            return toArray(segments);
        }

        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);

            // Whitespace
            if (c == ' ' || c == '\t') { i++; continue; }

            // Inline comment //
            if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/') {
                segments.addElement(new int[]{i, line.length(), COLOR_COMMENT});
                break;
            }

            // Block comment /*
            if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
                int end = line.indexOf("*/", i + 2);
                end = (end == -1) ? line.length() : end + 2;
                segments.addElement(new int[]{i, end, COLOR_COMMENT});
                i = end;
                continue;
            }

            // Comment #
            if (c == '#') {
                segments.addElement(new int[]{i, line.length(), COLOR_COMMENT});
                break;
            }

            // PHP open tag <?php or <?
            if (c == '<' && i + 1 < line.length() && line.charAt(i + 1) == '?') {
                int end = i + 2;
                if (end + 3 <= line.length() &&
                    line.substring(end, end + 3).equals("php")) {
                    end += 3;
                }
                segments.addElement(new int[]{i, end, COLOR_TAG});
                i = end;
                continue;
            }

            // PHP close tag ?>
            if (c == '?' && i + 1 < line.length() && line.charAt(i + 1) == '>') {
                segments.addElement(new int[]{i, i + 2, COLOR_TAG});
                i += 2;
                continue;
            }

            // Double-quoted string
            if (c == '"') {
                int end = findStringEnd(line, i + 1, '"');
                segments.addElement(new int[]{i, end, COLOR_STRING});
                i = end;
                continue;
            }

            // Single-quoted string
            if (c == '\'') {
                int end = findStringEnd(line, i + 1, '\'');
                segments.addElement(new int[]{i, end, COLOR_STRING});
                i = end;
                continue;
            }

            // Heredoc <<<
            if (c == '<' && i + 2 < line.length() &&
                line.charAt(i + 1) == '<' && line.charAt(i + 2) == '<') {
                segments.addElement(new int[]{i, line.length(), COLOR_STRING});
                break;
            }

            // Variable $var
            if (c == '$') {
                int end = i + 1;
                while (end < line.length() && isIdentChar(line.charAt(end))) end++;
                segments.addElement(new int[]{i, end, COLOR_VARIABLE});
                i = end;
                continue;
            }

            // Number (int or float)
            if (isDigit(c) || (c == '-' && i + 1 < line.length() &&
                               isDigit(line.charAt(i + 1)))) {
                int end = i + 1;
                boolean hasDot = false;
                while (end < line.length()) {
                    char nc = line.charAt(end);
                    if (isDigit(nc)) { end++; continue; }
                    if (nc == '.' && !hasDot) { hasDot = true; end++; continue; }
                    if (nc == 'x' || nc == 'X' ||
                       (nc >= 'a' && nc <= 'f') ||
                       (nc >= 'A' && nc <= 'F')) { end++; continue; } // hex
                    break;
                }
                segments.addElement(new int[]{i, end, COLOR_NUMBER});
                i = end;
                continue;
            }

            // Operators
            if (c == '=' || c == '!' || c == '<' || c == '>' ||
                c == '+' || c == '-' || c == '*' || c == '/' ||
                c == '%' || c == '&' || c == '|' || c == '^' ||
                c == '~' || c == '.' || c == '?' || c == ':') {
                int end = i + 1;
                // Two-char operators
                if (end < line.length()) {
                    char nc = line.charAt(end);
                    if ((c == '=' && nc == '=') || (c == '!' && nc == '=') ||
                        (c == '<' && nc == '=') || (c == '>' && nc == '=') ||
                        (c == '+' && nc == '+') || (c == '-' && nc == '-') ||
                        (c == '&' && nc == '&') || (c == '|' && nc == '|') ||
                        (c == '.' && nc == '=') || (c == '-' && nc == '>') ||
                        (c == '=' && nc == '>') || (c == ':' && nc == ':')) {
                        end++;
                        // Three-char ===, !==, <=>
                        if (end < line.length()) {
                            char nnc = line.charAt(end);
                            if ((c == '=' && nc == '=' && nnc == '=') ||
                                (c == '!' && nc == '=' && nnc == '=') ||
                                (c == '<' && nc == '=' && nnc == '>')) {
                                end++;
                            }
                        }
                    }
                }
                segments.addElement(new int[]{i, end, COLOR_OPERATOR});
                i = end;
                continue;
            }

            // Identifier: keyword, function, or constant
            if (isLetter(c) || c == '_') {
                int end = i + 1;
                while (end < line.length() && isIdentChar(line.charAt(end))) end++;
                String word = line.substring(i, end);

                // Check if it's a function call (followed by '(')
                boolean isFunc = false;
                int tempEnd = end;
                while (tempEnd < line.length() && line.charAt(tempEnd) == ' ') tempEnd++;
                if (tempEnd < line.length() && line.charAt(tempEnd) == '(') isFunc = true;

                if (isKeyword(word)) {
                    segments.addElement(new int[]{i, end, COLOR_KEYWORD});
                } else if (isFunc || isBuiltinFunction(word)) {
                    segments.addElement(new int[]{i, end, COLOR_FUNCTION});
                } else if (isConstant(word)) {
                    segments.addElement(new int[]{i, end, COLOR_CONSTANT});
                }

                i = end;
                continue;
            }

            i++;
        }

        return toArray(segments);
    }

    // ========================
    // CLDC-Safe char helpers
    // ========================

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean isUpperCase(char c) {
        return c >= 'A' && c <= 'Z';
    }

    private boolean isLowerCase(char c) {
        return c >= 'a' && c <= 'z';
    }

    private boolean isIdentChar(char c) {
        return isLetter(c) || isDigit(c) || c == '_';
    }

    private int findStringEnd(String line, int start, char quote) {
        for (int i = start; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\\') { i++; continue; }
            if (c == quote) return i + 1;
        }
        return line.length();
    }

    private boolean isKeyword(String word) {
        for (int i = 0; i < KEYWORDS.length; i++) {
            if (KEYWORDS[i].equals(word)) return true;
        }
        return false;
    }

    private boolean isBuiltinFunction(String word) {
        for (int i = 0; i < FUNCTIONS.length; i++) {
            if (FUNCTIONS[i].equals(word)) return true;
        }
        return false;
    }

    private boolean isConstant(String word) {
        if (word.equals("null") || word.equals("true") || word.equals("false")) return true;
        if (word.length() < 2) return false;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (isLetter(c) && isLowerCase(c)) return false;
        }
        return true;
    }

    private int[][] toArray(Vector v) {
        int[][] result = new int[v.size()][];
        for (int i = 0; i < v.size(); i++) {
            result[i] = (int[]) v.elementAt(i);
        }
        return result;
    }
}