package pmjourney.game;

public class Controller{

    public class ButtonState{
        // down = +1, up = + 1
        public int halfTransitionCount;
        public boolean endedDown;
    }

    public ButtonState[] buttonStates;
    public ButtonState moveUp;
    public ButtonState moveDown;
    public ButtonState moveLeft;
    public ButtonState moveRight;

    public Controller(){
        moveUp = new ButtonState();
        moveDown = new ButtonState();
        moveLeft = new ButtonState();
        moveRight = new ButtonState();
        buttonStates = new ButtonState[]{moveUp, moveDown, moveLeft, moveRight};
    }
}

