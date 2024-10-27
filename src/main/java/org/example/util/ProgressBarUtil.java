package org.example.util;

public class ProgressBarUtil {
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";


    public static void execute() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            printProgress(i, 100);
            Thread.sleep(250);
        }
        System.out.println();
    }

    private static void printProgress(int current, int total) {
        int progressWidth = 30;
        int progress = (int) ((double) current / total * progressWidth);
        int percent = (current * 100 / total);

        String time = String.format("[00:%02d]", current / 4);

        StringBuilder progressBar = new StringBuilder(time + " [");


        for (int i = 0; i < progressWidth; i++) {
            if (i < progress) {
                progressBar.append(GREEN).append("#");
            } else {
                progressBar.append(" ");
            }
        }
        progressBar.append(RESET).append("] ").append(percent).append("%");
        System.out.print("\r" + progressBar.toString());
    }


}
