import pmjourney.platform.App;

public class Main{
    static final int SCALE = 60;
    public static void main(String[] args){
        App app = new App("Title", 16*SCALE, 9*SCALE);
        app.init();
        app.start();
        app.dispose();
    }
}
