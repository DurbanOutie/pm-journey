package pmjourney.platform;

import static pmjourney.platform.ByteWriter.writeBytes;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import java.lang.reflect.*;
import java.nio.file.*;
import java.io.*;

/*
 * 4 Things we need from the platform
 * 1. Timing
 * 2. Input
 * 3. imageBuffer
 * 4. soundbuffer
 *
 */

public class App{
    KL keyListener;
    ML mouseListener;
    Graphics2D graphics;
    BufferedImage imageBuffer; 
    Graphics imageBufferGraphics;
    Object[] d = new Object[4];
    int[] imageDataRaw;
    int[] imageDataMeta;
    boolean appRunning = false;
    double appStartTime;
    String title;
    int WIDTH;
    int HEIGHT;
    Window window;
    PlatformController pc;
    public App(String title, int w, int h){
        this.title = title;
        this.WIDTH = w;
        this.HEIGHT = h;
    }

    public void init(){

        pc = new PlatformController();




        
        keyListener = new KL(pc);
        mouseListener = new ML(pc);

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
        imageDataMeta = new int[]{WIDTH, HEIGHT};

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

        Method updateAndRender = klass.getDeclaredMethods()[1];

        File f = new File("build/classes/" + className.replaceAll("\\.", "/") + ".class");

        long lastModified = 0;
        boolean paused = false;

        while(appRunning){



            if(!paused){

                if(pc.getButtonEndedDown(PlatformController.PAUSE)){
                    paused = true;
                }

                d[3] = pc.getData();

                //for(int i = 0; i < 30; ++i){
                //    System.out.print(String.format("%d-[%d], ", i, pc.getData()[i]));
                //}
                //System.out.println();

                try{

                    if(f.lastModified() - lastModified > 0){
                        System.out.println("Game Code Stale...Updating it");
                        myClassLoader = new MyClassLoader();
                        klass = myClassLoader.loadClass(className);
                        if(klass!=null){
                            lastModified = f.lastModified();
                            updateAndRender = klass.getDeclaredMethods()[1];
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
            }else{
                if(pc.getButtonEndedDown(PlatformController.PAUSE)){
                    paused = false;
                }
            }
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

    PlatformController pc;

    public KL(PlatformController pc){
        this.pc = pc;
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
                processKeyMessage(pc, PlatformController.MOVE_UP, isDown);
            }
            if(e.getKeyCode()==KeyEvent.VK_S){
                processKeyMessage(pc, PlatformController.MOVE_DOWN, isDown);
            }
            if(e.getKeyCode()==KeyEvent.VK_A){
                processKeyMessage(pc, PlatformController.MOVE_LEFT, isDown);
            }
            if(e.getKeyCode()==KeyEvent.VK_D){
                processKeyMessage(pc, PlatformController.MOVE_RIGHT, isDown);
            }
            
            if(e.getKeyCode()==KeyEvent.VK_P){
                processKeyMessage(pc, PlatformController.PAUSE, isDown);
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

    private void processKeyMessage(PlatformController pc, byte button, boolean isDown){
        if(pc.getButtonState(button) !=(isDown?1:0)){
            pc.setButtonState(button, isDown);
        }

    }
}

class ML extends MouseAdapter implements MouseMotionListener{
    boolean isPressed;
    double x, y;
    PlatformController pc;

    public ML(PlatformController pc){
        this.pc = pc;
    }
   
    @Override 
    public void mousePressed(MouseEvent e){
        processKeyMessage(pc, PlatformController.MOUSE_1, true);
        isPressed = true;
    }

    @Override 
    public void mouseReleased(MouseEvent e){
        processKeyMessage(pc, PlatformController.MOUSE_1, false);
        isPressed = false;
    }
    
    @Override 
    public void mouseMoved(MouseEvent e){

        
        x = e.getX();
        y = e.getY();

        writeBytes(pc.getData(), PlatformController.MOUSE_X, (int)x);
        writeBytes(pc.getData(), PlatformController.MOUSE_Y, (int)y);



    }
    
    private void processKeyMessage(PlatformController pc, byte button, boolean isDown){
        if(pc.getButtonState(button) !=(isDown?1:0)){
            pc.setButtonState(button, isDown);
        }

    }
}

class PlatformController{

    public static final byte BUTTON_COUNT = 11;
    public static final byte BUTTON_SIZE = 2;
    public static final byte MOUSE_COORD_SIZE = 4;
    public static final byte MOVE_UP;
    public static final byte MOVE_DOWN;
    public static final byte MOVE_LEFT;
    public static final byte MOVE_RIGHT;
    public static final byte ACTION_UP;
    public static final byte ACTION_DOWN;
    public static final byte ACTION_LEFT;
    public static final byte ACTION_RIGHT;
    public static final byte START;
    public static final byte PAUSE;
    public static final byte MOUSE_1;
    public static final byte MOUSE_X;
    public static final byte MOUSE_Y;

    private byte[] data = new byte[128];
    private static final byte[] d = new byte[128];

    /*
     *
     * Format:
     * header - 2 bytes - default "PC"
     * number of controllers - 1 byte
     * length of controller data - 1byte
     * controller data - composite
     * for each button listed in order there is several data points.
     * number of buttons
     * 1. move up
     * 2. move down
     * 3. move left
     * 4. move right
     * 5. action 1
     * 6. action 2
     * 7. start
     * 8. pause
     * more to come
     * data per button
     * isDown - 1 byte
     * wasDown - 1 byte
     *
     *
     */

    static{
        int p = 0;
        p = writeBytes(d, p, "PC");                         // 0 - 87 , 1 - 67
        p = writeBytes(d, p, BUTTON_COUNT);                 // 2 - 5
        MOVE_UP = (byte)(p + BUTTON_COUNT);  
        p = writeBytes(d, p, MOVE_UP);                      //3 - 8
                                                            //
        MOVE_DOWN = (byte)(MOVE_UP + BUTTON_SIZE);
        p = writeBytes(d, p, MOVE_DOWN);                    //4 - 10
                                                            //
        MOVE_LEFT = (byte)(MOVE_DOWN + BUTTON_SIZE);
        p = writeBytes(d, p, MOVE_LEFT);                    //5 - 12
                                                            //
        MOVE_RIGHT = (byte)(MOVE_LEFT + BUTTON_SIZE);
        p = writeBytes(d, p, MOVE_RIGHT);                   //6 - 14
                                                            //
        ACTION_UP = (byte)(MOVE_RIGHT + BUTTON_SIZE);  
        p = writeBytes(d, p, ACTION_UP);                      //3 - 8
                                                              //
        ACTION_DOWN = (byte)(ACTION_UP + BUTTON_SIZE);
        p = writeBytes(d, p, ACTION_DOWN);                    //4 - 10
                                                              //
        ACTION_LEFT = (byte)(ACTION_DOWN + BUTTON_SIZE);
        p = writeBytes(d, p, ACTION_LEFT);                    //5 - 12
                                                              //
        ACTION_RIGHT = (byte)(ACTION_LEFT + BUTTON_SIZE);
        p = writeBytes(d, p, ACTION_RIGHT);                   //6 - 14
                                                              //
        START = (byte)(ACTION_RIGHT + BUTTON_SIZE);
        p = writeBytes(d, p, START);                   //6 - 14
                                                              
        PAUSE = (byte)(START + BUTTON_SIZE);
        p = writeBytes(d, p, PAUSE);                   //6 - 14
                                                              
        MOUSE_1 = (byte)(PAUSE + BUTTON_SIZE);
        p = writeBytes(d, p, MOUSE_1);                     //7 - 16
                                                           //
        MOUSE_X = (byte)(MOUSE_1 + BUTTON_SIZE);
        p = writeBytes(d, p, MOUSE_X);                     //7 - 16
        MOUSE_Y = (byte)(MOUSE_X + MOUSE_COORD_SIZE);
        p = writeBytes(d, p, MOUSE_Y);                     //7 - 16
    }

    {
        int p = 0;
        p = writeBytes(data, p, d);
    }
    public void setButtonState(byte key, boolean isDown){
        data[key] = (byte)(isDown?1:0);
    }
    public byte getButtonState(byte key){
        return data[key];
    }
    public boolean getButtonEndedDown(byte key){
        return data[key]==1?true:false;
    }
    public int getMouseCoord(byte key){
        
        int i = ((data[key + 0] & 0xFF) <<  0) |
                ((data[key + 1] & 0xFF) <<  8) |
                ((data[key + 2] & 0xFF) << 16) |
                ((data[key + 3] & 0xFF) << 24) ;

        return i;

    }

    public byte[] getData(){
        return data;
    }

    public void setData(byte[] data){
        this.data = data;
    }
}

class ByteWriter{

    static int writeBytes(byte[] data, int pointer, byte b){
        data[pointer++] = b;
        return pointer;
    }
    static int writeBytes(byte[] data, int pointer, byte[] b){
        for(int i = 0; i < b.length; ++i){
            data[pointer++] = b[i];
        }
        
        return pointer;
    }
    
    static int writeBytes(byte[] data, int pointer, char c){
        data[pointer++] = (byte)c;
        return pointer;
    }
    
    static int writeBytes(byte[] data, int pointer, int i){
        data[pointer++] = (byte)((i >>  0) & 0xFF);
        data[pointer++] = (byte)((i >>  8) & 0xFF);
        data[pointer++] = (byte)((i >> 16) & 0xFF);
        data[pointer++] = (byte)((i >> 24) & 0xFF);
        return pointer;
    }
    
    static int writeBytes(byte[] data, int pointer, long l){
        data[pointer++] = (byte)((l >>  0) & 0xFF);
        data[pointer++] = (byte)((l >>  8) & 0xFF);
        data[pointer++] = (byte)((l >> 16) & 0xFF);
        data[pointer++] = (byte)((l >> 24) & 0xFF);
        data[pointer++] = (byte)((l >> 32) & 0xFF);
        data[pointer++] = (byte)((l >> 40) & 0xFF);
        data[pointer++] = (byte)((l >> 48) & 0xFF);
        data[pointer++] = (byte)((l >> 56) & 0xFF);
        return pointer;
    }
    
    static int writeBytes(byte[] data, int pointer, double d){

        long l = Double.doubleToLongBits(d);
        writeBytes(data, pointer, d);

        return pointer;
    }

    static int writeBytes(byte[] data, int pointer, String s){
        for(char c: s.toCharArray()){
            pointer = writeBytes(data, pointer, c);
        }
        return pointer;
    }
}

