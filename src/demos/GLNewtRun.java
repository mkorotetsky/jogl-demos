package demos;

import java.util.*;
import java.lang.reflect.*;

import javax.media.opengl.*;
import javax.media.nativewindow.*;

import com.sun.javafx.newt.*;
import com.sun.javafx.newt.opengl.*;

public class GLNewtRun implements WindowListener, KeyListener, MouseListener {

    static GLWindow window;
    static volatile boolean quit = false;

    public void windowResized(WindowEvent e) { }

    public void windowMoved(WindowEvent e) { }

    public void windowGainedFocus(WindowEvent e) { }

    public void windowLostFocus(WindowEvent e) { }

    public void windowDestroyNotify(WindowEvent e) {
        quit = true;
    }

    public void keyPressed(KeyEvent e) { 
        System.out.println(e);
        if(e.getKeyChar()=='f') {
            window.setFullscreen(!window.isFullscreen());
        } else if(e.getKeyChar()=='q') {
            quit = true;
        }
    }
    public void keyReleased(KeyEvent e) { 
        System.out.println(e);
    }
    public void keyTyped(KeyEvent e) { 
        System.out.println(e);
    }

    public void mouseClicked(MouseEvent e) {
        System.out.println(" mouseevent: "+e);
        switch(e.getClickCount()) {
            case 1:
                if(e.getButton()>MouseEvent.BUTTON1) {
                    window.setFullscreen(!window.isFullscreen());
                }
                break;
            default: 
                quit=true;
                break;
        }
    }
    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }
    public void mousePressed(MouseEvent e) {
    }
    public void mouseReleased(MouseEvent e) {
    }
    public void mouseMoved(MouseEvent e) {
    }
    public void mouseDragged(MouseEvent e) {
    }
    public void mouseWheelMoved(MouseEvent e) {
    }

    public boolean shouldQuit() { return quit; }

    public static int str2int(String str, int def) {
        try {
            return Integer.parseInt(str);
        } catch (Exception ex) { ex.printStackTrace(); }
        return def;
    }

    public static void main(String[] args) {
        boolean parented = false;
        boolean useAWTTestFrame = false;
        boolean useAWT = false;
        boolean undecorated = false;
        boolean fullscreen = false;
        int x_p = 0;
        int y_p = 0;
        int x = 0;
        int y = 0;
        int width = 800;
        int height = 480;
        String glProfileStr = null;

        if(0==args.length) {
            throw new RuntimeException("Usage: "+GLNewtRun.class+" <demo class name (GLEventListener)>");
        }

        GLNewtRun listener = new GLNewtRun();

        int i=0;
        while(i<args.length-1) {
            if(args[i].equals("-awt")) {
                useAWT = true;
            } else if(args[i].equals("-awttestframe")) {
                useAWT = true;
                useAWTTestFrame = true;
            } else if(args[i].equals("-undecorated")) {
                undecorated = true;
            } else if(args[i].equals("-parented")) {
                parented = true;
            } else if(args[i].equals("-fs")) {
                fullscreen = true;
            } else if(args[i].equals("-xp")) {
                i++;
                x_p = str2int(args[i], x_p);
            } else if(args[i].equals("-yp")) {
                i++;
                y_p = str2int(args[i], y_p);
            } else if(args[i].equals("-x")) {
                i++;
                x = str2int(args[i], x);
            } else if(args[i].equals("-y")) {
                i++;
                y = str2int(args[i], y);
            } else if(args[i].equals("-width")) {
                i++;
                width = str2int(args[i], width);
            } else if(args[i].equals("-height")) {
                i++;
                height = str2int(args[i], height);
            } else if(args[i].startsWith("-GL")) {
                glProfileStr = args[i].substring(1);
            }
            i++;
        }
        String demoClassName = args[i];
        Object demoObject = null;

        try {
            Class demoClazz = Class.forName(demoClassName);
            demoObject = demoClazz.newInstance();
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("Error while instaniating demo: "+demoClassName);
        }
        if( !(demoObject instanceof GLEventListener) ) {
            throw new RuntimeException("Not a GLEventListener: "+demoClassName);
        }
        GLEventListener demo = (GLEventListener) demoObject;

        GLProfile glp = GLProfile.get(glProfileStr);
        try {
            GLCapabilities caps = new GLCapabilities(glp);

            Window nWindow = null;
            if(useAWT) {
                Display nDisplay = NewtFactory.createDisplay(NativeWindowFactory.TYPE_AWT, null); // local display
                Screen nScreen  = NewtFactory.createScreen(NativeWindowFactory.TYPE_AWT, nDisplay, 0); // screen 0
                if(useAWTTestFrame) {
                    java.awt.MenuBar menuTest = new java.awt.MenuBar();
                    menuTest.add(new java.awt.Menu("External Frame Test - Menu"));
                    java.awt.Frame frame = new java.awt.Frame("External Frame Test");
                    frame.setMenuBar(menuTest);
                    nWindow = NewtFactory.createWindow(NativeWindowFactory.TYPE_AWT, new Object[] { frame }, nScreen, caps, undecorated);
                } else {
                    nWindow = NewtFactory.createWindow(NativeWindowFactory.TYPE_AWT, nScreen, caps, undecorated);
                }
            } else {
                Display nDisplay = NewtFactory.createDisplay(null); // local display
                Screen nScreen  = NewtFactory.createScreen(nDisplay, 0); // screen 0
                if(parented) {
                    Window parent = NewtFactory.createWindow(nScreen, caps, undecorated);
                    parent.setPosition(x_p, y_p);
                    parent.setSize(2*width, 2*height);
                    parent.setVisible(true);
                    nWindow = NewtFactory.createWindow(parent.getWindowHandle(), nScreen, caps, undecorated);
                } else {
                    nWindow = NewtFactory.createWindow(nScreen, caps, undecorated);
                }
            }
            window = GLWindow.create(nWindow);

            try {
                Field f = demo.getClass().getField("window");
                if(f.getType().isInstance(window)) {
                    f.set(demo, window);
                } else {
                    System.out.println("Demo's 'window' field not a Window, but: "+f.getType());
                    
                }
            } catch (NoSuchFieldException nsfe) {
                System.out.println("Demo has no 'window' field");
            } catch (Throwable t) {
                t.printStackTrace();
            }

            window.addWindowListener(listener);
            window.addMouseListener(listener);
            window.addKeyListener(listener);
            window.addGLEventListener(demo);
            // window.setEventHandlerMode(GLWindow.EVENT_HANDLER_GL_CURRENT); // default
            window.setEventHandlerMode(GLWindow.EVENT_HANDLER_GL_NONE); // no current ..
            window.setRunPumpMessages(true);

            window.setPosition(x, y);
            window.setSize(width, height);
            window.setFullscreen(fullscreen);
            // Size OpenGL to Video Surface
            window.setVisible(true);
            window.enablePerfLog(true);

            do {
                window.display();
            } while (!quit && window.getDuration() < 20000) ;

            window.destroy();

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}