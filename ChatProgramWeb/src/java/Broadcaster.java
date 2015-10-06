
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author James
 */
public class Broadcaster extends Thread{
    
    private static final int DELAY = 1000;
    private boolean running;
    private ArrayList<PrintWriter> allPrintWriters;
    private ArrayList<String> allMessages;

    public Broadcaster(ArrayList<PrintWriter> allPrintWriters, ArrayList<String> allMessages) {
        this.allPrintWriters = allPrintWriters;
        this.allMessages = allMessages;
        running = true;
    }

    public void run() {
        while (running) {
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException ex) {
            }
            for(String message : allMessages) {
                push(message);
                allMessages.remove(message);
            }
        }
    }

    public void push(String time) {
        for (PrintWriter printWriter:allPrintWriters) {
            System.out.print("Pushing: " + time);
            printWriter.println(time);
            System.out.println("...Pushed.");
        }
    }
    
    public void stopRunning() {
        running = false;
    }
}
