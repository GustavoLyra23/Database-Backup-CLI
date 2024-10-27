package org.example.util;

public class ProgressBarUtil {
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";

    public static void printProgress(int current, int total) {
        int progressWidth = 30;
        int progress = (int) ((double) current / total * progressWidth);
        int percent = (current * 100 / total);

        StringBuilder progressBar = new StringBuilder("[");

        for (int i = 0; i < progressWidth; i++) {
            if (i < progress) {
                progressBar.append(GREEN).append("#");
            } else {
                progressBar.append(" ");
            }
        }
        progressBar.append(RESET).append("] ").append(percent).append("%");
        System.out.print("\r" + progressBar);
    }
}
