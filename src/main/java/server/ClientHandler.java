package main.java.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }
    
    @Override
    public void run() {
        try {
            // Setup I/O
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            // Request a username and verify it's unique
            while (true) {
                out.println("SUBMIT_USERNAME");
                username = in.readLine();
                
                if (username == null) {
                    return;
                }
                
                // Username accepted
                synchronized (ChatServer.class) {
                    // Add this client's writer to the set of all writers
                    ChatServer.addClient(out);
                    break;
                }
            }
            
            // Announce the new user
            ChatServer.broadcast("SERVER: " + username + " has joined the chat!");
            
            // Send message history to the new user
            ArrayList<String> history = ChatServer.getMessageHistory();
            for (String msg : history) {
                out.println(msg);
            }
            
            // Accept and broadcast messages from this client
            while (true) {
                String input = in.readLine();
                if (input == null) {
                    return;
                }
                ChatServer.broadcast(username + ": " + input);
            }
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            // Client is leaving
            if (username != null) {
                ChatServer.broadcast("SERVER: " + username + " has left the chat!");
            }
            
            try {
                if (out != null) {
                    ChatServer.removeClient(out);
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                // Ignore
            }
        }
    }
}