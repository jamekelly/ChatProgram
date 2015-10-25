/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatprogramapplet;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import javax.swing.JTextArea;

/**
 *
 * @author James
 */
public class Connection extends Thread{
    private final int PORT = 12333;
    private Scanner networkInput;
    private ChatProgramApplet chatApplet;
    private JTextArea serverMessagesArea;
    private JTextArea responseArea;
    private boolean running = true;
    private boolean connected = false;
    private ServerSocket serverSocket;
    private Socket socket;
    private JTextArea messageArea;
    private static String clientID;

    public Connection(ChatProgramApplet chatApplet, JTextArea serverMessagesArea, JTextArea messageArea,
            JTextArea responseArea) {
        this.chatApplet = chatApplet;
        this.serverMessagesArea = serverMessagesArea;
        this.messageArea = messageArea;
        this.responseArea = responseArea;
    }


    public void connectToServer() {
        try {
            int port = makeServerSocket();
            if (port==-1) {
                serverMessagesArea.setText("Error: No free port.");
                return;
            }
            String handle = this.chatApplet.getHandle();
            String portString = "" + port;
            String resp = sendHttpRequest("cmd","Connect","port",portString,"handle",handle);
            System.out.println(resp);
            saveClientID(resp);
            socket = serverSocket.accept();
            networkInput = new Scanner(socket.getInputStream());
            serverSocket.close();
            setConnected(true);
            serverMessagesArea.setText("");
            responseArea.append("connected\n");
        } catch (IOException ex) {
            responseArea.setText("Error in connectToServer\n");
            responseArea.append(ex.toString());
        }
    }
    
    public void sendToServer() {
        if(socket != null) {
            String message = messageArea.getText();
            messageArea.setText("");
            sendHttpRequest("cmd","Send","message",message,"clientID",clientID);
        }
    }
    
    public String receiveMessages() {
        String response = sendHttpRequest("cmd","Receive","clientID",clientID);
        return response;
    }

    private int makeServerSocket() {
        for (int port=1024; port<= 65535; port++) {
            try {
                serverSocket = new ServerSocket(port);
                return port;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return -1;
    }
    
    public void disconnectFromServer() {
        setConnected(false);
        sendHttpRequest("cmd","Disconnect","clientID",clientID);
        try {
            socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }    
        socket = null;
    }

    public String sendHttpRequest(String... requestData) {
        String confirmation;
        try {
            URL sourceURL = chatApplet.getDocumentBase();
            String host = sourceURL.getHost();
            String webAppName = "/ChatProgramWeb";
            String servletName = "/ChatServlet";
            String address = "http://" + host + ":8080" + webAppName + servletName;
            QueryString query = buildQuery(requestData);
            URL url = new URL(address + "?" + query);
            URLConnection urlConnection = url.openConnection();
            confirmation = getResponse(urlConnection);
            if(!query.getQuery().contains("Receive"))
                responseArea.append(confirmation);
        } catch (IOException ex) {
            ex.printStackTrace();
            confirmation = "failed";
        }
        return confirmation;
    }
    
    private void saveClientID(String response) {
        int end = response.indexOf("#");
        clientID = response.substring(0,end);        
    }

    private QueryString buildQuery(String[] requestData) {
        QueryString query = new QueryString();
        for (int i = 0; i < requestData.length; i = i+2) {
            String name = requestData[i];
            String value = requestData[i+1];
            query.add(name, value);
        }
        return query;
    }

    private String getResponse(URLConnection urlConnection) {
        InputStream in = null;
        try {
            in = urlConnection.getInputStream();
            InputStreamReader r = new InputStreamReader(in);
            int c;
            String answer = "";
            while ((c = r.read()) != -1) {
                answer += (char) c;
            }
            return answer;
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return "Response stream error.";
    }


    public void run() {
        
        do {
            try {
                Thread.sleep(2500);
            } catch (InterruptedException ex) {}
            if (isConnected()) {
                String response = this.receiveMessages();
                if(response.trim().length() > 0){
                    serverMessagesArea.append(response.trim() + "\n");
                }
            }
        } while (isRunning());
    }

    public synchronized boolean isConnected() {
        return this.connected;
    }

    public synchronized void setConnected(boolean connected) {
        this.connected = connected;
    }

    public  synchronized boolean isRunning() {
        return this.running;
    }

    public synchronized void stopRunning() {
        this.running = false;
    }
    
    
}
