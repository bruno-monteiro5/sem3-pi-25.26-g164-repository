package ui;

public class EntryAnimation {

    public static void play() {
        animateTrain();
        showBanner();
    }


    private static final String[] TRAIN = {
            "                   _-====-__-======-__-========-_____-============-__          ",
            "                 _(                                                 _)         ",
            "               _(                                                   )_         ",
            "            OO(                 G164  -  Code on Rails               _)        ",
            "           0  (_                                                   _)          ",
            "         o0     (_                                                _)           ",
            "        o         '=-___-===-_____-========-___________-==-LAPR3-='            ",
            "      .o                                _________                              ",
            "     . ______          ______________  |         |      _____                  ",
            "   _()_||__|| ________ |            |  |_________|   __||___||__               ",
            "  (         | |      | |            | __Y______00_| |_         _|              ",
            " /-OO----OO\"\"=\"OO--OO\"=\"OO--------OO\"=\"OO-------OO\"=\"OO-------OO\"=P  "
    };


    private static final String GROUND =
            "#########################################################################################################################################################################################################################################";

    private static final String[] BANNER = {
            " /$$                           /$$             /$$     /$$                            /$$$$$$                  /$$$$$$$            /$$ /$$                 ",
            "| $$                          |__/            | $$    |__/                           /$$__  $$                | $$__  $$          |__/| $$                 ",
            "| $$        /$$$$$$   /$$$$$$  /$$  /$$$$$$$ /$$$$$$   /$$  /$$$$$$$  /$$$$$$$      | $$  \\ $$ /$$$$$$$       | $$  \\ $$  /$$$$$$  /$$| $$  /$$$$$$$      ",
            "| $$       /$$__  $$ /$$__  $$| $$ /$$_____/|_  $$_/  | $$ /$$_____/ /$$_____/      | $$  | $$| $$__  $$      | $$$$$$$/ |____  $$| $$| $$ /$$_____/       ",
            "| $$      | $$  \\ $$| $$  \\ $$| $$|  $$$$$$   | $$    | $$| $$      |  $$$$$$       | $$  | $$| $$  \\ $$      | $$__  $$  /$$$$$$$| $$| $$|  $$$$$$        ",
            "| $$      | $$  | $$| $$  | $$| $$ \\____  $$  | $$ /$$| $$| $$       \\____  $$      | $$  | $$| $$  | $$      | $$  \\ $$ /$$__  $$| $$| $$ \\____  $$       ",
            "| $$$$$$$$|  $$$$$$/|  $$$$$$$| $$ /$$$$$$$/  |  $$$$/| $$|  $$$$$$$ /$$$$$$$/      |  $$$$$$/| $$  | $$      | $$  | $$|  $$$$$$$| $$| $$ /$$$$$$$/       ",
            "|________/ \\______/  \\____  $$|__/|_______/    \\___/  |__/ \\_______/|_______/        \\______/ |__/  |__/      |__/  |__/ \\_______/|__/|__/|_______/        ",
            "                     /$$  \\ $$                                                                                                                            ",
            "                    |  $$$$$$/                                                                                                                            ",
            "                     \\______/                                                                                                                             "
    };

    private static void animateTrain() {

        int maxOffset = 150;   // onde começa
        int sleepMs   = 50;   // velocidade
        clear();

        for (int offset = maxOffset; offset >= 0; offset--) {

            clear();

            String spaces = " ".repeat(offset);

            for (String line : TRAIN) {
                System.out.println(spaces + line);
            }

            System.out.println(GROUND);

            sleep(sleepMs);
        }
    }


    private static void showBanner() {
        sleep(300);

        clear();

        System.out.println();
        for (String line : BANNER) {
            System.out.println(line);
        }
        System.out.println();

        sleep(1000);
    }



    private static void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
