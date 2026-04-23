import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class PHPIde extends MIDlet implements CommandListener {

    private Display display;
    private EditorScreen editorScreen;

    public void startApp() {
        display = Display.getDisplay(this);
        if (editorScreen == null) {
            editorScreen = new EditorScreen(this);
        }
        display.setCurrent(editorScreen);
    }

    public void pauseApp() {}

    public void destroyApp(boolean unconditional) {
        notifyDestroyed();
    }

    public void commandAction(Command c, Displayable d) {}

    public Display getDisplay() {
        return display;
    }

    public void exit() {
        destroyApp(true);
        notifyDestroyed();
    }
}