import javax.microedition.lcdui.*;
import java.util.Vector;

public class EditorScreen extends Canvas implements CommandListener {

    private PHPIde midlet;
    private PHPHighlighter highlighter;
    private FileManager fileManager;

    // Editor state
    private StringBuffer code;
    private StringBuffer undoBuffer;
    private int cursorPos;
    private int scrollY;
    private int scrollX;
    private boolean wordWrap = false;
    private boolean showLineNum = true;
    private String currentFile = null;
    private boolean isModified = false;
    private boolean cursorBlink = true;
    private long lastBlinkTime = 0;

    // Search
    private String searchQuery = "";
    private int searchPos = 0;

    // Leaderboard
    private int[] scores = new int[5];
    private String[] scoreFiles = new String[5];
    private int scoreCount = 0;

    // UI
    private Font codeFont;
    private Font boldFont;
    private Font smallFont;
    private int lineHeight;
    private int screenWidth;
    private int screenHeight;
    private int lineNumWidth = 32;

    // Colors
    private static final int COLOR_BG = 0x1E1E1E;
    private static final int COLOR_TEXT = 0xD4D4D4;
    private static final int COLOR_CURSOR = 0xFFFFFF;
    private static final int COLOR_LINE_NUM = 0x858585;
    private static final int COLOR_LINE_NUM_BG = 0x252526;
    private static final int COLOR_CURRENT_LINE = 0x2A2A2A;
    private static final int COLOR_TITLE_BG = 0x007ACC;
    private static final int COLOR_STATUS_BG = 0x007ACC;
    private static final int COLOR_SEARCH_HL = 0x515C6A;

    // Snippets
    private static final String[] SNIPPET_NAMES = {
        "PHP Tags", "Echo", "If/Else", "For Loop", "Foreach",
        "While", "Function", "Class", "Array", "Switch/Case",
        "Try/Catch", "Include", "PHP+HTML Page", "DB Connect",
        "POST Form", "GET Param", "Session", "JSON Response",
        "File Read", "Regex"
    };

    private static final String[] SNIPPETS = {
        "<?php\n\n?>",
        "echo \"Hello\";\n",
        "if ($x > 0) {\n\techo \"yes\";\n} else {\n\techo \"no\";\n}",
        "for ($i = 0; $i < 10; $i++) {\n\techo $i;\n}",
        "foreach ($array as $key => $value) {\n\techo $value;\n}",
        "while ($x < 10) {\n\t$x++;\n}",
        "function myFunc($param) {\n\treturn $param * 2;\n}",
        "class MyClass {\n\tprivate $name;\n\n\tpublic function __construct($name) {\n\t\t$this->name = $name;\n\t}\n\n\tpublic function getName() {\n\t\treturn $this->name;\n\t}\n}",
        "$array = array(\n\t'name' => 'John',\n\t'age' => 25\n);",
        "switch ($var) {\n\tcase 1:\n\t\techo \"One\";\n\t\tbreak;\n\tcase 2:\n\t\techo \"Two\";\n\t\tbreak;\n\tdefault:\n\t\techo \"Other\";\n}",
        "try {\n\t// code\n} catch (Exception $e) {\n\techo $e->getMessage();\n}",
        "include 'header.php';\nrequire_once 'config.php';",
        "<!DOCTYPE html>\n<html>\n<head>\n\t<title>PHP Page</title>\n</head>\n<body>\n<?php\n\techo \"Hello World\";\n?>\n</body>\n</html>",
        "$conn = new mysqli('localhost', 'user', 'pass', 'db');\nif ($conn->connect_error) {\n\tdie('Error: ' . $conn->connect_error);\n}\n$result = $conn->query(\"SELECT * FROM users\");",
        "if ($_SERVER['REQUEST_METHOD'] === 'POST') {\n\t$name = $_POST['name'];\n\t$email = $_POST['email'];\n}",
        "$id = isset($_GET['id']) ? $_GET['id'] : 0;",
        "session_start();\n$_SESSION['user'] = 'admin';\necho $_SESSION['user'];",
        "header('Content-Type: application/json');\necho json_encode(array(\n\t'status' => 'success',\n\t'data' => $result\n));",
        "$content = file_get_contents('data.txt');\nfile_put_contents('output.txt', $content);",
        "if (preg_match('/^[a-z]+$/', $str)) {\n\techo \"Match found\";\n}"
    };

    // Commands
    private Command cmdType, cmdSnippet, cmdNew, cmdOpen, cmdSave;
    private Command cmdSaveAs, cmdBrowse, cmdFind, cmdFindNext;
    private Command cmdGoLine, cmdUndo, cmdWordWrap, cmdCheck;
    private Command cmdScores, cmdRun, cmdExit;

    // ========================
    // Constructor
    // ========================

