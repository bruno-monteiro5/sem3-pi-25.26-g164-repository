package ui;

public class EntryAnimation {

    public static void play() {
        animateTrain();
        showBanner();
    }

    // COMBOIO
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

    // LINHA / CARRIS (fixa)
    private static final String GROUND =
            "##########################################################################################################################################################################";

    // BANNER
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
        int maxOffset = 80;      // onde começa (direita)
        int trainHeight = TRAIN.length;

        clear(); // limpamos antes do primeiro frame

        for (int offset = maxOffset; offset >= 0; offset --) {
            String spaces = " ".repeat(offset);

            // 1) comboio
            for (String line : TRAIN) {
                System.out.println(spaces + line);
            }
            // 2) linha fixa
            System.out.println(GROUND);

            sleep(30); // velocidade

            // sobe o cursor para voltar ao topo do comboio + chão,
            // excepto no último frame (offset == 0), para ele ficar visível
            if (offset > 0) {
                System.out.print("\033[" + (trainHeight + 1) + "A"); // A = move up
            }
        }
    }

    private static void showBanner() {
        // pequena pausa para “saborear” o comboio parado (opcional)
        sleep(300);

        clear();

        System.out.println(); // espaço antes do banner
        for (String line : BANNER) {
            System.out.println(line);
        }
        System.out.println(); // espaço depois do banner

        sleep(1000);
    }

    // ============================================================
    // AUXILIARES
    // ============================================================

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
