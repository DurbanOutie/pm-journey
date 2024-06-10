package pmjourney.game;

public class Controller{

    public int[] raw;

    public int[][] buttonStates;
    public int[] moveUp;
    public int[] moveDown;
    public int[] moveLeft;
    public int[] moveRight;

    public Controller(){
        moveUp = new int[]{0, 0};
        moveDown = new int[]{0, 0};
        moveLeft = new int[]{0, 0};
        moveRight = new int[]{0, 0};
        buttonStates = new int[][]{moveUp, moveDown, moveLeft, moveRight};
        raw = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
    }

    public boolean moveUpEndedDown(){       return moveUp[1] > 0;       }
    public boolean moveDownEndedDown(){     return moveDown[1] > 0;     }
    public boolean moveLeftEndedDown(){     return moveLeft[1] > 0;     }
    public boolean moveRightEndedDown(){    return moveRight[1] > 0;    }

    public void moveUpEndedDown     (boolean yes){    moveUp[1] = yes?1:0; raw[1] = yes?1:0;}
    public void moveDownEndedDown   (boolean yes){  moveDown[1] = yes?1:0; raw[3] = yes?1:0;}
    public void moveLeftEndedDown   (boolean yes){  moveLeft[1] = yes?1:0; raw[5] = yes?1:0;}
    public void moveRightEndedDown  (boolean yes){ moveRight[1] = yes?1:0; raw[7] = yes?1:0;}


    public void map(int[] raw){
        buttonStates[0][0] = raw[0];
        buttonStates[0][1] = raw[1];
        buttonStates[1][0] = raw[2];
        buttonStates[1][1] = raw[3];
        buttonStates[2][0] = raw[4];
        buttonStates[2][1] = raw[5];
        buttonStates[3][0] = raw[6];
        buttonStates[3][1] = raw[7];
        this.raw = raw;
    }

    public int[] getRaw(){
        return raw;
    }
}

