# PHP IDE for J2ME - README

## Overview

**PHP IDE** is a lightweight, feature-rich PHP code editor designed specifically for J2ME (Java 2 Micro Edition) devices. Write, edit, test, and run PHP code on your mobile phone with syntax highlighting, file management, and a built-in PHP interpreter.

---

## Features

### ✨ Core Features

- **📝 Full-Featured Code Editor**
  - Real-time syntax highlighting for PHP
  - Line numbers and current line highlighting
  - Cursor blinking with smooth navigation
  - Undo/Redo support (single level)
  - Auto-indentation for code blocks

- **🎨 Syntax Highlighting**
  - Keywords (echo, if, for, foreach, while, function, class, etc.)
  - Built-in functions (strlen, substr, array_push, etc.)
  - Variables ($var)
  - Strings (single and double quoted)
  - Comments (//, #, /* */)
  - Numbers and operators
  - PHP tags (<?php, ?>)

- **💾 File Management (JSR-75)**
  - Save PHP files to phone memory or memory card
  - Load and open existing PHP files
  - Browse file system
  - Auto-detects best storage location
  - Creates `Php/` directory automatically
  - Save output as text files

- **🔍 Search & Navigation**
  - Find text in code (case-insensitive)
  - Find next occurrence
  - Go to line number
  - Jump to start/end of file

- **🐛 Syntax Checking**
  - Basic PHP syntax validation
  - Error detection:
    - Unmatched braces `{ }`
    - Unmatched parentheses `( )`
    - Unmatched brackets `[ ]`
    - Unclosed quotes `" '`
    - Missing PHP tags
  - Code quality score (0-200)

- **▶️ PHP Interpreter**
  - Run PHP code directly on your phone
  - Supports:
    - `echo` and `print` statements
    - Variable assignment and retrieval
    - `var_dump()` and `print_r()` output
    - String and number handling
  - View execution output in real-time
  - Save output to file

- **📦 Code Snippets**
  - 20+ ready-to-use PHP snippets:
    - PHP tags, echo, if/else statements
    - Loops (for, foreach, while)
    - Functions and classes
    - Arrays and switch statements
    - Try/catch blocks
    - Database connections
    - Form handling (POST/GET)
    - JSON responses
    - Regular expressions
    - And more...

- **🏆 Leaderboard**
  - Track your code quality scores
  - Top 5 scores stored in memory
  - Grade system: S, A, B, C, D
  - Score breakdown and statistics

### 🎮 User Interface

- **Dark Theme** (VS Code inspired)
- **Real-time Status Bar** showing:
  - Current line and column
  - Total lines of code
  - Character count
- **Title Bar** with filename and modification indicator (*)
- **Responsive Canvas-based rendering**
- **Smooth scrolling** (vertical and horizontal)

---

## System Requirements

- **J2ME Device** with MIDP 2.0 support
- **CLDC 1.1** or higher
- **JSR-75 File API** (for file system access - optional)
- **Memory**: ~100KB for JAR file
- **Storage**: ~50KB for code files

---

## Installation

### Build from Source

1. **Prerequisites**
   - Java Wireless Toolkit (WTK) 2.5.2 or compatible
   - JDK 1.4+

2. **Compile**
   ```bash
   preverify -classpath %WTK_HOME%\lib\midpapi20.jar;%WTK_HOME%\lib\cldcapi11.jar -d bin src\*.java
   jar cvfm PHP IDE.jar manifest.mf -C bin .
   ```

3. **Deploy**
   - Copy `PHP IDE.jar` and `PHP IDE.jad` to your device
   - Install using device's application manager

### Pre-built JAR

- Download `PHP IDE.jar` and `PHP IDE.jad`
- Transfer to your J2ME device
- Install via device's application management menu

---

## Controls

### Navigation

| Key | Action |
|-----|--------|
| **D-Pad ↑↓←→** | Move cursor up/down/left/right |
| **2** | Move cursor up |
| **4** | Move cursor left |
| **6** | Move cursor right |
| **8** | Move cursor down |
| **1** | Go to start of file |
| **3** | Go to end of file |
| **5 / Fire** | Open type dialog |

### Editing

| Key | Action |
|-----|--------|
| **0** | Insert newline with auto-indent |
| **7** | Backspace (delete character before cursor) |
| **9** | Delete forward |
| **\*** | Insert tab |
| **#** | Insert newline |

### Menu Navigation

- **Soft Keys**: Access commands menu
- **Select**: Choose menu item

---

## Menu Commands

### File Operations
- **New** - Create new PHP file
- **Open** - Browse and open existing files
- **Save** - Save current file
- **Save As** - Save with new filename
- **Browse** - Browse file system

### Editing
- **Type** - Open text input dialog (shows current line)
- **Snippet** - Insert PHP code snippet
- **Undo** - Undo last action
- **Word Wrap** - Toggle word wrapping

### Tools
- **Find** - Search for text in code
- **Find Next** - Find next occurrence
- **Go To Line** - Jump to specific line number

### Execution
- **Run PHP** - Execute PHP code and view output
- **Check Syntax** - Validate PHP syntax and get score
- **Scores** - View code quality leaderboard

### Other
- **Exit** - Close application

---

## Quick Start Guide

### 1. Create a New File
```
Press 5 (or Fire) → Select "New" → Confirm
```

### 2. Write PHP Code
```
Press 5 to open type dialog
Type: echo "Hello World!";
Press "Insert"
```

### 3. Run Your Code
```
Select "Run PHP" from menu
View output in popup form
```

### 4. Save Your Work
```
Select "Save As"
Enter filename: myprogram.php
Press "Save"
```

### 5. Check Code Quality
```
Select "Check Syntax"
View syntax errors and score
Check leaderboard for progress
```

---

## Example Programs

### Hello World
```php
<?php
    echo "Hello World!";
?>
```

### Variables
```php
<?php
    $name = "John";
    $age = 25;
    echo "Name: ";
    echo $name;
    echo " Age: ";
    echo $age;
?>
```

### Loop
```php
<?php
    for ($i = 1; $i <= 10; $i++) {
        echo $i;
        echo " ";
    }
?>
```

### Function
```php
<?php
    function add($a, $b) {
        return $a + $b;
    }
    $result = add(5, 3);
    echo "Result: ";
    echo $result;
?>
```

---

## Limitations

### J2ME Constraints
- **Limited Memory**: Code limited to ~8KB per file
- **No Network**: Cannot connect to databases or APIs
- **Single-level Undo**: Only one undo action stored
- **Basic Interpreter**: Not a full PHP runtime
  - No objects or advanced OOP
  - No file I/O functions
  - No $_SERVER, $_GET, $_POST superglobals
  - Limited string/array functions

### Interpreter Features
- ✅ Variable assignment
- ✅ Echo/print output
- ✅ var_dump() and print_r()
- ✅ Basic math operations
- ❌ Loops and conditionals (display only)
- ❌ Functions and classes (parsing only)
- ❌ Database connections
- ❌ Network requests

---

## File Storage

### Default Locations
- **Memory Card**: `file:///MemoryCard/Php/`
- **SD Card**: `file:///SDCard/Php/`
- **Phone Memory**: `file:///root1/Php/`

The app automatically detects and uses the best available storage.

### File Naming
- Save files with `.php` extension
- Example: `mycode.php`, `script.php`

---

## Tips & Tricks

### Performance
- Keep files under 5KB for best performance
- Clear large output by creating new file
- Use simple variable names for faster execution

### Syntax Highlighting
- Keywords appear in blue
- Strings in orange
- Variables in light blue
- Comments in green
- Functions in yellow

### Code Quality Scoring
- Base points: 2 per line (max 100)
- No errors: +50 points
- Has functions: +10 points
- Has classes: +15 points
- Has comments: +5 points
- **Total**: 0-200 points

### Grades
- **S**: 150+ (Expert)
- **A**: 100-149 (Good)
- **B**: 70-99 (Average)
- **C**: 40-69 (Beginner)
- **D**: 0-39 (Needs Work)

---

## Troubleshooting

### Editor won't open
- Ensure device has 50KB free memory
- Restart device and try again

### Can't save files
- Check if JSR-75 is supported on your device
- Verify you have write permissions
- Try saving to phone memory instead of card

### Type dialog shows nothing
- This is normal - it shows the current line content
- Just start typing to add new code

### Syntax check fails
- Some valid PHP may not be recognized
- Check basic syntax (braces, quotes, semicolons)
- Review error messages for clues

### Cursor navigation issues
- Use D-Pad or number keys 2/4/6/8
- Fire key (or 5) opens text input
- Adjust cursor with arrow keys after input

---

## Keyboard Shortcuts Summary

```
Navigation:  D-Pad or 2468
Edit:        0=newline, 7=backspace, 9=delete, *=tab
Input:       5 or Fire key
Quick Goto:  1=start, 3=end
```

---

## Performance Notes

- **First Launch**: May take 10-15 seconds
- **Large Files**: Scrolling may be slower
- **Syntax Highlighting**: Applied in real-time
- **Execution**: PHP output generated instantly

---

## License & Credits

**PHP IDE for J2ME** - Educational Project
- Developed for J2ME mobile devices
- Based on J2ME MIDP 2.0 specifications
- Compatible with phones running Java games

### Technologies Used
- **J2ME MIDP 2.0**: User interface and events
- **JSR-75**: File system access
- **CLDC 1.1**: Core Java compatibility
- **Canvas API**: Graphics rendering

---

## Future Enhancements

Potential features for future versions:
- [ ] Multi-level undo/redo
- [ ] Search and replace
- [ ] Larger file support
- [ ] Enhanced PHP interpreter
- [ ] Code formatting
- [ ] Project management
- [ ] Theme customization
- [ ] Code folding
- [ ] Autocomplete
- [ ] Debugging tools

---

## Support & Feedback

For issues or feature requests:
1. Check this README
2. Review troubleshooting section
3. Test on different J2ME devices
4. Report device-specific issues

---

## Version History

### v2.0.0 (Current)
- ✅ Full syntax highlighting
- ✅ PHP interpreter
- ✅ File management (JSR-75)
- ✅ Search functionality
- ✅ Code quality scoring
- ✅ Leaderboard
- ✅ 20 code snippets

### v1.0.0
- Initial release
- Basic editor and syntax checking

---

## File Manifest

```
PHP IDE/
├── src/
│   ├── PHPIde.java              (MIDlet - Main entry point)
│   ├── EditorScreen.java        (UI and editor logic)
│   ├── PHPHighlighter.java      (Syntax highlighting)
│   └── FileManager.java         (JSR-75 file operations)
├── META-INF/
│   └── MANIFEST.MF              (JAR manifest)
├── PHP IDE.jad                  (Descriptor file)
└── README.md                    (This file)
```

---

## Getting Started

1. **Install** the JAR file on your J2ME device
2. **Launch** PHP IDE from applications menu
3. **Create** a new file or open existing one
4. **Write** PHP code using the editor
5. **Run** to see output immediately
6. **Save** your work to file system
7. **Improve** based on syntax check feedback

Happy coding! 🚀

---

**Enjoy PHP development on your mobile device!**
