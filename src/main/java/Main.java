import ui.MainMenu;
import ui.EntryAnimation;
public class Main {
    public static void main(String[] args) {

        EntryAnimation.play();

        Runnable main = new MainMenu();
        main.run();
    }
}
