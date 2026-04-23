import javax.microedition.io.*;
import javax.microedition.io.file.*;
import java.io.*;
import java.util.Vector;

public class FileManager {

    // Possible roots in priority order
    private static final String[] ROOTS = {
        "file:///MemoryCard/",
        "file:///SDCard/",
        "file:///SD/",
        "file:///E:/",
        "file:///root1/",
        "file:///C:/",
        "file:///TFCard/",
        "file:///Memory card/"
    };

    private static final String PHP_DIR = "Php/";
    private String bestRoot = null;
    private String phpDir   = null;

    // ========================
    // Root Detection
    // ========================

    public String detectBestRoot() {
        if (bestRoot != null) return bestRoot;

        // Try known roots
        for (int i = 0; i < ROOTS.length; i++) {
            if (rootExists(ROOTS[i])) {
                bestRoot = ROOTS[i];
                phpDir   = bestRoot + PHP_DIR;
                ensurePhpDir();
                return bestRoot;
            }
        }

        // Fallback: enumerate system roots
        try {
            java.util.Enumeration roots = FileSystemRegistry.listRoots();
            while (roots.hasMoreElements()) {
                String root = "file:///" + (String) roots.nextElement();
                if (rootExists(root)) {
                    bestRoot = root;
                    phpDir   = bestRoot + PHP_DIR;
                    ensurePhpDir();
                    return bestRoot;
                }
            }
        } catch (Exception e) {}

        return null;
    }

    private boolean rootExists(String root) {
        FileConnection fc = null;
        try {
            fc = (FileConnection) Connector.open(root, Connector.READ);
            return fc.exists();
        } catch (Exception e) {
            return false;
        } finally {
            closeFC(fc);
        }
    }

    private void ensurePhpDir() {
        FileConnection fc = null;
        try {
            fc = (FileConnection) Connector.open(phpDir, Connector.READ_WRITE);
            if (!fc.exists()) {
                fc.mkdir();
            }
        } catch (Exception e) {
        } finally {
            closeFC(fc);
        }
    }

    public String getPhpDir() {
        if (phpDir == null) detectBestRoot();
        return phpDir;
    }

    public String getBestRoot() {
        if (bestRoot == null) detectBestRoot();
        return bestRoot;
    }

    // ========================
    // File Operations
    // ========================

    public String readFile(String url) throws IOException {
        FileConnection fc = null;
        InputStream is    = null;
        try {
            fc = (FileConnection) Connector.open(url, Connector.READ);
            if (!fc.exists()) throw new IOException("File not found: " + url);

            is = fc.openInputStream();
            int size = (int) fc.fileSize();
            if (size <= 0) size = 8192; // default buffer

            byte[] buf = new byte[size];
            int total  = 0;
            int read;
            while ((read = is.read(buf, total, buf.length - total)) != -1) {
                total += read;
                if (total >= buf.length) break;
            }
            return new String(buf, 0, total);
        } finally {
            if (is != null) try { is.close(); } catch (Exception e) {}
            closeFC(fc);
        }
    }

    public void writeFile(String url, String content) throws IOException {
        FileConnection fc = null;
        OutputStream os   = null;
        try {
            fc = (FileConnection) Connector.open(url, Connector.READ_WRITE);
            if (!fc.exists()) {
                fc.create();
            } else {
                fc.truncate(0);
            }
            os = fc.openOutputStream();
            byte[] bytes = content.getBytes();
            os.write(bytes);
            os.flush();
        } finally {
            if (os != null) try { os.close(); } catch (Exception e) {}
            closeFC(fc);
        }
    }

    public void deleteFile(String url) throws IOException {
        FileConnection fc = null;
        try {
            fc = (FileConnection) Connector.open(url, Connector.READ_WRITE);
            if (fc.exists()) fc.delete();
        } finally {
            closeFC(fc);
        }
    }

    // ========================
    // Directory Listing
    // ========================

    public String[] listPhpFiles() {
        return listPhpFiles(getPhpDir());
    }

    public String[] listPhpFiles(String dirUrl) {
        FileConnection fc = null;
        Vector files      = new Vector();
        try {
            fc = (FileConnection) Connector.open(dirUrl, Connector.READ);
            if (!fc.exists() || !fc.isDirectory()) return new String[0];

            java.util.Enumeration en = fc.list("*.php", false);
            while (en.hasMoreElements()) {
                files.addElement(dirUrl + (String) en.nextElement());
            }
            // Also list .txt files
            en = fc.list("*.txt", false);
            while (en.hasMoreElements()) {
                files.addElement(dirUrl + (String) en.nextElement());
            }
        } catch (Exception e) {
        } finally {
            closeFC(fc);
        }

        String[] result = new String[files.size()];
        for (int i = 0; i < files.size(); i++) {
            result[i] = (String) files.elementAt(i);
        }
        return result;
    }

    public String[] listAllFiles(String dirUrl) {
        FileConnection fc = null;
        Vector files      = new Vector();
        try {
            fc = (FileConnection) Connector.open(dirUrl, Connector.READ);
            if (!fc.exists() || !fc.isDirectory()) return new String[0];

            java.util.Enumeration en = fc.list();
            while (en.hasMoreElements()) {
                String name = (String) en.nextElement();
                files.addElement(dirUrl + name);
            }
        } catch (Exception e) {
        } finally {
            closeFC(fc);
        }

        String[] result = new String[files.size()];
        for (int i = 0; i < files.size(); i++) {
            result[i] = (String) files.elementAt(i);
        }
        return result;
    }

    // ========================
    // Filename Helpers
    // ========================

    public String getFileName(String url) {
        if (url == null) return "untitled.php";
        int slash = url.lastIndexOf('/');
        return (slash >= 0) ? url.substring(slash + 1) : url;
    }

    public String buildSavePath(String filename) {
        String dir = getPhpDir();
        if (dir == null) return null;
        if (!filename.endsWith(".php") && !filename.endsWith(".txt")) {
            filename = filename + ".php";
        }
        return dir + filename;
    }

    // ========================
    // JSR-75 Available Check
    // ========================

    public static boolean isAvailable() {
        try {
            Class.forName("javax.microedition.io.file.FileConnection");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    // ========================
    // Close Helper
    // ========================

    private void closeFC(FileConnection fc) {
        if (fc != null) {
            try { fc.close(); } catch (Exception e) {}
        }
    }
}