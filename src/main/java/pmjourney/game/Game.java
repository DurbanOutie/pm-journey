package pmjourney.game;

import pmjourney.game.PlatformController;
import static pmjourney.game.ByteWriter.writeBytes;
import static pmjourney.game.PlatformController.MOVE_UP;
import static pmjourney.game.PlatformController.MOVE_DOWN;
import static pmjourney.game.PlatformController.MOVE_LEFT;
import static pmjourney.game.PlatformController.MOVE_RIGHT;
import static pmjourney.game.PlatformController.ACTION_UP;
import static pmjourney.game.PlatformController.ACTION_DOWN;
import static pmjourney.game.PlatformController.ACTION_LEFT;
import static pmjourney.game.PlatformController.ACTION_RIGHT;
import static pmjourney.game.PlatformController.START;
import static pmjourney.game.PlatformController.PAUSE;
import static pmjourney.game.PlatformController.MOUSE_1;
import static pmjourney.game.PlatformController.MOUSE_X;
import static pmjourney.game.PlatformController.MOUSE_Y;
import sira4j.Sira;
import sira4j.Canvas;

public class Game{




    static Canvas canvas = new Canvas(new int[0], 0, 0);
    static GameData g = new GameData();
    static PlatformController pc = new PlatformController();
    static int counter = 0;
    static int red = 0xFFFF0000;
    static int blk = 0xFF000000;
    static int col = red;


    static int rows = 10;
    static int cols = 10;
    static int tileW = 50;
    static int tileH = 50;
    static int offset = 0;

    static float dt = 1.0f/30;

    //static int[][] world1 = {
    //    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    //    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    //    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    //    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    //    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    //    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    //    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    //    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    //    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    //    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    //};


    static int[][] world1 = {
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 0, 0, 1, 1, 0, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 1, 1},
        {1, 0, 0, 1, 1, 0, 0, 0, 1, 1},
        {1, 0, 1, 0, 0, 1, 0, 0, 0, 1},
        {1, 0, 1, 0, 0, 1, 0, 0, 0, 1},
        {1, 0, 1, 1, 1, 1, 0, 0, 0, 1},
        {1, 0, 1, 0, 0, 1, 0, 0, 0, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
    };

    


    static boolean isPointEmpty(int[][] map, RawPosition pos){

        boolean isEmpty = false;

        int xTile = pos.x/tileW;
        int yTile = pos.y/tileH;

        if(     yTile >= 0 && yTile < map.length 
             && xTile >= 0 && xTile < map[0].length
        ){

            if(map[yTile][xTile] == 0){
                isEmpty = true;
            }

        }

        return isEmpty;
            

    }