    public EditorScreen(PHPIde midlet) {
        this.midlet = midlet;
        this.highlighter = new PHPHighlighter();
        this.fileManager = new FileManager();
        this.undoBuffer = new StringBuffer();

        code = new StringBuffer("<?php\n\t// PHP IDE for J2ME\n\techo \"Hello World!\";\n?>");
        cursorPos = code.length();
        scrollY = 0;
        scrollX = 0;

        codeFont = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        boldFont = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_SMALL);
        smallFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);

        lineHeight = codeFont.getHeight() + 2;

        buildCommands();
        fileManager.detectBestRoot();
    }

    private void buildCommands() {
        cmdType = new Command("Type", Command.SCREEN, 1);
        cmdSnippet = new Command("Snippet", Command.SCREEN, 2);
        cmdNew = new Command("New", Command.SCREEN, 3);
        cmdOpen = new Command("Open", Command.SCREEN, 4);
        cmdSave = new Command("Save", Command.SCREEN, 5);
        cmdSaveAs = new Command("Save As", Command.SCREEN, 6);
        cmdBrowse = new Command("Browse", Command.SCREEN, 7);
        cmdFind = new Command("Find", Command.SCREEN, 8);
        cmdFindNext = new Command("Find Next", Command.SCREEN, 9);
        cmdGoLine = new Command("Go To Line", Command.SCREEN, 10);
        cmdUndo = new Command("Undo", Command.SCREEN, 11);
        cmdWordWrap = new Command("Word Wrap", Command.SCREEN, 12);
        cmdCheck = new Command("Check Syntax", Command.SCREEN, 13);
        cmdRun = new Command("Run PHP", Command.SCREEN, 14);
        cmdScores = new Command("Scores", Command.SCREEN, 15);
        cmdExit = new Command("Exit", Command.EXIT, 99);

        addCommand(cmdType);
        addCommand(cmdSnippet);
        addCommand(cmdNew);
        addCommand(cmdOpen);
        addCommand(cmdSave);
        addCommand(cmdSaveAs);
        addCommand(cmdBrowse);
        addCommand(cmdFind);
        addCommand(cmdFindNext);
        addCommand(cmdGoLine);
        addCommand(cmdUndo);
        addCommand(cmdWordWrap);
        addCommand(cmdCheck);
        addCommand(cmdRun);
        addCommand(cmdScores);
        addCommand(cmdExit);

        setCommandListener(this);
    }

    // ========================
    // Paint
    // ========================

    protected void paint(Graphics g) {
        screenWidth = getWidth();
        screenHeight = getHeight();

        long now = System.currentTimeMillis();
        if (now - lastBlinkTime > 500) {
            cursorBlink = !cursorBlink;
            lastBlinkTime = now;
        }

        g.setColor(COLOR_BG);
        g.fillRect(0, 0, screenWidth, screenHeight);

        drawEditor(g);
        drawTitleBar(g);
        drawStatusBar(g);
    }

    private void drawTitleBar(Graphics g) {
        g.setColor(COLOR_TITLE_BG);
        g.fillRect(0, 0, screenWidth, lineHeight + 4);
        g.setFont(boldFont);
        g.setColor(0xFFFFFF);

        String fname = (currentFile != null)
            ? fileManager.getFileName(currentFile) : "untitled.php";
        String title = "PHP > " + fname + (isModified ? " *" : "");
        g.drawString(title, 4, 2, Graphics.TOP | Graphics.LEFT);
    }

    private void drawStatusBar(Graphics g) {
        int statusY = screenHeight - lineHeight - 2;
        g.setColor(COLOR_STATUS_BG);
        g.fillRect(0, statusY, screenWidth, lineHeight + 2);
        g.setFont(smallFont);
        g.setColor(0xFFFFFF);

        int ln = getCurrentLine() + 1;
        int col = getCursorColumn() + 1;
        int total = getTotalLines();

        String left = "Ln:" + ln + " Col:" + col + " | " + total + "L";
        String right = code.length() + " chars";

        g.drawString(left, 4, statusY + 1, Graphics.TOP | Graphics.LEFT);
        g.drawString(right, screenWidth - 4, statusY + 1, Graphics.TOP | Graphics.RIGHT);
    }

    private void drawEditor(Graphics g) {
        String fullCode = code.toString();
        String[] lines = splitLines(fullCode);
        int titleH = lineHeight + 4;
        int statusH = lineHeight + 2;
        int editorH = screenHeight - titleH - statusH;
        int startLine = scrollY / lineHeight;
        int visLines = (editorH / lineHeight) + 1;
        int currentLn = getCurrentLine();

        g.setColor(COLOR_LINE_NUM_BG);
        g.fillRect(0, titleH, lineNumWidth, editorH);

        g.setColor(0x3C3C3C);
        g.drawLine(lineNumWidth, titleH, lineNumWidth, titleH + editorH);

        for (int i = startLine; i < lines.length && i < startLine + visLines; i++) {
            int y = titleH + (i - startLine) * lineHeight;

            if (i == currentLn) {
                g.setColor(COLOR_CURRENT_LINE);
                g.fillRect(lineNumWidth + 1, y, screenWidth - lineNumWidth - 1, lineHeight);
            }

            if (showLineNum) {
                g.setFont(codeFont);
                g.setColor((i == currentLn) ? 0xC6C6C6 : COLOR_LINE_NUM);
                g.drawString(String.valueOf(i + 1), lineNumWidth - 2, y,
                    Graphics.TOP | Graphics.RIGHT);
            }

            drawSearchHighlight(g, lines[i], i, y);
            drawHighlightedLine(g, lines[i], lineNumWidth + 2 - scrollX, y);
        }

        if (cursorBlink) {
            drawCursor(g, lines, currentLn, startLine, titleH);
        }
    }

    private void drawSearchHighlight(Graphics g, String line, int lineIdx, int y) {
        if (searchQuery == null || searchQuery.length() == 0) return;
        String lower = toLowerCase(line);
        String queryLow = toLowerCase(searchQuery);
        int idx = 0;

        while (true) {
            int found = lower.indexOf(queryLow, idx);
            if (found == -1) break;
            int x = lineNumWidth + 2 - scrollX +
                codeFont.stringWidth(line.substring(0, found));
            int w = codeFont.stringWidth(searchQuery);
            g.setColor(COLOR_SEARCH_HL);
            g.fillRect(x, y, w, lineHeight);
            idx = found + 1;
        }
    }

    private void drawHighlightedLine(Graphics g, String line, int x, int y) {
        int[][] segments = highlighter.highlight(line);
        g.setFont(codeFont);

        if (segments == null || segments.length == 0) {
            g.setColor(COLOR_TEXT);
            if (line.length() > 0) g.drawString(line, x, y, Graphics.TOP | Graphics.LEFT);
            return;
        }

        int drawX = x;
        int covered = 0;

        for (int i = 0; i < segments.length; i++) {
            int start = segments[i][0];
            int end = segments[i][1];
            int color = segments[i][2];

            if (start > covered && covered < line.length()) {
                int gEnd = Math.min(start, line.length());
                String gap = line.substring(covered, gEnd);
                g.setColor(COLOR_TEXT);
                g.drawString(gap, drawX, y, Graphics.TOP | Graphics.LEFT);
                drawX += codeFont.stringWidth(gap);
            }

            if (start < line.length() && end <= line.length()) {
                String seg = line.substring(start, end);
                g.setColor(color);
                g.drawString(seg, drawX, y, Graphics.TOP | Graphics.LEFT);
                drawX += codeFont.stringWidth(seg);
                covered = end;
            }
        }

        if (covered < line.length()) {
            g.setColor(COLOR_TEXT);
            g.drawString(line.substring(covered), drawX, y, Graphics.TOP | Graphics.LEFT);
        }
    }

    private void drawCursor(Graphics g, String[] lines, int currentLn,
                             int startLine, int titleH) {
        int lineStart = getLineStart(currentLn);
        int posInLine = cursorPos - lineStart;
        if (posInLine < 0) posInLine = 0;

        String lineText = (currentLn < lines.length) ? lines[currentLn] : "";
        if (posInLine > lineText.length()) posInLine = lineText.length();

        String before = lineText.substring(0, posInLine);
        int cursorX = lineNumWidth + 2 - scrollX + codeFont.stringWidth(before);
        int cursorY = titleH + (currentLn - startLine) * lineHeight;

        g.setColor(COLOR_CURSOR);
        g.fillRect(cursorX, cursorY, 2, lineHeight - 1);
    }

    // ========================
    // Key Handling
    // ========================

    protected void keyPressed(int keyCode) {
        int action = getGameAction(keyCode);

        if (action == UP) {
            moveCursorUp();
            adjustScroll();
            repaint();
            return;
        }
        if (action == DOWN) {
            moveCursorDown();
            adjustScroll();
            repaint();
            return;
        }
        if (action == LEFT) {
            if (cursorPos > 0) {
                cursorPos--;
                adjustScroll();
                repaint();
            }
            return;
        }
        if (action == RIGHT) {
            if (cursorPos < code.length()) {
                cursorPos++;
                adjustScroll();
                repaint();
            }
            return;
        }
        if (action == FIRE) {
            showInputDialog();
            return;
        }

        switch (keyCode) {
            case KEY_NUM0:
                insertWithIndent();
                break;
            case KEY_NUM1:
                cursorPos = 0;
                scrollY = 0;
                scrollX = 0;
                break;
            case KEY_NUM2:
                moveCursorUp();
                break;
            case KEY_NUM3:
                cursorPos = code.length();
                break;
            case KEY_NUM4:
                if (cursorPos > 0) cursorPos--;
                break;
            case KEY_NUM5:
                showInputDialog();
                return;
            case KEY_NUM6:
                if (cursorPos < code.length()) cursorPos++;
                break;
            case KEY_NUM7:
                if (cursorPos > 0) {
                    saveUndo();
                    code.deleteCharAt(cursorPos - 1);
                    cursorPos--;
                    isModified = true;
                }
                break;
            case KEY_NUM8:
                moveCursorDown();
                break;
            case KEY_NUM9:
                if (cursorPos < code.length()) {
                    saveUndo();
                    code.deleteCharAt(cursorPos);
                    isModified = true;
                }
                break;
            case KEY_STAR:
                insertText("\t");
                break;
            case KEY_POUND:
                insertText("\n");
                break;
        }

        adjustScroll();
        repaint();
    }

    protected void keyRepeated(int keyCode) {
        keyPressed(keyCode);
    }

    private void insertText(String text) {
        saveUndo();
        code.insert(cursorPos, text);
        cursorPos += text.length();
        isModified = true;
    }

    private void insertWithIndent() {
        String lineText = getCurrentLineText();
        StringBuffer indent = new StringBuffer("\n");
        for (int i = 0; i < lineText.length(); i++) {
            char c = lineText.charAt(i);
            if (c == ' ' || c == '\t') indent.append(c);
            else break;
        }
        String trim = lineText.trim();
        if (trim.length() > 0 && trim.charAt(trim.length() - 1) == '{') {
            indent.append('\t');
        }
        insertText(indent.toString());
    }

    private void moveCursorUp() {
        int col = getCursorColumn();
        int lineStart = getLineStart(getCurrentLine());
        if (lineStart == 0) return;

        int prevEnd = lineStart - 1;
        String text = code.toString();
        int prevStart = text.lastIndexOf('\n', prevEnd - 1);
        prevStart = (prevStart == -1) ? 0 : prevStart + 1;
        int prevLen = prevEnd - prevStart;
        cursorPos = prevStart + Math.min(col, prevLen);
    }

    private void moveCursorDown() {
        int col = getCursorColumn();
        String text = code.toString();
        int nextNl = text.indexOf('\n', cursorPos);
        if (nextNl == -1) return;

        int nextStart = nextNl + 1;
        int nextNl2 = text.indexOf('\n', nextStart);
        int nextLen = (nextNl2 == -1) ? text.length() - nextStart : nextNl2 - nextStart;
        cursorPos = nextStart + Math.min(col, nextLen);
    }

    private void adjustScroll() {
        int titleH = lineHeight + 4;
        int statusH = lineHeight + 2;
        int editorH = screenHeight - titleH - statusH;
        int currentLn = getCurrentLine();
        int cursorY = currentLn * lineHeight;

        if (cursorY < scrollY) {
            scrollY = Math.max(0, cursorY - lineHeight);
        } else if (cursorY + lineHeight > scrollY + editorH) {
            scrollY = cursorY + lineHeight - editorH;
        }

        String lineText = getCurrentLineText();
        int posInLine = getCursorColumn();
        if (posInLine > lineText.length()) posInLine = lineText.length();
        String before = lineText.substring(0, posInLine);
        int cursorX = lineNumWidth + 2 + codeFont.stringWidth(before);
        int maxX = screenWidth - 4;

        if (cursorX > scrollX + maxX) {
            scrollX = cursorX - maxX;
        } else if (cursorX < scrollX + lineNumWidth + 4) {
            scrollX = Math.max(0, cursorX - lineNumWidth - 4);
        }
    }

    // ========================
    // FIXED: All Dialogs with 'final'
    // ========================

    private void showInputDialog() {
        String currentLineText = getCurrentLineText();
        
        final TextBox tb = new TextBox("Edit Line " + (getCurrentLine() + 1),
            currentLineText, 500, TextField.ANY);
        
        final Command cmdInsert = new Command("Insert", Command.SCREEN, 1);
        final Command cmdReplace = new Command("Replace Line", Command.SCREEN, 2);
        final Command cmdCancel = new Command("Cancel", Command.CANCEL, 3);
        
        tb.addCommand(cmdInsert);
        tb.addCommand(cmdReplace);
        tb.addCommand(cmdCancel);
        
        tb.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c == cmdInsert) {
                    String typed = ((TextBox) d).getString();
                    if (typed != null && typed.length() > 0) {
                        insertText(typed);
                        adjustScroll();
                    }
                } else if (c == cmdReplace) {
                    String typed = ((TextBox) d).getString();
                    saveUndo();
                    int lineStart = getLineStart(getCurrentLine());
                    int lineEnd = code.toString().indexOf('\n', lineStart);
                    if (lineEnd == -1) lineEnd = code.length();
                    
                    for (int i = lineStart; i < lineEnd && lineStart < code.length(); i++) {
                        code.deleteCharAt(lineStart);
                    }
                    
                    code.insert(lineStart, typed);
                    cursorPos = lineStart + typed.length();
                    isModified = true;
                    adjustScroll();
                }
                midlet.getDisplay().setCurrent(EditorScreen.this);
                repaint();
            }
        });
        midlet.getDisplay().setCurrent(tb);
    }

    // ========================
    // PHP Runner
    // ========================

    private void runPHP() {
        String phpCode = code.toString();
        final String output = executePHP(phpCode);
        
        final Form resultForm = new Form("PHP Output");
        resultForm.append(new StringItem("", output));
        
        final Command cmdBack = new Command("Back", Command.BACK, 1);
        final Command cmdSave = new Command("Save Output", Command.SCREEN, 2);
        resultForm.addCommand(cmdBack);
        resultForm.addCommand(cmdSave);
        
        resultForm.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c == cmdBack) {
                    midlet.getDisplay().setCurrent(EditorScreen.this);
                } else if (c == cmdSave) {
                    saveOutput(output);
                    midlet.getDisplay().setCurrent(EditorScreen.this);
                }
            }
        });
        
        midlet.getDisplay().setCurrent(resultForm);
    }

    private String executePHP(String phpCode) {
        StringBuffer output = new StringBuffer();
        output.append("=== PHP Output ===\n\n");

        try {
            String cleanCode = phpCode;
            cleanCode = strReplace(cleanCode, "<?php", "");
            cleanCode = strReplace(cleanCode, "?>", "");
            
            String[] lines = splitLines(cleanCode);
            
            String[] varNames = new String[20];
            String[] varValues = new String[20];
            int varCount = 0;
            
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                
                if (line.length() == 0) continue;
                if (line.indexOf("//") == 0) continue;
                if (line.indexOf("#") == 0) continue;
                if (line.indexOf("/*") != -1) continue;
                
                if (line.indexOf("echo ") != -1) {
                    String echoContent = extractEchoContent(line);
                    for (int v = 0; v < varCount; v++) {
                        echoContent = strReplace(echoContent, varNames[v], varValues[v]);
                    }
                    output.append(echoContent);
                    output.append("\n");
                }
                
                else if (line.indexOf("print ") != -1) {
                    String printContent = extractPrintContent(line);
                    for (int v = 0; v < varCount; v++) {
                        printContent = strReplace(printContent, varNames[v], varValues[v]);
                    }
                    output.append(printContent);
                    output.append("\n");
                }
                
                else if (line.indexOf("$") != -1 && line.indexOf("=") != -1) {
                    int dollarPos = line.indexOf("$");
                    int equalsPos = line.indexOf("=");
                    if (dollarPos < equalsPos) {
                        String varName = extractVarName(line, dollarPos);
                        String varValue = extractVarValue(line, equalsPos);
                        
                        boolean found = false;
                        for (int v = 0; v < varCount; v++) {
                            if (varNames[v].equals(varName)) {
                                varValues[v] = varValue;
                                found = true;
                                break;
                            }
                        }
                        if (!found && varCount < 20) {
                            varNames[varCount] = varName;
                            varValues[varCount] = varValue;
                            varCount++;
                        }
                    }
                }
                
                else if (line.indexOf("var_dump(") != -1) {
                    output.append("[var_dump]\n");
                    for (int v = 0; v < varCount; v++) {
                        output.append(varNames[v] + " = " + varValues[v] + "\n");
                    }
                }
                
                else if (line.indexOf("print_r(") != -1) {
                    output.append("[print_r]\n");
                    for (int v = 0; v < varCount; v++) {
                        output.append(varNames[v] + " => " + varValues[v] + "\n");
                    }
                }
            }
            
            if (output.length() <= 20) {
                output.append("(No output)\n\n");
                output.append("Variables: " + varCount + "\n");
                output.append("Lines: " + lines.length);
            }
            
        } catch (Exception e) {
            output.append("ERROR: " + e.toString());
        }
        
        return output.toString();
    }

    private String extractEchoContent(String line) {
        int start = line.indexOf("echo ") + 5;
        int end = line.indexOf(";", start);
        if (end == -1) end = line.length();
        String content = line.substring(start, end).trim();
        
        if (content.length() > 0 && content.charAt(0) == '"') content = content.substring(1);
        if (content.length() > 0 && content.charAt(content.length() - 1) == '"')
            content = content.substring(0, content.length() - 1);
        if (content.length() > 0 && content.charAt(0) == '\'') content = content.substring(1);
        if (content.length() > 0 && content.charAt(content.length() - 1) == '\'')
            content = content.substring(0, content.length() - 1);
        
        return content;
    }

    private String extractPrintContent(String line) {
        int start = line.indexOf("print ") + 6;
        int end = line.indexOf(";", start);
        if (end == -1) end = line.length();
        String content = line.substring(start, end).trim();
        
        if (content.length() > 0 && content.charAt(0) == '"') content = content.substring(1);
        if (content.length() > 0 && content.charAt(content.length() - 1) == '"')
            content = content.substring(0, content.length() - 1);
        if (content.length() > 0 && content.charAt(0) == '\'') content = content.substring(1);
        if (content.length() > 0 && content.charAt(content.length() - 1) == '\'')
            content = content.substring(0, content.length() - 1);
        
        return content;
    }

    private String extractVarName(String line, int start) {
        int end = start + 1;
        while (end < line.length()) {
            char c = line.charAt(end);
            if (!isIdentChar(c)) break;
            end++;
        }
        return line.substring(start, end);
    }

    private String extractVarValue(String line, int equalsPos) {
        int start = equalsPos + 1;
        int end = line.indexOf(";", start);
        if (end == -1) end = line.length();
        String value = line.substring(start, end).trim();
        
        if (value.length() > 0 && value.charAt(0) == '"') value = value.substring(1);
        if (value.length() > 0 && value.charAt(value.length() - 1) == '"')
            value = value.substring(0, value.length() - 1);
        if (value.length() > 0 && value.charAt(0) == '\'') value = value.substring(1);
        if (value.length() > 0 && value.charAt(value.length() - 1) == '\'')
            value = value.substring(0, value.length() - 1);
        
        return value;
    }

    private boolean isIdentChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') ||
               (c >= '0' && c <= '9') || c == '_';
    }

    private void saveOutput(String output) {
        try {
            String url = fileManager.buildSavePath("output.txt");
            if (url != null) {
                fileManager.writeFile(url, output);
                showToast("Saved: output.txt");
            }
        } catch (Exception e) {
            showToast("Save error: " + e.getMessage());
        }
    }

    // ========================
    // Undo & Search
    // ========================

    private void saveUndo() {
        undoBuffer = new StringBuffer(code.toString());
    }

    private void performUndo() {
        if (undoBuffer.length() > 0) {
            StringBuffer tmp = code;
            code = undoBuffer;
            undoBuffer = tmp;
            cursorPos = Math.min(cursorPos, code.length());
            isModified = true;
        }
    }

    private void findNext() {
        if (searchQuery == null || searchQuery.length() == 0) {
            showFindDialog();
            return;
        }
        String text = toLowerCase(code.toString());
        String query = toLowerCase(searchQuery);
        int from = searchPos + 1;
        if (from >= text.length()) from = 0;

        int found = text.indexOf(query, from);
        if (found == -1 && from > 0) {
            found = text.indexOf(query, 0);
        }

        if (found != -1) {
            cursorPos = found + searchQuery.length();
            searchPos = found;
            adjustScroll();
            repaint();
            showToast("Found at line " + (getCurrentLine() + 1));
        } else {
            showToast("Not found: " + searchQuery);
        }
    }

    // ========================
    // File Operations
    // ========================

    private void doSave() {
        if (currentFile == null) {
            doSaveAs();
            return;
        }
        try {
            fileManager.writeFile(currentFile, code.toString());
            isModified = false;
            showToast("Saved: " + fileManager.getFileName(currentFile));
        } catch (Exception e) {
            showToast("Save error: " + e.getMessage());
        }
        repaint();
    }

    private void doSaveAs() {
        final TextBox tb = new TextBox("Save As", "myfile.php", 64, TextField.ANY);
        final Command ok = new Command("Save", Command.OK, 1);
        final Command bk = new Command("Back", Command.CANCEL, 2);
        tb.addCommand(ok);
        tb.addCommand(bk);
        tb.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c == ok) {
                    String fname = ((TextBox) d).getString().trim();
                    if (fname.length() > 0) {
                        String path = fileManager.buildSavePath(fname);
                        if (path != null) {
                            currentFile = path;
                            doSave();
                        } else {
                            showToast("No storage");
                        }
                    }
                }
                midlet.getDisplay().setCurrent(EditorScreen.this);
                repaint();
            }
        });
        midlet.getDisplay().setCurrent(tb);
    }

    private void doBrowse() {
        String dir = fileManager.getPhpDir();
        if (dir == null) {
            showToast("No file system");
            return;
        }
        showFileBrowser(dir);
    }

    private void showFileBrowser(final String dir) {
        String[] files = fileManager.listAllFiles(dir);
        final List list = new List("Browse: Php/", List.IMPLICIT);
        list.append("[..] Up", null);

        for (int i = 0; i < files.length; i++) {
            list.append(fileManager.getFileName(files[i]), null);
        }

        final String[] filesCopy = files;
        final Command back = new Command("Back", Command.BACK, 1);
        list.addCommand(back);
        list.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                int idx = list.getSelectedIndex();
                if (c == back || idx == 0) {
                    midlet.getDisplay().setCurrent(EditorScreen.this);
                    return;
                }
                int fileIdx = idx - 1;
                if (fileIdx >= 0 && fileIdx < filesCopy.length) {
                    doOpenFile(filesCopy[fileIdx]);
                } else {
                    midlet.getDisplay().setCurrent(EditorScreen.this);
                }
            }
        });
        midlet.getDisplay().setCurrent(list);
    }

    private void doOpenFile(final String url) {
        final Alert confirm = new Alert("Open",
            "Open: " + fileManager.getFileName(url) + "?",
            null, AlertType.CONFIRMATION);
        final Command yes = new Command("Open", Command.OK, 1);
        final Command no = new Command("No", Command.CANCEL, 2);
        confirm.addCommand(yes);
        confirm.addCommand(no);
        confirm.setTimeout(Alert.FOREVER);
        confirm.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c == yes) {
                    loadFile(url);
                }
                midlet.getDisplay().setCurrent(EditorScreen.this);
                repaint();
            }
        });
        midlet.getDisplay().setCurrent(confirm);
    }

    private void loadFile(String url) {
        try {
            String content = fileManager.readFile(url);
            saveUndo();
            code = new StringBuffer(content);
            cursorPos = 0;
            scrollY = 0;
            scrollX = 0;
            currentFile = url;
            isModified = false;
            repaint();
            showToast("Opened: " + fileManager.getFileName(url));
        } catch (Exception e) {
            showToast("Open error: " + e.getMessage());
        }
    }

    // ========================
    // More Dialogs
    // ========================

    private void showSnippetList() {
        final List sl = new List("PHP Snippets", List.IMPLICIT);
        final Command bk = new Command("Back", Command.BACK, 1);
        for (int i = 0; i < SNIPPET_NAMES.length; i++) {
            sl.append(SNIPPET_NAMES[i], null);
        }
        sl.addCommand(bk);
        sl.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c != bk) {
                    int idx = ((List) d).getSelectedIndex();
                    if (idx >= 0 && idx < SNIPPETS.length) {
                        insertText(SNIPPETS[idx]);
                        adjustScroll();
                    }
                }
                midlet.getDisplay().setCurrent(EditorScreen.this);
                repaint();
            }
        });
        midlet.getDisplay().setCurrent(sl);
    }

    private void showNewDialog() {
        final Alert a = new Alert("New", "Create new? Unsaved changes will be lost.",
            null, AlertType.CONFIRMATION);
        final Command yes = new Command("Yes", Command.OK, 1);
        final Command no = new Command("No", Command.CANCEL, 2);
        a.addCommand(yes);
        a.addCommand(no);
        a.setTimeout(Alert.FOREVER);
        a.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c == yes) {
                    saveUndo();
                    code = new StringBuffer("<?php\n\t\n?>");
                    cursorPos = 7;
                    scrollY = 0;
                    scrollX = 0;
                    currentFile = null;
                    isModified = false;
                }
                midlet.getDisplay().setCurrent(EditorScreen.this);
                repaint();
            }
        });
        midlet.getDisplay().setCurrent(a);
    }

    private void showFindDialog() {
        final TextBox tb = new TextBox("Find", searchQuery, 100, TextField.ANY);
        final Command ok = new Command("Find", Command.OK, 1);
        final Command bk = new Command("Cancel", Command.CANCEL, 2);
        tb.addCommand(ok);
        tb.addCommand(bk);
        tb.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c == ok) {
                    searchQuery = ((TextBox) d).getString();
                    searchPos = cursorPos;
                    midlet.getDisplay().setCurrent(EditorScreen.this);
                    findNext();
                } else {
                    midlet.getDisplay().setCurrent(EditorScreen.this);
                }
                repaint();
            }
        });
        midlet.getDisplay().setCurrent(tb);
    }

    private void showGoToLineDialog() {
        final TextBox tb = new TextBox("Go To Line", "1", 6, TextField.NUMERIC);
        final Command ok = new Command("Go", Command.OK, 1);
        final Command bk = new Command("Cancel", Command.CANCEL, 2);
        tb.addCommand(ok);
        tb.addCommand(bk);
        tb.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c == ok) {
                    try {
                        int lineNum = Integer.parseInt(((TextBox) d).getString().trim()) - 1;
                        if (lineNum < 0) lineNum = 0;
                        cursorPos = getLineStart(lineNum);
                        if (cursorPos > code.length()) cursorPos = code.length();
                        adjustScroll();
                        repaint();
                    } catch (Exception e) {}
                }
                midlet.getDisplay().setCurrent(EditorScreen.this);
            }
        });
        midlet.getDisplay().setCurrent(tb);
    }

    private void showSyntaxCheckDialog() {
        String result = checkPHPSyntaxInternal(code.toString());
        int score = calculateScore(code.toString());
        addScore(score);

        String full = result + "\n\n--- Score: " + score + "/200 ---\n" +
            getScoreGrade(score);

        final Alert a = new Alert("Check", full, null,
            (result.indexOf("OK") == 0) ? AlertType.INFO : AlertType.ERROR);
        a.setTimeout(Alert.FOREVER);
        final Command ok = new Command("OK", Command.OK, 1);
        a.addCommand(ok);
        a.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                midlet.getDisplay().setCurrent(EditorScreen.this);
            }
        });
        midlet.getDisplay().setCurrent(a);
    }

    private String checkPHPSyntaxInternal(String phpCode) {
        StringBuffer errors = new StringBuffer();

        if (phpCode.indexOf("<?php") == -1 && phpCode.indexOf("<?") == -1) {
            errors.append("- Missing PHP tag\n");
        }

        int ob = countChar(phpCode, '{');
        int cb = countChar(phpCode, '}');
        if (ob != cb) errors.append("- Braces: " + ob + "{ " + cb + "}\n");

        int op = countChar(phpCode, '(');
        int cp = countChar(phpCode, ')');
        if (op != cp) errors.append("- Parentheses mismatch\n");

        int sq = countChar(phpCode, '\'');
        int dq = countChar(phpCode, '"');
        if (sq % 2 != 0) errors.append("- Unclosed '\n");
        if (dq % 2 != 0) errors.append("- Unclosed \"\n");

        if (errors.length() == 0) {
            return "OK - No errors!\n\nLines: " + getTotalLines() +
                "\nChars: " + code.length();
        }
        return "Errors:\n" + errors.toString();
    }

    private int calculateScore(String phpCode) {
        int score = 0;
        int lines = getTotalLines();
        score += Math.min(lines * 2, 100);

        String result = checkPHPSyntaxInternal(phpCode);
        if (result.indexOf("OK") == 0) score += 50;

        if (phpCode.indexOf("function ") != -1) score += 10;
        if (phpCode.indexOf("class ") != -1) score += 15;
        if (phpCode.indexOf("//") != -1) score += 5;
        if (phpCode.indexOf("<?php") != -1) score += 5;

        return Math.max(0, Math.min(score, 200));
    }

    private void addScore(int score) {
        String fname = (currentFile != null) ?
            fileManager.getFileName(currentFile) : "untitled.php";

        if (scoreCount < 5 || score > scores[scoreCount - 1]) {
            int pos = scoreCount < 5 ? scoreCount : 4;
            scores[pos] = score;
            scoreFiles[pos] = fname;
            scoreCount = Math.min(scoreCount + 1, 5);

            for (int i = scoreCount - 1; i > 0; i--) {
                if (scores[i] > scores[i - 1]) {
                    int tmpS = scores[i];
                    String tmpF = scoreFiles[i];
                    scores[i] = scores[i - 1];
                    scoreFiles[i] = scoreFiles[i - 1];
                    scores[i - 1] = tmpS;
                    scoreFiles[i - 1] = tmpF;
                }
            }
        }
    }

    private void showLeaderboard() {
        StringBuffer sb = new StringBuffer();
        sb.append("=== Scores ===\n\n");

        if (scoreCount == 0) {
            sb.append("No scores yet.");
        } else {
            for (int i = 0; i < scoreCount; i++) {
                sb.append((i + 1) + ". " + scoreFiles[i] + "\n");
                sb.append("   " + scores[i] + "/200 - " + getScoreGrade(scores[i]) + "\n\n");
            }
        }

        final Alert a = new Alert("Leaderboard", sb.toString(), null, AlertType.INFO);
        a.setTimeout(Alert.FOREVER);
        final Command cmdOk = new Command("OK", Command.OK, 1);
        a.addCommand(cmdOk);
        a.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                midlet.getDisplay().setCurrent(EditorScreen.this);
            }
        });
        midlet.getDisplay().setCurrent(a);
    }

    private String getScoreGrade(int score) {
        if (score >= 150) return "S (Expert)";
        if (score >= 100) return "A (Good)";
        if (score >= 70) return "B (Average)";
        if (score >= 40) return "C (Beginner)";
        return "D (Needs Work)";
    }

    private void showToast(String msg) {
        Alert a = new Alert("", msg, null, AlertType.INFO);
        a.setTimeout(1500);
        midlet.getDisplay().setCurrent(a, EditorScreen.this);
    }

    // ========================
    // Command Handler
    // ========================

    public void commandAction(Command c, Displayable d) {
        if (c == cmdType) showInputDialog();
        else if (c == cmdSnippet) showSnippetList();
        else if (c == cmdNew) showNewDialog();
        else if (c == cmdOpen) doBrowse();
        else if (c == cmdSave) doSave();
        else if (c == cmdSaveAs) doSaveAs();
        else if (c == cmdBrowse) doBrowse();
        else if (c == cmdFind) showFindDialog();
        else if (c == cmdFindNext) findNext();
        else if (c == cmdGoLine) showGoToLineDialog();
        else if (c == cmdUndo) { performUndo(); repaint(); }
        else if (c == cmdWordWrap) { wordWrap = !wordWrap; repaint(); }
        else if (c == cmdCheck) showSyntaxCheckDialog();
        else if (c == cmdRun) runPHP();
        else if (c == cmdScores) showLeaderboard();
        else if (c == cmdExit) midlet.exit();
    }

    // ========================
    // Helper Methods
    // ========================

    private String[] splitLines(String text) {
        Vector v = new Vector();
        int start = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                v.addElement(text.substring(start, i));
                start = i + 1;
            }
        }
        v.addElement(text.substring(start));
        String[] r = new String[v.size()];
        for (int i = 0; i < v.size(); i++) r[i] = (String) v.elementAt(i);
        return r;
    }

    private int getCurrentLine() {
        String t = code.toString().substring(0, Math.min(cursorPos, code.length()));
        int c = 0;
        for (int i = 0; i < t.length(); i++) if (t.charAt(i) == '\n') c++;
        return c;
    }

    private int getCursorColumn() {
        String t = code.toString().substring(0, Math.min(cursorPos, code.length()));
        int nl = t.lastIndexOf('\n');
        return (nl == -1) ? cursorPos : cursorPos - nl - 1;
    }

    private int getLineStart(int lineNum) {
        String t = code.toString();
        int count = 0;
        for (int i = 0; i < t.length(); i++) {
            if (count == lineNum) return i;
            if (t.charAt(i) == '\n') count++;
        }
        return (lineNum == 0) ? 0 : t.length();
    }

    private String getCurrentLineText() {
        String[] lines = splitLines(code.toString());
        int ln = getCurrentLine();
        return (ln < lines.length) ? lines[ln] : "";
    }

    private int getTotalLines() {
        int c = 1;
        for (int i = 0; i < code.length(); i++) if (code.charAt(i) == '\n') c++;
        return c;
    }

    private int countChar(String text, char ch) {
        int c = 0;
        for (int i = 0; i < text.length(); i++) if (text.charAt(i) == ch) c++;
        return c;
    }

    private String toLowerCase(String s) {
        StringBuffer sb = new StringBuffer(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 'A' && c <= 'Z') sb.append((char) (c + 32));
            else sb.append(c);
        }
        return sb.toString();
    }

    private String strReplace(String text, String find, String replace) {
        StringBuffer sb = new StringBuffer();
        int start = 0;
        int idx;
        while ((idx = text.indexOf(find, start)) != -1) {
            sb.append(text.substring(start, idx));
            sb.append(replace);
            start = idx + find.length();
        }
        sb.append(text.substring(start));
        return sb.toString();
    }
}