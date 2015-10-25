package controller;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author James
 */
@WebServlet(urlPatterns = {"/ChatServlet"})
public class ChatServlet extends HttpServlet {

    
    public static ArrayList<Socket> allSockets;
    private static ArrayList<String> allMessages;
    private static HashMap<Integer,Socket> idSocketMap;
    private static HashMap<Integer,String> idHandleMap;
    private static HashMap<Integer, Integer> idMessageMap;
    private static int clientCount = 0;

    public void init() throws ServletException {
        allSockets = new ArrayList<>();
        allMessages = new ArrayList<>();
        idSocketMap = new HashMap<>();
        idHandleMap = new HashMap<>();
        idMessageMap = new HashMap<>();
    }

    public void destroy() {
        try {
            for (Socket socket:ChatServlet.allSockets) {
                if (!socket.isClosed()) {
                    System.out.print("Closing a socket....");
                    socket.close();
                    System.out.println("closed.");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            String answer = null;
            String command = request.getParameter("cmd");
            if (command.equals("Connect")) {
                answer = makeConnection(request);
                out.println(answer);
            } else if(command.equals("Send")) {
                answer = sendMessage(request);
                out.println(answer);
            } else if(command.equals("Receive")) {
                answer = receiveMessages(request);
                out.println(answer);
            } else if (command.equals("Disconnect")) {
                answer = breakConnection(request);
                out.println(answer);
            } else answer = "Invalid command.";
            System.out.println( "ANSWER: " + answer);
        } finally {
            out.close();
        }
    }
    
    private String sendMessage(HttpServletRequest request) {
        
        String message1 = request.getParameter("message");
        int idParam = Integer.parseInt(request.getParameter("clientID"));
        String handle = idHandleMap.get(idParam);
        String message = handle + ": " + message1;
        allMessages.add(message);
        
        return "Message sent";
    }
    
    private String receiveMessages(HttpServletRequest request){
        
        int idParam = Integer.parseInt(request.getParameter("clientID"));
        int messageIndex = idMessageMap.get(idParam);
        StringBuilder builder = new StringBuilder();
       
        while(messageIndex < allMessages.size()){
            builder.append(allMessages.get(messageIndex)).append("\n");
            messageIndex++;
        }
        idMessageMap.replace(idParam, messageIndex);
        return builder.toString();
    }

    private String makeConnection(HttpServletRequest request) {
        String resp;
        HttpSession session = request.getSession();
        if (session.getAttribute("socket")!=null) {
            allMessages.add("not null");
            resp = "Error: Already connected.";
        } else {
            // Connect
            String host = request.getRemoteHost();
            int port = Integer.parseInt(request.getParameter("port"));
            System.out.println("Connecting on port: " + port);
            String handle = request.getParameter("handle");
            Socket socket = null;
            int clientID = clientCount++;
            
            try {
                InetAddress address = InetAddress.getByName(host);
                socket = new Socket(address, port);
                
                session.setAttribute("socket", socket);
                allSockets.add(socket);
                idSocketMap.put(clientID, socket);
                idHandleMap.put(clientID, handle);
                idMessageMap.put(clientID, 0);
                OutputStream output = socket.getOutputStream();
                allMessages.add(handle + " has entered the chat");
            } catch (IOException ex) {
                ex.printStackTrace();
                resp = "Connect request failed.";
            }
            resp = "" + clientID + "#";
        }
        System.out.println("resp: " + resp);
        return resp;
    }

    private String breakConnection(HttpServletRequest request) {
        String idParam = request.getParameter("clientID");
        if (idParam == null) {
            return "Error: Not connected.";
        } else {
            String handle = idHandleMap.get(Integer.parseInt(idParam));
            allMessages.add(handle + " has left the chat");
            Socket socket = idSocketMap.get(Integer.parseInt(idParam));
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            int index = allSockets.indexOf(socket);
            allSockets.remove(index);
            return "Disconnect request confirmed.";
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
