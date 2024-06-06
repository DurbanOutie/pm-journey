package pmjourney.platform;

import sira4j.Sira;
import sira4j.Canvas;

import pmjourney.game.GameData;
import pmjourney.game.Controller;
import pmjourney.game.Controller.ButtonState;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import java.lang.reflect.*;
import java.nio.file.*;
import java.io.*;



public class App{
    KL keyListener;
    ML mouseListener = new ML();
    Graphics2D graphics;
    BufferedImage imageBuffer; 
    Graphics imageBufferGraphics;
    Canvas canvas;
    boolean appRunning = false;
    double appStartTime;
    String title;
    int WIDTH;
    int HEIGHT;
    Window window;
    GameData gameData;
    Controller controller;
    public App(String title, int w, int h){
        this.title = title;
        this.WIDTH = w;
        this.HEIGHT = h;
    }

    public void init(){
        
        gameData = new GameData();
        controller = new Controller();
        keyListener = new KL(controller);

        window = new Window(
                title,
                WIDTH, HEIGHT,
                keyListener,
                mouseListener
                );

        // Handle to window graphics and backbuffer graphics
        graphics = (Graphics2D)window.getGraphics();

        imageBuffer = (BufferedImage)window.createImage(WIDTH, HEIGHT);

        canvas = new Canvas(((DataBufferInt)(imageBuffer.getRaster()
                    .getDataBuffer())).getData(), WIDTH, HEIGHT);
    }

    public void start(){
        double lastFrameTime = 0.0;
        System.out.println("Starting App");
        System.out.println("Sira version: " + Sira.version);
        window.setVisible(true);
        appRunning = true;
        
        String className = "pmjourney.game.Game";
        String methodName = "updateAndRender";


        while(appRunning){

            try{
                MyClassLoader myClassLoader = new MyClassLoader();
                Class klass = myClassLoader.loadClass(className);

                if(klass!=null){

                    Method mapCanvas = klass.getDeclaredMethods()[1];
                    mapCanvas.invoke(null, canvas.pixels, canvas.x, canvas.y, canvas.w, canvas.h, canvas.stride);

                    Method m = klass.getDeclaredMethods()[0];
                    m.setAccessible(true);
                    m.invoke(null);
                }
            }catch(LinkageError | IllegalAccessException | InvocationTargetException e){
                e.printStackTrace();
                System.out.println("Waiting for impl of Method "
                      + methodName);
            }

            graphics.drawImage(imageBuffer, 0, 0, window);
            try{
                Thread.sleep(200);
            }catch(Exception e){
                System.out.println("Uh oh");
            }
        }
        System.out.println("Ending App");
    }

    public void dispose(){
        window.dispose();
    }
}

class MyClassLoader extends ClassLoader{

    @Override
    public Class<?> loadClass(String s) {
        return findClass(s);
    }

    @Override
    public Class<?> findClass(String s) {
        try {
            byte[] bytes = loadClassData(s);
            return defineClass(s, bytes, 0, bytes.length);
        } catch (IOException ioe) {
            try {
                return super.loadClass(s);
            } catch (ClassFormatError | ClassNotFoundException ignore) { }
            //ioe.printStackTrace(System.out);
            return null;
        }
    }

    private byte[] loadClassData(String className) throws IOException {
        File f = new File("build/classes/" + className.replaceAll("\\.", "/") + ".class");
        int size = (int) f.length();
        byte buff[] = new byte[size];
        FileInputStream fis = new FileInputStream(f);
        DataInputStream dis = new DataInputStream(fis);
        dis.readFully(buff);
        dis.close();
        return buff;
    }

}

class Window extends JFrame{

    Window(String title, int w, int h, KL keyListener, ML mouseListener){
        this.setSize(w, h);
        this.setTitle(title);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.addKeyListener(keyListener);
        this.addMouseListener(mouseListener);
        this.addMouseMotionListener(mouseListener);
    }
}

class KL implements KeyListener{

    public void keyReleased(KeyEvent e){
        processKey(e);
    }
    public void keyPressed(KeyEvent e){
        processKey(e);
    }
    public void keyTyped(KeyEvent e){
        // Process keyPressed and keyReleased events OR keyTyped events
        // Dont process both.
    }

    Controller controller;

    public KL(Controller controller){
        this.controller = controller;
    }

    boolean[] wasDownKeys = new boolean[26];

    private void processKey(KeyEvent e){
        boolean isDown = e.getID()==KeyEvent.KEY_PRESSED;
        int index = e.getKeyCode()-65;
        if( index < 0 || index > 25){
            return;
        }

        boolean wasDown = wasDownKeys[index];
        
        if(wasDown!=isDown){
            if(e.getKeyCode()==KeyEvent.VK_W){
                processKeyMessage(controller.moveUp, isDown);
            }
            if(e.getKeyCode()==KeyEvent.VK_S){
                processKeyMessage(controller.moveDown, isDown);
            }
            if(e.getKeyCode()==KeyEvent.VK_A){
                processKeyMessage(controller.moveLeft, isDown);
            }
            if(e.getKeyCode()==KeyEvent.VK_D){
                processKeyMessage(controller.moveRight, isDown);
            }
        }else{
            //Dont do anything for repeating events yet
        }
        if(isDown){
            wasDownKeys[index] = true;
        }else{
            wasDownKeys[index] = false;
        }
    }

    private void processKeyMessage(ButtonState buttonState, boolean isDown){
        if(buttonState.endedDown!=isDown){
            buttonState.endedDown = isDown;
            buttonState.halfTransitionCount++;
        }
    }
}

class ML extends MouseAdapter implements MouseMotionListener{
    boolean isPressed;
    double x, y;
   
    @Override 
    public void mousePressed(MouseEvent e){
        isPressed = true;
    }

    @Override 
    public void mouseReleased(MouseEvent e){
        isPressed = false;
    }
    
    @Override 
    public void mouseMoved(MouseEvent e){
        x = e.getX();
        y = e.getY();
    }
}




