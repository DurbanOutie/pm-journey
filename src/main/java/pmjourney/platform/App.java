package pmjourney.platform;

import pmjourney.game.Controller;

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
    Object[] d = new Object[3];
    int[] imageDataRaw;
    int[] imageDataMeta;
    boolean appRunning = false;
    double appStartTime;
    String title;
    int WIDTH;
    int HEIGHT;
    Window window;
    Controller controller;
    public App(String title, int w, int h){
        this.title = title;
        this.WIDTH = w;
        this.HEIGHT = h;
    }

    public void init(){
        
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

        imageDataRaw = ((DataBufferInt)(imageBuffer.getRaster()
                    .getDataBuffer())).getData();
        imageDataMeta = new int[]{WIDTH, HEIGHT, 0, 0, WIDTH};

        d[0] = imageDataRaw;
        d[1] = imageDataMeta;


    }

    public void start(){
        double lastFrameTime = 0.0;
        System.out.println("Starting App");
        window.setVisible(true);
        appRunning = true;
        
        String className = "pmjourney.game.Game";
        MyClassLoader myClassLoader = new MyClassLoader();
        Class klass = myClassLoader.loadClass(className);

        for(int i = 0; i < klass.getDeclaredMethods().length; ++i){
            System.out.println(i + ". " + klass.getDeclaredMethods()[i]);
        }

        Method updateAndRender = klass.getDeclaredMethods()[0];

        File f = new File("build/classes/" + className.replaceAll("\\.", "/") + ".class");

        long lastModified = 0;
        while(appRunning){

            d[2] = controller.getRaw();

            try{

                if(f.lastModified() - lastModified > 0){
                    System.out.println("Game Code Stale...Updating it");
                    myClassLoader = new MyClassLoader();
                    klass = myClassLoader.loadClass(className);
                    if(klass!=null){
                        lastModified = f.lastModified();
                        updateAndRender = klass.getDeclaredMethods()[0];
                    }else{
                        System.out.println("Failed Loading Game Code...");
                    }
                }

                updateAndRender.invoke(null, (Object)d);
            }catch(LinkageError | IllegalAccessException | InvocationTargetException e){
                e.printStackTrace();
                System.out.println("Waiting for impl of Methods...");
            }

            graphics.drawImage(imageBuffer, 0, 0, window);
            try{
                Thread.sleep(16);
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

    public boolean initialised;

    @Override
    public Class<?> loadClass(String s) {
        return findClass(s);
    }

    @Override
    public Class<?> findClass(String s) {
        try {
            byte[] bytes = loadClassData(s);
            return defineClass(s, bytes, 0, bytes.length);
        } catch (IOException e) {
            try {
                return super.loadClass(s);
            } catch (ClassFormatError | ClassNotFoundException f) { }
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
                processKeyMessage(controller.buttonStates[0], isDown);
            }
            if(e.getKeyCode()==KeyEvent.VK_S){
                processKeyMessage(controller.buttonStates[1], isDown);
            }
            if(e.getKeyCode()==KeyEvent.VK_A){
                processKeyMessage(controller.buttonStates[2], isDown);
            }
            if(e.getKeyCode()==KeyEvent.VK_D){
                processKeyMessage(controller.buttonStates[3], isDown);
            }

            controller.raw[1] = controller.moveUpEndedDown()?1:0;
            controller.raw[3] = controller.moveDownEndedDown()?1:0;
            controller.raw[5] = controller.moveLeftEndedDown()?1:0;
            controller.raw[7] = controller.moveRightEndedDown()?1:0;

        }else{
            //Dont do anything for repeating events yet
        }
        if(isDown){
            wasDownKeys[index] = true;
        }else{
            wasDownKeys[index] = false;
        }
    }

    private void processKeyMessage(int[] buttonState, boolean isDown){
        if(buttonState[1]!=(isDown?1:0)){
            buttonState[1] = isDown?1:0;
            buttonState[0] += 1;
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




