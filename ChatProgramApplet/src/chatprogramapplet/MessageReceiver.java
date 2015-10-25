/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatprogramapplet;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

/**
 *
 * @author James
 */
public class MessageReceiver extends Thread{
    
    private static final int DELAY = 1000;
    private ChatProgramApplet chatApplet;
    private JTextArea serverMessagesArea;
    private String clientId;
    
    public MessageReceiver(ChatProgramApplet chatApplet, JTextArea chat, String id){
        this.chatApplet = chatApplet;
        this.serverMessagesArea = chat;
        this.clientId = id;
    }
    
    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException ex) {
            }
            String response = sendHttpRequest("cmd","Receive","clientID",this.clientId);
            serverMessagesArea.append(response + "y");
        }
    }
    
    public String sendHttpRequest(String... requestData) {
        String confirmation;
        try {
            URL sourceURL = chatApplet.getDocumentBase();
            System.out.println(sourceURL);
            //String host = sourceURL.getHost();
            //System.out.println("host: " + host);
            String webAppName = "/ChatProgramWeb";
            String servletName = "/Controller";
            String address = "http://" + "localhost" + ":8080" + webAppName + servletName;
            QueryString query = buildQuery(requestData);
            URL url = new URL(address + "?" + query);
            URLConnection urlConnection = url.openConnection();
            confirmation = getResponse(urlConnection);
            serverMessagesArea.append(confirmation);
        } catch (IOException ex) {
            ex.printStackTrace();
            confirmation = "failed";
        }
        return confirmation;
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
    
}
