package ui;

public class UIHelper {

    private static final boolean COLOR_ON = true;
    private static final String RESET  = COLOR_ON ? "\u001B[0m"  : "";
    private static final String CYAN   = COLOR_ON ? "\u001B[36m" : "";
    private static final String GREEN  = COLOR_ON ? "\u001B[32m" : "";
    private static final String YELLOW = COLOR_ON ? "\u001B[33m" : "";
    private static final String RED    = COLOR_ON ? "\u001B[31m" : "";
    private static final String BLUE   = COLOR_ON ? "\u001B[34m" : "";
    private static final String BOLD   = COLOR_ON ? "\u001B[1m"  : "";
    private static final String SEP    = "─";

    // ========== SCREEN MANAGEMENT ==========

    /**
     * Limpa a tela do terminal
     */
    public static void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    // ========== HEADERS & TITLES ==========

    /**
     * Imprime um header principal com título centrado
     * @param title O título a apresentar
     */
    public static void printHeader(String title) {
        System.out.println(CYAN + line(72));
        System.out.println(center(title, 72));
        System.out.println(line(72) + RESET);
    }

    /**
     * Imprime um sub-header (seção dentro de uma UI)
     * @param subtitle O subtítulo
     */
    public static void printSubHeader(String subtitle) {
        System.out.println("\n" + BOLD + CYAN + subtitle + RESET);
        System.out.println(line(72));
    }

    // ========== STATUS MESSAGES ==========

    /**
     * Imprime uma linha de status com símbolo
     * @param label Nome do componente
     * @param isReady Se está pronto (✓) ou não (⚠)
     * @param description Descrição adicional
     */
    public static void printStatus(String label, boolean isReady, String description) {
        String symbol = isReady ? GREEN + "  ✓" : YELLOW + "  ⚠";
        System.out.printf("%s %s%s %s%n", symbol, RESET, label, description);
    }

    /**
     * Imprime mensagem de sucesso
     */
    public static void printSuccess(String message) {
        System.out.println(GREEN + "✓ " + message + RESET);
    }

    /**
     * Imprime mensagem de aviso
     */
    public static void printWarning(String message) {
        System.out.println(YELLOW + "⚠ " + message + RESET);
    }

    /**
     * Imprime mensagem de erro
     */
    public static void printError(String message) {
        System.out.println(RED + "❌ " + message + RESET);
    }

    /**
     * Imprime mensagem de loading/processamento
     */
    public static void printLoading(String message) {
        System.out.println(CYAN + "⏳ " + message + RESET);
    }

    /**
     * Imprime mensagem informativa
     */
    public static void printInfo(String message) {
        System.out.println(BLUE + "ℹ " + message + RESET);
    }

    // ========== INPUT PROMPTS ==========

    /**
     * Mostra um prompt com a setinha (➤) e retorna o input do utilizador
     * @param message A mensagem a mostrar
     * @return O texto introduzido pelo utilizador (trimmed)
     */
    public static String prompt(String message) {
        System.out.print(CYAN + "➤ " + RESET + message);
        return System.console() != null ?
                System.console().readLine() :
                new java.util.Scanner(System.in).nextLine().trim();
    }

    /**
     * Lê uma opção numérica do menu
     * @return O número escolhido, ou -1 se inválido
     */
    public static int readOption() {
        System.out.print(CYAN + "➤ " + RESET + "Select option: ");
        try {
            return Integer.parseInt(new java.util.Scanner(System.in).nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Pausa a execução até o utilizador pressionar Enter
     */
    public static void pause() {
        System.out.print("\n" + CYAN + "➤ " + RESET + "Press Enter to continue...");
        new java.util.Scanner(System.in).nextLine();
    }

    // ========== FORMATTING UTILITIES ==========

    /**
     * Cria uma linha horizontal com n caracteres
     */
    public static String line(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(SEP);
        return sb.toString();
    }

    /**
     * Centraliza um texto numa largura especificada
     */
    public static String center(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s;
        int pad = (width - s.length()) / 2;
        StringBuilder left = new StringBuilder();
        for (int i = 0; i < pad; i++) left.append(' ');
        return left + s;
    }

    /**
     * Trunca uma string se exceder o tamanho máximo
     */
    public static String truncate(String s, int maxLen) {
        if (s == null) return "";
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen - 3) + "...";
    }

    // ========== COLOR GETTERS (para uso direto nas UIs) ==========

    public static String getReset()  { return RESET; }
    public static String getCyan()   { return CYAN; }
    public static String getGreen()  { return GREEN; }
    public static String getYellow() { return YELLOW; }
    public static String getRed()    { return RED; }
    public static String getBlue()   { return BLUE; }
    public static String getBold()   { return BOLD; }
}

