package pmjourney.game;

import sira4j.Sira;
import sira4j.Canvas;

public class Game{
    
    static GameData g = new GameData();
    static Canvas canvas = new Canvas(new int[0], 0, 0);
    static Controller c = new Controller();

    public static void mapCanvas(int[] pixels, int x, int y, int w, int h, int stride){
        canvas.w = w;
        canvas.h = h;
        canvas.pixels = pixels;
        canvas.x = x;
        canvas.y = y;
        canvas.stride = stride;
    }

    public static void mapGameData(int[] data){

    }
    public static void mapController(int[] data){
    }


    public static void updateAndRender(){


        if(!g.initialised){
            g.x1 = canvas.w*4/8;
            g.y1 = canvas.h*3/8;

            g.x2 = canvas.w*6/8;
            g.y2 = canvas.h*4/8;

            g.x3 = canvas.w*5/8;
            g.y3 = canvas.h*2/8;

            g.color = 0xFFFF0000;

            g.initialised = true;
        }


        if(c.moveUp.endedDown){
            g.y1 -= 10;
            g.y2 -= 10;
            g.y3 -= 10;
        }
        if(c.moveDown.endedDown){
            g.y1 += 10;
            g.y2 += 10;
            g.y3 += 10;
        }
        if(c.moveLeft.endedDown){
            g.x1 -= 10;
            g.x2 -= 10;
            g.x3 -= 10;
        }
        if(c.moveRight.endedDown){
            g.x1 += 10;
            g.x2 += 10;
            g.x3 += 10;
        }

        if(g.x1 < 0) g.x1 = 0;
        if(g.x1 > canvas.w) g.x1 = canvas.w;
        if(g.y1 < 0) g.y1 = 0;
        if(g.y1 > canvas.h) g.y1 = canvas.h;

        Sira.drawString(canvas, g.x3, g.y3, "Test", 0xFFFF0000);

        Sira.fillCircle(canvas, g.x1, g.y1, 100, 0xFF00FFFF);

    }

}

