package pmjourney.game;

import sira4j.Sira;
import sira4j.Canvas;

public class Game{
    static Canvas canvas = new Canvas(new int[0], 0, 0);
    static GameData g = new GameData();
    static Controller c = new Controller();
    static int counter = 0;

    public static void updateAndRender(Object d){

        if(counter++ > 30){
            counter = 0;
        }




        int[] pixels = (int[])((Object[])d)[0];
        int[] meta = (int[])((Object[])d)[1];
        int[] controllerData = (int[])((Object[])d)[2];

        c.map(controllerData);

        canvas.pixels = pixels;
        canvas.w = meta[0];
        canvas.h = meta[1];
        canvas.y = meta[2];
        canvas.x = meta[3];
        canvas.stride = meta[4];


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

        if(c.moveUpEndedDown()){
            g.y1 -= 10;
            g.y2 -= 10;
            g.y3 -= 10;
        }
        if(c.moveDownEndedDown()){
            g.y1 += 10;
            g.y2 += 10;
            g.y3 += 10;
        }
        if(c.moveLeftEndedDown()){
            g.x1 -= 10;
            g.x2 -= 10;
            g.x3 -= 10;
        }
        if(c.moveRightEndedDown()){
            g.x1 += 10;
            g.x2 += 10;
            g.x3 += 10;
        }

        if(g.x1 < 0) g.x1 = 0;
        if(g.x1 > canvas.w) g.x1 = canvas.w;
        if(g.y1 < 0) g.y1 = 0;
        if(g.y1 > canvas.h) g.y1 = canvas.h;

        Sira.fillRect(canvas, 0, 0, canvas.w, canvas.h, 0xFF000000);

        

        Sira.fillCircle(canvas, g.x1, g.y1, 100, 0xFF999900);
        Sira.drawString(canvas, 20, 20, "My Name is What!!!", 5, 0xFFFF0000);
        Sira.drawString(canvas, 1300, 800, "Frame: " + counter, 5, 0xFFFF0000);



        String s = "Player pos: " + g.x1 + ", " + g.y1 + "\n";
        s += "Player col: " + g.color + "\n";






        Sira.drawString(canvas, 1200, 400, s, 3, 0xFFFF0000);

    }

}