    public static void updateAndRender(Object d){

        if(counter++ > 29){
            counter = 0;
        }

        int[] pixels = (int[])((Object[])d)[0];
        int[] meta = (int[])((Object[])d)[1];
        pc.setData((byte[])((Object[])d)[3]);

        canvas.pixels = pixels;
        canvas.w = meta[0];
        canvas.h = meta[1];
        canvas.y = 0;
        canvas.x = 0;
        canvas.stride = meta[0];


        if(!g.initialised){
            g.x1 = 400;
            g.y1 = 400;

            g.x2 = canvas.w*6/8;
            g.y2 = canvas.h*4/8;

            g.x3 = canvas.w*5/8;
            g.y3 = canvas.h*2/8;

            g.color = 0xFF009999;

            g.initialised = true;
        }
        

        int curCellCol = (g.y1 - offset) / tileH;
        int curCellRow = (g.x1 - offset) / tileW;

        Sira.drawString(canvas, 1000, 500, String.format("Row: %d, Col: %d", curCellRow, curCellCol), 4, 0xFFFF0000);

        int playerW = 40;
        int playerH = 50;

        int xNew = g.x1;
        int yNew = g.y1;

        int dxPlayer = 0;
        int dyPlayer = 0;


        if(pc.getButtonEndedDown(MOVE_UP)){
            dyPlayer = -1;
        }
        if(pc.getButtonEndedDown(MOVE_DOWN)){
            dyPlayer = 1;
        }
        if(pc.getButtonEndedDown(MOVE_LEFT)){
            dxPlayer = -1;
        }
        if(pc.getButtonEndedDown(MOVE_RIGHT)){
            dxPlayer = 1;
        }

        dxPlayer *=256;
        dyPlayer *=256;

        xNew = g.x1 + (int)(dxPlayer*dt);
        yNew = g.y1 + (int)(dyPlayer*dt);


        RawPosition playerPos = new RawPosition(xNew, yNew);
        RawPosition playerPosLeft = new RawPosition(xNew - playerW/2, yNew);
        RawPosition playerPosRight = new RawPosition(xNew + playerW/2, yNew);
        RawPosition playerPosTop = new RawPosition(xNew, yNew - playerH/2);
        RawPosition playerPosBottom = new RawPosition(xNew, yNew);

        boolean ppInEp = isPointEmpty(world1, playerPos);
        boolean pplInEp = isPointEmpty(world1, playerPosLeft);
        boolean pprInEp = isPointEmpty(world1, playerPosRight);

        if(ppInEp && pplInEp && pprInEp){

            g.x1 = playerPos.x;
            g.y1 = playerPos.y;
        }


        Sira.fillRect(canvas, 0, 0, canvas.w, canvas.h, 0xFF000000);

        Sira.drawString(canvas, 300, 500, String.format("LeftEmpty: %B, CentreEmpty: %B, RightEmpty: %B", pplInEp, ppInEp, pprInEp), 2, 0xFFFF0000);

        

        for(int col = 0; col < cols; ++col){
            for(int row = 0; row < rows; ++row){
                int xCell = row*tileW + offset;
                int yCell = col*tileH + offset;
                int color = 0xFFCCCCCC;
                if(world1[col][row] == 1){
                    color = 0xFF777777;
                }
            
                Sira.fillRect(canvas, xCell, yCell, tileW, tileH, color);
            }
        }

        for(int col = 0; col < cols; ++col){
            for(int row = 0; row < rows; ++row){
                int xCell = row*tileW + offset;
                int yCell = col*tileH + offset;
                int color = 0xFF880000;
            
                Sira.drawRect(canvas, xCell, yCell, tileW, tileH, color);
            }
        }

        

        if(g.x1 < 0) g.x1 = 0;
        if(g.x1 > canvas.w) g.x1 = canvas.w;
        if(g.y1 < 0) g.y1 = 0;
        if(g.y1 > canvas.h) g.y1 = canvas.h;
        

        Sira.fillCircle(canvas, pc.getMouseCoord(MOUSE_X), pc.getMouseCoord(MOUSE_Y), 10, 0xFF999900);


        Sira.fillRect(canvas, g.x1 - playerW/2, g.y1 - playerH, playerW, playerH, g.color);


        int playerLeft = g.x1 - playerW/2;
        int playerRight = g.x1 + playerW/2 - playerLeft;
        int playerTop = g.y1 - playerH/2;
        int playerBottom = g.y1 - playerTop;


        Sira.fillRect(canvas, playerLeft, playerTop, playerRight, playerBottom, 0xFFFF00FF);

        //System.out.println(String.format("x-[%d-%d], y-[%d-%d]", MOUSE_X, mouse_x, MOUSE_Y, mouse_y));


        Sira.drawString(canvas, 20, 20, "Debug view", 4, 0xFFFF0000);
        Sira.drawString(canvas, 650, 400, "Frame: " + counter, 2, 0xFFFF0000);



        String s = "Player pos: " + g.x1 + ", " + g.y1 + "\n";
        s += String.format("Player col: 0x%X\n", g.color);

        Sira.drawString(canvas, 500, 250, s, 2, 0xFFFF0000);


        if(counter == 0){
            col = 0xFFFF0000;
        }

        byte a = (byte)((col >> 24) & 0xFF);
        byte r = (byte)((col >> 16) & 0xFF);
        byte g = (byte)((col >>  8) & 0xFF);
        byte b = (byte)((col >>  0) & 0xFF);



        col = (a << 24) | 
              (r << 16) |
              (g <<  8) |
              (b <<  0);

        String colHex = String.format("%X", col);


        String s1 = "Test Fade, Col: 0x" + colHex;
        String version = Sira.version;

        Sira.drawString(canvas, 500, 300, version, 3, col);
        Sira.drawString(canvas, 450, 350, s1, 2, 0x00FFFFFF, false);
        Sira.drawString(canvas, 450, 380, "RawPosCount: " + RawPosition.count, 2, 0x00FFFFFF, false);

    }

}

class RawPosition{

    static int count = 0;


    int x;
    int y;

    RawPosition(int x, int y){
        this.x = x;
        this.y = y;
        ++count;

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
