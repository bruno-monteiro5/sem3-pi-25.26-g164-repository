import ui.EntryAnimation;
import ui.MainMenu;

public class main {
    public static void main(String[] args) {

        EntryAnimation.play();

        Runnable main = new MainMenu();
        main.run();
    }
}
