package de.bluplayz.logging;

public class Color {
    // Colors
    public static String WHITE = (char) 27 + "[30m";
    public static String RED = (char) 27 + "[31m";
    public static String GREEN = (char) 27 + "[32m";
    public static String YELLOW = (char) 27 + "[33m";
    public static String BLUE = (char) 27 + "[34m";
    public static String MAGENTA = (char) 27 + "[35m";
    public static String CYAN = (char) 27 + "[36m";
    public static String GRAY = (char) 27 + "[37m";

    // Formatters
    public static String BOLD = (char) 27 + "[1m";
    public static String RESET = (char) 27 + "[0m";
    public static String RESET_BOLD = (char) 27 + "[21m";
    public static String UNDERLINED = (char) 27 + "[4m";

    // Backgrounds
    public static String WHITE_BACKGROUND = (char) 27 + "[38m";
    public static String RED_BACKGROUND = (char) 27 + "[39m";
    public static String YELLOW_BACKGROUND = (char) 27 + "[43m";
    public static String BLUE_BACKGROUND = (char) 27 + "[44m";
    public static String MAGENTA_BACKGROUND = (char) 27 + "[45m";
    public static String CYAN_BACKGROUND = (char) 27 + "[46m";
    public static String GRAY_BACKGROUND = (char) 27 + "[47m";
}